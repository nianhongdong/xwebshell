package com.github.xshell.ssh.impl;

public class RemoteAuthenticationConnection{
	
	private final String host;
	private final int port;
	private final String username;
	private final String password;
	
	public RemoteAuthenticationConnection(String host, int port, String username, String password) {
		super();
		if(host == null) {
			throw new NullPointerException("host");
		}
		if(port <= 0) {
			throw new IllegalArgumentException("port <= 0");
		}
		if(username == null) {
			throw new NullPointerException("username");
		}
		if(password == null) {
			throw new NullPointerException("password");
		}
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
