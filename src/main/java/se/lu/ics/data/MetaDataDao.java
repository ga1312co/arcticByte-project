package se.lu.ics.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class MetaDataDao {
    private ConnectionHandler connectionHandler;

    public MetaDataDao() throws IOException {
        connectionHandler = new ConnectionHandler(); // Create a new connection handler object
    }

    // RETRIEVE THE NAMES OF ALL TABLES IN THE DATABASE
    public List<String> getColumnNames() throws DaoException {
        List<String> metadataColumnNames = new ArrayList<>();
        String query = "SELECT COLUMN_NAME " +
                       "FROM INFORMATION_SCHEMA.COLUMNS " +
                       "WHERE TABLE_CATALOG = 'ArcticByte'";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                metadataColumnNames.add(columnName);
            }
        } catch (SQLException e) {
            throw new DaoException("Error getting column names", e);
        }

        return metadataColumnNames;
    }

    // RETrIEVE THE NAMES OF ALL PRIMARY KEY CONSTRAINTS IN THE DATABASE
    public List<String> getPKConstraints() throws DaoException {
        List<String> metadataPKConstraints = new ArrayList<>();
        String query = "SELECT CONSTRAINT_NAME " +
                       "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                       "WHERE CONSTRAINT_TYPE = 'PRIMARY KEY' AND TABLE_CATALOG = 'ArcticByte'";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String constraintName = resultSet.getString("CONSTRAINT_NAME");
                metadataPKConstraints.add(constraintName);
            }
        } catch (SQLException e) {
            throw new DaoException("Error getting primary key constraints", e);
        }

        return metadataPKConstraints;
    }

    // RETRIEVE THE NAMES OF ALL CHECK CONSTRAINTS IN THE DATABASE
    public List<String> getCheckConstraints() throws DaoException {
        List<String> metadataCheckConstraints = new ArrayList<>();
        String query = "SELECT CONSTRAINT_NAME " +
                       "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                       "WHERE CONSTRAINT_TYPE = 'CHECK' AND TABLE_CATALOG = 'ArcticByte'";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String constraintName = resultSet.getString("CONSTRAINT_NAME");
                metadataCheckConstraints.add(constraintName);
            }
        } catch (SQLException e) {
            throw new DaoException("Error getting check constraints", e);
        }
        return metadataCheckConstraints;
    }

    // RETRIEVE THE NAMES OF ALL COLUMNS IN YOUR EMPLOYEE TABLE THAT ARE NOT OF TYPE INTEGER
    public List<String> getNonIntegerColumns() throws DaoException {
        List<String> metadataNonIntegerColumns = new ArrayList<>();
        String query = "SELECT COLUMN_NAME " +
                       "FROM INFORMATION_SCHEMA.COLUMNS " +
                       "WHERE TABLE_CATALOG = 'ArcticByte' " +
                       "AND TABLE_NAME = 'Employee' " +
                       "AND DATA_TYPE NOT IN ('int', 'smallint', 'tinyint', 'bigint')";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) { // Loop through the result set
                String columnName = resultSet.getString("COLUMN_NAME");
                metadataNonIntegerColumns.add(columnName);
            }
        } catch (SQLException e) {
            throw new DaoException("Error getting non-integer columns", e);
        }
        return metadataNonIntegerColumns;
    }


    // RETRIEVE THE NAME AND NUMBER OF ROWS OF THE TABLE WITH THE HIGHEST NUMBER OF ROWS
    public List<String> getTableWithMostRows() throws DaoException {
        List<String> metadataTableWithMostRows = new ArrayList<>();
        String query = "SELECT TOP 1 " +
                       "t.name AS TableName, " +
                       "SUM(p.rows) AS RowCounts " +
                       "FROM sys.tables t " +
                       "INNER JOIN sys.partitions p ON t.object_id = p.object_id " +
                       "WHERE p.index_id IN (0, 1) " + // 0: Heap, 1: Clustered. 0 and 1 are the index_id for the heap and clustered index
                       "GROUP BY t.name " +
                       "ORDER BY RowCounts DESC";

        try (Connection connection = connectionHandler.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String tableName = resultSet.getString("TableName");
                String rowCounts = resultSet.getString("RowCounts");
                metadataTableWithMostRows.add("Table Name: " + tableName);
                metadataTableWithMostRows.add("Row Count: " + rowCounts);
            }
        } catch (SQLException e) {
            throw new DaoException("Error getting table with most rows", e);
        }
        return metadataTableWithMostRows;
    }

}
