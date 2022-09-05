package com.domain.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.hamcrest.CoreMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.domain.model.Batch;
import com.domain.model.DbSync;
import com.domain.model.Domain;
import com.domain.model.GraphTypes;
import com.domain.model.MeasureType;
import com.domain.model.Measurement;
import com.domain.model.Process;
import com.domain.model.Sensor;
import com.domain.model.Category;
import com.domain.model.User;
import com.domain.model.UserRoles;
import com.domain.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith( SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, 
				properties = { "blueTooth.enabled=false", "wiFi.enabled=false" }
			)
@AutoConfigureMockMvc
class RestApiControllerTest {

    static private Logger LOG = LoggerFactory.getLogger( RestApiControllerTest.class );
	
	@LocalServerPort
	private int port;

    @Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@MockBean
	DataService dataService;
	
	@MockBean
	JavaMailSender mailSender;
	
	private Domain testDomain = new Domain( 0L, 0, "Brewery", "sports_bar_white_36dp.svg", "Home Brewery", "Style", "Brewery", DbSync.ADD, null);
	private Category testCategory = new Category( "IPA", "18a", "Hoppy" );
	private Process process = new Process( "FRM", "Fermentation" );
	private MeasureType measureType = new MeasureType( "TMP", "Temperature", true, 0, 200, GraphTypes.GAUGE, DbSync.ADD  );
	private Batch testBatch = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, testDomain, new Date() );
	private Measurement measurement = new Measurement( 70.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
	private Sensor sensor = new Sensor();
		
	@Test
	@WithMockUser( authorities = "API" )
	void heartBeat() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/heartBeat")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString("ACK")));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void responseAdviceTest() throws Exception
	{
    	List<Batch> batches = new ArrayList<Batch>();
    	batches.add( null );
        Mockito.when(dataService.getActiveBatches()).thenReturn( batches );
		
		mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/summary")
	            .accept(MediaType.ALL))
	            .andExpect(status().isBadRequest())
	            .andExpect(content().string(containsString( "Errors found in request, try again later" )))
	            ;
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void getMeasurementSummary() throws Exception
	{
		testBatch.setId( 1L );
    	List<Batch> batches = new ArrayList<Batch>();
    	batches.add( testBatch );
		
		MeasureType measureType2 = new MeasureType( "PH", "PH", true, 0, 14, GraphTypes.SOLID_GUAGE, DbSync.ADD );
		Measurement measurement2 = new Measurement( 7.0, "{\"target\":7.0}", testBatch, process, measureType2, new Date() );

    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add( measurement );
    	measurements.add( measurement2 );
		
        Mockito.when(dataService.getActiveBatches()).thenReturn( batches );
        Mockito.when(dataService.getRecentMeasurement( 1L )).thenReturn( measurements );
		
		mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/summary")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "\"{\\\"target\\\":70.0}\"," )))
	            ;
	}	

	//
	//	Category Rest API Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void getCategory() throws Exception
	{
		testCategory.setId( 1L );
		Mockito.when(dataService.getCategory( 1L )).thenReturn( testCategory );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/category/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"id\":1," )));
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void getCategories() throws Exception
	{
    	List<Category> categories = new ArrayList<Category>();
    	testCategory.setId( 1L );
    	categories.add( testCategory );
		Mockito.when(dataService.getAllCategories()).thenReturn( categories );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/category")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void saveCategory() throws Exception
	{
		testCategory.setId( 1L );
		Mockito.when(dataService.saveCategory( Mockito.any(Category.class) )).thenReturn( testCategory );

        mockMvc.perform( MockMvcRequestBuilders.post("http://localhost:" + port + "/api/category" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( testCategory ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void updateCategory() throws Exception
	{
		testCategory.setId( 1L );
		testCategory.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getCategory( "TestToken" )).thenReturn( testCategory );
		Mockito.when(dataService.updateCategory( Mockito.any(Category.class) )).thenReturn( testCategory );

        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/category" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( testCategory ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));

        testCategory.setDbSynchToken( "" );
        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/category" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( testCategory ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void deleteCategory() throws Exception
	{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/category/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void deleteCategoryRemote() throws Exception
	{
		testCategory.setId( 1L );
		testCategory.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getCategory( "TestToken" )).thenReturn( testCategory );

		mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/category/synchToken/TestToken")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	
	//
	//	Process Rest API Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void getProcess() throws Exception
	{
        Mockito.when(dataService.getProcess( "FRM" )).thenReturn( process );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/process/FRM")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"code\":\"FRM\"," )));
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void getProcesses() throws Exception
	{
    	List<Process> processes = new ArrayList<Process>();
    	processes.add( process );
        Mockito.when(dataService.getAllProcesses( )).thenReturn( processes );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/process")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"code\":\"FRM\"," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void saveProcess() throws Exception
	{
		Mockito.when(dataService.saveProcess( Mockito.any(Process.class) )).thenReturn( process );

        mockMvc.perform( MockMvcRequestBuilders.post("http://localhost:" + port + "/api/process" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( process ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"code\":\"FRM\"," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void updateProcess() throws Exception
	{
		Mockito.when(dataService.updateProcess( Mockito.any(Process.class) )).thenReturn( process );

        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/process" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( process ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"code\":\"FRM\"," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void deleteProcess() throws Exception
	{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/process/FRM")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	//
	//	MeasureType Rest API Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void getMeasureType() throws Exception
	{
        Mockito.when(dataService.getMeasureType( "TMP" )).thenReturn( measureType );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/measureType/TMP")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"code\":\"TMP\"," )));
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void getMeasureTypes() throws Exception
	{
    	List<MeasureType> measureTypes = new ArrayList<MeasureType>();
    	measureTypes.add( measureType );
		
        Mockito.when(dataService.getAllMeasureTypes( )).thenReturn( measureTypes );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/measureType")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"code\":\"TMP\"," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void saveMeasureType() throws Exception
	{
		Mockito.when(dataService.saveMeasureType( Mockito.any(MeasureType.class) )).thenReturn( measureType );

        mockMvc.perform( MockMvcRequestBuilders.post("http://localhost:" + port + "/api/measureType" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( measureType ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"code\":\"TMP\"," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void updateMeasureType() throws Exception
	{
		Mockito.when(dataService.updateMeasureType( Mockito.any(MeasureType.class) )).thenReturn( measureType );

        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/measureType" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( measureType ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"code\":\"TMP\"," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void deleteMeasureType() throws Exception
	{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/measureType/TMP")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	//
	//	Batch Rest API Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void getBatch() throws Exception
	{
		testCategory.setId( 1L );
		Mockito.when(dataService.getBatch( 1L )).thenReturn( testBatch );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/batch/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void saveBatch() throws Exception
	{
		testCategory.setId( 1L );
		testCategory.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getCategory( "TestToken" )).thenReturn( testCategory );

		Mockito.when(dataService.saveBatch( Mockito.any( Batch.class) )).thenReturn( testBatch );

        mockMvc.perform( MockMvcRequestBuilders.post("http://localhost:" + port + "/api/batch" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( testBatch ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void updateBatch() throws Exception
	{
		testCategory.setId( 1L );
		testCategory.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getCategory( "TestToken" )).thenReturn( testCategory );

		testBatch.setCategory( testCategory );
		testBatch.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getBatch( "TestToken" )).thenReturn( testBatch );
		Mockito.when(dataService.updateBatch( Mockito.any( Batch.class) )).thenReturn( testBatch );

        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/batch" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( testBatch ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));

        testCategory.setDbSynchToken( "" );
		testBatch.setDbSynchToken( "" );
        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/batch" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( testBatch ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void deleteBatch() throws Exception
	{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/batch/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void deleteBatchRemote() throws Exception
	{
		testBatch.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getBatch( "TestToken" )).thenReturn( testBatch );
		mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/batch/synchToken/TestToken")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	
	//
	//	Measurement Rest API Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void getMeasurement() throws Exception
	{
		measurement.setId( 1L );
        Mockito.when(dataService.getMeasurement( 1L )).thenReturn( measurement );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/measurement/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void saveMeasurement() throws Exception
	{
		Batch testBatch = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, testDomain, new Date() );
		testBatch.setId( 1L );
		testBatch.setDbSynchToken( "test" );
		Mockito.when( dataService.getBatch(  "test" ) ).thenReturn( testBatch );

		measurement.setId( 1L );
		measurement.setBatch( testBatch );
		Mockito.when(dataService.saveMeasurement( Mockito.any( Measurement.class ) )).thenReturn( measurement );

        mockMvc.perform( MockMvcRequestBuilders.post("http://localhost:" + port + "/api/measurement" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( measurement ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void updateMeasurement() throws Exception
	{
    	MeasureType measureType = new MeasureType( "TMP", "Temperature", true, 20, 200, GraphTypes.GAUGE, DbSync.ADD  );
		Category testCategory = new Category( "IPA", "18a", "Hoppy" );
		Batch testBatch = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, testDomain, new Date() );
		testBatch.setId( 1L );
		testBatch.setDbSynchToken( "test" );
    	Process process = new Process( "FRM", "Fermentation" );
		Measurement measurement = new Measurement( 70.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
		measurement.setId( 1L );
		measurement.setDbSynchToken( "TestToken" );
		LOG.info( "Test updateMeasurement Measurement: " + measurement );
		Mockito.when( dataService.getMeasurement( "TestToken" ) ).thenReturn( measurement );
		Mockito.when( dataService.updateMeasurement( Mockito.any(Measurement.class) ) ).thenReturn( measurement );
		Mockito.when( dataService.getBatch(  "test" ) ).thenReturn( testBatch );

        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/measurement" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( measurement ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));

		testBatch.setDbSynchToken( "" );
		measurement.setDbSynchToken( "" );
        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/measurement" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( measurement ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void deleteMeasurement() throws Exception
	{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/measurement/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void deleteMeasurementRemote() throws Exception
	{
		Measurement measurement = new Measurement( 70.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
		measurement.setId( 1L );
		measurement.setDbSynchToken( "TestToken" );
		Mockito.when( dataService.getMeasurement( "TestToken" ) ).thenReturn( measurement );
		
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/measurement/synchToken/TestToken")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	//
	//	Sensor Rest API Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void getSensor() throws Exception
	{
		sensor.setId( 1L );
		Mockito.when(dataService.getSensor( 1L )).thenReturn( sensor );

        mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/sensor/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void saveSensor() throws Exception
	{
		testBatch.setDbSynchToken( "test" );
		Mockito.when(dataService.getBatch(  Mockito.any( String.class )) ).thenReturn( testBatch );

		sensor.setId( 1L );
		sensor.setDbSynchToken( "test" );
		sensor.setBatch( testBatch );
		Mockito.when(dataService.saveSensor( Mockito.any(Sensor.class) )).thenReturn( sensor );

        mockMvc.perform( MockMvcRequestBuilders.post("http://localhost:" + port + "/api/sensor" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( sensor ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void updateSensor() throws Exception
	{
		testBatch.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getBatch( "TestToken" )).thenReturn( testBatch );

		sensor.setId( 1L );
		sensor.setDbSynchToken( "TestToken" );
		sensor.setBatch( testBatch );
		Mockito.when(dataService.getSensor( "TestToken" )).thenReturn( sensor );
		Mockito.when(dataService.updateSensor( Mockito.any(Sensor.class) )).thenReturn( sensor );

        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/sensor" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( sensor ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));

		testBatch.setDbSynchToken( "" );
		sensor.setDbSynchToken( "" );
        mockMvc.perform( MockMvcRequestBuilders.put("http://localhost:" + port + "/api/sensor" )
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString( sensor ))		
	            .accept(MediaType.ALL))
        		.andExpect(status().isOk())
        		.andExpect(content().string(containsString( "{\"id\":1," )));
	}	
	
	@Test
	@WithMockUser( authorities = "API" )
	void deleteSensor() throws Exception
	{
        mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/sensor/1")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void deleteSensorRemote() throws Exception
	{
		sensor.setId( 1L );
		sensor.setDbSynchToken( "TestToken" );
		Mockito.when(dataService.getSensor( "TestToken" )).thenReturn( sensor );

		mockMvc.perform(MockMvcRequestBuilders.delete("http://localhost:" + port + "/api/sensor/synchToken/TestToken")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk());
	}	

	//
	// Rest API Authentication Test Methods
	//
	@Test
	@WithMockUser( authorities = "API" )
	void login() throws Exception
	{
		User user = new User( "TEST", "test", DbSync.ADD, UserRoles.ADMIN.toString() );
		user.setPassword( passwordEncoder.encode( "test" ));
		Mockito.when(dataService.getUserByName( "TEST" )).thenReturn( user );

        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:" + port + "/api/authorize?user=TEST&password=test")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "\"username\":\"TEST\"," )));
        
        //
        //	Test for invalid login
        //
        mockMvc.perform(MockMvcRequestBuilders.post("http://localhost:" + port + "/api/authorize?user=TEST2&password=test2")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect( content().string(  CoreMatchers.not(containsString( "\"username\":\"TEST2\"," ) ) ))
	            ;
	            
	}	

	@Test
	@WithMockUser( authorities = "API" )
	void notAuthenticated() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get("http://localhost:" + port + "/api/notauthenticated")
	            .accept(MediaType.ALL))
	            .andExpect(status().isOk())
	            .andExpect(content().string(containsString( "{error: Not Autheticated}" )))
	            ;

	}	
	
	
}
