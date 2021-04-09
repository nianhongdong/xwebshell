/**
 * <p>Copyright (R) 2014 正方软件股份有限公司。<p>
 */
package com.github.xshell.message;

import com.alibaba.fastjson.JSONObject;

public class DefaultUpstreamAction implements UpstreamAction {
	/**
	 * 动作类型
	 */
	private String action;
	/**
	 * 数据
	 */
	private String data;
	/**
	 * 
	 */
	private int cols;
	/**
	 * 
	 */
	private int rows;
	
	@Override
	public String getAction() {
		return action;
	}
	
	@Override
	public void setAction(String action) {
		this.action = action;
	}
	
	@Override
	public String getData() {
		return data;
	}
	
	@Override
	public void setData(String data) {
		this.data = data;
	}
	
	@Override
	public int getCols() {
		return cols;
	}
	
	@Override
	public void setCols(int cols) {
		this.cols = cols;
	}
	
	@Override
	public int getRows() {
		return rows;
	}
	
	@Override
	public void setRows(int rows) {
		this.rows = rows;
	}
	
	@Override
	public String toJSONString() {
		return JSONObject.toJSONString(this);
	}

	public static UpstreamAction fromJSONString(String json) {
		return JSONObject.parseObject(json, DefaultUpstreamAction.class);
	}
}
