package com.github.xshell.endpoint;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

public class WebSocketModifyHandshakeConfigurator extends Configurator{

    @Override
    public void modifyHandshake(ServerEndpointConfig serverEndpointConfig, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        if(httpSession != null) {
        	serverEndpointConfig.getUserProperties().put(HttpSession.class.getName(),httpSession);
        }
    }
}