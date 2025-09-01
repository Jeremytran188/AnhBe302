package com.example.worldwise.worldwise;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:sqlite:worldwise.db";

    // Connect to the database
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Create users table if it doesn’t exist
    public static void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "firstname TEXT NOT NULL,"
                + "lastname TEXT NOT NULL,"
                + "email TEXT UNIQUE NOT NULL,"
                + "password TEXT NOT NULL"
                + "salt TEXT NOT NULL)";
    }
}
