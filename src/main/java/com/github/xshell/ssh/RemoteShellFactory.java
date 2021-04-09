package com.github.xshell.ssh;

public interface RemoteShellFactory {
	
	RemoteShell create(final String host,final int port,final String username,final String password);
}
