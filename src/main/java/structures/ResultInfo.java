package structures;

public class ResultInfo {

	private int resultID;
	private String login;
	private int groupID;
	private String type;
	private String date;
	private double value;

	public ResultInfo(int resultID, String login, int groupID, String type, String date, double value) {
		this.resultID = resultID;
		this.login = login;
		this.groupID = groupID;
		this.type = type;
		this.date = date;
		this.value = value;
	}

	public int getResultID() {
		return resultID;
	}

	public void setResultID(int resultID) {
		this.resultID = resultID;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
