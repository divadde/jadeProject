package minorityGame;

import java.util.HashMap;
import java.util.LinkedList;

public class Observer {

    private LinkedList<Integer> sideA;
    private double fs=0;
    private HashMap<Integer,Integer> choices;
    private int commutation=0;
    private double commutationRate=0;

    public Observer(){
        choices = new HashMap<>();
        sideA = new LinkedList<>();
    }

    public void updateSideA(int numerosity){
        sideA.addLast(numerosity);
    }
    public void getSideA(){
        System.out.print("<");
        for (Integer i: sideA) System.out.print(i+",");
        System.out.print(">");
    }
    public void calculateFsPerStep(int winningPlayers){
        fs = fs + ((double) winningPlayers)/((double) Parameters.N);
    }

    public double getFinalFs(){
        return fs/((double) Parameters.T);
    }

    public void myFirstChoice(int id, int choice){
        choices.put(id,choice);
    }

    public void myChoice(int id, int choice){
        int lastChoice = choices.get(id);
        if (choice!=lastChoice) commutation++;
        choices.replace(id,choice);
    }

    public void calculateCommutationPerStep(){
        commutationRate=commutationRate+(((double) commutation)/((double)Parameters.N));
        commutation=0;
    }

    public double getCommutationRate(){
        return commutationRate/((double) Parameters.T);
    }

}
