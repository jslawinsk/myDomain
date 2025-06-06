package com.domain.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.microedition.io.Connector;
import javax.microedition.io.InputConnection;
import javax.microedition.io.StreamConnection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;

import com.domain.controller.UiController;
import com.domain.core.DataSynchThread;
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

@SpringBootTest( properties = { "blueTooth.enabled=false", 
								"dataSynch.delayMinutes=0",
								"dataSynch.url=http://localhost:8080/api/",
								"dataSynch.apiId=DomainAppTest", 
								"dataSynch.apiPassword=DomainAppTest", 
								"dataSynch.deleteSynched=true",
								"dataSynch.deleteDuplicates=false",
								"wiFi.enabled=false"
				} )
@AutoConfigureMockMvc
class DataSynchThread2Test {

	static private Logger LOG = LoggerFactory.getLogger( DataSynchThread2Test.class );
	
	@MockBean
	DataService dataService;

	@MockBean
	JavaMailSender mailSender;
	
	@Autowired
    @Qualifier( "restTemplate" )
	RestTemplate  restTemplate;
	
	@Autowired
	private ObjectMapper objectMapper;	

	@Autowired
	DataSynchThread dbSyncThread;

	private MockRestServiceServer mockServer;	
	
	@BeforeEach
	void setUp() throws Exception {
    	LOG.info( "Initiaizing MockRestServiceServer" );
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(restTemplate);
        mockServer = MockRestServiceServer.createServer(gateway);
	}
	
	@Test
	void runEdgeCasesTest() throws Exception
	{
		//
		//	Test for null user
		//
		Mockito.when(dataService.getUserByName( "DomainAppTest" )).thenReturn( null );
		dbSyncThread.run();
		
		//
		//	Test for authentication exception
		//
		User user = new User( "DomainAppTest", "test", DbSync.ADD, UserRoles.API.toString() );
		Mockito.when(dataService.getUserByName( "DomainAppTest" )).thenReturn( user );

		mockServer.reset();
		mockServer.expect( 
			ExpectedCount.times(10),
			requestTo("http://localhost:8080/api/authorize") 
		)
 		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK  )
		.contentType(MediaType.APPLICATION_JSON )
		.body(objectMapper.writeValueAsString( "test" ))
		); 		
	
		dbSyncThread.run();
		
		mockServer.verify();

		//
		//	Test for heartbeat exception
		//
	
	}

	@Test
	void runEdgeCasesTest2() throws Exception
	{
		//
		//	Test for heartbeat exception
		//

		User user = new User( "DomainAppTest", "test", DbSync.ADD, UserRoles.API.toString() );
		user.setToken( "test token" );
		Mockito.when(dataService.getUserByName( "DomainAppTest" )).thenReturn( user );
		
		mockServer.reset();
		mockServer.expect( 
				requestTo("http://localhost:8080/api/authorize") )
	 		.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatus.OK  )
			.contentType(MediaType.APPLICATION_JSON )
			.body(objectMapper.writeValueAsString(user))
			); 		
		
		mockServer.expect( 
				ExpectedCount.times(10),				
				requestTo("http://localhost:8080/api/heartBeat") )
		 	.andExpect(method(HttpMethod.GET))
		 	.andRespond( withServerError());
		 	;
		
		
		dbSyncThread.run();
		
		mockServer.verify();

	}
	
	@Test( )
	void runMeasurementsEdgeCasesTest() throws Exception
	{
		
		User user = new User( "DomainAppTest", "test", DbSync.ADD, UserRoles.API.toString() );
		user.setToken( "test token" );
		Mockito.when(dataService.getUserByName( "DomainAppTest" )).thenReturn( user );
		
		mockServer.reset();
		mockServer.expect( requestTo("http://localhost:8080/api/authorize") )
	 		.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatus.OK  )
			.contentType(MediaType.APPLICATION_JSON )
			.body(objectMapper.writeValueAsString(user))
			); 		
		
		mockServer.expect( requestTo("http://localhost:8080/api/heartBeat") )
		 	.andExpect(method(HttpMethod.GET))
	        .andRespond(withStatus(HttpStatus.OK  )
	        .contentType( MediaType.TEXT_PLAIN )
	        .body( "ACK" )
        	); 		

		MeasureType measureType = new MeasureType( "TMP", "Temperature", true, 0, 200, GraphTypes.GAUGE, DbSync.ADD  );
		MeasureType measureType2 = new MeasureType( "PH", "PH", true, 0, 14, GraphTypes.SOLID_GUAGE, DbSync.ADD );
		Domain testDomain = new Domain( 0L, 0, "Brewery", "sports_bar_white_48dp.svg", "Home Brewery", "Style", "Brewery", DbSync.ADD, null);
		Category testCategory = new Category( "IPA", "18a", "Hoppy" );
		Process process = new Process( "FRM", "Fermentation", false, DbSync.ADD );
		Process process2 = new Process( "SFRM", "Secondary Fermentation", false, DbSync.ADD );
		Batch testBatch = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, testDomain, new Date() );
		Batch testBatch2 = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, testDomain, new Date() );
		 	
		Measurement measurement = new Measurement( 70.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add( measurement );
    	measurements.add( new Measurement( 71.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() ) );
    	measurements.add( new Measurement( 69.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() ) );
    	measurements.add( new Measurement( 69.3, "", testBatch, process, measureType2, new Date() ) );
    	measurements.add( new Measurement( 69.3, "", testBatch, process2, measureType2, new Date() ) );
    	measurements.add( new Measurement( 69.3, "", testBatch, process2, measureType2, new Date() ) );
		Mockito.when( dataService.getMeasurementsToSynchronize() ).thenReturn( measurements );
        
		mockServer.expect( 
				ExpectedCount.times( 5 ),
				requestTo("http://localhost:8080/api/measurement") )
 		.andExpect(method(HttpMethod.POST))
		.andRespond(withStatus(HttpStatus.OK  )
		.contentType(MediaType.APPLICATION_JSON )
		.body(objectMapper.writeValueAsString( measurement ))
		); 		
	
		dbSyncThread.run();
		
		mockServer.verify();

		
	}
	
	@Test( )
	void runExceptionsTest() throws Exception
	{
		
		User user = new User( "DomainAppTest", "test", DbSync.ADD, UserRoles.API.toString() );
		user.setToken( "test token" );
		Mockito.when(dataService.getUserByName( "DomainAppTest" )).thenReturn( user );
		
		mockServer.reset();
		mockServer.expect( requestTo("http://localhost:8080/api/authorize") )
	 		.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatus.OK  )
			.contentType(MediaType.APPLICATION_JSON )
			.body(objectMapper.writeValueAsString(user))
			); 		
		
		mockServer.expect( requestTo("http://localhost:8080/api/heartBeat") )
		 	.andExpect(method(HttpMethod.GET))
	        .andRespond(withStatus(HttpStatus.OK  )
	        .contentType( MediaType.TEXT_PLAIN )
	        .body( "ACK" )
        	); 		

		Category testCategory = new Category( "IPA", "18a", "Hoppy" );
    	List<Category> categories = new ArrayList<Category>();
    	categories.add( testCategory );
		Mockito.when(dataService.getCategoriesToSynchronize( )).thenReturn( categories );
		
		mockServer.expect( requestTo("http://localhost:8080/api/category") )
 		.andExpect(method(HttpMethod.POST))
		.andRespond( withServerError());
	
		dbSyncThread.run();
		mockServer.verify();		
	}

}
