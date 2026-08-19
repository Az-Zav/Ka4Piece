package com.ka4piece.repository;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public abstract class MySqlStore {
    private final String jdbcUrl, dbUser, dbPassword;

    public MySqlStore(String jdbcUrl, String dbUser, String dbPassword) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    //For sql SELECT statements
    protected List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query error: " + e.getMessage(), e);
        }
        return results;
    }

    //For sql INSERT, UPDATE, DELETE statements
    protected int executeUpdate(String sql, Object... params) { //Object... params are all values to be bound to the SQL statement
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepareStatement(conn, sql, params)) {
            return stmt.executeUpdate(); //execute the update and return the number of affected rows
        } catch (SQLException e) {
            throw new RuntimeException("Database update error: " + e.getMessage(), e);
        }
    }

    //----- Helper Methods-----
    private PreparedStatement prepareStatement(Connection conn, String sql, Object... params) throws SQLException { //Binds the parameters to the prepared statement
        PreparedStatement stmt = conn.prepareStatement(sql);
        //
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]); //param[] is 1 row's worth of value binding
        }
        return stmt;
    }

}