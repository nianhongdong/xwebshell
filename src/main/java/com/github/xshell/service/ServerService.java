package com.github.xshell.service;


/**
 * 实现自己的类来实现功能
 */
public interface ServerService {

    String getServerIp(String serverId);

    ServerInfo getServerInfo(String serverId);
}
