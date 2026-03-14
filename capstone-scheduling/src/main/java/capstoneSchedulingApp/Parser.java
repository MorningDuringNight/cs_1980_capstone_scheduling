package capstoneSchedulingApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Parser {

    public static void parseFile(String databaseName, String fileName, String delin) {
        // Schedule sched = new Schedule(5, 480, 1200, 5);
        String url = "jdbc:sqlite:" + databaseName;

        try (Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.createStatement()) {
                System.out.println("Created DB");

                var sql = "CREATE TABLE IF NOT EXISTS classes ("
                    + "	clas_num INTEGER PRIMARY KEY,"
                    + "	course_num INTEGER,"
                    + "	asso_num INTEGER,"
                    + " days STRING,"
                    + " start STRING,"
                    + " end STRING,"
                    + " room STRING,"
                    + " instructor STRING,"
                    + " type STRING,"
                    + " enroll INTEGER"
                    + ");";
                
                statement.execute(sql);

                sql = "INSERT OR IGNORE INTO classes(clas_num,course_num,asso_num,days,start,end,room,instructor,type,enroll) VALUES(?,?,?,?,?,?,?,?,?,?)";

                var statement1 = dbConnection.prepareStatement(sql);

                // Basic parsing
            try (Scanner scanner = new Scanner(new File(fileName))) {
                scanner.nextLine(); // Skip over label line, may want to parse from it later
                while (scanner.hasNextLine()) {
                    String[] lineArray = scanner.nextLine().split(delin); // We can *not* hard-code later

                    if (!lineArray[5].equals("ByAppt")) {

                        statement1.setInt(1, Integer.parseInt(lineArray[3]));   // 3 is Class Number
                        statement1.setInt(2, Integer.parseInt(lineArray[1]));   // 1 is Course Number                   
                        statement1.setInt(3, Integer.parseInt(lineArray[4]));   // 4 is Associated Class Number
                        statement1.setString(4, lineArray[5]);                  // 5 is Days
                        statement1.setString(5, lineArray[6]);                  // 6 is Start Time
                        statement1.setString(6, lineArray[7]);                  // 7 is Stop Time
                        statement1.setString(7, lineArray[8]);                  // 8 is Room
                        statement1.setString(8, lineArray[9]);                  // 9 is Instructor
                        statement1.setString(9, lineArray[13]);                 // 13 is Type                        
                        statement1.setInt(10, Integer.parseInt(lineArray[15]));  // 15 is Enrollment
                        statement1.executeUpdate();
                        // Super hard coded, might parse correct columns from label line
                        /*Course temp = new Course(Integer.parseInt(lineArray[1]), Integer.parseInt(lineArray[3]),
                                Integer.parseInt(lineArray[4]), meetingPattern.valueOf(lineArray[5]),
                                lineArray[6], lineArray[7], lineArray[8], lineArray[9],
                                classType.valueOf(lineArray[13]),
                                Integer.parseInt(lineArray[15]));*/
                        // sched.add(temp);
                        // System.out.println(temp);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
                return;
            }

            } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }
        
        createCourseTable(databaseName);
    }

    public static void createCourseTable(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;

        try(Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.createStatement()) {
            
            String createSQL = "CREATE TABLE IF NOT EXISTS courses ("
                + "	course_num INTEGER,"
                + "	asso_num INTEGER,"
                + "	clas_nums STRING," //stored as comma seperated class numbers
                + " PRIMARY KEY (course_num, asso_num)"
                + ");";
                
            statement.execute(createSQL);
            System.out.println("Created Course DB");

            String groupSQL = "SELECT course_num, asso_num, GROUP_CONCAT(clas_num) AS clas_nums "
                            + "FROM classes "
                            + "GROUP BY course_num, asso_num;";
            var results = statement.executeQuery(groupSQL);
            String insertSQL = "INSERT OR IGNORE INTO courses(course_num, asso_num, clas_nums) VALUES(?, ?, ?)";
            var insertStatement = dbConnection.prepareStatement(insertSQL);

            while(results.next()) {
                insertStatement.setInt(1, results.getInt("course_num"));
                insertStatement.setInt(2, results.getInt("asso_num"));
                insertStatement.setString(3, results.getString("clas_nums"));
                insertStatement.executeUpdate();
            }
        }
        catch (SQLException e) {            
            System.out.println(e.getMessage());
        }
        createInstructorTable(databaseName);
    }

    public static void createInstructorTable(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;

        try(Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.createStatement()) {
            
            String createSQL = "CREATE TABLE IF NOT EXISTS instructors ("
                + "	instructor STRING PRIMARY KEY,"
                + " course_groups STRING" //stored as comma-seperated course_num:asso_num pairs
                + ");";
                
            statement.execute(createSQL);
            System.out.println("Created Instructor DB");

            String groupSQL = "SELECT s.instructor, "
                            + " GROUP_CONCAT(co.course_num || ':' || co.asso_num) AS course_groups "
                            + "FROM classes s "
                            + "JOIN courses co ON s.course_num = co.course_num AND s.asso_num = co.asso_num "
                            + "GROUP BY s.instructor;";
                            
            var results = statement.executeQuery(groupSQL);
            String insertSQL = "INSERT OR IGNORE INTO instructors(instructor, course_groups) VALUES(?, ?)";
            var insertStatement = dbConnection.prepareStatement(insertSQL);

            while(results.next()) {
                insertStatement.setString(1, results.getString("instructor"));
                insertStatement.setString(2, results.getString("course_groups"));
                insertStatement.executeUpdate();
            }
        }
        catch (SQLException e) {            
            System.out.println(e.getMessage());
        }
    }
}
