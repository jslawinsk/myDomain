package com.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import com.domain.core.DataSynchThread;
import com.domain.service.DataService;
import com.domain.util.PasswordListener;

@SpringBootTest( properties = { "testdata.create=true", "testdata.createAdmin=true", "blueTooth.enabled=false", "wiFi.enabled=false", "dataSynch.enabled=false" } )
@AutoConfigureMockMvc 
public class ApplicationTest {

	public ApplicationTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	@MockBean
    private DataService dataService;

	@MockBean
	PasswordListener passwordListener;

	@MockBean
	JavaMailSender mailSender;

	@Disabled
	@Test
	void runTest() {
		Application.main( new String[] {} );
		assertTrue( true);
	}
	
}
