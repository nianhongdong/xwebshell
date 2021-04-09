package com.github.xshell.ssh;

/**
 * 远程异常
 */
public class RemoteException extends Exception{

	private static final long serialVersionUID = 1L;

	public RemoteException(String message) {
        super(message);
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
