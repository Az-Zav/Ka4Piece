package com.ka4piece.repository;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfig {
    private final String jdbcurl, username, password;
    
    //Reads from a properties file to get the database connection details
    public DbConfig(String propertiesFilePath) throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(propertiesFilePath)) {
            props.load(input); }
            
            this.jdbcurl = props.getProperty("jdbcUrl");
            this.username = props.getProperty("dbUser");
            this.password = props.getProperty("dbPassword");
    }

    public String getJdbcurl() {return jdbcurl;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}
}
