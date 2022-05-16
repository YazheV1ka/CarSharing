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
        dropTableCompany(getStatement());
        createTableCompany(getStatement());
        resetID(getStatement());
        mainMenu();
    }


    private static void mainMenu() throws SQLException, ClassNotFoundException {
        while (true) {
            System.out.println("1. Log in as a manager\n" +
                    "0. Exit");
            int option = Integer.parseInt(scanner.nextLine());

            switch (option) {
                case 1:
                    logInAsAManager();
                    break;
                case 0:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid operation try again!");
                    break;
            }
        }
    }
    private static void logInAsAManager() throws SQLException, ClassNotFoundException {
        System.out.println("1. Company list\n" +
                "2. Create a company\n" +
                "0. Back");
        int option = Integer.parseInt(scanner.nextLine());

        switch (option) {
            case 1:
                companyListAsManager();
                break;
            case 2:
                createACompany();
                break;
            case 0:
                mainMenu();
                break;
            default:
                System.out.println("Invalid operation !");
                break;
        }
        logInAsAManager();
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
                logInAsAManager();
            }
        }
    }



    private static void createACompany() throws SQLException, ClassNotFoundException {
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
        String sql = "drop table if exists COMPANY";
        stmt.executeUpdate(sql);
        System.out.println("Dropping table in given database...");
        stmt.close();
        conn.close();
    }


    private static void createTableCompany(Statement stmt) throws SQLException {
        String sql = "create table if not exists COMPANY" +
                "(id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
                " name VARCHAR(255) not NULL UNIQUE)";
        stmt.executeUpdate(sql);
        System.out.println("Created table in given database...");
        stmt.close();
        conn.close();
    }


    private static void resetID(Statement stmt) throws SQLException {
        String sql = "ALTER TABLE company ALTER COLUMN id RESTART WITH 1;";
        stmt.executeUpdate(sql);
        System.out.println("resetID company...");
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