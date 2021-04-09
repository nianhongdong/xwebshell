package com.github.xshell.common;

import org.apache.commons.lang3.StringUtils;

/**
 * 延迟命令，当初始化ssh连接之后，可延迟执行，用于可提供展示数据的命令，如
 * tail -f 
 * top
 * 
 * @author weiguangyue
 */
public class WebShellDelayCommand {

	/**
	 * 命令文本
	 */
	private String command;
	
	/**
	 * 延迟时间,单位毫秒
	 */
	private int timeout;
	
	public WebShellDelayCommand() {
		super();
	}

	public WebShellDelayCommand(String command, int timeout) {
		super();
		if(StringUtils.isEmpty(command)){
			throw new NullPointerException("command");
		}
		this.command = command;
		if(timeout < 0){
			timeout = 0;
		}
		this.timeout = timeout;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
}
