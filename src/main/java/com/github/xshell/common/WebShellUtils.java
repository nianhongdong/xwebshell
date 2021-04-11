package com.github.xshell.common;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebShellUtils {
	
	private static final Logger log = LoggerFactory.getLogger(WebShellUtils.class);

	/**
	 * 构建单连接的webshell
	 * @param request
	 * @param webShellURL
	 * @return
	 */
	public static String buildWebShellPageUrl(ServletRequest request, WebShellURL webShellURL) {
		int port = request.getLocalPort();
		String localAddr = request.getLocalAddr();
		if("0:0:0:0:0:0:0:1".equals(localAddr)){
			localAddr = "127.0.0.1";
		}
		String scheme = request.getScheme();
		String contextPath = request.getServletContext().getContextPath();
		String url = String.format("%s://%s:%d%s/%s?webShellURL=%s", scheme,localAddr,port,contextPath,"webshell/index",webShellURL.toURLString());
		log.debug("build webshell url {}",url);
		return url;
	}
	
	/**
	 * 构建多连接的webshell
	 * @param request
	 * @param webShellURLs
	 * @return
	 */
	public static String buildMultiWebShellPageUrl(ServletRequest request, List<WebShellURL> webShellURLs) {
		int port = request.getLocalPort();
		String localAddr = request.getLocalAddr();
		String scheme = request.getScheme();
		String contextPath = request.getServletContext().getContextPath();
		
		List<String> webshelURLStrs = new ArrayList<String>(webShellURLs.size());
		for(int i =0;i<webShellURLs.size();i++){
			WebShellURL webShellURL = webShellURLs.get(i);
			webshelURLStrs.add(webShellURL.toURLString());
		}
		String strs = StringUtils.join(webshelURLStrs, ",");
		
		String url = String.format("%s://%s:%d%s/%s?webShellURLs=%s", scheme,localAddr,port,contextPath,"webshell/mindex",strs);
		log.debug("build multi webshell url {}",url);
		return url;
	}
}
