package de.shurablack.sql;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * The FluentSqlBuilder class is a utility for building SQL queries in a fluent and readable manner.
 * It allows you to easily construct complex SQL queries by chaining together method calls
 * that correspond to different clauses in the query.
 * <br><br>
 * The class defines several static constants that can be used to specify
 * common values in SQL queries, such as comparison operators, sorting directions, and the NULL value.
 * <br><br>
 * The class also defines several methods that can be used to build the different parts of an SQL query.
 * <br><br>
 * Example:
 * </p>
 * <pre>{@code
 * // Create a new FluentSqlBuilder instance
 * FluentSqlBuilder builder = FluentSqlBuilder
 *  .create()
 *  .INSERT("users", FluentSqlBuilder.VALUE("John Doe", 25, "john.doe@gmail.com"))
 *  .APPEND("ON DUPLICATE KEY UPDATE name = name");
 *
 * String sql = builder.toString();
 * }</pre>
 *
 * @version sql-1.0.0
 * @date 09.06.2023
 * @author ShuraBlack
 */
public class FluentSqlBuilder implements Serializable {

    private static final long serialVersionUID = 4413271984887210214L;

    /** Variable for equal (example 1 EQUAL 1)*/
    public static final String EQUAL = "=";

    /** Variable for greater (example 2 GREATER 1)*/
    public static final String GREATER = ">";

    /** Variable for less (example 1 LESSER 2) */
    public static final String LESSER = "<";

    /** Variable for equal or greater than (example 1 >= 1)*/
    public static final String GREATER_EQUAL = ">=";

    /** Variable for equal or less than (example  2 <= 2) */
    public static final String LESSER_EQUAL = "<=";

    /** Variable for ascented order */
    public static final String ASC = "ASC";

    /** Variable for decented order */
    public static final String DESC = "DESC";

    /** Variable for NULL object */
    public static final String NULL = "NULL";

    /** Variable for distinct values */
    public static final String DISTINCT = "DISTINCT";

    /** Variable for left joined tables */
    public static final String LEFT_JOIN = "LEFT JOIN";

    /** Variable for right joined tables */
    public static final String RIGHT_JOIN = "RIGHT JOIN";

    /** Variable for inner joined tables */
    public static final String INNER_JOIN = "INNER JOIN";

    /** Variable for cross joined tables */
    public static final String CROSSJOIN = "CROSSJOIN";

    /** The currently building sql string */
    private final StringBuilder sql = new StringBuilder();

    /**
     * Creates a new instance of the FluentSqlBuilder
     * @return the builder for chaining
     */
    public static FluentSqlBuilder create() {
        return new FluentSqlBuilder();
    }

    /**
     * Convenience Method to make multiple changes in one request.
     * <br><b>This is a closed SQL Request.</b>
     * @param table the specified table
     * @param colums the specified colums in the table, which will be updated
     * @param values the data which will be updated (use {@link FluentSqlBuilder#VALUE(String...)} to wrap the data)
     * @return the builder for chaining
     */
    public FluentSqlBuilder UPDATE_CHAIN(final String table, final List<String> colums, List<String> values) {
        sql.append("INSERT INTO ").append(table).append(" (").append(String.join(", ", colums)).append(")")
                .append(" VALUES ").append(String.join(",", values))
                .append(" ON DUPLICATE KEY UPDATE ")
                .append(colums.stream().map(col -> String.format("%s=VALUES(%s)", col, col)).collect(Collectors.joining(",")));
        return this;
    }

    /**
     * Adds a SELECT clause to the query with the specified columns.
     * @param columns the specified columns
     * @return the builder for chaining
     */
    @Starter
    public FluentSqlBuilder SELECT(final String... columns) {
        String cols = String.join(", ", columns);
        sql.append("SELECT ").append(cols).append(" ");
        return this;
    }

    /**
     * Adds a SELECT and DISTINCT clause to the query with the specified columns
     * @param column the specified column
     * @return the builder for chaining
     */
    @Starter
    public FluentSqlBuilder SELECT_DISTINCT(final String column) {
        sql.append("SELECT DESTINCT").append(column).append(" ");
        return this;
    }

    /**
     * Adds an UPDATE clause to the query with the specified tables
     * @param tables the specified tables
     * @return the builder for chaining
     */
    @Starter
    public FluentSqlBuilder UPDATE(final String... tables) {
        String cols = String.join(", ", tables);
        sql.append("UPDATE ").append(cols).append(" ");
        return this;
    }

    /**
     *  Adds a DELETE FROM clause to the query with the specified tables
     * @param tables the specified tables
     * @return the builder for chaining
     */
    @Starter
    public FluentSqlBuilder DELETE(final String... tables) {
        String cols = String.join(", ", tables);
        sql.append("DELETE FROM ").append(cols).append(" ");
        return this;
    }

    /**
     * Adds an INSERT INTO clause to the query with the specified table and values
     * @param table the specified table
     * @param values the specified values
     * @return the builder for chaining
     */
    @Starter
    public FluentSqlBuilder INSERT(final String table, final String... values) {
        sql.append("INSERT INTO ").append(table).append(" ");
        String val = String.join(", ", values);
        sql.append("VALUES ").append(val);
        return this;
    }

    /**
     * Adds a FROM clause to the query with the specified tables
     * @param tables the specified tables
     * @return the builder for chaining
     */
    public FluentSqlBuilder FROM (final String... tables) {
        String cols = String.join(", ", tables);
        sql.append("FROM ").append(cols).append(" ");
        return this;
    }

    /**
     * Adds a CALL clause to the query with the specified function
     * @param function the function which will be called
     * @return the builder for chaining
     */
    public FluentSqlBuilder CALL(final String function) {
        sql.append("CALL").append(function).append(" ");
        return this;
    }

    /**
     * Returns a string representation of the values in parentheses, suitable for use in an INSERT clause
     * @param value which will be used to build the representation
     * @return the SQL subtext
     */
    public static String VALUE(final String... value) {
        return String.format("(%s)", String.join(",", value));
    }

    /**
     * Returns a string representation of the COUNT operation
     * @param column the specified column
     * @param extra the specified operation like {@link FluentSqlBuilder#DISTINCT}
     * @return the SQL subtext
     */
    public static String COUNT(final String column, final String extra) {
        return String.format("COUNT(%s %s)", extra, column);
    }

    /**
     * Returns a string representation of the table and columns in the
     * format table (column1, column2, ...), suitable for use in an INSERT clause.
     * @param table the specified table where the columns are present
     * @param columns the specified columns which should be used
     * @return the SQL subtext
     */
    public static String TABLE_COLUMNS(final String table, final String... columns) {
        return String.format("%s (%s)", table, String.join(", ", columns));
    }

    /**
     * Adds a SET clause to the query with the specified changes
     * @param changes the specified changes
     * @return the builder for chaining
     */
    public FluentSqlBuilder SET(final String... changes) {
        String cols = String.join(", ", changes);
        sql.append("SET ").append(cols).append(" ");
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query with the specified order
     * @param order the specified column
     * @return the builder for chaining
     */
    public FluentSqlBuilder ORDER(final String order) {
        sql.append("ORDER BY ").append(order);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query with the specified order and sort direction (ascending or descending).
     * @param order the specified column
     * @param sort the specified sorting {@link FluentSqlBuilder#ASC} or {@link FluentSqlBuilder#DESC}
     * @return the builder for chaining
     */
    public FluentSqlBuilder ORDER(final String order, final String sort) {
        sql.append("ORDER BY ").append(order).append(" ").append(sort);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query with the specified function and sort direction (ascending or descending).
     * @param function the specified sql function which is defined on the database
     * @param sort the specified sorting {@link FluentSqlBuilder#ASC} or {@link FluentSqlBuilder#DESC}
     * @param functionArgs the arguments which will be passed to the function
     * @return the builder for chaining
     */
    public FluentSqlBuilder ORDER(final String function, final String sort, final String... functionArgs) {
        String args = String.join(", ", functionArgs);
        sql.append("ORDER BY").append(function).append("(").append(args).append(")").append(" ").append(sort);
        return this;
    }

    /**
     * Adds a LIMIT clause to the query with the specified limit value
     * @param limit the specified limit value
     * @return the builder for chaining
     */
    public FluentSqlBuilder LIMIT(final int limit) {
        sql.append("LIMIT ").append(limit);
        return this;
    }

    /**
     * Adds a WHERE clause to the query with the specified conditions
     * @param conditions the specified conditions
     * @return the builder for chaining
     */
    public FluentSqlBuilder WHERE (final String... conditions) {
        String cons = String.join(" ", conditions);
        sql.append("WHERE ").append(cons).append(" ");
        return this;
    }

    /**
     * Returns a string representation of an WHERE condition
     * @param column the specified column
     * @param operator the comparing operator
     * @param value the value which will be used for comparison
     * @return the SQL subtext
     */
    public static String CONDITION(final String column, final String operator, final String value) {
        return String.format("%s%s%s", column, operator, value);
    }

    /**
     * Adds an AND clause to the query
     * @return the builder for chaining
     */
    public FluentSqlBuilder AND() {
        sql.append("AND ");
        return this;
    }

    /**
     * Adds an OR clause to the query
     * @return the builder for chaining
     */
    public FluentSqlBuilder OR() {
        sql.append("OR ");
        return this;
    }

    /**
     * Adds a NOT clause to the query
     * @return the builder for chaining
     */
    public FluentSqlBuilder NOT() {
        sql.append("NOT ");
        return this;
    }

    /**
     * Adds a custom string to the query
     * @param custom the string which will be added
     * @return the builder for chaining
     */
    public FluentSqlBuilder APPEND(final String custom) {
        sql.append(custom).append(" ");
        return this;
    }

    /**
     * Return a string representation of an function with arguments
     * @param function the function which wraps the arguments
     * @param args the arguments of the function
     * @return the SQL subtext
     */
    public static String FUNCTION(final String function, final String... args) {
        return String.format("%s(%s)",function,String.join(",",args));
    }

    /**
     * Return a string representation of two tables joining
     * @param table_1 the first table
     * @param join the types of join
     * @param table_2 the second table
     * @return the SQL subtext
     */
    public static String JOIN(final String table_1, final String join, final String table_2) {
        return String.format("%s %s %s", table_1, join, table_2);
    }

    /**
     * Return a string representation of two tables joining on specified columns
     * @param table_1 the first table
     * @param join the types of join
     * @param table_2 the second table
     * @param column_1 the first column to compare
     * @param operator the comparing operator
     * @param column_2 the second column to compare
     * @return the SQL subtext
     */
    public static String JOIN(final String table_1, final String join, final String table_2
            , final String column_1, final String operator, final String column_2) {
        return String.format("%s %s %s ON %s %s %s", table_1, join, table_2, column_1, operator , column_2);
    }

    /**
     * Returns a string representation of an AND condition with the
     * specified column and operator, in the format 'AND column operator ?'
     * @param column the specified column which will be used to check
     * @param operator the operator which will be used for comparison
     * @return the SQL subtext
     */
    public static String AND (final String column, final String operator) {
        return String.format("AND %s%s?", column, operator);
    }

    /**
     * Returns a string representation of an OR condition with the
     * specified column and operator, in the format 'OR column operator ?'
     * @param column the specified column which will be used to check
     * @param operator the operator which will be used for comparison
     * @return the SQL subtext
     */
    public static String OR (final String column, final String operator) {
        return String.format("OR %s%s?", column, operator);
    }

    /**
     * Returns a string representation of an AND condition with the
     * specified column, operator, and value, in the format 'AND column operator value'
     * @param column the specified column which will be used to check
     * @param operator the operator which will be used for comparison
     * @param value the vaue which will be used to compare
     * @return the SQL subtext
     */
    public static String AND (final String column, final String operator, final String value) {
        return String.format("AND %s%s%s", column, operator,value);
    }

    /**
     * Returns a string representation of an OR condition with the
     * specified column, operator, and value, in the format 'OR column operator value'
     * @param column the specified column which will be used to check
     * @param operator the operator which will be used for comparison
     * @param value the vaue which will be used to compare
     * @return the SQL subtext
     */
    public static String OR (final String column, final String operator, final String value) {
        return String.format("OR %s%s%s", column, operator,value);
    }

    /**
     * Return a String which is valid for SQL
     * @param value the specifid value to wrap
     * @return the SQL subtext
     */
    public static String STR(final String value) {
        return String.format("'%s'", value);
    }

    /**
     * Returns a string representation which sets the specified table with the given name
     * @param table the specified table
     * @param name the specified name
     * @return the SQL subtext
     */
    public static String AS (final String table, final String name) {
        return String.format("%s AS %s", table, name);
    }

    public String asSubQuery() {
        return "(" + sql + ")";
    }

    /**
     * Build the sql and return it as a finished string
     * @return the built sql
     */
    @Override
    public String toString() {
        sql.append(";");
        return sql.toString();
    }
}
