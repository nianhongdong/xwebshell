package com.github.xshell.ssh;

import java.util.concurrent.TimeUnit;

/**
 * 远程shell
 */
public interface RemoteShell extends RemoteConnection{
	/**
	 * 是否支持命令
	 */
	boolean support(RemoteShellCommand remoteShellCommand);
	
	/**
	 * 执行远程shell命令
	 */
	RemoteShellExecResult execute(RemoteShellCommand remoteShellCommand);
	
	/**
	 * 执行远程shell命令
	 */
	RemoteShellExecResult execute(RemoteShellCommand remoteShellCommand,TimeUnit timeUnit,long timeout);
}
