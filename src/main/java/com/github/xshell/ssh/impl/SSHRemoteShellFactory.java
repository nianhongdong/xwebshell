package com.github.xshell.ssh.impl;


import com.github.xshell.ssh.RemoteShell;
import com.github.xshell.ssh.RemoteShellFactory;

public class SSHRemoteShellFactory implements RemoteShellFactory {

	@Override
	public RemoteShell create(String host, int port, String username, String password) {
		return new SSHRemoteShell(host, port, username, password);
	}
}
