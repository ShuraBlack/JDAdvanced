package de.shurablack.sql;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The SQLRequest class is a utility class for executing SQL queries and commands on a database.
 * <br><br>
 * It relies on the Apache Commons DbUtils library for handling database connections and executing SQL statements.
 * <br><br>
 * The class is instantiated with a ConnectionPool object, which it uses
 * to obtain database connections for executing SQL statements. The user need to request a connection and
 * return it as well with the {@link SQLRequest#close()} funtion.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Create a ConnectionPool object
 * ConnectionPool pool = ConnectionPool.create("jdbc:mysql://localhost:3306/mydatabase", "username", "password");
 *
 * List<User> users = new ArrayList<>();
 * String sql = "SELECT * FROM users";
 *
 * SQLRequest
 *     // Create a SQLRequest object
 *     .create();
 *     // Obtain a database connection
 *     .connect();
 *     // Execute a SELECT query and store the results in a List
 *     .result(sql, User.class, users);
 *     // Close the database connection
 *     .close();
 * }</pre>
 *
 * @see FluentSqlBuilder
 * @version sql-1.0.0
 * @date 12.07.2022
 * @author ShuraBlack
 */
public class SQLRequest {

    /** Class Logger */
    private static final Logger LOGGER = LogManager.getLogger(SQLRequest.class);

    /** Database connection pool */
    private static ConnectionPool POOL;

    /** Requested Connection */
    private Connection conn;

    private SQLRequest() { }

    /**
     * Initialize the ConnectionPool for future requests
     * @param pool the Connection Pool (JDAUtil)
     */
    public static void init(final ConnectionPool pool) {
        POOL = pool;
    }

    /**
     * A static factory method for creating SQLRequest objects
     * @return the SQLRequest for chaining
     */
    public static SQLRequest create() {
        return new SQLRequest();
    }

    /**
     * Obtains a database connection from the ConnectionPool and stores it for use by subsequent SQL statements.
     * <br><br>
     * This method should be called before calling any of the other methods of the class
     * @return the SQLRequest for chaining
     */
    public SQLRequest connect() {
        if (this.conn != null) {
            LOGGER.error("SQLRequest tried to get another connection. Do not call SQLRequest.connect() twice");
            return this;
        }
        this.conn = POOL.getConnection();
        return this;
    }

    /**
     * Closes the database connection.
     * <br><br>
     * This method should be called after you are finished using the SQLRequest object
     * to ensure that the database connection is properly cleaned up and returned to the ConnectionPool
     */
    public void close() {
        POOL.returnConnection(this.conn);
    }

    /**
     * A static convenience method for executing a SQL query that returns a result set and storing the results in a List.
     * <br><br>
     * This method creates a new SQLRequest object, obtains a database connection, executes the query,
     * stores the results in a List, and closes the database connection
     * @param sql the SQL statement which will be executed
     * @param cls the class type of the returning object
     * @param <T> the return class type
     * @return a {@link ArrayList} with the results
     */
    public static <T> List<T> runList(final String sql, final Class<T> cls) {
        SQLRequest request = new SQLRequest();
        request.connect();
        List<T> retval = new ArrayList<>();
        request.result(sql, cls, retval);
        request.close();
        return retval;
    }

    /**
     * A static convenience method for executing a SQL query that returns a result set and storing the results in a {@link Result}.
     * <br><br>
     * This method creates a new SQLRequest object, obtains a database connection, executes the query,
     * stores the results in a List, and closes the database connection
     * @param sql the SQL statement which will be executed
     * @param cls the class type of the returning object
     * @param <T> the return class type
     * @return the {@link Result} with possible value
     */
    public static <T> Result<T> runSingle(final String sql, final Class<T> cls) {
        SQLRequest request = new SQLRequest();
        request.connect();
        List<T> retval = new ArrayList<>();
        Result<T> single = new Result<>();
        request.result(sql, cls, retval);
        request.close();
        if (!retval.isEmpty()) {
            single.value = retval.get(0);
        }
        return single;
    }

    /**
     * A static convenience method for executing a SQL query that returns a result set and storing the results in a {@link Result}.
     * <br><br>
     * This methods is implemented for scalar queries only.
     * <br><br>
     * This method creates a new SQLRequest object, obtains a database connection, executes the query,
     * stores the results, and closes the database connection
     * @param sql the SQL statement which will be executed
     * @param cls the class type of the returning object
     * @param <T> the return class type
     * @return the {@link Result} with possible value
     */
    public static <T> Result<T> runScalar(final String sql, final Class<T> cls) {
        SQLRequest request = new SQLRequest();
        request.connect();
        Result<T> retval = new Result<>();
        request.result(sql, cls, retval);
        request.close();
        return retval;
    }

    /**
     * A static convenience method for executing a SQL query that doesnt need to return a value.
     * <br><br>
     * This method creates a new SQLRequest object, obtains a database connection, executes the query
     * and closes the database connection
     * @param sql the SQL statement which will be executed
     */
    public static void run(final String sql) {
        SQLRequest request = new SQLRequest();
        request.connect();
        request.execute(sql);
        request.close();
    }

    /**
     * Executes a SQL query that returns a result set and stores the results in a List
     * @param sql the SQL statement which will be executed
     * @param cls the class type of the returning object
     * @param retval the list which will be filled
     * @param <T> the return class type
     * @return the SQLRequest for chaining (more requests or closing)
     */
    public <T> SQLRequest result(final String sql, final Class<T> cls, final List<T> retval) {
        try {
            final QueryRunner runner = new QueryRunner();
            final BeanListHandler<T> handler = new BeanListHandler<>(cls);

            retval.addAll(runner.query(this.conn,sql,handler));
        } catch (Exception e) {
            LOGGER.error(String.format("An error occurred while executing SQL\nClass: %s\nSQL: <%s>", cls.getSimpleName(), sql),e);
        }
        return this;
    }

    /**
     * Executes a SQL query that returns a single object and stores the result in a {@link Result}
     * @param sql the SQL statement which will be executed
     * @param cls the class type of the returning object
     * @param retval the {@link Result} object with the return value
     * @param <T> the return class type
     * @return the SQLRequest for chaining (more requests or closing)
     */
    public <T> SQLRequest result(final String sql, final Class<T> cls, final Result<T> retval) {
        try {
            final QueryRunner runner = new QueryRunner();
            final ScalarHandler<T> handler = new ScalarHandler<>();

           retval.value = runner.query(this.conn,sql,handler);
        } catch (Exception e) {
            LOGGER.error(String.format("An error occurred while executing SQL\nClass: %s\nSQL: <%s>", cls.getSimpleName(), sql),e);
        } finally {
            POOL.returnConnection(this.conn);
        }
        return this;
    }

    /**
     * Execute a SQL query which doesnt need to return a value
     * @param sql the SQL statement which will be executed
     * @return the SQLRequest for chaining (more requests or closing)
     */
    public SQLRequest execute(final String sql) {
        try (final Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            LOGGER.error(String.format("An error occurred while executing SQL\nSQL: <%s>", sql),e);
        }
        return this;
    }

    /**
     * Nested class for scalar sql results
     * @param <V> the return class type
     */
    public static class Result<V> {

        /** The returning value */
        public V value;

        /**
         * Checks if the value is present
         * @return true if the Result contains a value
         */
        public boolean isPresent() {
            return this.value != null;
        }
    }
}
