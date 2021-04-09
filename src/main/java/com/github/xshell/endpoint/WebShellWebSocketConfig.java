package com.github.xshell.endpoint;

public class WebShellWebSocketConfig {
	
	private int maxIdleTimeout = 60000;
	
	private int maxTextMessageBufferSize = 1024*4;
	
	private String welcomeMessage = "try to connect......\r";
	
	public int getMaxIdleTimeout() {
		return maxIdleTimeout;
	}

	public void setMaxIdleTimeout(int maxIdleTimeout) {
		this.maxIdleTimeout = maxIdleTimeout;
	}

	public int getMaxTextMessageBufferSize() {
		return maxTextMessageBufferSize;
	}

	public void setMaxTextMessageBufferSize(int maxTextMessageBufferSize) {
		this.maxTextMessageBufferSize = maxTextMessageBufferSize;
	}

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}
}
