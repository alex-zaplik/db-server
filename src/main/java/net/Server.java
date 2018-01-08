package net;

import com.google.common.hash.Hashing;
import database.DatabaseManager;
import exceptions.AccessDeniedException;
import message.builder.IMessageBuilder;
import message.builder.JSONMessageBuilder;
import message.parser.IMessageParser;
import message.parser.JSONMessageParser;
import structures.Password;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server {

	private ServerSocket sSocket;
	private volatile boolean running;

	private List<User> users;

	private IMessageParser parser = new JSONMessageParser();
	private IMessageBuilder builder = new JSONMessageBuilder();

	private Runnable emptySocket = new Runnable() {
		@Override
		public void run() {
			if (sSocket != null) {
				System.out.println("Server is waiting for clients...");

				while (waitForUsers()) {
					try {
						Socket socket = sSocket.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

						new Thread(() -> {
							initConnection(in, out);
						}).start();
					} catch (SocketException e) {
						System.err.println("Socket closed");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	};

	public Server(int port) throws IOException {
		System.out.println("Starting the server at " + port + "...");

		users = new ArrayList<>();

		sSocket = new ServerSocket(port);
		running = true;

		System.out.println("Server started");

		new Thread(emptySocket).start();
	}

	private void initConnection(BufferedReader in, PrintWriter out) {
		try {
			String input = in.readLine();

			if (input != null) {
				Map<String, Object> response = parser.parse(input);

				String login = (String) response.get("login");
				String pass = (String) response.get("pass");
				String typeStr = (String) response.get("type");
				UserType type;

				if (typeStr.equals("admin"))
					type = UserType.ADMIN;
				else if (typeStr.equals("lecturer"))
					type = UserType.LECTURER;
				else
					type = UserType.STUDENT;

				Password password = DatabaseManager.getInstance().getPassword(login, type);

				if (!password.getPass().equals(hashPass(pass, password.getSalt())))
					throw new AccessDeniedException("Access denied");

				User u = new User(login, type, in, out);
				users.add(u);
				u.start();
			}
		} catch (AccessDeniedException e) {
			out.println(builder.put("error", "Invalid password or login").get());
		} catch (IOException e) {
			out.println(builder.put("error", "Server exception"));
			System.err.println("Unable to connect user...");
		}
	}

	public void stopServer() throws IOException {
		System.out.println("Stopping the server...");

		running = false;
		sSocket.close();

		System.out.println("Server stopped");
	}

	private boolean waitForUsers() {
		return running;
	}

	private String hashPass(String pass, String salt) {
		return Hashing.sha256().hashString(pass + salt, StandardCharsets.UTF_8).toString();
	}
}
