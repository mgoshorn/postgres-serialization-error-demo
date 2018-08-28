package com.revature.launcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionUtil {
	public static Connection getConnection() {
		Properties properties = new Properties();
		InputStream is = ColorDao.class.getClassLoader().getResourceAsStream("db.properties");
		try {
			properties.load(is);
			return DriverManager.getConnection(System.getenv(properties.getProperty("URL")),
					System.getenv(properties.getProperty("USER")), System.getenv(properties.getProperty("PASS")));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
