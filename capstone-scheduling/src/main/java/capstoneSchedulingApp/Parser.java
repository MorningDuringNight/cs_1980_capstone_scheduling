package capstoneSchedulingApp;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    
    public static Schedule parseFile(String fileName, String delin) {
        Schedule sched = new Schedule(5, 480, 1200, 5);
        // Basic parsing
        try (Scanner scanner = new Scanner(new File(fileName))) {
            scanner.nextLine(); // Skip over label line, may want to parse from it later
            while (scanner.hasNextLine()) {
                String[] lineArray = scanner.nextLine().split(delin); // We can *not* hard-code later

                if (!lineArray[5].equals("ByAppt")) {
                    //Super hard coded, might parse correct columns from label line
                    Course temp = new Course(Integer.parseInt(lineArray[1]), Integer.parseInt(lineArray[3]),
                                         Integer.parseInt(lineArray[4]), meetingPattern.valueOf(lineArray[5]), 
                                         lineArray[6], lineArray[7], lineArray[8], lineArray[9], classType.valueOf(lineArray[13]),
                                         Integer.parseInt(lineArray[15]));
                    sched.add(temp);
                    //System.out.println(temp);
                }
            }
            return sched;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }
}
