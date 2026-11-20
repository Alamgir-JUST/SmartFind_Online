package edu.smartfind.db;

import java.sql.*;

public final class Database {
    private Database() {}
    public static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/smartfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        return DriverManager.getConnection(url, System.getenv().getOrDefault("DB_USER", "smartfind"), System.getenv().getOrDefault("DB_PASSWORD", "classroom_dev_only"));
    }
}
