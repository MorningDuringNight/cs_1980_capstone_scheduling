package capstoneSchedulingApp;

import static org.mockito.ArgumentMatchers.any;

class Thruple<A, B, C> {

    private final A first;
    private final B second;
    private final C third;

    public Thruple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }
}

//requires AM for capital times
final public class rules {
    public static Thruple<Integer,Integer,Boolean> StringToHour(String timing) {
        int hours = 0;
        int mins = 0;
        Boolean halfer = true; // true is AM false is PM
        
        String[] arr = timing.split(":");
        hours = Integer.valueOf(arr[0]);
        String[] arr2 = arr[1].split(" ");

        mins = Integer.valueOf(arr2[0]);
        if (arr2[1].contains("AM")){
            halfer = true;
        }
        else if(arr2[1].contains("PM")){
            halfer = false;
        }
        Thruple<Integer, Integer, Boolean> myThruple = new Thruple<>(hours, mins, halfer);
        // the idea is that it can go from 0 up to 8

        return myThruple;
    }
    public static int CalculatTimings(Thruple<Integer, Integer, Boolean> Thruple1, Thruple<Integer, Integer, Boolean> Thruple2){

        int save1, save2;
        Boolean save3;
        int startTimeHR =  Thruple1.getFirst();
        int startTimeMin = Thruple1.getSecond();

        int endTimeHR =  Thruple2.getFirst();
        int endTimeMin = Thruple2.getSecond();
        //both AM
        if(Thruple1.getThird() && Thruple2.getThird()){
            save1 = endTimeHR - startTimeHR;
            save2 = endTimeMin - startTimeMin;
        }
        else if (Thruple1.getThird() && !Thruple2.getThird()){
            save1 = endTimeHR - startTimeHR;
            save2 = endTimeMin - startTimeMin;
        }
        return 0;
    }

    // 
}
