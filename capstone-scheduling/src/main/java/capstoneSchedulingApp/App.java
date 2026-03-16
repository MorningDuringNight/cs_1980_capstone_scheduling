package capstoneSchedulingApp;


public class App {
    public static void main(String[] args) {
        Parser.parseFile("schedule.db","/workspaces/cs_1980_capstone_scheduling/capstone-scheduling/src/main/java/capstoneSchedulingApp/Mock_Schedule_Correct_Classrooms.csv", ",");
        //System.out.print(sched.toString());
    }
}
