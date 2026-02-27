package capstoneSchedulingApp;

import static org.mockito.Answers.valueOf;

import java.util.ArrayList;

public class Course {

    private int courseNumber;        //Class number, ex: CS 1501  
    private int classNumber;            //Unique identifier 
    private int associatedClassNumber;  //Helps identify which classes and recs work
    private meetingPattern days;
    private String startTime;
    private String endTime;
    private String room;
    private String instructor;
    private classType type;
    private int enrollment;

    private ArrayList<Recitation> recitations;
    
    public Course(int courseNumber, int classNumber, int associatedClassNumber,
                    meetingPattern days, String startTime, 
                    String endTime, String room, String instructor, 
                    classType type, int enrollment) {
        this.courseNumber = courseNumber;
        this.classNumber = classNumber;
        this.associatedClassNumber = associatedClassNumber;
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.instructor = instructor;
        this.type = type;
        this.enrollment = enrollment;
        this.recitations = new ArrayList<>();
    }
    
    public meetingPattern getDays() {
        return days;
    }

    public int getStartToInt() {
        //System.out.println(startTime);
        //System.out.println(timeToMinutes(startTime));
        return timeToMinutes(startTime);
    }

    public int getEndToInt() {
        return timeToMinutes(endTime);
    }

    //Time in "XX:XX AM" format
    public int timeToMinutes(String time) {
        String[] hourMin = time.split(":| ");
        int minutesPassedInDay = (Integer.parseInt(hourMin[0]) * 60) + Integer.parseInt(hourMin[1]);
        if ( (hourMin[2].toUpperCase().equals("PM")) && (Integer.parseInt(hourMin[0]) != 12) ){
            minutesPassedInDay += (60 * 12);
        }
        return minutesPassedInDay;
    }

    public String toString() {
        return classNumber + " " + courseNumber + " " + associatedClassNumber + " " + type + " " + instructor + " " + days + " " + startTime + "-" + endTime;
    }
}
