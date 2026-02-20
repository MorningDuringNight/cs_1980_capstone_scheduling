package capstoneSchedulingApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {

        ArrayList<Course> sched = new ArrayList<>();
        // Basic parsing
        try (Scanner scanner = new Scanner(new File("sched.csv"))) {
            scanner.nextLine(); // Skip over label line
            while (scanner.hasNextLine()) {
                String[] lineArray = scanner.nextLine().split("\t"); // We can not hard-code later

                if (!lineArray[5].equals("ByAppt")) {
                    //Super hard coded, might parse correct columns from label line
                    Course temp = new Course(Integer.parseInt(lineArray[1]), Integer.parseInt(lineArray[3]),
                                         Integer.parseInt(lineArray[4]), meetingPattern.valueOf(lineArray[5]), 
                                         lineArray[6], lineArray[7], lineArray[8], lineArray[9], classType.valueOf(lineArray[13]),
                                         Integer.parseInt(lineArray[15]));
                    sched.add(temp);
                    System.out.println(temp);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
