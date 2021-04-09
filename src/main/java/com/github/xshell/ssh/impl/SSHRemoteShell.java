package com.github.xshell.ssh.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.github.xshell.ssh.RemoteShell;
import com.github.xshell.ssh.RemoteShellCommand;
import com.github.xshell.ssh.RemoteShellExecResult;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SSHRemoteShell extends RemoteSshClientSession implements RemoteShell {
	
	private static final Logger log = LoggerFactory.getLogger(SSHRemoteShell.class);
	
	private static final Integer SUCCESS_EXEC_FLAG = Integer.valueOf(0);

	public SSHRemoteShell(String host, int port, String username, String password) {
		super(host, port, username, password);
	}

	@Override
	public boolean support(RemoteShellCommand remoteShellCommand) {
		String name = remoteShellCommand.getName();
		
		RemoteShellCommand typeCommand = new RemoteShellCommand("type");
		typeCommand.value(name);
		
		RemoteShellExecResult result = this.execute(typeCommand);
		if(result.result().contains("not") && result.result().contains("found")) {
			return false;			
		}
		return true;
	}
	
	@Override
	public RemoteShellExecResult execute(RemoteShellCommand remoteShellCommand) {
		return this.execute(remoteShellCommand, TimeUnit.MILLISECONDS, 0L);
	}

	@Override
	public RemoteShellExecResult execute(RemoteShellCommand remoteShellCommand,TimeUnit timeUnit,long timeout) {
		
		DefaultRemoteShellExecResult result = new DefaultRemoteShellExecResult();
		
		log.info("execute cmd [{}]",remoteShellCommand.toCommandString());

		ChannelExec exec = null;
		try {
			exec = getClientSession().createExecChannel(remoteShellCommand.toCommandString());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();

			exec.setOut(out);
			exec.setErr(err);

			exec.open();
			exec.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 0L);
			Integer exitStatus = exec.getExitStatus();

			// 返回0代表成功
			if (SUCCESS_EXEC_FLAG.equals(exitStatus)) {
				result.setSuccess();
				result.setResult(out.toString());
			} else {
				// 设置错误
				result.setResult(err.toString());
			}
		} catch (Throwable e) {
			result.setCause(e.getMessage());
		} finally {
			closeExec(exec);
		}
		return result;
	}
	
	private void closeExec(ChannelExec exec) {
		if(exec != null && !exec.isClosed()) {
			try {
				exec.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
