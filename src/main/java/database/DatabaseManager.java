package database;

import exceptions.AccessDeniedException;
import message.Default;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {

	private static DatabaseManager instance;

	private Connection connection;

	private CallableStatement studentList;
	private CallableStatement resultList;
	private CallableStatement studentResultList;
	private CallableStatement groupList;

	private DatabaseManager() {}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			synchronized (DatabaseManager.class) {
				if (instance == null) {
					instance = new DatabaseManager();
				}
			}
		}

		return instance;
	}

	public void init(String address, String dbName, String user, String password) throws SQLException {
		connection = DriverManager.getConnection("jdbc:mysql://" + address + "/" + dbName + "?" +
				"user=" + user + "&password=" + password + "&noAccessToProcedureBodies=true");

		studentList = connection.prepareCall("{call student_list(?, ?)}");
		resultList = connection.prepareCall("{call result_list(?, ?)}");
		studentResultList = connection.prepareCall("{call student_result_list(?, ?)}");
		groupList = connection.prepareCall("{call group_list(?)}");
	}

	public String getStudentList(int groupID, String lecturerLogin) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			try {
				studentList.setInt(1, groupID);
				studentList.setString(2, lecturerLogin);

				ResultSet rs = studentList.executeQuery();

				if (rs == null)
					throw new NullPointerException("Result set is null");

				if (hasColumn(rs, "Access denied")) throw new AccessDeniedException("Access denied");

				ArrayList<String> parts = new ArrayList<>();

				while (rs.next()) {
					parts.add(Default.builder
							.put("login", rs.getString("login"))
							.put("name_str", rs.getString("name_str"))
							.put("last_name", rs.getString("last_name"))
							.get()
					);
				}

				for (int i = 0; i < parts.size(); i++)
					Default.builder.put("part" + i, parts.get(i));

				return Default.builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public String getResultList(int groupID, String lecturerLogin) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			try {
				resultList.setInt(1, groupID);
				resultList.setString(2, lecturerLogin);

				ResultSet rs = resultList.executeQuery();

				if (rs == null)
					throw new NullPointerException("Result set is null");

				if (hasColumn(rs, "Access denied")) throw new AccessDeniedException("Access denied");

				ArrayList<String> parts = new ArrayList<>();

				while (rs.next()) {
					parts.add(Default.builder
							.put("login", rs.getString("login"))
							.put("name_str", rs.getString("name_str"))
							.put("last_name", rs.getString("last_name"))
							.put("when_given", rs.getString("when_given"))
							.put("res_value", rs.getInt("res_value"))
							.get()
					);
				}

				return Default.builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public String getStudentResultList(int groupID, String studentLogin) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			try {
				studentResultList.setInt(1, groupID);
				studentResultList.setString(2, studentLogin);

				ResultSet rs = studentResultList.executeQuery();

				if (rs == null)
					throw new NullPointerException("Result set is null");

				if (hasColumn(rs, "Access denied")) throw new AccessDeniedException("Access denied");

				ArrayList<String> parts = new ArrayList<>();

				while (rs.next()) {
					parts.add(Default.builder
							.put("when_given", rs.getString("when_given"))
							.put("res_value", rs.getDouble("res_value"))
							.get()
					);
				}

				return Default.builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public String getGroupList(String userLogin, boolean isLecturer) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			try {
				groupList.setString(1, userLogin);

				ResultSet rs = groupList.executeQuery();

				if (rs == null)
					throw new NullPointerException("Result set is null");

				if (hasColumn(rs, "Access denied")) throw new AccessDeniedException("Access denied");

				ArrayList<String> parts = new ArrayList<>();

				if (isLecturer) {
					while (rs.next()) {
						parts.add(Default.builder
								.put("ID", rs.getInt("ID"))
								.put("group_name", rs.getString("group_name"))
								.put("week_patt", rs.getString("week_patt"))
								.put("week_day", rs.getInt("week_day"))
								.get()
						);
					}
				} else {
					while (rs.next()) {
						parts.add(Default.builder
								.put("ID", rs.getInt("ID"))
								.put("group_name", rs.getString("group_name"))
								.put("last_name", rs.getString("last_name"))
								.put("degree", rs.getString("degree"))
								.put("week_patt", rs.getString("week_patt"))
								.put("week_day", rs.getInt("week_day"))
								.get()
						);
					}
				}

				return Default.builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public void updateRows() {
		// TODO: Implement updateRows
	}

	public void insertResultRows() {
		// TODO: Implement insertResultRows
	}

	public void deleteResultRow() {
		// TODO: Implement deleteResultRow
	}

	private boolean hasColumn(ResultSet rs, String name) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();

		for (int i = 1; i <= columns; i++) {
			if (name.equals(meta.getColumnName(i))) return true;
		}

		return false;
	}
}
