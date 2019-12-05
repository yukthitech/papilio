package com.yukthitech.papilio;

/**
 * Listener to observe erorrs.
 * @author akiran
 */
public interface IErrorListener
{
	public String LOCK_FAILED = "Failed to obtain the lock.";
	
	/**
	 * Called on error.
	 * @param changesetId
	 * @param errorMssg
	 */
	public void onError(String changesetId, String errorMssg, Object... args);
}
