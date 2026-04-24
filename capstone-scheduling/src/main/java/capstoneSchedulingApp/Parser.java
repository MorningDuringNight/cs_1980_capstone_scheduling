package capstoneSchedulingApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Parser {

    public static ArrayList<String> parseFile(String databaseName, String fileName, String delin) {
        String url = "jdbc:sqlite:" + databaseName;
        ArrayList<String> output = new ArrayList<>();

        try (Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.createStatement()) {
                System.out.println("Created DB");

                //Deletes previous tables so we don't have to keep deleting them
                statement.execute("DROP TABLE IF EXISTS classes");
                statement.execute("DROP TABLE IF EXISTS courses");
                statement.execute("DROP TABLE IF EXISTS instructors");

                var sql = "CREATE TABLE classes ("
                    + "	id INTEGER PRIMARY KEY,"
                    + " sub_code STRING,"
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
                    + "sub_code,"
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
                    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                var statement1 = dbConnection.prepareStatement(sql);

                // Basic parsing
            try (Scanner scanner = new Scanner(new File(fileName))) {
                scanner.nextLine(); // Skip over label line, may want to parse from it later
                int lineNum = 1;
                while (scanner.hasNextLine()) {
                    String currentLine = scanner.nextLine();
                    lineNum++;
                    String[] lineArray = currentLine.split(delin);
                    
                    /*System.out.println("START--------------------------------");
                    for (String test : lineArray) {
                        System.out.println(test);
                    }
                    System.out.println("END----------------------------------");*/
                    
                    //Filter out "ByAppt"
                    if (lineArray.length > 5) {
                        if (lineArray[5].equals("ByAppt")) {
                            continue;
                        }
                    }

                    /*Line entry number check
                    if (lineArray.length != 16) {
                        output.add("Row: " + lineNum + "\nData: " + currentLine + "\nProblem: Missing information\n");
                        continue;
                    }*/

                    //Invalid Input Check
                    String[] invalidInput = nullInputCheck(lineArray).split("~"); //I bundled the flag that something changed into the string, this just separates it
                    if (invalidInput[0].equals("true")) {
                        output.add("Row: "  + lineNum + "\n\t" + invalidInput[1]);
                        continue;
                    }

                    statement1.setString(1, lineArray[0]);                                     // 0 is Subject Code
                    statement1.setInt(2, Integer.parseInt(lineArray[3]));                   // 3 is Class Number
                    statement1.setInt(3, Integer.parseInt(lineArray[1]));                   // 1 is Course Number                   
                    statement1.setInt(4, Integer.parseInt(lineArray[4]));                   // 4 is Associated Class Number
                    statement1.setString(5, lineArray[5]);                                  // 5 is Days
                    statement1.setBoolean(6, doesDayStringHaveDay(lineArray[5], 0));   // 5 is Days, 0 -> Mon
                    statement1.setBoolean(7, doesDayStringHaveDay(lineArray[5], 1));   // 5 is Days, 1 -> Tues
                    statement1.setBoolean(8, doesDayStringHaveDay(lineArray[5], 2));   // 5 is Days, 2 -> Wed
                    statement1.setBoolean(9, doesDayStringHaveDay(lineArray[5], 3));   // 5 is Days, 3 -> Thurs
                    statement1.setBoolean(10, doesDayStringHaveDay(lineArray[5], 4));   // 5 is Days, 4 -> Fri
                    statement1.setString(11, lineArray[6]);                                  // 6 is Start Time
                    statement1.setString(12, lineArray[7]);                                 // 7 is Stop Time
                    statement1.setInt(13, timeToMinutes(lineArray[6]));                     // 6 is Start Time FOR INT
                    statement1.setInt(14, timeToMinutes(lineArray[7]));                     // 7 is Stop Time FOR INT
                    statement1.setString(15, lineArray[8]);                                 // 8 is Room
                    statement1.setString(16, lineArray[9]);                                 // 9 is Instructor
                    statement1.setString(17, lineArray[13]);                                // 13 is Type                        
                    statement1.setInt(18, Integer.parseInt(lineArray[15]));                 // 15 is Enrollment
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
            } catch (Exception e) {
                System.out.println("HERE" + e.toString());
                return output;
            }

            } catch (SQLException e) {
            //System.out.println(e.getMessage());
            return output;
        }
        
        createCourseTable(databaseName);
        return output;
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
        createCrossListedTable(databaseName);
    }

    public static void createCrossListedTable(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;

        try (Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.createStatement()) {
                
                statement.execute("DROP TABLE IF EXISTS cross_listed");

                String createSQL = "CREATE TABLE cross_listed ("
                    + " id INTEGER PRIMARY KEY,"
                    + " sub_code_a STRING,"
                    + " course_num_a INTEGER,"
                    + " sub_code_b STRING,"
                    + " course_num_b INTEGER"
                    + ")";

                statement.execute(createSQL);
                System.out.println("Created Cross-Listed DB");

                String insertSQL = "INSERT INTO cross_listed("
                    + "sub_code_a, "
                    + "course_num_a, "
                    + "sub_code_b, "
                    + "course_num_b) "
                    + "VALUES(?, ?, ?, ?)";
                var insertStatement = dbConnection.prepareStatement(insertSQL);

                String[][] pairs = {
                    {"CS", "1501", "CS", "2015"},
                    {"CS", "1510", "CS", "2012"},
                    {"CS", "1511", "CS", "2110"},
                    {"CS", "1530", "CS", "2030"},
                    {"CS", "1541", "CS", "2041"},
                    {"CS", "1555", "CS", "2055"},
                    {"CS", "1621", "CS", "2021"},
                    {"CS", "1635", "CS", "2035"},
                    {"CS", "1640", "CS", "1980"},
                    {"CS", "1651", "CS", "2051"},
                    {"CS", "1656", "CS", "2056"},
                    {"CS", "1660", "CS", "2060"},
                    {"CS", "1671", "CS", "2071"},
                    {"CS", "1674", "CS", "2074"},
                    {"CS", "1675", "CS", "2075"},
                    {"CS", "1678", "CS", "2078"},
                    {"CS", "1684", "CS", "2084"},
                    {"CS", "1900", "CS", "1950"},
                    {"CS", "2520", "TELCOM", "2321"},
                    {"CS", "2710", "ISSP", "2160"},
                    {"CS", "2731", "ISSP", "2230"},
                };

                for (String[] pair : pairs) {
                    insertStatement.setString(1, pair[0]);
                    insertStatement.setInt(2, Integer.parseInt(pair[1]));
                    insertStatement.setString(3, pair[2]);
                    insertStatement.setInt(4, Integer.parseInt(pair[3]));
                    insertStatement.executeUpdate();
                }
            }
            catch(SQLException e) {
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

    public static String nullInputCheck(String[] input) {
        String output = "";
        String[] out = new String[16];
        boolean flag = false;

        if (input.length < 16) {
            flag = true;
            for (int i = 0; i < input.length; i++) {
                out[i] = input[i];
            }       
            for (int i = input.length; i < 16; i++) {
                out[i] = "";
            }
            input = out;
            output += "Line Incomplete\n";
        }

        //CLASS NUMBER CHECK
        if (!input[3].equals("") && !isInt(input[3])) {
            flag = true;
            input[3] = "*INVALID*";
            output += "Invalid Class Number\n";
        }
        else if (input[3].equals("")) {
            flag = true;
            input[3] = "*MISSING*";
            output += "Missing Class Number\n";
        }

        //COURSE NUMBER CHECK
        if (!input[1].equals("") && !isInt(input[1])) {
            flag = true;
            input[1] = "*INVALID*";
            output += "Invalid Course Number\n";
        }
        else if (input[1].equals("")) {
            flag = true;
            input[1] = "*MISSING*";
            output += "Missing Course Number\n";
        }

        //ASSOCIATED CLASS NUMBER CHECK
        if (!input[4].equals("") && !isInt(input[4])) {
            flag = true;
            input[4] = "*INVALID*";
            output += "Invalid Associated Class Number\n";
        }
        else if (input[4].equals("")) {
            flag = true;
            input[4] = "*MISSING*";
            output += "Missing Associated Class Number\n";
        }

        //DAYS CHECK
        if (  !  (
            doesDayStringHaveDay(input[5], 0) ||
            doesDayStringHaveDay(input[5], 1) ||
            doesDayStringHaveDay(input[5], 2) ||
            doesDayStringHaveDay(input[5], 3) ||
            doesDayStringHaveDay(input[5], 4))) {
            flag = true;
            input[5] = "*" + input[5] + "*";
            output += "Missing/Invalid Class Days\n";
        }

        //START TIME CHECK
        if (!isTime(input[6])) {
            flag = true;
            if (input[6].equals("")) {
                input[6] = "*MISSING*";
                output += "Missing Start Time\n";
            }
            else {
                input[6] = "*" + input[6] + "*";
                output += "Invalid Start Time\n";
            }
        }

        //END TIME CHECK
        if (!isTime(input[7])) {
            flag = true;
            if (input[7].equals("")) {
                input[7] = "*MISSING*";
                output += "Missing Start Time\n";
            }
            else {
                input[7] = "*" + input[7] + "*";
                output += "Invalid Start Time\n";
            }
        }

        //INSTRUCTOR CHECK
        if (input[9].equals("") && (input[13].equals("LEC") || input[13].equals("PRA") || input[13].equals("SEM"))) {
            flag = true;
            input[9] = "*MISSING*";
            output += "Missing Instructor\n";
        }

        //TYPE CHECK
        if (input[13].equals("")) {
            flag = true;
            input[13] = "*MISSING*";
            output += "Missing Class Type\n";
        }

        return flag + "~" + String.join("," , input) + "\n" + output;
    }

    public static boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isTime(String input) {
        return input.matches("^([0-9]|0[0-9]|1[0-2]):[0-5][0-9] (AM|PM|Am|am|Pm|pm)$");
    }
}
