package com.github.xshell.ssh;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class RemoteShellCommand {
	
	private String name;
	private String value = "";
	private Map<String,String> options = new HashMap<String,String>();
	
	public RemoteShellCommand() {
		super();
	}
	
	public RemoteShellCommand(String name) {
		super();
		this.name = name;
	}
	
	public RemoteShellCommand(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public RemoteShellCommand option(String key,String value) {
		options.put(key, value);
		return this;
	}
	
	public RemoteShellCommand option(String key) {
		options.put(key, "");
		return this;
	}
	
	public RemoteShellCommand name(String name) {
		this.name = name;
		return this;
	}
	
	public RemoteShellCommand value(String value) {
		this.value = value;
		return this;
	}
	
	public String toCommandString() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(this.name).append(" ").append(this.value);
		
		Iterator<Entry<String, String>>  it = options.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, String> e = it.next();
			builder.append(e.getKey()).append(" ").append(e.getValue());
		}
		
		return builder.toString();
	}

	@Override
	public String toString() {
		return toCommandString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
