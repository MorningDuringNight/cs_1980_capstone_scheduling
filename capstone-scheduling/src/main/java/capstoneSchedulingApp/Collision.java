package capstoneSchedulingApp;

import java.util.ArrayList;

public class Collision {

    public Course base;
    public ArrayList<Course> hits;

    public Collision(Course base, ArrayList<Course> hits) {
        this.base = base;
        this.hits = hits;
    }

    public String toString() {
        String outString = "";
        outString += "---------------------------------------------\n";
        outString += base.toString() + " collides with:\n";
        outString += "\n";
        for (Course e : hits) {
            outString += e.toString() + "\n";
        }
        outString += "---------------------------------------------\n";
        return outString;
    }
}
