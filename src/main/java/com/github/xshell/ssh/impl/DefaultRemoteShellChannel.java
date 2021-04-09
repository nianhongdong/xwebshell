package com.github.xshell.ssh.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.github.xshell.ssh.RemoteException;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.OpenFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class DefaultRemoteShellChannel extends ReconnectRemoteSshClientSession implements RemoteShellChannel,Runnable{
	
	private static final Logger log = LoggerFactory.getLogger(ReconnectRemoteSshClientSession.class);
	
	private volatile RemoteShellChannelHandler remoteShellChannelHandler;
	
	private Thread readThread;

	private BufferedWriter bufferWriter;
	
	private ChannelShell channelShell;
	
	public DefaultRemoteShellChannel(String host, int port, String username, String password) {
		super(host, port, username, password);
	}
	
	@Override
	public synchronized void connect() throws RemoteException {
		super.connect();

		if(isConnected()) {
			
			OpenFuture openFuture = null;
			
			try {
				this.channelShell = getClientSession().createShellChannel();
				this.channelShell.setPtyType("xterm");
				openFuture = channelShell.open();
				openFuture.await();
			} catch (IOException e) {
				throw new RemoteException(e.getMessage(),e);
			}
			if(openFuture.isOpened()){
				
				OutputStream outputStream = channelShell.getInvertedIn();
				try {
					this.bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					throw new RemoteException(e.getMessage(),e);
				}
				
				this.readThread = new Thread(this, String.format("DefaultRemoteShellChannel-readThread-%s-%d-%s",getHost(), getPort(),getUsername()));
				this.readThread.setDaemon(true);
				this.readThread.start();
			}else{
				String msg = String.format("connect remote address[{}:{}] fail", getHost(),getPort());
				throw new RemoteException(msg);
			}
		}
	}
	
	@Override
	public synchronized void close() {
		
		if(this.bufferWriter != null) {
			try {
				this.bufferWriter.close();
			} catch (IOException e) {
				//ignore
			}
		}
		
		if(this.channelShell != null) {
			try {
				this.channelShell.close();
			} catch (IOException e) {
				//ignore
			}
		}
		
		if(this.readThread != null) {
			this.readThread.interrupt();
		}
		super.close();
	}

	@Override
	public void write(String msg) throws IOException{
		if(isConnected()) {
			this.bufferWriter.write(msg);
			this.bufferWriter.flush();
		}else{
			log.warn("ssh channel close , ignore message:[{}]",msg);
		}
	}

	@Override
	public void setRemoteShellChannelHandler(RemoteShellChannelHandler remoteShellChannelHandler) {
		this.remoteShellChannelHandler = remoteShellChannelHandler;
	}
	
	@Override
	protected synchronized void doReconnect() {

		super.doReconnect();
		
		if(this.remoteShellChannelHandler != null) {
			this.remoteShellChannelHandler.handleReconnect(this);
		}
	}

	@Override
	public void run() {
		
		String threadName = Thread.currentThread().getName();
		log.debug("read thread[{}] start",threadName);
		
		byte[] buff = new byte[4 * 1024];
		try {
			int readCount = -1;
			while (//
					isConnected() &&// 
					this.channelShell.isOpen() &&// 
					(readCount = this.channelShell.getInvertedOut().read(buff)) > 0 &&// 
					!Thread.currentThread().isInterrupted()//
				) {//

				StringBuilder stringBuilder = new StringBuilder();
				for (int i = 0; i < readCount; i++) {
					char c = (char) (buff[i] & 0xff);
					stringBuilder.append(c);
				}
				if (this.remoteShellChannelHandler != null) {
					this.remoteShellChannelHandler.handleMessage(new String(stringBuilder.toString().getBytes("ISO-8859-1"),"UTF-8"));
				}
			}
		} catch (IOException e){
			//when inputStream is close
			log.warn("read remote ssh[{}:{}] errro,cause:"+e.getMessage(),getHost(),getPort(),e);
		} catch (Throwable e) {
			log.error("read remote ssh[{}:{}] error,cause:"+e.getMessage(),getHost(),getPort(),e);
		}
		
		log.debug("read thread[{}] stoped",threadName);
	}

	@Override
	public void sendWindowChange(int columns, int lines) {
		try {
			this.channelShell.sendWindowChange(columns, lines);
		} catch (IOException e) {
			log.error("",e);
		}
	}
}
