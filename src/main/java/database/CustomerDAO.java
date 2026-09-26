package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDAO {

    public static int getOrCreateCustomer(
            int userId,
            String name,
            String phone
    ) throws Exception {

        try (Connection conn = DBConnection.getConnection()) {

            // Check whether customer already exists

            String findSql =
                    "SELECT id FROM customers WHERE user_id = ?";

            try (PreparedStatement ps =
                         conn.prepareStatement(findSql)) {

                ps.setInt(1, userId);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    int customerId = rs.getInt("id");

                    // Update latest customer information

                    String updateSql = """
                            UPDATE customers
                            SET name = ?, phone = ?
                            WHERE id = ?
                            """;

                    try (PreparedStatement update =
                                 conn.prepareStatement(updateSql)) {

                        update.setString(1, name);
                        update.setString(2, phone);
                        update.setInt(3, customerId);

                        update.executeUpdate();
                    }

                    return customerId;
                }
            }


            // Customer does not exist
            // Create new customer

            String insertSql = """
                    INSERT INTO customers
                    (user_id, name, phone)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 insertSql,
                                 java.sql.Statement.RETURN_GENERATED_KEYS
                         )) {

                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.setString(3, phone);

                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();

                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new Exception("Could not create customer.");
    }
    public static int findOrCreateCustomer(
            String name,
            String phone
    ) throws Exception {

        try (Connection conn = DBConnection.getConnection()) {

            // First try to find customer by phone

            String findSql =
                    "SELECT id FROM customers WHERE phone = ?";

            try (PreparedStatement ps =
                         conn.prepareStatement(findSql)) {

                ps.setString(1, phone);

                ResultSet rs =
                        ps.executeQuery();

                if (rs.next()) {

                    return rs.getInt("id");
                }
            }


            // Customer does not exist

            String insertSql = """
                INSERT INTO customers
                (user_id, name, phone)
                VALUES (NULL, ?, ?)
                """;


            try (PreparedStatement ps =
                         conn.prepareStatement(
                                 insertSql,
                                 java.sql.Statement.RETURN_GENERATED_KEYS
                         )) {

                ps.setString(1, name);

                ps.setString(2, phone);

                ps.executeUpdate();


                ResultSet keys =
                        ps.getGeneratedKeys();


                if (keys.next()) {

                    return keys.getInt(1);
                }
            }
        }


        throw new Exception(
                "Could not create customer."
        );
    }
}