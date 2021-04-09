package com.github.xshell.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.github.xshell.common.WebShellURL;
import com.github.xshell.common.WebShellUtils;
import com.github.xshell.service.ServerService;
import com.github.xshell.vo.WebShellURLVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("webshell")
//TODO 安全监测 比如说serverId是否存在等
public class WebShellController {
	
	private static final AtomicLong ELEMENT_ID = new AtomicLong(System.currentTimeMillis()); 

	@Autowired
	private ServerService serverService;
	
	/**
	 * 单ssh连接的页面
	 * @param httpServletRequest
	 * @param webShellURL
	 * @return
	 */
	@RequestMapping("index")
	public String index(HttpServletRequest httpServletRequest,@RequestParam(required=true)String webShellURL){
		
		WebShellURL webshellURLObject = WebShellURL.parse(webShellURL);
		
		Map<String,Object> configMap = new HashMap<String,Object>();
		
		String localAddr = httpServletRequest.getLocalAddr();
		int port = httpServletRequest.getLocalPort();
		String context = httpServletRequest.getContextPath();
		
		String host = String.format("http://%s:%d%s", localAddr,port,context);
		String app_context = String.format("%s/webshell",host);
		String ws_url = String.format("ws://%s:%d%s/webshell/%s",localAddr,port,context,webShellURL);
		
		String eleId = ""+ELEMENT_ID.incrementAndGet();
		
		Map<String,Object> map = new HashMap<String,Object>(configMap);
		map.put("web_host", host);
		map.put("app_context", app_context);
		map.put("ws_url", ws_url);
		map.put("eleId",eleId);
		String str = JSON.toJSONString(map);
		String startupJavaScript = String.format("console.log('init');G=%s",str);
		
		httpServletRequest.setAttribute("webshellURLObject",webshellURLObject);
		httpServletRequest.setAttribute("eleId",eleId);
		httpServletRequest.setAttribute("startupJavaScript", startupJavaScript);
		
		return "/globalweb/comp/ops/webshell/webshellHome";
	}
	
	/**
	 * 多ssh连接的页面，页面内有标签，可点击切换
	 * @param httpServletRequest
	 * @return
	 */
	@RequestMapping("mindex")
	public String mindex(HttpServletRequest httpServletRequest,@RequestParam(required=true)String[] webShellURLs){
		
		List<String> list = Arrays.asList(webShellURLs);
		
		List<String> serverIds = new ArrayList<String>(list.size());
		
		List<WebShellURLVO> webShellURLVOList = new ArrayList<WebShellURLVO>();
		
		for(String str : Arrays.asList(webShellURLs)){
			
			WebShellURL webShellURLObject = WebShellURL.parse(str);
			String serverId = webShellURLObject.getServerId();
			
			String ip = serverService.getServerIp(serverId);

			WebShellURLVO vo = new WebShellURLVO();
			vo.setServerId(webShellURLObject.getServerId());
			vo.setHost(ip);
			
			String title = String.format("ssh-serverId-%s-(%s)",// 
					webShellURLObject.getServerId(),//
					ip//
					);//
			vo.setTitle(title);
			
			String url = WebShellUtils.buildWebShellPageUrl(httpServletRequest, webShellURLObject);
			vo.setUrl(url);
			
			webShellURLVOList.add(vo);
		}
		
		httpServletRequest.setAttribute("webShellURLVOList",webShellURLVOList);
		httpServletRequest.setAttribute("title","ssh连接-"+ StringUtils.join(serverIds, ","));
		
		return "/globalweb/comp/ops/webshell/mwebshellHome";
	}
}
