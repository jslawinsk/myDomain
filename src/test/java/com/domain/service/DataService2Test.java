package com.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import com.domain.model.DbSync;
import com.domain.model.Category;
import com.domain.repository.CategoryRepository;
import com.domain.service.BlueToothService;
import com.domain.service.DataService;

@SpringBootTest( 
				properties = { "blueTooth.enabled=false", 
								"wiFi.enabled=false",
								"dataSynch.enabled=false" }
			)
@AutoConfigureMockMvc
class DataService2Test {

	@MockBean
	CategoryRepository categoryRepository;

	@MockBean
	JavaMailSender mailSender;

	@MockBean
	BlueToothService blueToothService;
	
	@Autowired
	DataService dataService;

	private Category testCategory = new Category( "IPA", "18a", "Hoppy", DbSync.SYNCHED, "TestToken" );
	
	@Test
	void testSaveCategory() throws Exception {
        
        Mockito.when(categoryRepository.save( testCategory )).thenReturn( testCategory );
        testCategory.setDbSynchToken( "" );
        Category category = dataService.saveCategory( testCategory );
        assertEquals( category.getName(), "IPA");
        testCategory.setDbSynchToken( "TestToken" );

        assertEquals( 1, 1 );
        
   	        
		try (MockedStatic<InetAddress> inetAddress = Mockito.mockStatic(InetAddress.class)) {
			inetAddress.when(() -> InetAddress.getLocalHost() ).thenThrow( new UnknownHostException("test"));

			testCategory.setDbSynchToken( "" );

/*
 * 	Sample of catching an exception, which my class does not throw.
 * 	        
	        Assertions.assertThrows( java.net.UnknownHostException.class, () -> {
	    		dataService.saveCategory( testCategory );
	    	}, "UnknownHostException was expected");
	        testCategory.setDbSynchToken( "TestToken" );
*/
			category = dataService.saveCategory( testCategory );
    		
    		assertThat( category.getDbSynchToken() ).doesNotStartWith( "test" );

		}
        
        
        
	}

}
