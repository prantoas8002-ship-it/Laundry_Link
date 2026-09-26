package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class DBConnection {

    private static final String URL = "jdbc:sqlite:laundrylink.db";

    public static Connection getConnection() throws SQLException {

        Connection conn = DriverManager.getConnection(URL);

        System.out.println(
                "DATABASE PATH: " +
                        new File("laundrylink.db").getAbsolutePath()
        );

        return conn;
    }
}