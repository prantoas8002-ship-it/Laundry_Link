package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseSeeder {

    public static void seedDatabase() {

        try (Connection conn = DBConnection.getConnection()) {

            insertUser(
                    conn,
                    "admin",
                    "1234",
                    "ADMIN"
            );

            insertUser(
                    conn,
                    "staff",
                    "1234",
                    "STAFF"
            );

            insertUser(
                    conn,
                    "customer",
                    "1234",
                    "CUSTOMER"
            );


            // =========================
            // SERVICES
            // =========================

            insertService(
                    conn,
                    "Wash",
                    "Regular washing service",
                    100
            );

            insertService(
                    conn,
                    "Dry Clean",
                    "Professional dry cleaning",
                    200
            );

            insertService(
                    conn,
                    "Iron",
                    "Ironing service",
                    50
            );

            insertService(
                    conn,
                    "Wash + Iron",
                    "Washing and ironing service",
                    140
            );


            // =========================
            // INVENTORY
            // =========================

            insertInventory(
                    conn,
                    "Detergent",
                    "Cleaning",
                    50,
                    "kg",
                    "Local Supplier",
                    250
            );

            insertInventory(
                    conn,
                    "Fabric Softener",
                    "Cleaning",
                    30,
                    "liter",
                    "Local Supplier",
                    350
            );

            insertInventory(
                    conn,
                    "Plastic Bag",
                    "Packaging",
                    500,
                    "pcs",
                    "Packaging Supplier",
                    5
            );


            System.out.println("Initial database data inserted.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void insertUser(
            Connection conn,
            String username,
            String password,
            String role
    ) throws Exception {

        String checkSql =
                "SELECT id FROM users WHERE username = ?";

        try (PreparedStatement check =
                     conn.prepareStatement(checkSql)) {

            check.setString(1, username);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                return;
            }
        }


        String sql =
                "INSERT INTO users(username, password, role) VALUES(?,?,?)";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            ps.executeUpdate();
        }
    }


    private static void insertService(
            Connection conn,
            String name,
            String description,
            double price
    ) throws Exception {

        String checkSql =
                "SELECT id FROM services WHERE name = ?";

        try (PreparedStatement check =
                     conn.prepareStatement(checkSql)) {

            check.setString(1, name);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                return;
            }
        }


        String sql =
                "INSERT INTO services(name, description, price) VALUES(?,?,?)";

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setDouble(3, price);

            ps.executeUpdate();
        }
    }


    private static void insertInventory(
            Connection conn,
            String itemName,
            String category,
            int quantity,
            String unit,
            String supplier,
            double cost
    ) throws Exception {

        String checkSql =
                "SELECT id FROM inventory WHERE item_name = ?";

        try (PreparedStatement check =
                     conn.prepareStatement(checkSql)) {

            check.setString(1, itemName);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                return;
            }
        }


        String sql = """
                INSERT INTO inventory
                (item_name, category, quantity, unit, supplier, cost)
                VALUES(?,?,?,?,?,?)
                """;

        try (PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, itemName);
            ps.setString(2, category);
            ps.setInt(3, quantity);
            ps.setString(4, unit);
            ps.setString(5, supplier);
            ps.setDouble(6, cost);

            ps.executeUpdate();
        }
    }
}