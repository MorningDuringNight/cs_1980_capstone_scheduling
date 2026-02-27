package capstoneSchedulingApp;


public class App {
    public static void main(String[] args) {
        Schedule sched = Parser.parseFile("capstone-scheduling\\src\\main\\java\\capstoneSchedulingApp\\Mock_Schedule_Correct_Classrooms.csv", "\t");
        System.out.print(sched.toString());
    }
}
