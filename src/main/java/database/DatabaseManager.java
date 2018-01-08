package database;

import exceptions.AccessDeniedException;
import exceptions.WrongFileException;
import message.builder.IMessageBuilder;
import message.builder.JSONMessageBuilder;
import net.UserType;
import structures.Password;
import structures.ResultInfo;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseManager {

	private static DatabaseManager instance;

	private Connection connection;

	private CallableStatement studentList;
	private CallableStatement resultList;
	private CallableStatement studentResultList;
	private CallableStatement groupList;
	private CallableStatement resultListWithParam;
	private CallableStatement resultUpdate;
	private CallableStatement passwordStatement;

	private Pattern loginPattern = Pattern.compile("[a-zA-Z0-9.]{1,20}");
	private Pattern typePattern = Pattern.compile("[a-zA-Z_]+");
	private Pattern datePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

	private IMessageBuilder builder = new JSONMessageBuilder();

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
		resultListWithParam = connection.prepareCall("{call result_list_param(?, ?, ?, ?)}");
		resultUpdate = connection.prepareCall("{call activity_update(?, ?, ?, ?, ?)}");
		passwordStatement = connection.prepareCall("SELECT pass, salt FROM Users WHERE login = ?");
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
					parts.add(builder
							.put("login", rs.getString("login"))
							.put("name_str", rs.getString("name_str"))
							.put("last_name", rs.getString("last_name"))
							.get()
					);
				}

				builder.put("size", parts.size());

				for (int i = 0; i < parts.size(); i++)
					builder.put("part" + i, parts.get(i));

				return builder.get();

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
					parts.add(builder
							.put("ID", rs.getInt("ID"))
							.put("login", rs.getString("login"))
							.put("name_str", rs.getString("name_str"))
							.put("last_name", rs.getString("last_name"))
							.put("when_given", rs.getString("when_given"))
							.put("res_value", rs.getInt("res_value"))
							.put("res_type", rs.getString("res_type"))
							.get()
					);
				}

				builder.put("size", parts.size());

				for (int i = 0; i < parts.size(); i++)
					builder.put("part" + i, parts.get(i));

				return builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public String getResultList(int groupID, String lecturerLogin, String input, boolean isLogin, boolean isLecturer) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			try {
				Matcher match = loginPattern.matcher(input);

				if (!(match.matches() && isLecturer))
					throw new AccessDeniedException("Access denied");

				resultListWithParam.setInt(1, groupID);
				resultListWithParam.setString(2, lecturerLogin);
				resultListWithParam.setString(3, (isLogin) ? input : "");
				resultListWithParam.setString(4, (isLogin) ? "" : input);
				ResultSet rs = resultListWithParam.executeQuery();

				if (rs == null)
					throw new NullPointerException("Result set is null");

				if (hasColumn(rs, "Access denied"))
					throw new AccessDeniedException("Access denied");

				ArrayList<String> parts = new ArrayList<>();

				while (rs.next()) {
					parts.add(builder
							.put("ID", rs.getInt("ID"))
							.put("login", rs.getString("login"))
							.put("name_str", rs.getString("name_str"))
							.put("last_name", rs.getString("last_name"))
							.put("when_given", rs.getString("when_given"))
							.put("res_value", rs.getDouble("res_value"))
							.put("res_type", rs.getString("res_type"))
							.get()
					);
				}

				builder.put("size", parts.size());

				for (int i = 0; i < parts.size(); i++)
					builder.put("part" + i, parts.get(i));

				return builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new AccessDeniedException("Unable to retrieve data");
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
					String msg = builder
							.put("when_given", rs.getString("when_given"))
							.put("res_value", rs.getDouble("res_value"))
							.put("res_type", rs.getString("res_type"))
							.get();
					parts.add(
							msg
					);
				}

				builder.put("size", parts.size());

				for (int i = 0; i < parts.size(); i++)
					builder.put("part" + i, parts.get(i));

				return builder.get();

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

				while (rs.next()) {
					if (isLecturer) {
						parts.add(builder
								.put("ID", rs.getInt("ID"))
								.put("group_name", rs.getString("group_name"))
								.put("start_time", rs.getString("start_time"))
								.put("week_patt", rs.getString("week_patt"))
								.put("week_day", rs.getInt("week_day"))
								.get()
						);
					} else {
						parts.add(builder
								.put("ID", rs.getInt("ID"))
								.put("group_name", rs.getString("group_name"))
								.put("last_name", rs.getString("last_name"))
								.put("degree", rs.getString("degree"))
								.put("start_time", rs.getString("start_time"))
								.put("week_patt", rs.getString("week_patt"))
								.put("week_day", rs.getInt("week_day"))
								.get()
						);
					}
				}

				builder.put("size", parts.size());

				for (int i = 0; i < parts.size(); i++)
					builder.put("part" + i, parts.get(i));

				return builder.get();

			} catch (SQLException e) {
				System.err.println("SQL Exception in getStudentList");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public void updateResultRows(ResultInfo[] results, boolean doAddition, boolean isLecturer) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			if (!isLecturer)
				throw new AccessDeniedException("Access denied");

			if (results.length == 0)
				return;

			String date = results[0].getDate();
			if (!checkDateFormat(date)) {
				throw new AccessDeniedException("Invalid date");
			}

			try {
				connection.setAutoCommit(false);

				for (ResultInfo res : results) {
					resultUpdate.setString(1, res.getLogin());
					resultUpdate.setInt(2, res.getGroupID());
					resultUpdate.setString(3, date);
					resultUpdate.setDouble(4, res.getValue());
					resultUpdate.setBoolean(5, doAddition);

					resultUpdate.executeUpdate();
				}

				connection.commit();

			} catch (SQLException e) {

				try {
					connection.rollback();
				} catch (SQLException ignored) {
				}

				System.err.println("SQL Exception in updateResultRows");
				throw new NullPointerException("Unable to retrieve data");
			} finally {
				try {


					connection.setAutoCommit(true);
				} catch (SQLException e) {
					System.err.println("SQL Exception in updateResultRows");
					throw new NullPointerException("Unable to update data");
				}
			}
		}
	}

	public void insertResultRows(ResultInfo[] results, boolean isLecturer) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			if (!isLecturer)
				throw new AccessDeniedException("Access denied");

			if (results.length == 0)
				return;

			String date = results[0].getDate();
			if (!checkDateFormat(date)) {
				throw new AccessDeniedException("Invalid date");
			}

			try {
				connection.setAutoCommit(false);
				PreparedStatement stmt;

				stmt = connection.prepareStatement("INSERT INTO Results VALUES(NULL, ?, ?, ?, ?, ?)");

				for (ResultInfo res : results) {
					Matcher matchLogin = loginPattern.matcher(res.getLogin());
					Matcher matchType = typePattern.matcher(res.getType());
					Matcher matchDate = datePattern.matcher(res.getDate());

					boolean loginMatcher = matchLogin.matches();
					boolean typeMatcher = matchType.matches();
					boolean dateMatcher = matchDate.matches();

					if (!(loginMatcher && typeMatcher && dateMatcher))
						throw new AccessDeniedException("Invalid data pattern");

					stmt.setString(1, res.getLogin());
					stmt.setInt(2, res.getGroupID());
					stmt.setString(3, res.getType());
					stmt.setString(4, date);
					stmt.setDouble(5, res.getValue());

					stmt.executeUpdate();
				}

				connection.commit();

			} catch (SQLException e) {

				try {
					connection.rollback();
				} catch (SQLException ignored) {
				}

				System.err.println("SQL Exception in updateResultRows");
				throw new NullPointerException("Unable to retrieve data");
			} finally {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					System.err.println("SQL Exception in updateResultRows");
					throw new NullPointerException("Unable to update data");
				}
			}
		}
	}

	public void deleteResultRow(ResultInfo[] results, boolean isLecturer) throws NullPointerException, AccessDeniedException {
		synchronized (this) {
			if (!isLecturer)
				throw new AccessDeniedException("Access denied");

			try {
				connection.setAutoCommit(false);
				PreparedStatement stmt;

				stmt = connection.prepareStatement("DELETE FROM Results WHERE ID = ?");

				for (ResultInfo res : results) {
					stmt.setInt(1, res.getResultID());

					stmt.executeUpdate();
				}

				connection.commit();

			} catch (SQLException e) {

				try {
					connection.rollback();
				} catch (SQLException ignored) {}

				System.err.println("SQL Exception in updateResultRows");
				throw new NullPointerException("Unable to retrieve data");
			} finally {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					System.err.println("SQL Exception in updateResultRows");
					throw new NullPointerException("Unable to update data");
				}
			}
		}
	}

	public Password getPassword(String login, UserType type) throws AccessDeniedException {
		synchronized (this) {
			try {
				Matcher match = loginPattern.matcher(login);

				if (!match.matches())
					throw new AccessDeniedException("Access denied");

				int count = 0;
				ResultSet rs;
				if (type == UserType.ADMIN) {
					if (login.equals("admin"))
						count++;
				} else if (type == UserType.LECTURER) {
					rs = passwordStatement.executeQuery("SELECT login FROM Lecturers WHERE login = \'" + login + "\'");

					while (rs.next())
						count++;
				} if (type == UserType.STUDENT) {
					rs = passwordStatement.executeQuery("SELECT login FROM Students WHERE login = \'" + login + "\'");

					while (rs.next())
						count++;
				}

				if (count == 0)
					throw new AccessDeniedException("Access denied");

				rs = passwordStatement.executeQuery("SELECT pass, salt FROM Users WHERE login = \'" + login + "\'");

				if (rs == null)
					throw new NullPointerException("Result set is null");

				if (hasColumn(rs, "Access denied"))
					throw new AccessDeniedException("Access denied");

				if (rs.next())
					return new Password(rs.getString("pass"), rs.getString("salt"));
				else
					throw new AccessDeniedException("Access denied");

			} catch (SQLException e) {
				System.err.println("SQL Exception in updateResultRows");
				throw new NullPointerException("Unable to retrieve data");
			}
		}
	}

	public void doBackup(String file) throws AccessDeniedException, WrongFileException {
		synchronized (this) {
			// TODO: Make a backup
			System.out.println("Performing a backup to " + file);
		}
	}

	public void doRestore(String file) throws AccessDeniedException, WrongFileException {
		synchronized (this) {
			// TODO: Restore from backup
			System.out.println("Performing a backup to " + file);
		}
	}

	private boolean hasColumn(ResultSet rs, String name) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();

		for (int i = 1; i <= columns; i++) {
			if (name.equals(meta.getColumnName(i))) return true;
		}

		return false;
	}

	public boolean checkDateFormat(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);

		try {
			sdf.parse(date);
		} catch (ParseException e) {
			return false;
		}

		return true;
	}
}
