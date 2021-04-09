package com.github.xshell.ssh;

public interface ReconnectableRemoteConnection extends RemoteConnection{

	boolean isReconnecting();
	
}
