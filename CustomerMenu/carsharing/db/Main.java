package carsharing.db;


import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static final String JDBC_DRIVER = "org.h2.Driver";
    static String DB_URL = "jdbc:h2:./src/carsharing/db/";
    static Connection conn = null;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        String dbFilename = getDbFilename(args);
        DB_URL = DB_URL + dbFilename;
        //dropTableCompany(getStatement());
        //dropTableCustomer(getStatement());
        createTableCompany(getStatement());
        createTableCustomer(getStatement());
        //resetID(getStatement());
        mainMenu();
    }


    private static void mainMenu() throws SQLException, ClassNotFoundException {
        while (true) {
            System.out.println("1. Log in as a manager\n" +
                    "2. Log in as a customer\n" +
                    "3. Create a customer\n" +
                    "0. Exit");
            int option = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            switch (option) {
                case 1:
                    loginAsManager();
                    break;
                case 2:
                    loginAsCustomer();
                    break;
                case 3:
                    createCustomer();
                    break;
                case 0:
                    System.exit(0);
                default:
                    System.out.println("Invalid operation try again!");
                    break;
            }
        }

    }

    private static void loginAsCustomer() throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        String sql = "SELECT * FROM CUSTOMER";
        ResultSet rs = stat.executeQuery(sql);
        if (!rs.next()) {
            System.out.println("The customer list is empty!");
        } else {
            System.out.println("Customer list:");
            do {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                System.out.printf("%s. %s%n", id, name);
            } while (rs.next());
            System.out.println("0. Back");
            int option = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            if (option == 0) {
                mainMenu();
            }
        }
        System.out.println();

    }

    private static void createCustomer() throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        System.out.println("Enter the customer name:");
        String customerName = scanner.nextLine();
        String sql = String.format("\n" +
                "INSERT INTO customer (NAME) VALUES ('%s')", customerName);
        stat.execute(sql);
        stat.close();
        conn.close();
        System.out.println("The customer was added!");
        System.out.println();
    }

    private static void loginAsManager() throws SQLException, ClassNotFoundException {
        System.out.println("1. Company list\n" +
                "2. Create a company\n" +
                "0. Back");
        int option = scanner.nextInt();
        scanner.nextLine();
        System.out.println();

        switch (option) {
            case 1:
                companyListAsManager();

                break;
            case 2:
                createCompany();
                break;
            case 0:
                mainMenu();
                break;
            default:
                System.out.println("Invalid operation !");
        }
        System.out.println();
        loginAsManager();
    }

    private static void companyListAsManager() throws SQLException, ClassNotFoundException {
        Map<String, String> map = new HashMap<>();
        Statement stat = getStatement();
        String sql = "SELECT * FROM COMPANY";
        ResultSet rs = stat.executeQuery(sql);
        if (!rs.next()) {
            System.out.println("The company list is empty!");
        } else {
            System.out.println("Choose a company:");
            do {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                map.put(id, name);
                System.out.printf("%s. %s%n", id, name);
            } while (rs.next());
            stat.close();
            conn.close();
            System.out.println("0. Back");

            int option = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            if (option == 0) {
                loginAsManager();
            } else {
                System.out.println();
                String companyName = map.get(String.valueOf(option));
                System.out.printf("'%s' company:%n", companyName);
            }
        }
    }

    private static void createCompany() throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        System.out.println("Enter the company name:");
        String companyName = scanner.nextLine();
        String sql = String.format("INSERT INTO COMPANY (name) VALUES ('%s')", companyName);
        stat.execute(sql);
        System.out.println("The company was created!");
        stat.close();
        conn.close();

    }


    private static void dropTableCompany(Statement stmt) throws SQLException {
        String sql = "drop table IF EXISTS COMPANY";
        stmt.executeUpdate(sql);
        System.out.println("Dropping table in given database...");
        stmt.close();
        conn.close();
    }
    private static void dropTableCar(Statement stmt) throws SQLException {
        String sql = "drop table IF EXISTS  CAR";
        stmt.executeUpdate(sql);
        System.out.println("Dropping table in given database...");
        stmt.close();
        conn.close();
    }
    private static void dropTableCustomer(Statement stmt) throws SQLException {
        String sql = "drop table IF EXISTS Customer";
        stmt.executeUpdate(sql);
        System.out.println("Dropping table in given database...");
        stmt.close();
        conn.close();
    }

    // create tables
    private static void createTableCustomer(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS CUSTOMER(\n" +
                "ID INT PRIMARY KEY AUTO_INCREMENT,\n" +
                "NAME VARCHAR(255) UNIQUE NOT NULL,\n" +
                "RENTED_CAR_ID INT,\n" +
                "CONSTRAINT fk_customer FOREIGN KEY (RENTED_CAR_ID) \n" +
                "REFERENCES CAR (ID)\n" +
                ");";
        stmt.executeUpdate(sql);
        System.out.println("Create table customer in given database...");
        stmt.close();
        conn.close();
    }

    private static void createTableCompany(Statement stmt) throws SQLException {
        String sql = "create table IF NOT EXISTS COMPANY" +
                "(id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                " name VARCHAR(255) not NULL UNIQUE)";
        stmt.executeUpdate(sql);
        System.out.println("Created table in given database...");
        stmt.close();
        conn.close();
    }

    private static void createTableCar(Statement stmt) throws SQLException {
        String sql = "create table IF NOT EXISTS CAR(\n" +
                "ID INT PRIMARY KEY AUTO_INCREMENT,\n" +
                "NAME VARCHAR(255) UNIQUE NOT NULL,\n" +
                "COMPANY_ID INT NOT NULL,\n" +
                "CONSTRAINT fk_company FOREIGN KEY (COMPANY_ID)\n" +
                "REFERENCES COMPANY(id)\n" +
                ");";
        stmt.executeUpdate(sql);
        System.out.println("Create table car in given database...");
        stmt.close();
        conn.close();
    }

    private static void resetID(Statement stmt) throws SQLException {
        String sql = "ALTER TABLE company ALTER COLUMN id RESTART WITH 1;\n" +
                "ALTER TABLE car ALTER COLUMN id RESTART WITH 1;\n" +
                "ALTER TABLE customer ALTER COLUMN id RESTART WITH 1;";
        stmt.executeUpdate(sql);
        System.out.println("resetID company...");
        System.out.println("resetID car...");
        stmt.close();
        conn.close();

    }

    private static Statement getStatement() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(true);
        return conn.createStatement();
    }

    private static String getDbFilename(String[] args) {
        String dbFilename = "newData";
        if (args.length > 1) {
            if (args[0].equals("-databaseFileName")) {
                dbFilename = args[1];
            }
        }
        return dbFilename;
    }
}