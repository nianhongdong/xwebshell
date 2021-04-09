package com.github.xshell.ssh.impl;

public interface RemoteShellChannelFactory {

	RemoteShellChannel create(final String host,final int port,final String username,final String password);
}
