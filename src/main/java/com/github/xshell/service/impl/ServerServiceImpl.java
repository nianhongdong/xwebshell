package com.github.xshell.service.impl;

import com.github.xshell.service.ServerInfo;
import com.github.xshell.service.ServerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServerServiceImpl implements ServerService {

    @Value("${ssh.testhost}")
    private String ip;

    @Value("${ssh.port}")
    private int port;

    @Value("${ssh.username}")
    private String username;

    @Value("${ssh.password}")
    private String password;

    @Override
    public String getServerIp(String serverId) {
        return this.ip;
    }

    @Override
    public ServerInfo getServerInfo(String serverId) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setIp(this.ip);
        serverInfo.setPassword(this.password);
        serverInfo.setPort(this.port);
        serverInfo.setUsername(this.username);
        return serverInfo;
    }
}
