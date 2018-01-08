package structures;

public class Password {

	private String pass;
	private String salt;

	public Password(String pass, String salt) {
		this.pass = pass;
		this.salt = salt;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
}
