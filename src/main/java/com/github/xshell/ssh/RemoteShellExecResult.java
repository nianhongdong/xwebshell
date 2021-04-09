package com.github.xshell.ssh;

/**
 * 命令执行存根
 * 
 * 执行的命令可能会长时间占用，我们必须提供能够取消命令执行的手段
 * 
 */
public interface RemoteShellExecResult {
	
	/**
	 * 是否成功
	 * @return
	 */
	boolean success();
	
	/**
	 * 失败原因
	 * @return
	 */
	String cause();

	/**
	 * 结果输出流
	 * @return
	 */
	String result();

}
