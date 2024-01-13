package de.shurablack.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * <p>
 * The ConnectionPool class is a pool of Connection objects that can be used to connect to a database.
 * <br><br>
 * It provides a way to manage a group of connections to a database and ensure that connections are always
 * available for use.
 * <br><br>
 * This is especially useful when there are multiple threads
 * or processes that need to access the database simultaneously.
 * </p>
 *
 * @version core-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class ConnectionPool {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(ConnectionPool.class);

    /** URL of the database to connect to */
    private final String databaseUrl;

    /** Username to use when connecting to the database */
    private final String username;

    /** Password to use when connecting to the database */
    private final String password;

    /** Maximum number of connections that can be stored in the pool */
    private final int maxPoolSize;

    /** Number of connections currently in the pool */
    private int connNum = 0;

    /** SQL query used to verify that a Connection object is still valid */
    private static final String SQL_VERIFYCONN = "SELECT 1";

    /** Stack of Connection objects that are currently not in use and available for use */
    private final Deque<Connection> freePool = new LinkedList<>();

    /** Set of Connection objects that are currently in use and not available for use */
    private final Set<Connection> occupiedPool = new HashSet<>();

    /** Driver name for MySQL Database*/
    public static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";

    public static void registerDriver(final String driver) {
        try {
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            final String msg = "Couldnt register sql driver";
            LOGGER.error(msg, e);
        }
    }

    /**
     * This constructs a new ConnectionPool object
     * @param databaseUrl the provided URL
     * @param username the provided username
     * @param password the provided password
     * @param maxSize the maximum size of the pool
     */
    public ConnectionPool(final String databaseUrl, final String username,
                          final String password, final int maxSize) {
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxSize;
        if (maxSize < 1) {
            LOGGER.error("Database Poolsize cant be lower than 1", new IllegalArgumentException());
            System.exit(1);
        }
    }

    /**
     * Returns a {@link Connection} object from the pool. If the pool is full, it logs an error and returns null.
     * <br><br>
     * If there are available connections in the pool, it returns one of those.
     * <br><br>
     * Otherwise, it creates a new connection, adds it to the occupiedPool set, and returns it
     * @return a connection if possible or null
     */
    public synchronized Connection getConnection() {
        Connection conn;

        if (isFull()) {
            LOGGER.error("The connection pool is full", new SQLException());
        }

        conn = getConnectionFromPool();
        if (conn == null) {
            conn = createNewConnectionForPool();
        }

        conn = makeAvailable(conn);
        return conn;
    }

    /**
     * returns a {@link Connection} object to the pool. If conn is null, it throws a {@link NullPointerException}.
     * <br><br>
     * If the conn object is not in the occupiedPool set, it logs an error.
     * Otherwise, it removes conn from the occupiedPool set and adds it to the freePool stack.
     * @param conn the {@link Connection} which will be returned
     */
    public synchronized void returnConnection(final Connection conn) {
        if (!occupiedPool.remove(conn)) {
            LOGGER.error("The connection is returned already or it isn't for this pool", new SQLException());
        }
        freePool.push(conn);
    }

    /**
     * Check if the pool is full
     * @return true if the pool is empty and the number of connections is equal to the maximum
     */
    private synchronized boolean isFull() {
        return ((freePool.isEmpty()) && (connNum >= maxPoolSize));
    }

    /**
     * Creates a new Connection object and adds it to the occupiedPool set
     * @return te new Connection
     */
    private Connection createNewConnectionForPool() {
        final Connection conn = createNewConnection();
        connNum++;
        occupiedPool.add(conn);
        return conn;
    }

    /**
     * Creates and returns a new Connection object, or logs an error and returns
     * null if the connection could not be established.
     * @return the new Connection
     */
    private Connection createNewConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(databaseUrl, username, password);
        } catch (SQLException e) {
            final String msg = String.format("Couldnt establish connection <%s>", databaseUrl);
            LOGGER.error(msg);
        }
        return conn;
    }

    /**
     * Returns a Connection object from the freePool stack, or null if the stack is empty
     * @return a Connection or null
     */
    private Connection getConnectionFromPool() {
        Connection conn = null;
        if (!freePool.isEmpty()) {
            conn = freePool.pop();
            occupiedPool.add(conn);
        }
        return conn;
    }

    /**
     * Checks if the conn connection is available and returns it if it is.
     * Otherwise, it removes conn from the occupiedPool set, decrements the connNum field, closes the conn connection,
     * creates a new connection, adds it to the occupiedPool set, and increments the connNum field
     * @param conn to be checked
     * @return the checked one or a new Connection
     */
    private Connection makeAvailable(Connection conn) {
        if (isConnectionAvailable(conn)) {
            return conn;
        }

        occupiedPool.remove(conn);
        connNum--;
        try {
            conn.close();
        } catch (SQLException e) {
            final String msg = String.format("Couldnt close connection <%s>", databaseUrl);
            LOGGER.error(msg);
        }

        conn = createNewConnection();
        occupiedPool.add(conn);
        connNum++;
        return conn;
    }

    /**
     * Check if the connection is stil available
     * @param conn the specified Connection which will be checkd
     * @return true if the Connection is stil valid
     */
    private boolean isConnectionAvailable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(SQL_VERIFYCONN);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
