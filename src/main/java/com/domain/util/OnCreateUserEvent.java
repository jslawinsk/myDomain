package com.domain.util;

import org.springframework.context.ApplicationEvent;

import com.domain.model.User;

public class OnCreateUserEvent extends ApplicationEvent {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6049122818855465972L;
	
	private String serverUrl;
	private String appPath;
    private User user;

    public OnCreateUserEvent(User user, String serverUrl, String appPath) {
        super(user);

        this.user = user;
        this.serverUrl = serverUrl;
        this.appPath = appPath;
    }

    public String getAppPath() {
        return appPath;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public User getUser() {
        return user;
    }
}
