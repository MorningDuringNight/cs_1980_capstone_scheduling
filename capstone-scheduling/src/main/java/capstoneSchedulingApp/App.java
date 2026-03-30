package capstoneSchedulingApp;


public class App {
    public static void main(String[] args) {
        Parser.parseFile("schedule.db","src/main/java/capstoneSchedulingApp/Courses_with_Overlapping_Times.csv", ",");
        Query.queryLecCollision("schedule.db");
    }
}
