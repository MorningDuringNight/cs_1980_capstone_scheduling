package capstoneSchedulingApp;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        ArrayList<String> out = Parser.parseFile("schedule.db","target/classes/Fall2026_formattedschedule.csv", ",");
        //Query.queryGenericInst("schedule.db");
        //System.out.println(Parser.isTime("5:50PM"));
        for (String i : out) {
            System.out.println("-------------------------------");
            System.out.println(i);
            System.out.println("-------------------------------");
        }
        ArrayList<Collision> finalt = Query.queryLecCollision("schedule.db");
        for (Collision i : finalt) {
            //System.out.println(i.toString());
        }
    }
}
