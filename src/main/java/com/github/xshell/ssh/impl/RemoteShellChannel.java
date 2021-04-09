package com.github.xshell.ssh.impl;

import com.github.xshell.ssh.RemoteConnection;

import java.io.IOException;


public interface RemoteShellChannel extends RemoteConnection {

	void write(String msg) throws IOException;
	
	void sendWindowChange(int columns, int lines);
	
	void setRemoteShellChannelHandler(RemoteShellChannelHandler remoteShellChannelHandler);
}
