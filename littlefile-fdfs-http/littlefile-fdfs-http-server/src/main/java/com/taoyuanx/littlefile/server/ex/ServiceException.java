package com.taoyuanx.littlefile.server.ex;


/**
 * @author dushitaoyuan
 *  业务异常
 */
public class ServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -907834946583023231L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}
	public ServiceException(String message,Throwable cause) {
		super(message,cause);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

}
