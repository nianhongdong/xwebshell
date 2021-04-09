package com.github.xshell.ssh.impl;


import com.github.xshell.ssh.RemoteShellExecResult;

public class DefaultRemoteShellExecResult implements RemoteShellExecResult {

	private String cause;
	private boolean success = false;
	private String result;
	
	public void setSuccess() {
		this.success = true;
	}
	
	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public boolean success() {
		return this.success;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	@Override
	public String cause() {
		return cause;
	}

	@Override
	public String result() {
		return result;
	}

	@Override
	public String toString() {
		return "DefaultRemoteShellExecResult"
				+ "["
				+ " cause=" + cause + ", \n"
				+ " success=" + success + ",\n"
				+ " result= \n" + result + "\n]";
	}
}
