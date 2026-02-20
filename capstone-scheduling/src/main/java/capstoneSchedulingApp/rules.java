package capstoneSchedulingApp;

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
public class rules {
    static Thruple<Integer,Integer,Boolean> StringToHour(String timing) {
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
}
