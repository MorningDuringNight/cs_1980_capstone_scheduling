package capstoneSchedulingApp;

import java.lang.reflect.Array;
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
        typeStringsArray[0] = "Recitation of same section overlap";
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
        typeStringsArray[2] = "Recitation of any section overlap";
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

            //RULE 4: TEACHER PROXIMITY CHECK
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

    public static ArrayList<Collision> queryTestCrossRoom(String databaseName) {
        String firstSql = "SELECT * "
                        + " FROM classes "
                        + " WHERE id == " + "~i"
                        + " AND room != 'TBA'"
                        + " AND room != ''";
        
        final int RULES = 1;
        String secondSql[] = new String[RULES];
        String typeStringsArray[] = new String[RULES];
        int impactArray[] = new int[RULES];

        typeStringsArray[0] = "CROSS-LISTED ROOM MISMATCH";
        impactArray[0] = 2;
        secondSql[0] = "SELECT * "
                    + " FROM classes "
                    + " WHERE id > " + "~i"
                    + " AND type == '~type'"
                    + " AND asso_num == " + "~asso_num"
                    + " AND " + CROSS_LISTED_EXISTS
                    + " AND room != '~room'"
                    + " AND TRIM(room) != 'TBA'"
                    + " AND TRIM(room) != ''"
                    + " AND (" + "~start_int"  + " < end_int"
                    + " AND start_int < " + "~end_int" + ")";

        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        }

        return queryOutput;
    }

    public static ArrayList<Collision> queryCrossProf(String databaseName) {
        String firstSql = "SELECT *"
                        + " FROM classes"
                        + " WHERE id == " + "~i"
                        + " AND TRIM(instructor) != 'TBD' "
                        + " AND TRIM(instructor) != ''";

        final int RULES = 1;
        String secondSql[]       = new String[RULES];
        String typeStringsArray[] = new String[RULES];
        int impactArray[]         = new int[RULES];

        typeStringsArray[0] = "CROSS-LISTED INSTRUCTOR MISMATCH";
        impactArray[0] = 2;
        secondSql[0] = "SELECT *"
                    + " FROM classes"
                    + " WHERE id > " + "~i"
                    + " AND type == '~type'"
                    // Cross-listed partner: same asso_num, clas_num offset by ±1
                    + " AND " + CROSS_LISTED_EXISTS
                    // Instructor differs from base course and is not empty
                    + " AND TRIM(instructor) != ''"
                    + " AND TRIM(instructor) != 'TBD'";


        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
        ArrayList<Collision> filtered = new ArrayList<Collision>();
        for (Collision col : queryOutput) {
            ArrayList<Course> mismatch = new ArrayList<Course>();
            for (Course c : col.hits) {
                if(!col.base.instructor.trim().equals(c.instructor.trim())) {
                    mismatch.add(c);
                }
            }
            if(!mismatch.isEmpty()) {
                col.hits = mismatch;
                filtered.add(col);
            }
        }

        for (Collision e : filtered) {
            System.out.println(e.toString());
        }
        return queryOutput;
    }

    public static ArrayList<Collision> queryRoomCollision(String databaseName) {
        String firstSql = "SELECT * "
                        + " FROM classes "
                        + " WHERE id == " + "~i"
                        + " AND room != 'TBA'"
                        + " AND room != ''"
                        + " AND room != 'WEB'"
                        + " AND days != ''";

        final int RULES = 1;
        String secondSql[] = new String[RULES];
        String typeStringsArray[] = new String[RULES];
        int impactArray[] = new int[RULES];

        typeStringsArray[0] = "ROOM COLLISION";
        impactArray[0] = 3;
        secondSql[0] = "SELECT * "
                    + " FROM classes "
                    + " WHERE id > " + "~i"
                    + " AND room ==  '~room'"
                    + " AND TRIM(room) != 'TBA'"
                    + " AND room != ''"
                    + " AND room != 'WEB'"
                    + " AND (" + "~start_int"  + " < end_int"
                    + " AND start_int < " + "~end_int" + ")"
                    + " AND (day_mon AND " + "~day_mon"
                    + " OR day_tues AND " + "~day_tues"
                    + " OR day_wed AND " + "~day_wed"
                    + " OR day_thurs AND " + "~day_thurs"
                    + " OR day_fri AND "+ "~day_fri" + ")"
                    + " AND NOT" + CROSS_LISTED_EXISTS;

        ArrayList<Collision> queryOutput = queryEachInCourse(databaseName, firstSql, secondSql, typeStringsArray, impactArray);
        for (Collision e : queryOutput) {
            System.out.println(e.toString());
        }
    
        return queryOutput;
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

    private static final String CROSS_LISTED_EXISTS = 
          " EXISTS ("
        + " SELECT 1 FROM cross_listed c1"
        + " WHERE (c1.sub_code_a == '~sub_code' AND c1.course_num_a == ~course_num"
        + "         AND c1.sub_code_b == classes.sub_code AND c1.course_num_b == classes.course_num)"
        + " OR (c1.sub_code_b == '~sub_code' AND c1.course_num_b == ~course_num"
        + "         AND c1.sub_code_a == classes.sub_code AND c1.course_num_a == classes.course_num)"
        + " )";

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
