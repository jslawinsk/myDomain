package com.domain.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.domain.model.DbSync;
import com.domain.model.GraphTypes;
import com.domain.model.MeasureType;
import com.domain.model.Process;
import com.domain.service.BlueToothService;
import com.domain.service.DataService;


@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
				properties = { "blueTooth.enabled=false", 
								"wiFi.enabled=false",
								"dataSynch.enabled=false" }
			)
@AutoConfigureMockMvc
class UiController2Test {

	@LocalServerPort
	private int port;
	
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	DataService dataService;
	
	@MockBean
	BlueToothService blueToothService;

	@MockBean
	JavaMailSender mailSender;
	
	@MockBean
	H2ConsoleProperties h2ConsoleProperties;
	
	@Test
	@WithMockUser(roles = "ADMIN")
	void testDeleteProcess() throws Exception {
		Process process = new Process( "FRM", "Fermentation" );
		Mockito.when(dataService.getProcess( "FRM" )).thenReturn( process );
		Mockito.when(dataService.getProcessSensorCount( "FRM" )).thenReturn( 0L );
		Mockito.when(dataService.getProcessMeasurementCount( "FRM" )).thenReturn( 0L );
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/process/delete/FRM/0")
				.with(csrf())
	            .accept(MediaType.ALL))
				.andExpect( MockMvcResultMatchers.redirectedUrl("/process/0"));
    }

	@Test
	@WithMockUser(roles = "ADMIN")
	void testDeleteMeasureType() throws Exception {
		MeasureType measureType = new MeasureType( "TMP", "Temperature", true, 0, 200, GraphTypes.GAUGE, DbSync.ADD  );
		Mockito.when(dataService.getMeasureType( "TMP" )).thenReturn( measureType );
		Mockito.when(dataService.getMeasureTypeSensorCount( "TMP" )).thenReturn( 0L );
		Mockito.when(dataService.getMeasureTypeMeasurementCount( "TMP" )).thenReturn( 0L );
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/measureType/delete/TMP/0")
				.with(csrf())
	            .accept(MediaType.ALL))
				.andExpect( MockMvcResultMatchers.redirectedUrl("/measureType/0"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void testDeleteDuplicateMeasurements() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/measurement/duplicatedelete/1/0")
				.with(csrf())
	            .accept(MediaType.ALL))
				.andExpect( MockMvcResultMatchers.redirectedUrl("/measurement/batch/1/0"));
	}

}
