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
        //dropTableCar(getStatement());
        //dropTableCustomer(getStatement());
        createTableCompany(getStatement());
        createTableCar(getStatement());
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
            } else {
                customerMenu(String.valueOf(option));
            }
        }
        System.out.println();

    }

    private static void customerMenu(String customerID) throws SQLException, ClassNotFoundException {
        System.out.println("1. Rent a car\n" +
                "2. Return a rented car\n" +
                "3. My rented car\n" +
                "0. Back");
        int option = scanner.nextInt();
        scanner.nextLine();
        System.out.println();

        switch (option) {
            case 1:
                rentCar(customerID);
                break;
            case 2:
                returnCar(customerID);
                break;
            case 3:
                getRentedCar(customerID);
                break;
            case 0:
                mainMenu();
                break;
            default:
                System.out.println("Invalid operation!");
                break;
        }
        System.out.println();
        customerMenu(customerID);
    }

    private static void getRentedCar(String customerID) throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        String sql = String.format("SELECT cr.Name As car_name, co.name AS company_name\n" +
                "FROM CUSTOMER cu\n" +
                "JOIN CAR cr\n" +
                "ON cu.RENTED_CAR_ID =  cr.ID\n" +
                "JOIN company co\n" +
                "ON cr.COMPANY_ID = co.id\n" +
                "WHERE cu.id = %s", customerID);
        ResultSet rs = stat.executeQuery(sql);
        if (!rs.next()) {
            System.out.println("You didn't rent a car!");
        } else {

            do {
                String carName = rs.getString("car_name");
                String companyName = rs.getString("company_name");
                System.out.println("Your rented a car:");
                System.out.println(carName);
                System.out.println("Company:");
                System.out.println(companyName);

            } while (rs.next());

        }
        stat.close();
        conn.close();
    }

    private static void rentCar(String customerID) throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        String sql = String.format("Select rented_car_id FROM customer where id = %s\n" +
                "and rented_car_id is not null",customerID);
        ResultSet rs = stat.executeQuery(sql);
        if (!rs.next()) {
            rentingCar(customerID);
        } else {
            System.out.println("You've already rented a car!");
        }
        stat.close();
        conn.close();
    }

    private static void rentingCar(String customerID) throws ClassNotFoundException, SQLException {
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
                System.out.printf("%s. %s%n", id, name);
            } while (rs.next());
            System.out.println("0. Back");

            int option = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            if (option == 0) {
                customerMenu(customerID);
            } else {
                System.out.println();
                carCustomerMenu(String.valueOf(option), customerID);
            }

        }
        stat.close();
        conn.close();
    }

    private static void carCustomerMenu(String companyID, String customerID) throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        Map<String, String> map = new HashMap<>();
        String sql = String.format("SELECT * FROM CAR WHERE COMPANY_ID = %s", companyID);
        ResultSet rs = stat.executeQuery(sql);
        if (!rs.next()) {
            System.out.println("The car list is empty!");
        } else {
            System.out.println("Choose a car:");
            do {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                map.put(id, name);
                System.out.printf("%s. %s%n", id, name);
            } while (rs.next());
            System.out.println("0. Back");
            int option = scanner.nextInt();
            scanner.nextLine();
            System.out.println();
            if (option == 0) {
                customerMenu(customerID);
            } else {
                String rentedCarSql = String.format("UPDATE customer SET RENTED_CAR_ID = %s\n" +
                        "WHERE Id = %s", option, customerID);
                stat.executeUpdate(rentedCarSql);
                String carName = map.get(String.valueOf(option));
                System.out.printf("You rented '%s'%n", carName);

            }
        }
        stat.close();
        conn.close();
    }

    private static void returnCar(String customerID) throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        String checkCarSql = String.format("SELECT RENTED_CAR_ID\n" +
                "From customer\n" +
                "WHERE ID = %s\n" +
                "AND RENTED_CAR_ID IS NOT NULL", customerID);
        ResultSet rs = stat.executeQuery(checkCarSql);

        if (!rs.next()) {
            System.out.println("You didn't rent a car!");
        } else {
            String returnCarSql = String.format("\n" +
                    "UPDATE customer SET RENTED_CAR_ID = null\n" +
                    "WHERE ID = %s", customerID);
            stat.executeUpdate(returnCarSql);
            System.out.println("You've returned a rented car!");
        }
        stat.close();
        conn.close();
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
                carManagerMenu(String.valueOf(option));
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

    private static void carManagerMenu(String companyID) throws SQLException, ClassNotFoundException {
        System.out.println("1. Car list\n" +
                "2. Create a car\n" +
                "0. Back");
        int option = scanner.nextInt();
        scanner.nextLine();
        System.out.println();
        switch (option) {
            case 1:
                carListAsManager(companyID);
                break;
            case 2:
                createCar(companyID);
                break;
            case 0:
                loginAsManager();
                break;
            default:
                System.out.println("Invalid operation!");
        }
        System.out.println();
        carManagerMenu(companyID);

    }

    private static void carListAsManager(String companyID) throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        String sql = String.format("SELECT * FROM CAR WHERE COMPANY_ID = %s", companyID);
        ResultSet rs = stat.executeQuery(sql);
        int count = 0;
        if (!rs.next()) {
            System.out.println("The car list is empty!");
        } else {
            System.out.println("Car list:");
            do {
                count++;
                String name = rs.getString("NAME");
                System.out.printf("%s. %s%n", count, name);
            } while (rs.next());

        }
        stat.close();
        conn.close();
    }

    private static void createCar(String companyID) throws SQLException, ClassNotFoundException {
        Statement stat = getStatement();
        System.out.println("Enter the car name:");
        String carName = scanner.nextLine();
        String sql = String.format("\n" +
                "INSERT INTO CAR (NAME, COMPANY_ID) VALUES ('%s',%s)", carName, companyID);
        stat.execute(sql);
        System.out.println("The car was added!");
        System.out.println();
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