package exceptions;

public class WrongFileException extends Exception {

	public WrongFileException() {
		super();
	}

	public WrongFileException(String msg) {
		super(msg);
	}
}
