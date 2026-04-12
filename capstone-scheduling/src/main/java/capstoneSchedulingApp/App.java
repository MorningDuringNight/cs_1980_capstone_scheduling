package capstoneSchedulingApp;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        ArrayList<String> out = Parser.parseFile("schedule.db","target/classes/sched.csv", ",");
        //Query.queryGenericInst("schedule.db");
        for (String i : out) {
            System.out.println(i);
        }
        Query.queryTeacherProximity("schedule.db", 30);
    }
}
