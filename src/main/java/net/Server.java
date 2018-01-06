package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server {

	private ServerSocket sSocket;
	private volatile boolean running;

	private List<User> users;

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
		String login = null;
		UserType type = null;

		// TODO: Logging in

		users.add(new User(login, type, in, out));
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
}
