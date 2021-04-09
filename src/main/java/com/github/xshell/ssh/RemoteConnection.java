package com.github.xshell.ssh;

import java.io.IOException;

public interface RemoteConnection {

	/**
	 * 是否处于连接状态
	 * @return
	 */
	boolean isConnected();
	
	/**
	 * 连接远程主机
	 * @throws IOException
	 */
	void connect() throws RemoteException;
	
	/**
	 * 关闭连接
	 */
	void close();
}
