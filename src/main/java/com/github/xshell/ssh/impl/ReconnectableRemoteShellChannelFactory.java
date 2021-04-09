package com.github.xshell.ssh.impl;

public class ReconnectableRemoteShellChannelFactory implements RemoteShellChannelFactory{

	@Override
	public RemoteShellChannel create(String host, int port, String username, String password) {
		return new DefaultRemoteShellChannel(host, port, username, password);
	}
}
