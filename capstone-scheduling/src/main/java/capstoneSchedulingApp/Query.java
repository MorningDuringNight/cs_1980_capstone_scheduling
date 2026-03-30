package capstoneSchedulingApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

public class Query {

    public static void queryLecCollision(String databaseName) {
        //Loops through each class in classes
        String firstSql = "SELECT *" 
                        + " FROM classes" 
                        + " WHERE id == " + "~i"
                        + " AND type == 'LEC'";

        String secondSql [] = new String[1];

        //Check for class overlap
        secondSql [0] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND type == 'LEC'"
                        + " AND course_num == " + "~course_num"
                        //Condition of the class times overlapping at all
                        + " AND (" + "~start_int"  + " <= end_int"
                        + " AND start_int <= " + "~end_int" + ")"
                        //Condtion to make sure class shares at least one day of the week
                        + " AND (day_mon AND " + "~day_mon"
                        + " OR day_tues AND " + "~day_tues"
                        + " OR day_wed AND " + "~day_wed"
                        + " OR day_thurs AND " + "~day_thurs"
                        + " OR day_fri AND " + "~day_fri" + ")";

        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql);
        System.out.println("Output: ");
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        } 
    }

        public static void queryRecCollision(String databaseName) {
        //Loops through each class in classes
        String firstSql = "SELECT *" 
                        + " FROM classes" 
                        + " WHERE id == " + "~i"
                        + " AND (type == 'REC'"
                        + " OR type == 'LAB')";

        //Checks for class collisions
        String secondSql [] = new String[1];

        secondSql [0] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND (type == 'REC' OR type == 'LAB')"
                        + " AND course_num == " + "~course_num"
                        + " AND asso_num == " + "~asso_num"
                        //Condition of the class times overlapping at all
                        + " AND (" + "~start_int"  + " <= end_int"
                        + " AND start_int <= " + "~end_int" + ")"
                        //Condtion to make sure class shares at least one day of the week
                        + " AND (day_mon AND " + "~day_mon"
                        + " OR day_tues AND " + "~day_tues"
                        + " OR day_wed AND " + "~day_wed"
                        + " OR day_thurs AND " + "~day_thurs"
                        + " OR day_fri AND "+ "~day_fri" + ")";

        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql);
        System.out.println("Output: ");
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        } 
    }

    public static ArrayList<Collision> queryEachInCourse(String databaseName, String firstSql, String[] secondSql) {
        String url = "jdbc:sqlite:" + databaseName;
        ArrayList<Collision> allHits = new ArrayList<Collision>();

        String[] getTable = firstSql.split(" ");
        String tableString = "";
        Boolean flag = false;

        for (String table : getTable) {
            if (flag) {
                tableString = table;
                break;
            }
            if (table.equals("FROM")) {
                flag = true;
            }
        }

        for (int i = 1; i <= tableLength(databaseName, tableString); i++) {

            //Substitutes i into firstSql
            String sql = firstSql.replaceAll("\\~i", "" + i);

            Course A = new Course();

            try (Connection dbConnection = DriverManager.getConnection(url);
                var statement = dbConnection.prepareStatement(sql)) {

                var rs = statement.executeQuery();

                while (rs.next()) {
                    A = new Course(rs);
                }

            } catch (Exception e) {
                
            }

            //If initial query failed skip to next loop
            if (A.clas_num == -1) {
                continue;
            }

            for (int j = 0; j < secondSql.length; j++) {
                String sql2 = secondSql[j].replaceAll("\\~i", "" + i);
                Collision temp = querySub(databaseName, A, sql2);
                if (temp != null) {
                    allHits.add(temp);
                }
            }
        }

        return allHits;
    }

    public static Collision querySub(String databaseName, Course A, String secondSql){
        String url = "jdbc:sqlite:" + databaseName;
        ArrayList<Course> B = new ArrayList<Course>();
        String finalSql = A.queryGen(secondSql);
        try (Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.prepareStatement(finalSql)) {

                var rs = statement.executeQuery();

                while (rs.next()) {
                    B.add(new Course(rs));           
                }

            } catch (Exception e) {
                    System.out.println(e.toString());
                    return null;
            }

            //The default constructor for Course sets clas_num to -1, so this means the query for A failed
            if (B.size() > 0)  {
                return new Collision(A, B);
            }
            return null;
    }

    //Instructor conseq courses
    public static void queryGenericInst(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;

        for (int h = 1; h <= tableLength(databaseName, "instructors"); h++) {
            String sql =  "SELECT *" 
                        + " FROM instructors" 
                        + " WHERE id == " + h
                        + " AND instructor != ''";

            String inst = "";

            try (Connection dbConnection = DriverManager.getConnection(url);
                var statement = dbConnection.prepareStatement(sql)) {

                var rs = statement.executeQuery();

                while (rs.next()) {
                    inst = rs.getString("instructor");
                }

            } catch (Exception e) {
                    System.out.println(e.toString());
                    return;
            }

            //If initial query failed skip to next loop
            if (inst == "") {
                continue;
            }

            for (int i = 1; i <= tableLength(databaseName, "classes"); i++) {
                sql     =     "SELECT *" 
                            + " FROM classes" 
                            + " WHERE id == " + i
                            + " AND instructor == '" + inst +"'";

                Course A = new Course();
                ArrayList<Course> B = new ArrayList<Course>();

                try (Connection dbConnection = DriverManager.getConnection(url);
                    var statement = dbConnection.prepareStatement(sql)) {

                    var rs = statement.executeQuery();

                    while (rs.next()) {
                        A = new Course(rs);
                    }

                } catch (Exception e) {
                        System.out.println(e.toString());
                        return;
                }

                //If initial query failed skip to next loop
                if (A.clas_num == -1) {
                    continue;
                }

                sql =         "SELECT *" 
                            + " FROM classes"
                            + " WHERE id != " + i
                            //Checks that both instances are Lectures of the same Course Number
                            + " AND instructor == '" + A.instructor + "'"
                            //Condition of the class times overlapping at all
                            + " AND (" + A.start_int + " - end_int < 30"
                            + " AND " + A.start_int + " - end_int > 0)"
                            //Condtion to make sure class shares at least one day of the week
                            + " AND (day_mon AND " + A.day_mon
                            + " OR day_tues AND " + A.day_tues
                            + " OR day_wed AND " + A.day_wed
                            + " OR day_thurs AND " + A.day_thurs
                            + " OR day_fri AND "+ A.day_fri + ")";

                try (Connection dbConnection = DriverManager.getConnection(url);
                var statement = dbConnection.prepareStatement(sql)) {

                    var rs = statement.executeQuery();

                    while (rs.next()) {
                        B.add(new Course(rs));           
                    }

                } catch (Exception e) {
                        System.out.println(e.toString());
                        return;
                }

                if (B.size() > 0)  {
                    System.out.println("Within 30 mins " + A.toString());
                    for (Course check : B) {
                        System.out.println(check.toString());
                    }
                }
            }
        }
    }

    public static int tableLength(String databaseName, String table) {
        String url = "jdbc:sqlite:" + databaseName;
        String sql = "SELECT * FROM " + table + " ORDER BY ROWID DESC LIMIT 1";
        try (Connection dbConnection = DriverManager.getConnection(url);
            var statement = dbConnection.prepareStatement(sql)) {

                var rs = statement.executeQuery();
                while (rs.next()) {
                    return rs.getInt("id");
                }
                return 0;
                

        } catch (Exception e) {
                System.out.println(e.toString());
                return 0;
        }
    }
}
