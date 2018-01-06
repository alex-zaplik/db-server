package exceptions;

public class AccessDeniedException extends Exception {

	public AccessDeniedException() {
		super();
	}

	public AccessDeniedException(String msg) {
		super(msg);
	}
}
