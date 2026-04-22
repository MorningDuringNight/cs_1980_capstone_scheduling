package capstoneSchedulingApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

public class Query {

    public static ArrayList<Collision> queryLecCollision(String databaseName) {
        //Loops through each class in classes
        String firstSql = "SELECT *" 
                        + " FROM classes" 
                        + " WHERE id == " + "~i"
                        + " AND (type == 'LEC'"
                        + " OR type == 'PRA'"
                        + " OR type == 'SEM')";
        final int RULES = 2;
        String secondSql [] = new String[RULES];
        String typeStringsArray[] = new String[RULES];
        int impactArray[] = new int[RULES];

        //RULE 1: LECTURE OVERLAP WITH OTHER OF SAME COURSE NUM CHECK
        typeStringsArray[0] = "Lecture overlaps with other lecture sections of the same course";
        impactArray[0] = 3;
        secondSql[0] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND sub_code == " + "'~sub_code'"
                        + " AND type == " + "'~type'"
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

        //RULE 7: LECTURE OVERLAP WITH RECITATION CHECK
        typeStringsArray[1] = "Lecture overlaps with its own recitation";
        impactArray[1] = 3;
        secondSql[1] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND sub_code == " + "'~sub_code'"
                        + " AND type != " + "'~type'"
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
                        + " OR day_fri AND " + "~day_fri" + ")";

        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
        System.out.println("Output: ");
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        } 
        return queryOutput;
    }

    public static ArrayList<Collision> queryRecCollision(String databaseName, int minutesBetweenAmount) {
        //Loops through each class in classes
        String firstSql = "SELECT *" 
                        + " FROM classes" 
                        + " WHERE id == " + "~i"
                        + " AND (type == 'REC'"
                        + " OR type == 'LAB')";
        
        final int RULES = 4;
        String secondSql [] = new String[RULES];
        String typeStringsArray[] = new String[RULES];
        int impactArray[] = new int[RULES];

        //RULE 2: RECITATION OVERLAP CHECK
        typeStringsArray[0] = "Recitations of the same section overlap";
        impactArray[0] = 3;
        secondSql[0] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND (type == 'REC' OR type == 'LAB')"
                        + " AND sub_code == " + "'~sub_code'"
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

        //RULE 3: RECITATION TIME BETWEEN CHECK
        typeStringsArray[1] = "Time between recitations of the same section is within " + minutesBetweenAmount + " minutes";
        impactArray[1] = 1;
        secondSql[1] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND (type == 'REC' OR type == 'LAB')"
                        + " AND sub_code == " + "'~sub_code'"
                        + " AND course_num == " + "~course_num"
                        + " AND asso_num == " + "~asso_num"
                        //Condition of the class times overlapping at all
                        + " AND (" + "~start_int" + " - end_int <= " + minutesBetweenAmount
                        + " AND " + "~start_int" + " - end_int > 0)"
                        //Condtion to make sure class shares at least one day of the week
                        + " AND (day_mon AND " + "~day_mon"
                        + " OR day_tues AND " + "~day_tues"
                        + " OR day_wed AND " + "~day_wed"
                        + " OR day_thurs AND " + "~day_thurs"
                        + " OR day_fri AND "+ "~day_fri" + ")";

        //RULE 4: RECITATION FOR ALL COURSE NUM OVERLAP CHECK
        typeStringsArray[2] = "Recitations of any section overlap";
        impactArray[2] = 2;
        secondSql[2] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND (type == 'REC' OR type == 'LAB')"
                        + " AND sub_code == " + "'~sub_code'"
                        + " AND course_num == " + "~course_num"
                        + " AND asso_num != " + "~asso_num"
                        //Condition of the class times overlapping at all
                        + " AND (" + "~start_int"  + " <= end_int"
                        + " AND start_int <= " + "~end_int" + ")"
                        //Condtion to make sure class shares at least one day of the week
                        + " AND (day_mon AND " + "~day_mon"
                        + " OR day_tues AND " + "~day_tues"
                        + " OR day_wed AND " + "~day_wed"
                        + " OR day_thurs AND " + "~day_thurs"
                        + " OR day_fri AND "+ "~day_fri" + ")";

        //RULE 5: RECITATION TIME BETWEEN CHECK FOR ALL COURSE NUM
        typeStringsArray[3] = "Time between recitations of the any section is within " + minutesBetweenAmount + " minutes";
        impactArray[3] = 1;
        secondSql[3] = "SELECT *" 
                        + " FROM classes"
                        + " WHERE id != " + "~i"
                        //Checks that both instances are Lectures of the same Course Number
                        + " AND (type == 'REC' OR type == 'LAB')"
                        + " AND sub_code == " + "'~sub_code'"
                        + " AND course_num == " + "~course_num"
                        + " AND asso_num != " + "~asso_num"
                        //Condition of the class times overlapping at all
                        + " AND (" + "~start_int" + " - end_int <= " + minutesBetweenAmount
                        + " AND " + "~start_int" + " - end_int > 0)"
                        //Condtion to make sure class shares at least one day of the week
                        + " AND (day_mon AND " + "~day_mon"
                        + " OR day_tues AND " + "~day_tues"
                        + " OR day_wed AND " + "~day_wed"
                        + " OR day_thurs AND " + "~day_thurs"
                        + " OR day_fri AND "+ "~day_fri" + ")";

        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
        System.out.println("Output: ");
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        } 
        return queryOutput;
    }

    public static ArrayList<Collision> queryTeacherProximity(String databaseName, int minutesBetweenAmount) {
        String url = "jdbc:sqlite:" + databaseName;
        ArrayList<Collision> output = new ArrayList<Collision>();
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
                    
            }

            //If initial query failed skip to next loop
            if (inst == "") {
                continue;
            }

            String firstSql = "SELECT *" 
                            + " FROM classes" 
                            + " WHERE id == " + "~i"
                            + " AND instructor == '" + inst +"'";;

            final int RULES = 1;
            String secondSql [] = new String[RULES];
            String typeStringsArray[] = new String[RULES];
            int impactArray[] = new int[RULES];

            //RULE 6: TEACHER PROXIMITY CHECK
            typeStringsArray[0] = "TIME BETWEEN CHECK";
            impactArray[0] = 1;
            secondSql [0] = "SELECT *" 
                            + " FROM classes"
                            + " WHERE id != " + "~i"
                            //Checks that both instances are Lectures of the same Course Number
                            + " AND instructor == '" + inst + "'"
                            //Condition within
                            + " AND (" + "~start_int" + " - end_int < " + minutesBetweenAmount
                            + " AND " + "~start_int" + " - end_int > 0)"
                            //Condtion to make sure class shares at least one day of the week
                            + " AND (day_mon AND " + "~day_mon"
                            + " OR day_tues AND " + "~day_tues"
                            + " OR day_wed AND " + "~day_wed"
                            + " OR day_thurs AND " + "~day_thurs"
                            + " OR day_fri AND "+ "~day_fri" + ")";

            ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
            output.addAll(queryOutput);
        }

        for (Collision e : output) {
            System.out.println(e.toString());
        } 
        return output;
    }

    public static ArrayList<Collision> queryEachInCourse(String databaseName, String firstSql, String[] secondSql, String[] typeStringsArray, int[] impactArray) {
        String url = "jdbc:sqlite:" + databaseName;
        ArrayList<Collision> allHits = new ArrayList<Collision>();

        //This section parses out the table name from the SQL query
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
                Collision temp = querySub(databaseName, A, sql2, typeStringsArray[j], impactArray[j]);
                if (temp != null) {
                    allHits.add(temp);
                }
            }
        }

        return allHits;
    }

    public static Collision querySub(String databaseName, Course A, String secondSql, String typeStrings, int impact){
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
                Collision col = new Collision(A, B);
                col.setCollisionParameters(typeStrings, impact);
                return col;
            }
            return null;
    }

    public static void queryTestCrossRoom(String databaseName) {
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


            // for each class for instructor
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
                            + " WHERE id != " + "~i"
                            //Checks that both instances are Lectures of the same Course Number
                            + " AND instructor == '" + "~instructor" + "'"
                            //Condition of the class times overlapping at all
                            // + " AND (" + A.start_int  + " <= end_int"
                            // + " AND start_int <= " + A.end_int + ")"
                            //check for class and associated from original to cross List
                            + " AND clas_num == "  + ("~clas_num+1")
                            + " AND " + "~asso_num"  + " == asso_num"
                            //Condtion to make sure class shares at least one day of the week
                            + " AND (day_mon AND " + "~day_mon"
                            + " OR day_tues AND " + "~A.day_tues"
                            + " OR day_wed AND " + "~day_wed"
                            + " OR day_thurs AND " + "~day_thurs"
                            + " OR day_fri AND "+ "~day_fri" + ")";

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

                for(Course match: B) {
                    System.out.println("Checking Cross listed courses for rooms");
                    if(!A.room.equals(match.room)) {
                        System.out.println("Cross listed courses in different rooms: " + A.toString() + " AND " + match.toString());
                    }
                }
            }
        }
    }

    public static ArrayList<Collision> crossProf(String databaseName) {
        String firstSql = "SELECT *"
                        + " FROM classes"
                        + " WHERE id == " + "~i"
                        + " AND type == 'LEC' ";

        final int RULES = 1;
        String secondSql[]       = new String[RULES];
        String typeStringsArray[] = new String[RULES];
        int impactArray[]         = new int[RULES];

        typeStringsArray[0] = "CROSS-LISTED INSTRUCTOR MISMATCH";
        impactArray[0] = 2;
        secondSql[0] = "SELECT *"
                    + " FROM classes"
                    + " WHERE id != " + "~i"
                    + " AND type == 'LEC'"
                    // Cross-listed partner: same asso_num, clas_num offset by ±1
                    + " AND asso_num == " + "~asso_num"
                    + " AND (clas_num == (" + "~clas_num" + " + 1)"
                    + " OR clas_num == (" + "~clas_num" + " - 1))"
                    // Instructor differs from base course and is not empty
                    + " AND instructor != '" + "~instructor" + "'"
                    + " AND instructor != ''";


        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
        System.out.println("Output: ");
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        }
        return queryOutput;
    }

    public static void queryRoomCollision(String databaseName) {
        String url = "jdbc:sqlite:" + databaseName;

        for (int i = 1; i <= tableLength(databaseName, "classes"); i++) {
            String sql = "SELECT * "
                                + "FROM classes "
                                + "WHERE id == " + "~i";

            Course A = new Course();
            ArrayList<Course> B = new ArrayList<Course>();

            try (Connection dbConnection = DriverManager.getConnection(url);
                var statement = dbConnection.prepareStatement(sql)) {
                var rs = statement.executeQuery();
                
                while (rs.next()) { 
                    A = new Course(rs); 
                }
            } 
            catch (Exception e) {
                System.out.println(e.toString());
                return;
            }

            sql = "SELECT *"
                + " FROM classes"
                + " WHERE id != " + i
                // Same room
                + " AND room == '" + "~room" + "'"
                // Overlapping times
                + " AND (" + "~start_int" + " <= end_int"
                + " AND start_int <= " + "~end_int" + ")"
                // Same day
                + " AND (day_mon AND " + "~day_mon"
                + " OR day_tues AND " + "~day_tues"
                + " OR day_wed AND " + "~day_wed"
                + " OR day_thurs AND " + "~day_thurs"
                + " OR day_fri AND " + "~day_fri" + ")"
                // Not a cross listed course (clas_num not off by 1 with same asso_num)
                + " AND NOT (asso_num == " + "~asso_num"
                + " AND (clas_num == " + "(" + "~clas_num" + " + 1)"
                + " OR clas_num == " + "(~clas_num - 1)" + "))";

            try (Connection dbConnection = DriverManager.getConnection(url);
                var statement = dbConnection.prepareStatement(sql)) {
                var rs = statement.executeQuery();

                while (rs.next()) { 
                    B.add(new Course(rs)); 
                }
            } 
            catch (Exception e) {
                System.out.println(e.toString());
                return;
            }

            for (Course match : B) {
                System.out.println("Room collision: " + A.toString() + " AND " + match.toString());
            }
        }
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
                            + " WHERE id == " + "~i"
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
                            + " WHERE id != " + "~i"
                            //Checks that both instances are Lectures of the same Course Number
                            + " AND instructor == '" + "~instructor" + "'"
                            //Condition within
                            + " AND (" + "~start_int" + " - end_int < 30"
                            + " AND " + "~start_int" + " - end_int > 0)"
                            //Condtion to make sure class shares at least one day of the week
                            + " AND (day_mon AND " + "~day_mon"
                            + " OR day_tues AND " + "~day_tues"
                            + " OR day_wed AND " + "~day_wed"
                            + " OR day_thurs AND " + "~day_thurs"
                            + " OR day_fri AND "+ "~day_fri" + ")";

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
