package com.github.xshell.ssh.impl;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.github.xshell.ssh.ReconnectableRemoteConnection;
import com.github.xshell.ssh.RemoteException;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectRemoteSshClientSession extends RemoteSshClientSession implements ReconnectableRemoteConnection, Runnable{

	private static final AtomicLong EXECUTOR_ID = new AtomicLong(0);
	
	private static final Logger log = LoggerFactory.getLogger(ReconnectRemoteSshClientSession.class);
	
	private static final long RECONNECT_INTERVAL = TimeUnit.SECONDS.toMillis(5);
	
	private AtomicLong reconnectTimes = new AtomicLong(0L);

	//TODO 这个线程池会随着配置的ssh连接数而增长,当配置了100个ssh连接，则就会有100个这样的线程池!!!
	//如果是这台机器发生网络错误,那么就有100个网络要进行重连......
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			String name = String.format("remoteShell-reconnect-thread-%d-[%s:%d]", EXECUTOR_ID.getAndIncrement(),getHost(),getPort());
			return new Thread(r, name);
		}
	});

	private volatile ScheduledFuture<?> future;

	private final SessionClosedListener sessionClosedListener = new SessionClosedListener();
	
	public ReconnectRemoteSshClientSession(String host, int port, String username, String password) {
		super(host, port, username, password);
	}

	protected synchronized void connectInner() throws RemoteException {
		super.connect();
	}

	@Override
	public synchronized void connect() throws RemoteException {
		
		super.connect();

		this.getClientSession().addSessionListener(sessionClosedListener);
		
	}

	protected synchronized void closeInner() {
		
		ClientSession clientSession = this.getClientSession();

		if(clientSession != null){
			clientSession.removeSessionListener(sessionClosedListener);
		}

		super.close();
	}

	@Override
	public synchronized void close() {

		if (this.future != null) {
			this.future.cancel(true);
		}

		scheduledExecutorService.shutdown();

		for (int i = 0; i < 1000; i++) {
			try {
				if (scheduledExecutorService.awaitTermination(10, TimeUnit.MILLISECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				// ignore
			}
		}
		this.closeInner();
	}

	protected synchronized void doReconnect() {

		this.closeInner();

		this.future = scheduledExecutorService.scheduleAtFixedRate(this,0,reconnectInterval(), TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void run() {
		try {
			log.info("reconnect remoteShell[host=" + getHost() + ",port=" + getPort() + "]");
			
			reconnectTimes.incrementAndGet();
			
			connectInner();
			
			if (isConnected()) {
				getClientSession().addSessionListener(sessionClosedListener);
				this.future.cancel(false);
			}
		} catch (Throwable e) {
			log.error("reconnect remote shell[host="+ getHost() +",port="+ getPort() +"] error : " + e.getMessage(),e);
		}
	}
	
	private long reconnectInterval() {
		return RECONNECT_INTERVAL + new Random(1000).nextLong();
	}
	
	private class SessionClosedListener implements SessionListener {

		@Override
		public void sessionClosed(Session session) {
			log.warn("remoteShell[host=" + getHost() + ",port=" + getPort() + "] closed");
			doReconnect();
		}
	}

	@Override
	public synchronized boolean isReconnecting() {
		return this.future != null && !this.future.isDone();
	}
}
