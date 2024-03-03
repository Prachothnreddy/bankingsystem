import java.sql.*;

public class BankingSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/banking";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database");

            // Example: Authenticate user
            boolean isAuthenticated = authenticateUser(conn, "username", "password");
            System.out.println("Authentication status: " + isAuthenticated);

            // Example: Create a new account
            createAccount(conn, 1, "savings");

            // Example: Transfer funds
            transferFunds(conn, 1, 2, 100.00);

            conn.close();
            System.out.println("Disconnected from database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean authenticateUser(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void createAccount(Connection conn, int userId, String accountType) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, account_type) VALUES (?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, accountType);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Account created successfully");
            }
        }
    }

    private static void transferFunds(Connection conn, int fromAccountId, int toAccountId, double amount) throws SQLException {
        String sqlDebit = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String sqlCredit = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement debitStatement = conn.prepareStatement(sqlDebit);
             PreparedStatement creditStatement = conn.prepareStatement(sqlCredit)) {
            conn.setAutoCommit(false);
            debitStatement.setDouble(1, amount);
            debitStatement.setInt(2, fromAccountId);
            debitStatement.executeUpdate();

            creditStatement.setDouble(1, amount);
            creditStatement.setInt(2, toAccountId);
            creditStatement.executeUpdate();

            conn.commit();
            System.out.println("Funds transferred successfully");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    conn.rollback();
                } catch(SQLException excep) {
                    excep.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
