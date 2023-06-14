package minorityGame;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;



public class Player extends Agent {
    private int[] memory;
    private int[][] strategyPool;
    private int[] virtualScore = new int[minorityGame.Parameters.S];;
    private int realScore=0;
    private int myId;
    private int stepSim=0;
    private Observer observer;

    private int myDecision;
    private int myStrategy;
    private int lastStrategy;


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

    private class PlayerBehaviour extends Behaviour{
        private MessageTemplate reqTempl= MessageTemplate.and(MessageTemplate.MatchConversationId("asking-players"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        private MessageTemplate outcomeTempl = MessageTemplate.and(MessageTemplate.MatchConversationId("outcome-to-players"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        @Override
        public void action() {
            ACLMessage req = myAgent.receive(reqTempl);
            if (req!=null) {
                ACLMessage decision = new ACLMessage(ACLMessage.INFORM);
                decision.addReceiver(new AID("manager", AID.ISLOCALNAME));
                decision.setConversationId("decision"); //Id della conversazione
                myStrategy = selectBestStrategy();
                myDecision = takeDecision(myStrategy);
                observer.playerChoice(lastStrategy, myStrategy);
                lastStrategy = myStrategy;
                decision.setContent(String.valueOf(myDecision));
                myAgent.send(decision);
                ACLMessage outcome = myAgent.receive(outcomeTempl);
                if (outcome!=null) {
                    update(myDecision, Integer.parseInt(outcome.getContent()), myStrategy);
                    stepSim++;
                }
            }
            else {
                block();
            }
        }
        @Override
        public boolean done() {
            if (stepSim==Parameters.T) {
                myAgent.doDelete();
            }
            return stepSim == Parameters.T;
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

    private int selectBestStrategy(){
        int s = 0;
        int currMax = virtualScore[s];
        for (int i=0; i<virtualScore.length; i++){
            if (currMax<virtualScore[i]) {
                s=i;
                currMax=virtualScore[i];
            }
        }
        return s;
    }

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


    //Versione alternativa con i Behaviour
    /*
    private class TakeDecision extends Behaviour {
        private MessageTemplate reqTempl= MessageTemplate.and(MessageTemplate.MatchConversationId("asking-players"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        private boolean doneAction=false;

        @Override
        public void action() {
            ACLMessage req = myAgent.receive(reqTempl);
            if (req!=null) {
                ACLMessage decision = new ACLMessage(ACLMessage.INFORM);
                decision.addReceiver(new AID("manager", AID.ISLOCALNAME));
                decision.setConversationId("decision"); //Id della conversazione
                myStrategy = selectBestStrategy();
                myDecision = takeDecision(myStrategy);
                observer.playerChoice(lastStrategy, myStrategy);
                lastStrategy = myStrategy;
                decision.setContent(String.valueOf(myDecision));
                myAgent.send(decision);
                doneAction=true;
            }
            else {
                block();
            }
        }

        @Override
        public boolean done() {
            if (doneAction) {
                myAgent.addBehaviour(new ReadResults());
            }
            return doneAction;
        }

    }

    private class ReadResults extends Behaviour{
        private MessageTemplate outcomeTempl = MessageTemplate.and(MessageTemplate.MatchConversationId("outcome-to-players"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        private boolean doneAction=false;

        @Override
        public void action() {
            ACLMessage outcome = myAgent.receive(outcomeTempl);
            if (outcome!=null) {
                update(myDecision,Integer.parseInt(outcome.getContent()),myStrategy);
                stepSim++;
                doneAction=true;
            }
            else { block(); }
        }

        @Override
        public boolean done() {
            if (doneAction) {
                myAgent.addBehaviour(new TakeDecision());
            }
            if (stepSim>=Parameters.T && doneAction) {
                myAgent.doDelete();
            }
            return doneAction;
        }
    }
     */

}
