package LearnCases;


@SuppressWarnings("serial")
public class TestTimedOutException extends TestException {

	/**
	 * Constructor for TransactionTimedOutException.
	 * @param msg the detail message
	 */
	public TestTimedOutException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for TransactionTimedOutException.
	 * @param msg the detail message
	 * @param cause the root cause from the transaction API in use
	 */
	public TestTimedOutException(String msg, Throwable cause) {
		super(msg, cause);
	}

}