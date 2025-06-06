package se.lu.ics.data;

import java.sql.SQLException;

// Exception class for handling database errors
// dao stands for data access object
// This class is used to handle exceptions that occur when 
// accessing the database, such as connection errors or SQL syntax errors.

public class DaoException extends Exception {
	public DaoException() {
		super();
	}

	public DaoException(String message) {
		super(message);
		printSQLDetails();
	}

	public DaoException(String message, Throwable cause) {
		super(message, cause);
		printSQLDetails();
	}

	public DaoException(Throwable cause) {
		super(cause);
		printSQLDetails();
	}

	// METHOD TO PRINT SQL ERROR DETAILS
	private void printSQLDetails() {
		Throwable cause = this.getCause();
		if (cause instanceof SQLException) {
			SQLException sqlException = (SQLException) cause;
			System.err.println("SQL Error Code: " + sqlException.getErrorCode());
			System.err.println("SQL State: " + sqlException.getSQLState());
			System.err.println("SQL Error Message: " + sqlException.getMessage());
		}
	}
}
