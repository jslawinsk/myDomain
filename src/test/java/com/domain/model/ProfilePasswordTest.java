package com.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.domain.model.ProfilePassword;

class ProfilePasswordTest {

	@Test
	void testSetUsername() {
		ProfilePassword profilePassword = new ProfilePassword();
		profilePassword.setUsername( "Test" );
		assertEquals( "Test", profilePassword.getUsername() );
	}

}
