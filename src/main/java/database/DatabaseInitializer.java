package database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // =========================
            // USERS TABLE
            // =========================

            String usersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL CHECK(role IN ('ADMIN', 'STAFF', 'CUSTOMER'))
                    )
                    """;

            stmt.execute(usersTable);


            // =========================
            // CUSTOMERS TABLE
            // =========================

            String customersTable = """
        CREATE TABLE IF NOT EXISTS customers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER UNIQUE,
            name TEXT NOT NULL,
            phone TEXT,
            email TEXT,
            address TEXT,
            FOREIGN KEY(user_id) REFERENCES users(id)
        )
        """;

            stmt.execute(customersTable);


            // =========================
            // STAFF TABLE
            // =========================

            String staffTable = """
                    CREATE TABLE IF NOT EXISTS staff (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        phone TEXT,
                        email TEXT,
                        position TEXT,
                        FOREIGN KEY(user_id) REFERENCES users(id)
                    )
                    """;

            stmt.execute(staffTable);


            // =========================
            // SERVICES TABLE
            // =========================

            String servicesTable = """
                    CREATE TABLE IF NOT EXISTS services (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        description TEXT,
                        price REAL NOT NULL,
                        status TEXT NOT NULL DEFAULT 'ACTIVE'
                    )
                    """;

            stmt.execute(servicesTable);


            // =========================
            // ORDERS TABLE
            // =========================

            String ordersTable = """
        CREATE TABLE IF NOT EXISTS orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            customer_id INTEGER NOT NULL,
            staff_id INTEGER,
            order_date TEXT NOT NULL,
            pickup_date TEXT,
            delivery_date TEXT,
            status TEXT NOT NULL DEFAULT 'PENDING',
            total_cost REAL NOT NULL DEFAULT 0,
            notes TEXT,
            FOREIGN KEY(customer_id) REFERENCES customers(id),
            FOREIGN KEY(staff_id) REFERENCES staff(id)
        )
        """;

            stmt.execute(ordersTable);


            // =========================
            // ORDER ITEMS TABLE
            // =========================

            String orderItemsTable = """
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        service_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        unit_price REAL NOT NULL,
                        subtotal REAL NOT NULL,
                        FOREIGN KEY(order_id) REFERENCES orders(id),
                        FOREIGN KEY(service_id) REFERENCES services(id)
                    )
                    """;

            stmt.execute(orderItemsTable);


            // =========================
            // INVENTORY TABLE
            // =========================

            String inventoryTable = """
                    CREATE TABLE IF NOT EXISTS inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        item_name TEXT NOT NULL,
                        category TEXT,
                        quantity INTEGER NOT NULL DEFAULT 0,
                        unit TEXT,
                        supplier TEXT,
                        cost REAL NOT NULL DEFAULT 0
                    )
                    """;

            stmt.execute(inventoryTable);


            // =========================
            // PAYMENTS TABLE
            // =========================

            String paymentsTable = """
                    CREATE TABLE IF NOT EXISTS payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        payment_date TEXT NOT NULL,
                        payment_method TEXT,
                        status TEXT NOT NULL DEFAULT 'PAID',
                        FOREIGN KEY(order_id) REFERENCES orders(id)
                    )
                    """;

            stmt.execute(paymentsTable);


            System.out.println("All database tables created successfully.");


            var rs = stmt.executeQuery(
                    "PRAGMA table_info(customers)"
            );

            while (rs.next()) {
                System.out.println(
                        rs.getString("name")
                                + " | notnull="
                                + rs.getInt("notnull")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}