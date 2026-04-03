package capstoneSchedulingApp;

import java.util.ArrayList;

public class Collision {

    public Course base;
    public ArrayList<Course> hits;
    public String type;
    public int impact;              //High is higher level warning, currently I'm thinking 1-3

    public Collision(Course base, ArrayList<Course> hits) {
        this.base = base;
        this.hits = hits;
        this.type = "";
        this.impact = 0;
    }

    public void setCollisionParameters(String type, int impact) {
        this.type = type;
        this.impact = impact;
    }

    @Override
    public String toString() {
        String outString = "";
        outString += "---------------------------------------------\n";
        outString += base.toString() + " " + type + " with:\n";
        outString += "\n";
        for (Course e : hits) {
            outString += e.toString() + "\n";
        }
        outString += "---------------------------------------------\n";
        return outString;
    }

    public String getTypeSafe(){
        return type == null ? "" : type;
    }
}
