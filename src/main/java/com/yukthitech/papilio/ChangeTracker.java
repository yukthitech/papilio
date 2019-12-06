package com.yukthitech.papilio;

/**
 * Tracker to track changes going on.
 * 
 * @author akiran
 */
public class ChangeTracker
{
	/**
	 * The lock failed.
	 */
	public static final String LOCK_FAILED = "Failed to obtain the lock.";

	/**
	 * Total number of changeset loaded.
	 */
	private int totalCount;

	/**
	 * Number of changesets skipped.
	 */
	private int skipCount;

	/**
	 * Number of changsets executed successfully.
	 */
	private int executedCount;

	/**
	 * Changeset id which resulted in error, if any.
	 */
	private String erroredChangesetId;

	/**
	 * Error message which resulted in execution error, if any.
	 */
	private String errorMessage;
	
	/**
	 * Exit code to be used.
	 */
	private int exitCode = 0;

	/**
	 * Gets the total number of changeset loaded.
	 *
	 * @return the total number of changeset loaded
	 */
	public int getTotalCount()
	{
		return totalCount;
	}

	/**
	 * Sets the total number of changeset loaded.
	 *
	 * @param totalCount the new total number of changeset loaded
	 */
	public void setTotalCount(int totalCount)
	{
		this.totalCount = totalCount;
	}

	/**
	 * Gets the number of changesets skipped.
	 *
	 * @return the number of changesets skipped
	 */
	public int getSkipCount()
	{
		return skipCount;
	}

	/**
	 * Called when a changeset is being skipped.
	 * @param changesetId
	 */
	public void skippingChangeset(String changesetId)
	{
		this.skipCount++;
	}

	/**
	 * Gets the number of changsets executed successfully.
	 *
	 * @return the number of changsets executed successfully
	 */
	public int getExecutedCount()
	{
		return executedCount;
	}

	/**
	 * Called when a changeset is executed successfully.
	 * @param changesetId
	 */
	public void executedChangeset(String changesetId)
	{
		this.executedCount++;
	}

	/**
	 * Gets the error message which resulted in execution error, if any.
	 *
	 * @return the error message which resulted in execution error, if any
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Sets the error message which resulted in execution error, if any.
	 *
	 * @param errorMessage the new error message which resulted in execution error, if any
	 */
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Gets the exit code to be used.
	 *
	 * @return the exit code to be used
	 */
	public int getExitCode()
	{
		return exitCode;
	}

	/**
	 * Sets the exit code to be used.
	 *
	 * @param exitCode the new exit code to be used
	 */
	public void setExitCode(int exitCode)
	{
		this.exitCode = exitCode;
	}

	/**
	 * Gets the changeset id which resulted in error, if any.
	 *
	 * @return the changeset id which resulted in error, if any
	 */
	public String getErroredChangesetId()
	{
		return erroredChangesetId;
	}

	/**
	 * Called on error.
	 * 
	 * @param changesetId
	 * @param errorMssg
	 */
	public void erroredChangeset(String changesetId, String errorMssg)
	{
		this.erroredChangesetId = changesetId;
		this.errorMessage = errorMssg;
	}
}
