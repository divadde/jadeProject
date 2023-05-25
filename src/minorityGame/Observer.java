package minorityGame;

import java.util.HashMap;
import java.util.LinkedList;

public class Observer {

    private LinkedList<Integer> sideA = new LinkedList<>();
    private double fs=0;
    private double commutationRate=0;
    private int commutation=0;

    public void updateSideA(int numerosity){
        sideA.addLast(numerosity);
    }
    public void getSideA(){
        System.out.println("---");
        for (Integer i: sideA) System.out.println(i);
        System.out.println("---");
    }
    public void calculateFsPerStep(int winningPlayers){
        fs = fs + ((double) winningPlayers)/((double) Parameters.N);
    }
    public double getFinalFs(){
        return fs/((double) Parameters.T);
    }
    public void playerChoice(int lastStrategy, int currentStrategy){
        if(lastStrategy != currentStrategy)
            commutation++;
    }
    public void calculateCommutationPerStep(){
        commutationRate=commutationRate+(((double) commutation)/((double)Parameters.N));
        commutation=0;
    }
    public double getCommutationRate(){
        return commutationRate/((double) Parameters.T);
    }

}
