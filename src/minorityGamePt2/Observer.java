package minorityGamePt2;

import java.util.LinkedList;

public class Observer {

    private final double Am = ((double) (Parameters.N-1))/2.0;
    private LinkedList<Integer> sideA = new LinkedList<>();
    private LinkedList<Double> utilityList = new LinkedList<>();

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
        return commutationRate/((double)Parameters.T);
    }

    public void updateUtility(int numerosity) {
        double utility =( (double) ( (1-heaviside(numerosity))*numerosity+heaviside(numerosity)*(Parameters.N-numerosity) ) )/Am;
        utilityList.addLast(utility);
    }
    private int heaviside(int numerosity) {
        if (numerosity<=Am) return 0;
        return 1;
    }
    public void getUtilty(){
        System.out.println("---");
        for (Double i: utilityList) System.out.println(i);
        System.out.println("---");
    }

}
