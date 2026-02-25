package capstoneSchedulingApp;

import java.util.ArrayList;

import javax.swing.plaf.TreeUI;

import capstoneSchedulingApp.rules;
class Tuple<A, B> {

    private final A first;
    private final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}

public class Society extends ArrayList{
    private int AssClass;  // associated class number
    private int CoarseNumber; // course number
    public ArrayList<Tuple<Integer, Integer>>  lookDown  =  new ArrayList<Tuple<Integer,Integer>>();

    public ArrayList<Society> SocList = new ArrayList<Society>();
    //Thruple contains Associated class (ass)
    //coarse numbers
    //arraylist of truple contains tuple of data struct lookup

    public Society(int AssClass, int CoarseNumber){
        this.AssClass = AssClass;
        this.CoarseNumber = CoarseNumber;
    }

    public Society(int AssClass, int CoarseNumber, Tuple<Integer,Integer> look){
        this.AssClass = AssClass;
        this.CoarseNumber = CoarseNumber;
        this.lookDown.add(look);
    }

    public int getAss(){
        return this.AssClass;
    }

    public int getCoar(){
        return this.CoarseNumber;
    }
    // if false the coarse and class are already in the society
    //else if not
    public Boolean AssCoar(int AssClass, int CoarseNumber){

        int total = lastIndexOf(SocList);
        for(int i = 0; i < total; i++){
            if (SocList.get(i).getAss() == AssClass && SocList.get(i).getCoar() == CoarseNumber) {
                return false;
            }
        }

        return true;
    }

    public int IndexHave(int AssClass, int CoarseNumber){
        int index = -1;
        int total = lastIndexOf(SocList);
        for(int i = 0; i < total; i++){
            if (SocList.get(i).getAss() == AssClass && SocList.get(i).getCoar() == CoarseNumber) {
                return i;
            }
        }

        return index;
    }

    public void addLookDown(Tuple<Integer, Integer> maker){
        lookDown.add(maker);
    }

    // intesion give the integer
    public void putLookup(int a, int b) {
        Tuple<Integer,Integer> putter = new Tuple(a, b);
        
        if(AssCoar(this.AssClass,this.CoarseNumber)){
            Society soc = new Society(this.AssClass, this.CoarseNumber, putter);
            SocList.add(soc);
        }
        else {
            int checker = IndexHave(this.AssClass, this.CoarseNumber);
            if (checker != -1){
                SocList.get(checker).addLookDown(putter);
            }
        }
    }

    public ArrayList<Tuple<Integer, Integer>> getLookDown(int asso, int coaro){
        int checker = IndexHave(this.AssClass, this.CoarseNumber);
        if (checker != -1) {
           return SocList.get(checker).getLookDown(asso, coaro);
        }
        return null;
    }

}
