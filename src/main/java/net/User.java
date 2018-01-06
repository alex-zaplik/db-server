package net;

import database.DatabaseManager;
import exceptions.AccessDeniedException;
import message.Default;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class User extends Thread {

	private String login;
	private UserType type;

	private PrintWriter out;
	private BufferedReader in;

	private String accessDenied;

	User(String login, UserType type, BufferedReader in, PrintWriter out) {
		this.login = login;
		this.type = type;
		this.in = in;
		this.out = out;

		accessDenied = Default.builder.put("error", "Access denied").get();
	}

	private void listOfStudents(Map<String, Object> response) {
		int groupID = (int) response.get("group");

		try {
			if (type != UserType.LECTURER && type != UserType.ADMIN)
				throw new AccessDeniedException();

			String result = DatabaseManager.getInstance().getStudentList(groupID, login);
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(accessDenied);
		}
	}

	private void listOfResults(Map<String, Object> response) {
		int groupID = (int) response.get("group");

		try {
			if (type != UserType.LECTURER && type != UserType.ADMIN)
				throw new AccessDeniedException();

			String result = DatabaseManager.getInstance().getResultList(groupID, login);
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(accessDenied);
		}
	}

	private void listOfStudentResults(Map<String, Object> response) {
		int groupID = (int) response.get("group");

		try {
			if (type != UserType.STUDENT && type != UserType.ADMIN)
				throw new AccessDeniedException();

			String result = DatabaseManager.getInstance().getStudentResultList(groupID, login);
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(accessDenied);
		}
	}

	private void listOfSubjects() {
		try {
			String result = DatabaseManager.getInstance().getGroupList(login, (type == UserType.LECTURER));
			out.println(result);
		} catch (AccessDeniedException e) {
			out.println(accessDenied);
		}
	}

	private void newResults(Map<String, Object> response) {
		// TODO: Protocol for newResults
	}

	private void deleteRows(Map<String, Object> response) {
		// TODO: Protocol for deleting rows
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
				Map<String, Object> response = Default.parser.parse(input);

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
							deleteRows(response);
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
		}
	}
}
