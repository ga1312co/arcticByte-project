package se.lu.ics.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionHandler {

    private Properties connectionProperties;

    // CONSTRUCTOR TO READ CONNECTION PROPERTIES FROM CONFIG.PROPERTIES
    public ConnectionHandler() throws IOException {
        connectionProperties = new Properties();
        FileInputStream stream = new FileInputStream("src/main/resources/config.properties"); // Open the file
        connectionProperties.load(stream); // Load properties from where the stream is pointing
    }
    // METHOD TO GET CONNECTION TO DATABASE
    public Connection getConnection() throws SQLException {
        String databaseServerName = connectionProperties.getProperty("database.server.name");
        String databaseServerPort = connectionProperties.getProperty("database.server.port");
        String databaseName = connectionProperties.getProperty("database.name");
        String databaseUsername = connectionProperties.getProperty("database.username");
        String databaseUserPassword = connectionProperties.getProperty("database.password");

        String connectionURL = "jdbc:sqlserver://"
                + databaseServerName + ":"
                + databaseServerPort + ";"
                + "database=" + databaseName + ";"
                + "user=" + databaseUsername + ";"
                + "password=" + databaseUserPassword + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;";

        return DriverManager.getConnection(connectionURL);
    }
}
