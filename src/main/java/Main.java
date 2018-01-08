
import database.DatabaseManager;
import net.Server;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseManager.getInstance().init("localhost", "uni_results", "server", "server");
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
