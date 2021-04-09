/**
 * <p>Copyright (R) 2014 正方软件股份有限公司。<p>
 */
package com.github.xshell.message;

public interface UpstreamAction {

	String getAction();

	void setAction(String action);

	String getData();

	void setData(String data);

	int getCols();

	void setCols(int cols);

	int getRows();

	void setRows(int rows);
	
	String toJSONString();
	
}