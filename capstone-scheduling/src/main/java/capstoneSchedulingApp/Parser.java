package capstoneSchedulingApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

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

                //Deletes previous tables so we don't have to keep deleting them
                statement.execute("DROP TABLE IF EXISTS classes");
                statement.execute("DROP TABLE IF EXISTS courses");
                statement.execute("DROP TABLE IF EXISTS instructors");

                var sql = "CREATE TABLE classes ("
                    + "	id INTEGER PRIMARY KEY,"
                    + "	clas_num INTEGER,"
                    + "	course_num INTEGER,"
                    + "	asso_num INTEGER,"
                    + " days STRING,"
                    + " day_mon BOOLEAN,"
                    + " day_tues BOOLEAN,"
                    + " day_wed BOOLEAN,"
                    + " day_thurs BOOLEAN,"
                    + " day_fri BOOLEAN,"
                    + " start STRING,"
                    + " end STRING,"
                    + " start_int INTEGER,"
                    + " end_int INTEGER,"
                    + " room STRING,"
                    + " instructor STRING,"
                    + " type STRING,"
                    + " enroll INTEGER"
                    + ");";
                
                statement.execute(sql);

                sql = "INSERT OR IGNORE INTO classes("
                    + "clas_num,"
                    + "course_num,"
                    + "asso_num,"
                    + "days,"
                    + "day_mon,"
                    + "day_tues,"
                    + "day_wed,"
                    + "day_thurs,"
                    + "day_fri,"
                    + "start,"
                    + "end,"
                    + "start_int,"
                    + "end_int,"
                    + "room,"
                    + "instructor,"
                    + "type,"
                    + "enroll)"
                    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                var statement1 = dbConnection.prepareStatement(sql);

                // Basic parsing
            try (Scanner scanner = new Scanner(new File(fileName))) {
                scanner.nextLine(); // Skip over label line, may want to parse from it later
                while (scanner.hasNextLine()) {
                    String[] lineArray = scanner.nextLine().split(delin); // We can *not* hard-code later

                    if (!lineArray[5].equals("ByAppt")) {

                        statement1.setInt(1, Integer.parseInt(lineArray[3]));                   // 3 is Class Number
                        statement1.setInt(2, Integer.parseInt(lineArray[1]));                   // 1 is Course Number                   
                        statement1.setInt(3, Integer.parseInt(lineArray[4]));                   // 4 is Associated Class Number
                        statement1.setString(4, lineArray[5]);                                  // 5 is Days
                        statement1.setBoolean(5, doesDayStringHaveDay(lineArray[5], 0));   // 5 is Days, 0 -> Mon
                        statement1.setBoolean(6, doesDayStringHaveDay(lineArray[5], 1));   // 5 is Days, 1 -> Tues
                        statement1.setBoolean(7, doesDayStringHaveDay(lineArray[5], 2));   // 5 is Days, 2 -> Wed
                        statement1.setBoolean(8, doesDayStringHaveDay(lineArray[5], 3));   // 5 is Days, 3 -> Thurs
                        statement1.setBoolean(9, doesDayStringHaveDay(lineArray[5], 4));   // 5 is Days, 4 -> Fri
                        statement1.setString(10, lineArray[6]);                                  // 6 is Start Time
                        statement1.setString(11, lineArray[7]);                                 // 7 is Stop Time
                        statement1.setInt(12, timeToMinutes(lineArray[6]));                     // 6 is Start Time FOR INT
                        statement1.setInt(13, timeToMinutes(lineArray[7]));                     // 7 is Stop Time FOR INT
                        statement1.setString(14, lineArray[8]);                                 // 8 is Room
                        statement1.setString(15, lineArray[9]);                                 // 9 is Instructor
                        statement1.setString(16, lineArray[13]);                                // 13 is Type                        
                        statement1.setInt(17, Integer.parseInt(lineArray[15]));                 // 15 is Enrollment
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
            //System.out.println(e.getMessage());
            return;
        }
        
        createCourseTable(databaseName);
    }

    public static void createCourseTable(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;

        try(Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.createStatement()) {
            
            String createSQL = "CREATE TABLE courses ("
                + "	id INTEGER PRIMARY KEY,"
                + "	course_num INTEGER,"
                + "	asso_num INTEGER,"
                + "	clas_nums STRING," //stored as comma seperated class numbers
                + "	FOREIGN KEY(course_num) REFERENCES classes(course_num)"
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
            
            String createSQL = "CREATE TABLE instructors ("
                + "	id INTEGER PRIMARY KEY,"
                + "	instructor STRING,"
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

    //0 - Mon, 1 - Tues, 2 - Wed, 3 - Thurs, 4 - Fri
    public static boolean doesDayStringHaveDay(String input, int day) {
        switch(input) {
            case "MTuWThF":
                return true;
            case "MWF":
                return (day == 0 || day == 2 || day == 4);
            case "MW":
                return (day == 0 || day == 2);
            case "TuTh":
                return (day == 1 || day == 3);
            case "MF":
                return (day == 0 || day == 4);
            case "WF":
                return (day == 2 || day == 4);
            case "M":
                return (day == 0);
            case "Tu":
                return (day == 1);
            case "W":
                return (day == 2);
            case "Th":
                return (day == 3);
            case "F":
                return (day == 4);
            default:
                return false;
        }
    }

    public static int timeToMinutes(String time) {
    String[] hourMin = time.split(":| ");
    int minutesPassedInDay = (Integer.parseInt(hourMin[0]) * 60) + Integer.parseInt(hourMin[1]);
    if ( (hourMin[2].toUpperCase().equals("PM")) && (Integer.parseInt(hourMin[0]) != 12) ){
        minutesPassedInDay += (60 * 12);
    }
    return minutesPassedInDay;
    }

    public static String queryParser(String queryTemplate) {
        String testInput = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND type == 'LEC'"
                        + " AND course_num == " + "~course_num"
                        //Condition of the class times overlapping at all
                        + " AND (" + "~A.start_int"  + " <= end_int"
                        + " AND start_int <= " + "~end_int" + ")"
                        //Condtion to make sure class shares at least one day of the week
                        + " AND (day_mon AND " + "~day_mon"
                        + " OR day_tues AND " + "~day_tues"
                        + " OR day_wed AND " + "~day_wed"
                        + " OR day_thurs AND " + "~day_thurs"
                        + " OR day_fri AND " + "~day_fri" + ")";

        String out = testInput.replaceAll("\\~i", "test");
        System.out.println(out);

        return "";
    }
}
