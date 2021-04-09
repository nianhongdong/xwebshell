package com.github.xshell.common;

import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSON;

/**
 * WebShellURL
 * @author 1571
 */
public class WebShellURL {
	/**
	 * 协议
	 */
	@SuppressWarnings("unused")
    private String protocol = WebShellPotocols.ZFOPS_DEFAULT_WEBSHELL;
	/**
	 * 标题
	 */
	private String title = "";
	/**
	 * 服务器id
	 */
	private String serverId = "";
	
	private String host;
	
	private int port;
	
	@SuppressWarnings("unused")
    private String password;
	/**
	 * 写的空闲超时时间
	 */
	private long writeIdleTimeout = 0L;
	/**
	 * 读的空闲超时时间
	 */
	private long readIdleTimeout = 0L;
	/**
	 * 延时任务
	 */
	private WebShellDelayCommand delayCommand;
	
	private String random = UUID.randomUUID().toString();
	
	private boolean canRead = true;
	
	private boolean canWrite = true;
	
	public WebShellURL() {
		super();
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getRandom() {
		return random;
	}
	
	public void setRandom(String random) {
		this.random = random;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getWriteIdleTimeout() {
		return writeIdleTimeout;
	}

	public void setWriteIdleTimeout(long writeIdleTimeout) {
		this.writeIdleTimeout = writeIdleTimeout;
	}

	public long getReadIdleTimeout() {
		return readIdleTimeout;
	}

	public void setReadIdleTimeout(long readIdleTimeout) {
		this.readIdleTimeout = readIdleTimeout;
	}

	public WebShellDelayCommand getDelayCommand() {
		return delayCommand;
	}

	public void setDelayCommand(WebShellDelayCommand delayCommand) {
		this.delayCommand = delayCommand;
	}

	public String toURLString(){
		String json = JSON.toJSONString(this);
		return Base64.encodeBase64URLSafeString(json.getBytes());
	}
	
	public static WebShellURL parse(String str) {
		String originJsonStr = new String(Base64.decodeBase64(str));
		WebShellURL url = JSON.parseObject(originJsonStr,WebShellURL.class);
		return url;
	}
}
