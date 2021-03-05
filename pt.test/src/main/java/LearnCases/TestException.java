package LearnCases;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public abstract class TestException extends NestedRuntimeException {

	/**
	 * Constructor for TransactionException.
	 * @param msg the detail message
	 */
	public TestException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for TransactionException.
	 * @param msg the detail message
	 * @param cause the root cause from the transaction API in use
	 */
	public TestException(String msg, Throwable cause) {
		super(msg, cause);
	}

}