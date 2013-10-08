package test;

@SuppressWarnings("serial")
public class InvalidUserInput extends Exception {
	String mistake;

	// Default constructor - initializes instance variable to unknown
	public InvalidUserInput() {
		super();
		mistake = "unknown";
	}

	// Store error inside this string
	public InvalidUserInput(String err) {
		super(err); // call super class constructor
		mistake = err; // save message
	}

	// public method, callable by exception catcher. It returns the error
	// message
	public String getError() {
		return mistake;
	}
}
