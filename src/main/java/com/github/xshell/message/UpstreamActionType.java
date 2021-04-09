/**
 * <p>Copyright (R) 2014 正方软件股份有限公司。<p>
 */
package com.github.xshell.message;

public enum UpstreamActionType {
	
	MESSAGE("message"),RESIZE("resize"),READ("read");

	private String name;

	private UpstreamActionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
