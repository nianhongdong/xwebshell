package com.github.xshell.endpoint;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.github.xshell.common.WebShellDelayCommand;
import com.github.xshell.common.WebShellURL;
import com.github.xshell.message.DefaultUpstreamAction;
import com.github.xshell.message.UpstreamAction;
import com.github.xshell.message.UpstreamActionType;
import com.github.xshell.service.ServerInfo;
import com.github.xshell.service.ServerService;
import com.github.xshell.ssh.RemoteException;
import com.github.xshell.ssh.impl.ReconnectableRemoteShellChannelFactory;
import com.github.xshell.ssh.impl.RemoteShellChannel;
import com.github.xshell.ssh.impl.RemoteShellChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * webshell的websocket端点
 * @author 1571
 */
@ServerEndpoint(value = "/webshell/{webshellURL}", configurator = WebSocketModifyHandshakeConfigurator.class)
public class WebShellWebSocketEndpoint implements RemoteShellChannelHandler {

	private static final Logger log = LoggerFactory.getLogger(WebShellWebSocketEndpoint.class);
	
	//private static final long MIN_IDLE_TIME = TimeUnit.MINUTES.toMillis(3);
	private static final long MIN_IDLE_TIME = TimeUnit.MINUTES.toMillis(1);
	
	private static final String WEBSOCKET_SHELL_CHANNEL_KEY = "com.zfsoft.ops.webshell.endpoint.WebShellWebSocketEndpoint.RemoteShellChannel";
	
	private final ReconnectableRemoteShellChannelFactory factory = new ReconnectableRemoteShellChannelFactory();
	
	private static final AtomicLong ID = new AtomicLong(0);
	
	//FIXME 共享此周期调度线程池
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3,new ThreadFactory(){

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r,"WebShellWebSocketEndpoint-scheduledExecutor-"+ID.incrementAndGet());
		}
	});
	
	private volatile ScheduledFuture<?> delayCommandFuture;
	
	private volatile ScheduledFuture<?> idleCheckFuture;
	
	//TODO 可配置的webSocketConfig
	private final WebShellWebSocketConfig webShellWebSocketConfig = new WebShellWebSocketConfig();

	private volatile Session session;
	
	//write这里代表用户在浏览器输入
	private final AtomicLong lastWriteTime = new AtomicLong(System.currentTimeMillis());
	
	//read这里代码从ssh服务器读取数据
	private final AtomicLong lastReadTime = new AtomicLong(System.currentTimeMillis());
	
	private volatile WebShellURL webShellURLObject = null;
	
	/**
	 * TODO 防止反复刷新!!!
	 * @param webshellURL
	 * @param session
	 * @param config
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
    @OnOpen
	public void onOpen(@PathParam("webshellURL")String webshellURL,Session session, EndpointConfig config) throws IOException {
		
		this.session = session;
		
		session.setMaxIdleTimeout(this.webShellWebSocketConfig.getMaxIdleTimeout());
		session.setMaxTextMessageBufferSize(this.webShellWebSocketConfig.getMaxTextMessageBufferSize());
		
		session.getAsyncRemote().sendText(this.webShellWebSocketConfig.getWelcomeMessage());

		HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
		ServletContext servletContext = httpSession.getServletContext();
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.findWebApplicationContext(servletContext);
		
		//TODO 异常处理 !!!
		this.webShellURLObject = WebShellURL.parse(webshellURL);
		
		String serverId = this.webShellURLObject.getServerId();

		ServerService serverService = webApplicationContext.getBean(ServerService.class);
		ServerInfo serverInfo = serverService.getServerInfo(serverId);
		
		String username = serverInfo.getUsername();
		String password = serverInfo.getPassword();
		String ip = serverInfo.getIp();
		int port = serverInfo.getPort();
		
		final RemoteShellChannel channel = factory.create(ip, port, username,password);
		//DO NOT FORGATE THIS LINE CODE
		channel.setRemoteShellChannelHandler(this);
		
		//if you want test idle check,just open this two line's comments!!!
		//this.webShellURLObject.setReadIdleTimeout(1000);
		//this.webShellURLObject.setWriteIdleTimeout(1000);
		
		//读写空闲检测
		{
			if(this.webShellURLObject.getReadIdleTimeout() > 0){
				long idleTimeout = this.webShellURLObject.getReadIdleTimeout() < MIN_IDLE_TIME  ? MIN_IDLE_TIME : this.webShellURLObject.getReadIdleTimeout();
				this.webShellURLObject.setReadIdleTimeout(idleTimeout);
			}
		}
		
		{
			if(this.webShellURLObject.getWriteIdleTimeout() > 0){
				long idleTimeout = this.webShellURLObject.getWriteIdleTimeout() < MIN_IDLE_TIME  ? MIN_IDLE_TIME : this.webShellURLObject.getWriteIdleTimeout();
				this.webShellURLObject.setWriteIdleTimeout(idleTimeout);
			}
		}
		
		//10秒检测一次
		int interval = 10;
		if(this.webShellURLObject.getReadIdleTimeout() > 0 || this.webShellURLObject.getWriteIdleTimeout() > 0){
			//this.idleCheckFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new IdleCheckTask(),interval, interval, TimeUnit.SECONDS);
		}
		
		if(this.webShellURLObject.getDelayCommand() != null){
			WebShellDelayCommand command = this.webShellURLObject.getDelayCommand();
			this.delayCommandFuture = this.scheduledExecutorService.schedule(new Runnable(){

				@Override
				public void run() {
					try {
						if(channel.isConnected()){
							channel.write(command.getCommand());
							channel.write("\r");
						}else{
							log.debug("channel is close , ignore send command :{}",command.getCommand());
						}
					} catch (IOException e) {
						log.error("send command [{}] error ,cause:"+e.getMessage(),e);
					}
				}
			}, command.getTimeout(), TimeUnit.MILLISECONDS);
		}

		log.debug("open {}", this);
		
		try {
			channel.connect();

		} catch (RemoteException e) {
			
			String msg = String.format("connect server[serverId=%s,ip=%s,port=%s] fail , cause:%s", serverId,ip,port,e.getMessage());
			
			log.error(msg,e);
			
			this.doClose(channel);
			
		}
		//此处无论是否连接成功，都必须要设置
		session.getUserProperties().put(WEBSOCKET_SHELL_CHANNEL_KEY, channel);	
	}
	
	private void doClose(RemoteShellChannel channel){
		
		if(this.delayCommandFuture != null){
			this.delayCommandFuture.cancel(true);			
		}
		
		if(this.idleCheckFuture != null){
			this.idleCheckFuture.cancel(true);
		}
		
		this.scheduledExecutorService.shutdown();
		
		for(int i =0;i<100;i++){
			try {
				if(this.scheduledExecutorService.awaitTermination(10, TimeUnit.MILLISECONDS)){
					break;
				}
			} catch (InterruptedException e) {
				//ignore
			}
		}
		channel.close();
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) throws IOException {

		RemoteShellChannel channel  = (RemoteShellChannel) session.getUserProperties().remove(WEBSOCKET_SHELL_CHANNEL_KEY);
		if (channel != null) {
			this.doClose(channel);
		}
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException {

		if(log.isDebugEnabled()){
			log.debug("recevie message : {}", message);			
		}

		UpstreamAction upstreamAction = DefaultUpstreamAction.fromJSONString(message);
		String actionType = upstreamAction.getAction();
		
		if (UpstreamActionType.MESSAGE.getName().equals(actionType)) {
			
			this.doActionMessage(session, upstreamAction);
			
		} else if (UpstreamActionType.RESIZE.getName().equals(actionType)) {
			
			this.doActionResize(session, upstreamAction);
			
		} else if (UpstreamActionType.READ.getName().equals(actionType)) {
			
			this.doActionRead(session, upstreamAction);
			
		} else {
			log.warn("actionType:{} is not supported!!!",actionType);
		}
	}

	private void doActionRead(Session session, UpstreamAction upstreamAction) throws IOException {
		RemoteShellChannel channel  =  (RemoteShellChannel) session.getUserProperties().get(WEBSOCKET_SHELL_CHANNEL_KEY);
		if(channel != null && channel.isConnected()){
			channel.write(upstreamAction.getData());		
			
			this.lastReadTime.set(System.currentTimeMillis());
			
		}else{
			log.warn("ignore read action , cause channel is not connected!!!");
		}
	}

	private void doActionResize(Session session, UpstreamAction upstreamAction) {
		RemoteShellChannel channel  =  (RemoteShellChannel)session.getUserProperties().get(WEBSOCKET_SHELL_CHANNEL_KEY);
		if(channel != null && channel.isConnected()){
			channel.sendWindowChange(upstreamAction.getCols(), upstreamAction.getRows());			
		}else{
			log.warn("ignore resize action , cause channel is not connected!!!");
		}
	}

	private void doActionMessage(Session session, UpstreamAction upstreamAction) throws IOException{
		RemoteShellChannel channel  =  (RemoteShellChannel)session.getUserProperties().get(WEBSOCKET_SHELL_CHANNEL_KEY);
		if(channel != null &&  channel.isConnected()){
			String data = upstreamAction.getData();

			if ("exit".equals(data) || "logout".equals(data)) {
				
				if (channel != null) {
					channel.close();
				}
				session.close();
			} else {
				channel.write(data);
			}
		}else{
			log.warn("ignore messager action , cause channel is not connected!!!");
		}
	}

	@OnError
	public void onError(Session session, Throwable thr) {
		log.error("webSocket error:"+thr.getMessage(),thr);
	}

	@Override
	public void handleMessage(String msg) {
		try {
			if(session != null && session.isOpen()){
				
				//session.getAsyncRemote().sendText(msg);
				session.getBasicRemote().sendText(msg);
				
				this.lastReadTime.set(System.currentTimeMillis());
				
			}else{
				log.warn("session:[{}] is close",session);
			}
		} catch (Exception e) {
			log.error("",e);
		}
	}

	@Override
	public void handleReconnect(RemoteShellChannel channel) {
		try {
			if(session != null && session.isOpen()){
				//session.getAsyncRemote().sendText("reconnecting , please wait ... ");
				session.getBasicRemote().sendText("reconnecting , please wait ... ");				
			}else{
				log.warn("session:[{}] is close",session);
			}
		} catch (Exception e) {
			log.error("webSocket ioException,cause:"+e.getMessage(),e);
		}
	}
	
	@SuppressWarnings("unused")
    private class IdleCheckTask implements Runnable{

		@Override
		public void run() {
			try{
				doTask();				
			}catch (Throwable t) {
				log.error("idleCheck error,cause:"+t.getMessage(),t);
			}
		}

		private void doTask() {
			
			final Session session = WebShellWebSocketEndpoint.this.session;
			ScheduledFuture<?> thisTaskFuture = WebShellWebSocketEndpoint.this.idleCheckFuture;
			final RemoteShellChannel channel  = (RemoteShellChannel) session.getUserProperties().get(WEBSOCKET_SHELL_CHANNEL_KEY);
			
			if(!session.isOpen() && channel == null || !channel.isConnected()){
				log.warn("remote ssh[{}:{}] is closed !",webShellURLObject.getHost(),webShellURLObject.getPort());
				return;
			}
			
			long now = System.currentTimeMillis();
			
			boolean isIdle = (now - lastReadTime.get() > webShellURLObject.getReadIdleTimeout())// 
					|| (now - lastWriteTime.get() > webShellURLObject.getWriteIdleTimeout());//

			if(isIdle){
				log.warn("remote ssh[{}:{}] read timeout or write timeout ,close it after 5 seconds",webShellURLObject.getHost(),webShellURLObject.getPort());
				
				thisTaskFuture.cancel(true);
				
				Future<Void> closeSessionFutrue = session.getAsyncRemote().sendText("close ssh connection,you can refresh page if you want reconnect ssh");
				
				WebShellWebSocketEndpoint.this.scheduledExecutorService.schedule(new CloseWebsocketSessionTask(session,closeSessionFutrue),5, TimeUnit.SECONDS);

			}else{
				log.debug("remote ssh[{}:{}] idle check , pass",webShellURLObject.getHost(),webShellURLObject.getPort());
			}
		}
	}
	
	private class CloseWebsocketSessionTask implements Runnable{

		private final Session session;
		private final Future<Void> closeSessionFutrue;
		
		public CloseWebsocketSessionTask(Session session,Future<Void> closeSessionFutrue) {
			super();
			this.session = session;
			this.closeSessionFutrue = closeSessionFutrue;
		}
		@Override
		public void run() {
			try {
				log.debug("send bay bay message done:"+this.closeSessionFutrue.isDone());
				this.session.close(new CloseReason(CloseCodes.VIOLATED_POLICY,"reconnect later"));
			} catch (IOException e) {
				log.error("");
			}
		}
	}
}