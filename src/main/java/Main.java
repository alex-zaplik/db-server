
import database.DatabaseManager;
import net.Server;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        if (args.length != 4)
            System.err.println("Usage: server_username server_password backup_username backup_password");

        try {
            DatabaseManager.getInstance().init("localhost", "uni_results", args[0], args[1], args[2], args[3]);
            new Server(4444);

        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
            System.err.println("SQLState: " + ex.getSQLState());
            System.err.println("VendorError: " + ex.getErrorCode());
        } catch (IOException e) {
            System.err.println("Unable to start the server");
        }
    }
}
