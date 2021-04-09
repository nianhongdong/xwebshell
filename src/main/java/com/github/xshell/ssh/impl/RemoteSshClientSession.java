package com.github.xshell.ssh.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.github.xshell.ssh.RemoteConnection;
import com.github.xshell.ssh.RemoteException;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.password.PasswordIdentityProvider;
import org.apache.sshd.client.config.hosts.HostConfigEntry;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.future.CloseFuture;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class RemoteSshClientSession extends RemoteAuthenticationConnection implements RemoteConnection {
	
	private static final Logger log = LoggerFactory.getLogger(RemoteSshClientSession.class);
	
	private static final ClientBuilder CLIENT_BUILDER = ClientBuilder.builder();
	
	private static final SshFutureListener<CloseFuture> CLOSEFUTURE_LISTENER = new SshFutureListener<CloseFuture>() {
		
		@Override
		public void operationComplete(CloseFuture future) {
			log.info("close {} completely",future.getId());
		}
	};
	/**
	 * 心跳检测间隔
	 */
	private static long HEART_BEAT_INTERVAL = TimeUnit.SECONDS.toMillis(10);
	/**
	 * 连接超时时间
	 */
	private static long CONNECT_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
	/**
	 * 登录验证时间
	 */
	private static long AUTH_VERIFY_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
	/**
	 * 关闭等待超时时间
	 */
	private long CLOSE_WAIT_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
	
	private volatile ClientSession clientSession;
	
	private volatile SshClient sshClient;
	
	public RemoteSshClientSession(String host, int port, String username, String password) {
		super(host, port, username, password);
	}
	
	@Override
	public synchronized void connect() throws RemoteException{
		
		if(isConnected()) {
			return;
		}
		
		this.sshClient = CLIENT_BUILDER.build();
		
		HostConfigEntry hostConfig = new HostConfigEntry();
		hostConfig.setHostName(getHost());
		hostConfig.setPort(getPort());
		hostConfig.setUsername(getUsername());
		
		//设置访问密码
		this.sshClient.setPasswordIdentityProvider(new InnerPasswordIdentityProvider(getPassword()));
		
		//设置心跳检测
		this.sshClient.setSessionHeartbeat(HeartbeatType.IGNORE, TimeUnit.MILLISECONDS, HEART_BEAT_INTERVAL);
		
		sshClient.start();
		
		String connectMsg = "ssh connect remote host["+ hostConfig.getHostName() +"],port["+ hostConfig.getPort() +"]";
		
		ConnectFuture connectFuture = null;
		try {
			connectFuture = sshClient.connect(hostConfig);
			connectFuture.await(CONNECT_TIMEOUT,TimeUnit.MILLISECONDS);
			if (connectFuture.isConnected()) {
				log.info(connectMsg+" success");
			} else {
				throw new RemoteException(connectMsg+" fail");
			}
		} catch (IOException e) {
			this.close();
			throw new RemoteException(connectMsg+" fail", e);
		}
		
		this.clientSession = connectFuture.getSession();
		
		String verifyMsg = "ssh verify remote host["+ hostConfig.getHostName() +"],port["+ hostConfig.getPort() +"]";
		
		try {
			clientSession.auth().verify(AUTH_VERIFY_TIMEOUT,TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			throw new RemoteException(verifyMsg+" fail",e);
		}

		if (clientSession.isAuthenticated()) {
			log.info(verifyMsg+" success");
		} else {
			this.close();
			throw new RemoteException(connectMsg+" fail");
		}
	}
	
	@Override
	public synchronized void close() {
		//先关闭session
		{
			if(this.clientSession != null) {
				CloseFuture closeFuture = clientSession.close(false);
				closeFuture.addListener(CLOSEFUTURE_LISTENER);
				closeFuture.awaitUninterruptibly(CLOSE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
			}
			this.clientSession = null;
		}
		//最后关闭sshClient
		{
			if(sshClient != null) {
				sshClient.stop();
				CloseFuture closeFuture = sshClient.close(false);
				closeFuture.addListener(CLOSEFUTURE_LISTENER);
				closeFuture.awaitUninterruptibly(CLOSE_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
			}
			this.sshClient = null;
		}
	}
	
	@Override
	public synchronized boolean isConnected() {
		return this.sshClient != null && this.sshClient.isOpen() && this.clientSession != null && this.clientSession.isOpen();
	}

	protected synchronized ClientSession getClientSession() {
		return clientSession;
	}

	protected SshClient getSshClient() {
		return sshClient;
	}
	
	private static final class InnerPasswordIdentityProvider implements PasswordIdentityProvider{
		
		private String password;
		
		public InnerPasswordIdentityProvider(String password) {
			super();
			this.password = password;
		}

		@Override
		public Iterable<String> loadPasswords() {
			return Collections.singleton(password);
		}
	};
}
