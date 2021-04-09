package com.github.xshell.service.impl;

import com.github.xshell.service.ServerInfo;
import com.github.xshell.service.ServerService;
import org.springframework.stereotype.Service;

@Service
public class ServerServiceImpl implements ServerService {

    @Override
    public String getServerIp(String serverId) {
        return "127.0.0.1";
    }

    @Override
    public ServerInfo getServerInfo(String serverId) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setIp("127.0.0.1");
        serverInfo.setPassword("123456");
        serverInfo.setPort(22);
        serverInfo.setUsername("root");
        return serverInfo;
    }
}
