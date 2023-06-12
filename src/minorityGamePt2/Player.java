package minorityGamePt2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Collections;

public class Player extends Agent {
    private int[] memory;
    private int[][] strategyPool;
    private int[] virtualScore = new int[Parameters.S];;
    private int realScore=0;
    private int myId;
    private int stepSim=0;
    private Observer observer;


    protected void setup(){
        Object[] args = getArguments();
        myId = (Integer) args[0];
        observer = (Observer) args[1];
        memory = memoryInitialization();
        strategyPool = strategyPoolInitialization();
        ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
        ready.addReceiver(new AID("manager",AID.ISLOCALNAME));
        ready.setConversationId("ready");
        send(ready);
        addBehaviour(new PlayerBehaviour());
    }


    private class PlayerBehaviour extends CyclicBehaviour{
        private int myDecision;
        private int myStrategy;
        private int lastStrategy;
        private MessageTemplate reqTempl= MessageTemplate.and(MessageTemplate.MatchConversationId("asking-players"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        private MessageTemplate outcomeTempl = MessageTemplate.and(MessageTemplate.MatchConversationId("outcome-to-players"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        @Override
        public void action() {
            ACLMessage req = myAgent.receive(reqTempl);
            if (req!=null) {
                ACLMessage decision = new ACLMessage(ACLMessage.INFORM);
                decision.addReceiver(new AID("manager",AID.ISLOCALNAME));
                decision.setConversationId("decision"); //Id della conversazione
                myStrategy = selectBestStrategy(-1);
                myDecision = takeDecision(myStrategy);
                observer.playerChoice(lastStrategy, myStrategy);
                //System.out.println("player "+myId+" take decision: "+ choice +", at step "+stepSim);
                lastStrategy = myStrategy;
                decision.setContent(String.valueOf(myDecision));
                myAgent.send(decision);
                ACLMessage outcome = myAgent.receive(outcomeTempl);
                //System.out.println("Provo a ricevere outcome, player "+myId);
                if (outcome!=null) {
                    update(myDecision,Integer.parseInt(outcome.getContent()),myStrategy);
                    stepSim++;
                    if (stepSim>=Parameters.T) {
                        doDelete();
                    }
                    if ((stepSim%Parameters.tau)==0) { //se è tempo di adattamento
                        if ( ( ((double)realScore)/((double)stepSim) ) < Parameters.nFraction){ //se sono nella frazione perdente
                            updateStrategy(Parameters.schema);
                        }
                    }
                    //System.out.println("player "+myId+" memory after update: "+ Arrays.toString(memory) +", step "+stepSim);
                    //System.out.println("player "+myId+" strategies after update: "+ Arrays.toString(virtualScore) +", step "+stepSim);
                    //System.out.println("player "+myId+" score: "+ realScore +", step "+stepSim);
                    //System.out.println("Player "+myId+"ha ricevuto l'outcome "+updateValue); //debug
                    //System.out.println("Player "+myId+" score: "+realScore); //debug
                }
                else { block(); }
            }
            else { block(); }
        }


    }


    private int[] memoryInitialization() {
        int[] m=new int[Parameters.M];
        for (int i = 0; i < Parameters.M; ++i) {
            m[i] = (Math.random() < 0.5) ? 1 : 0;
        }
        return m;
    }

    private int[][] strategyPoolInitialization(){
        int[][] s=new int[Parameters.S][(int)Math.pow(2, Parameters.M)];
        for(int i = 0; i< Parameters.S; ++i ) {
            for (int j=0; j<s[i].length; ++j) {
                s[i][j] = (Math.random() < 0.5)? 1 : 0;
            }
        }
        return s;
    }

    private int takeDecision(int strategyChosen){
        int v=0, p;
        for(int i = Parameters.M-1; i>=0; --i ){ //for all bits of m, from M-1 (least significant bit) to 0 (most significant bit)
            p=(int)Math.pow(2,(Parameters.M-1)-i);
            v=v+p*memory[i];
        }
        return strategyPool[strategyChosen][v];
    }

    //todo: da verificare
    private int selectBestStrategy(int strategyExcluded){
        int s = 0;
        int currMax = virtualScore[s];
        for (int i=0; i<virtualScore.length; i++){
            if (i!=strategyExcluded && currMax<virtualScore[i]) {
                s=i;
                currMax=virtualScore[i];
            }
        }
        return s;
    }

    //0 == B, 1 == A
    private void update(int decision, int outcome, int strategyChosen){
        for (int i = Parameters.M-1; i>0; i--) { //right-shift
            memory[i] = memory[i-1];
        }
        memory[0] = outcome;
        if (decision==outcome){
            realScore++;
            virtualScore[strategyChosen]++;
        }
        else {
            virtualScore[strategyChosen]--;
        }
    }

    //todo: da verficiare
    private int[][] createChildren(int[] mother1, int[] mother2){
        int strategyDim = (int)Math.pow(2, Parameters.M);
        int[][] ret = new int[2][strategyDim];
        int crossOverPoint =(int) Math.floor(Math.random() * (strategyDim + 1));
        for(int i = 0; i<strategyDim; ++i ) {
            if (i<crossOverPoint) {
                ret[0][i] = mother2[i];
                ret[1][i] = mother1[i];
            }
            else {
                ret[0][i] = mother1[i];
                ret[1][i] = mother2[i];
            }
        }
        return ret;
    }

    //todo da verificare
    private void updateStrategy(String schema){
        //Seleziono strategie madre
        int motherS1, motherS2;
        if (schema=="A" || schema=="B"){
            ArrayList<Integer> list = new ArrayList();
            for (int i=1; i<Parameters.S; i++) list.add(i);
            Collections.shuffle(list);
            motherS1 = list.get(0);
            motherS2 = list.get(1);
        }
        else {
            motherS1 = selectBestStrategy(-1);
            motherS2 = selectBestStrategy(motherS1);
        }
        //creo i figli
        int[][] children = createChildren(strategyPool[motherS1], strategyPool[motherS2]);
        //sostituisco strategie per i figli
        int worstS1, worstS2;
        if (schema=="A" || schema=="C"){ //vengono sostituite le madri
            worstS1 = motherS1; worstS2 = motherS2;
        }
        else { //vengono sostituite le peggiori strategie
            worstS1 = selectWorstStrategy(-1);
            worstS2 = selectWorstStrategy(worstS1);
        }
        strategyPool[worstS1] = children[0];
        strategyPool[worstS2] = children[1];
        virtualScore[worstS1] = 0;
        virtualScore[worstS2] = 0;
    }

    //todo: da verificare
    private int selectWorstStrategy(int strategyExcluded){
        int s = 0;
        int currMin = virtualScore[s];
        for (int i=0; i<virtualScore.length; i++){
            if (i!=strategyExcluded && currMin>virtualScore[i]) {
                s=i;
                currMin=virtualScore[i];
            }
        }
        return s;
    }

}
