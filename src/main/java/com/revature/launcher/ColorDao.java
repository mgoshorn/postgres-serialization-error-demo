package com.revature.launcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Demonstrates the possibility for concurrent serialiazable transactions to result in a Serialization Error.
 * Due to this, serializable transactions should have exception catching logic which handles retrying or
 * otherwise handling the failure of the serializable transaction.
 * A read-only serializable transaction can prevent this error by being declared readonly and deferrable.
 * 
 * In this example, concurrency is forced and controlled by nesting the operations, allowing for
 * deterministic behavior. In practice, these errors are likely to occur when operations running are called
 * from multiple threads or separate applications.
 * 
 * @author Mitch Goshorn
 *
 */

/*
 *  Order of calls:
 *  
 *  1. Outer Query, but does not commit.
 *  2. Inner query called, does commit.
 *  3. Outer query attempts to commit, but this will raise a SerializationException
 *  4. Outer query is then retried, now without any interaction from the inner query.
 *  5. Outer query resolves correctly.
 */

public class ColorDao {

	
	public void concurrentSerializableWrite() {
		// Initial transaction
		try(Connection conn = ConnectionUtil.getConnection()) {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			PreparedStatement ps = conn.prepareStatement("UPDATE colors SET color = 'black' WHERE color = 'white'");
			ps.executeUpdate();
			
			// Secondary transaction
			try(Connection conn2 = ConnectionUtil.getConnection()) {
				conn2.setAutoCommit(false);
				conn2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				conn2.prepareStatement("UPDATE colors SET color = 'white' WHERE color = 'black'")
					.executeUpdate();
				conn2.commit();
			}
			// This commit will raise a PSQLException as the concurrent transaction havin already
			// committed will cause write skew in this transaction
			conn.commit();
			System.out.println("Complete");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Serialization error occurs when the outer query commits. It should be retried"
					+ " in an attempt to resolve the issue.");
			int retries = 0;
			int maxRetries = 3;
			
			// Transaction is being handled by repeating the query, in practice this should
			// be handled in a DRYer fashion, but suffices for this example.
			do {
				System.out.println("Retrying...");
				retries++;
				if(retries > maxRetries)	
					throw new RuntimeException("Unable to complete serializable transaction");
			} while (!outerQuery());
			System.out.println("Retry successful.");
		}
	}
	
	// Outer query with no inner query, called for repeating the operation on failure
	private boolean outerQuery() {
		try(Connection conn = ConnectionUtil.getConnection()) {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			PreparedStatement ps = conn.prepareStatement("UPDATE colors SET color = 'black' WHERE color = 'white'");
			ps.executeUpdate();
			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Serialization error occurs when the outer query commits. It should be retried"
					+ " in an attempt to resolve the issue.");
			return false;
		}
	}
	
	
}
