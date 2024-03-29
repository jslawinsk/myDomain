package com.domain.actuator;

import org.springframework.stereotype.Component;

@Component
public class DataSynchStatus {
	boolean up;
	String message;
	public boolean isUp() {
		return up;
	}
	public void setUp(boolean up) {
		this.up = up;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "DataSynchStatus [up=" + up + ", message=" + message + "]";
	}

	
}
