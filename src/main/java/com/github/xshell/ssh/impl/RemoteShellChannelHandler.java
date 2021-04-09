package com.github.xshell.ssh.impl;

public interface RemoteShellChannelHandler {

	void handleMessage(String msg);
	
	void handleReconnect(RemoteShellChannel channel);
}
