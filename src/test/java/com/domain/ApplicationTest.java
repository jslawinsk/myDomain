package com.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import com.domain.service.DataService;
import com.domain.util.PasswordListener;

@RunWith(SpringRunner.class)
@SpringBootTest( properties = { "testdata.create=true", "testdata.createAdmin=true", "blueTooth.enabled=false", "wiFi.enabled=false", "dataSynch.enabled=false" } )
class ApplicationTest {

	@MockBean
    private DataService dataService;

	@MockBean
	PasswordListener passwordListener;

	@MockBean
	JavaMailSender mailSender;

	@Ignore
	@Test
	void runTest() {
		Application.main( new String[] {} );
		assertTrue( true);
	}
	
}
