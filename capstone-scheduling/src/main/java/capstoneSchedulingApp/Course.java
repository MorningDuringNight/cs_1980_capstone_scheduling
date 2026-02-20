package capstoneSchedulingApp;

import java.util.ArrayList;

public class Course {

    private int courseNumber;        //Class number, ex: CS 1501  
    private int classNumber;            //Unique identifier 
    private int associatedClassNumber;  //Helps identify which classes and recs work
    private Enum<meetingPattern> days;
    private String startTime;
    private String endTime;
    private String room;
    private String instructor;
    private Enum<classType> type;
    private int enrollment;

    private ArrayList<Recitation> recitations;
    
    public Course(int courseNumber, int classNumber, int associatedClassNumber,
                    Enum<meetingPattern> days, String startTime, 
                    String endTime, String room, String instructor, 
                    Enum<classType> type, int enrollment) {
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

    public String toString() {
        return courseNumber + " " + instructor;
    }
}
