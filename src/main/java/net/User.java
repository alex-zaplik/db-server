package net;

import database.DatabaseManager;
import exceptions.AccessDeniedException;
import message.builder.IMessageBuilder;
import message.builder.JSONMessageBuilder;
import message.parser.IMessageParser;
import message.parser.JSONMessageParser;
import structures.ResultInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class User extends Thread {

	// TODO: Protocol for backup/restore

	private String login;
	private UserType type;

	private PrintWriter out;
	private BufferedReader in;

	private IMessageParser parser = new JSONMessageParser();
	private IMessageBuilder builder = new JSONMessageBuilder();

	User(String login, UserType type, BufferedReader in, PrintWriter out) {
		this.login = login;
		this.type = type;
		this.in = in;
		this.out = out;

		out.println(builder
				.put("msg", "Logged in")
				.get()
		);

		System.out.println("User " + login + " connected");
	}

	private void listOfStudents(Map<String, Object> response) {
		int groupID = (int) response.get("group");

		try {
			if (type != UserType.LECTURER && type != UserType.ADMIN)
				throw new AccessDeniedException();

			String result = DatabaseManager.getInstance().getStudentList(groupID, login);
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", e.getMessage()).get());
		}
	}

	private void listOfResults(Map<String, Object> response) {
		int groupID = (int) response.get("group");

		try {
			if (!(type == UserType.LECTURER || type == UserType.ADMIN))
				throw new AccessDeniedException();

			String result;

			if (response.containsKey("login")) {
				result = DatabaseManager.getInstance().getResultList(groupID, login, (String) response.get("login"), true, type == UserType.LECTURER || type == UserType.ADMIN);
			} else if (response.containsKey("last_name")) {
				result = DatabaseManager.getInstance().getResultList(groupID, login, (String) response.get("last_name"), false, type == UserType.LECTURER || type == UserType.ADMIN);
			} else {
				result = DatabaseManager.getInstance().getResultList(groupID, login);
			}
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", e.getMessage()).get());
		}
	}

	private void listOfStudentResults(Map<String, Object> response) {
		int groupID = (int) response.get("group");

		try {
			if (!(type == UserType.STUDENT || type == UserType.ADMIN))
				throw new AccessDeniedException();

			String result = DatabaseManager.getInstance().getStudentResultList(groupID, login);
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", e.getMessage()).get());
		}
	}

	private void listOfSubjects() {
		try {
			String result = DatabaseManager.getInstance().getGroupList(login, (type == UserType.LECTURER));
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", e.getMessage()).get());
		}
	}

	private void newResults(Map<String, Object> response) {
		try {
			int size = (int) response.get("size");
			boolean isActivity = (boolean) response.get("activity");
			boolean doAddition = (boolean) response.get("add");
			ResultInfo[] results = new ResultInfo[size];

			for (int i = 0; i < size; i++) {
				String part = (String) response.get("part" + i);
				Map<String, Object> partMap = parser.parse(part);

				Object resVal = partMap.get("value");
				int resultID = (int) partMap.get("ID");
				double value = (resVal instanceof Integer) ? ((int) resVal) : ((double) resVal);
				String login = (String) partMap.get("login");
				int groupID = (int) partMap.get("group");
				String type = (String) partMap.get("type");
				String date = (String) partMap.get("date");

				results[i] = new ResultInfo(resultID, login, groupID, type, date, value);
			}

			boolean isLecturer = type == UserType.LECTURER || type == UserType.ADMIN;

			if (isActivity)
				DatabaseManager.getInstance().updateResultRows(results, doAddition, isLecturer);
			else
				DatabaseManager.getInstance().insertResultRows(results, isLecturer);

			out.println(builder.put("msg", "Rows added/updated successfully").get());
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", e.getMessage()).get());
		}
	}

	private void deleteResults(Map<String, Object> response) {
		try {
			int size = (int) response.get("size");
			ResultInfo[] results = new ResultInfo[size];

			for (int i = 0; i < size; i++) {
				String part = (String) response.get("part" + i);
				Map<String, Object> partMap = parser.parse(part);

				int resultID = (int) partMap.get("ID");
				results[i] = new ResultInfo(resultID, null, 0, null, null, 0);
			}

			DatabaseManager.getInstance().deleteResultRow(results, type == UserType.LECTURER || type == UserType.ADMIN);

			out.println(builder.put("msg", "Rows deleted successfully").get());
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", e.getMessage()).get());
		}
	}

	private void closeOut() {
		out.close();
	}

	private void closeIn() throws IOException {
		in.close();
	}

	@Override
	public void run() {
		super.run();

		try {
			String input;
			while ((input = in.readLine()) != null) {
				Map<String, Object> response = parser.parse(input);

				if (response.containsKey("action")) {
					int action = (int) response.get("action");

					switch (action) {
						case 0:
							listOfStudents(response);
							break;
						case 1:
							listOfResults(response);
							break;
						case 2:
							listOfStudentResults(response);
							break;
						case 4:
							listOfSubjects();
							break;
						case 5:
							newResults(response);
							break;
						case 6:
							deleteResults(response);
							break;
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Socket closed");

			try {
				closeIn();
			} catch (IOException e1) {
				System.err.println("Unable to close out for " + login);
			}
			closeOut();
		} finally {
			System.err.println("User disconnected");

			try {
				closeIn();
			} catch (IOException e1) {
				System.err.println("Unable to close out for " + login);
			}
			closeOut();
		}
	}
}
