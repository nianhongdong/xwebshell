package com.github.xshell.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.xshell.common.WebShellDelayCommand;
import com.github.xshell.common.WebShellURL;
import com.github.xshell.common.WebShellUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * @author weiguangyue
 */
@Controller
@RequestMapping("webshellDemo")
public class WebShellDemoController {
	
	private String serverId = "1";

	private String command = "tail -f /Users/xiaomo/logs/a.log";
	
	@RequestMapping("index")
	public String index(HttpServletRequest httpServletRequest){
		httpServletRequest.setAttribute("base",httpServletRequest.getContextPath());
		return "webshell/webshellDemo";
	}
	
	@RequestMapping("watchLogPage")
	public void watchLogPage(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) throws IOException{
		
		WebShellDelayCommand delayCommand = new WebShellDelayCommand(command,5000);
		
		WebShellURL webShellURL = new WebShellURL();
		webShellURL.setDelayCommand(delayCommand);
		webShellURL.setServerId(serverId);
		
		String url = WebShellUtils.buildWebShellPageUrl(httpServletRequest, webShellURL);
		
		System.out.println("重定向到"+url);
		
		httpServletResponse.sendRedirect(url);
	}
	
	@RequestMapping("page")
	public void page(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) throws IOException{
		
		WebShellURL webShellURL = new WebShellURL();
		webShellURL.setServerId(serverId);
		
		String url = WebShellUtils.buildWebShellPageUrl(httpServletRequest, webShellURL);
		
		System.out.println("重定向到"+url);

		httpServletResponse.sendRedirect(url);
	}
	
	@RequestMapping("watchLogMultiPage")
	public void watchLogMultiPage(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) throws IOException{
		
		List<WebShellURL> webShellURLs = new ArrayList<WebShellURL>();
		
		{
			WebShellDelayCommand delayCommand = new WebShellDelayCommand(command,5000);
			
			WebShellURL webShellURL = new WebShellURL();
			webShellURL.setDelayCommand(delayCommand);
			webShellURL.setServerId(serverId);
			webShellURLs.add(webShellURL);
		}
		
		{
			WebShellDelayCommand delayCommand = new WebShellDelayCommand(command,5000);
			
			WebShellURL webShellURL = new WebShellURL();
			webShellURL.setDelayCommand(delayCommand);
			webShellURL.setServerId(serverId);
			webShellURLs.add(webShellURL);
		}
		
		String url = WebShellUtils.buildMultiWebShellPageUrl(httpServletRequest, webShellURLs);
		
		System.out.println("重定向到"+url);
		
		httpServletResponse.sendRedirect(url);
	}
	
	@RequestMapping("mpage")
	public void mpage(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) throws IOException{
		
		List<WebShellURL> webShellURLs = new ArrayList<WebShellURL>();
		
		{
			WebShellURL webShellURL = new WebShellURL();
			webShellURL.setServerId(serverId);
			webShellURLs.add(webShellURL);
		}
		
		{
			WebShellURL webShellURL = new WebShellURL();
			webShellURL.setServerId(serverId);
			webShellURLs.add(webShellURL);
		}
		
		String url = WebShellUtils.buildMultiWebShellPageUrl(httpServletRequest, webShellURLs);
		
		System.out.println("重定向到"+url);
		
		httpServletResponse.sendRedirect(url);
	}
}
