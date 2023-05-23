package minorityGame;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Player extends Agent {
    //idea: un behavior (calcolo decisione e aggiornamento score) in un cyclic

    private int[] memory;
    private int[][] strategyPool;
    private int[] virtualScore;
    private int realScore=0;
    private int myId;

    private int stepSim=0;

    private Observer observer;

    protected void setup(){
        Object[] args = getArguments();
        myId = (Integer) args[0];
        observer = (Observer)  args[1];
        memory = memoryInitialization();
        strategyPool = strategyPoolInitialization();
        virtualScore = new int[Parameters.S];
        System.out.println("Player "+myId+" pronto.");
        addBehaviour(new PlayerBehaviour());

        /*
        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                myAgent.addBehaviour(new PlayerBehaviour());
                stepSim++;
            }
            @Override
            public boolean done() {
                return stepSim==Parameters.T;
            }
        });
        //doDelete();
         */
    }


    private class PlayerBehaviour extends CyclicBehaviour{
        private int step=0;
        private int myDecision;
        private int myStrategy;

        private MessageTemplate reqTempl, outcomeTempl;

        @Override
        public void action() {
            switch(step){
                case 0: //Receiving request
                    //System.out.println("Player "+myId+" pronto a ricevere la richiesta"); //debug
                    reqTempl = MessageTemplate.and(MessageTemplate.MatchConversationId("asking-players"),
                            MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                    ACLMessage req = myAgent.receive(reqTempl);
                    if (req!=null) {
                        //System.out.println("Player "+myId+" ha ricevuto la richiesta"); //debug
                        step=1;
                    }
                    else {
                        //System.out.println("Ora mi blocco, player "+myId);
                        block(); }
                    //System.out.println("Player "+myId+", step "+step);
                    break;
                case 1: //Send decision
                    ACLMessage decision = new ACLMessage(ACLMessage.INFORM);
                    decision.addReceiver(new AID("manager",AID.ISLOCALNAME));
                    decision.setConversationId("decision"); //Id della conversazione
                    myStrategy = selectBestStrategy();
                    myDecision = takeDecision(myStrategy);
                    if (stepSim==0) observer.myFirstChoice(myId,myDecision);
                    else observer.myChoice(myId,myDecision);
                    decision.setContent(Integer.toString(myDecision));
                    myAgent.send(decision);
                    //System.out.println("Player "+myId+" ha inviato la decisione: "+myDecision); //debug
                    step=2;
                    break;
                case 2: //Receiving outcome
                    outcomeTempl = MessageTemplate.and(MessageTemplate.MatchConversationId("outcome-to-players"),
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                    ACLMessage outcome = myAgent.receive(outcomeTempl);
                    //System.out.println("Provo a ricevere outcome, player "+myId);
                    if (outcome!=null) {
                        update(myDecision,Integer.parseInt(outcome.getContent()),myStrategy);
                        //System.out.println("Player "+myId+"ha ricevuto l'outcome"); //debug
                        step=0;
                        stepSim++;
                        //if (stepSim<Parameters.T) myAgent.addBehaviour(new PlayerBehaviour()); //provo così
                        if (stepSim>=Parameters.T) {
                            System.out.println("Player "+myId+" score: "+realScore);
                            doDelete();
                        }
                        //System.out.println("Player "+myId+" score: "+realScore); //debug
                    }
                    else { block(); }
                    break;
            }
        }

        //@Override
        //public boolean done() {
        //    return step==3;
        //}
    }

    private int[] memoryInitialization() {
        int[] m=new int[Parameters.M];
        for (int i = 0; i < Parameters.M; ++i) {
            m[i] = Math.random() < 0.5 ? 1 : 0;
        }
        return m;
    }

    private int[][] strategyPoolInitialization(){
        int[][] s=new int[Parameters.S][(int)Math.pow(2,Parameters.M)];
        for( int i=0; i<Parameters.S; ++i ) {
            for (int j=0; j<s[i].length; ++j) {
                s[i][j] = Math.random() < 0.5 ? 1 : 0;
            }
        }
        return s;
    }

    //todo verifica correttezza
    private int takeDecision(int strategyChosen){
        int v=0, p;
        for( int i=Parameters.M-1; i>=0; --i ){ //for all bits of m, from M-1 (least significant bit) to 0 (most significant bit)
            p=(int)Math.pow(2,(Parameters.M-1)-i);
            v=v+p*memory[i];
        }
        return strategyPool[strategyChosen][v];
    }

    private int selectBestStrategy(){
        int s = 0;
        int currMax = virtualScore[0];
        for (int i=0; i<virtualScore.length; i++){
            if (currMax<virtualScore[i]) {
                s=i;
                currMax=virtualScore[i];
            }
        }
        return s;
    }

    //0 == B, 1 == A
    private void update(int decision, int outcome, int strategyChosen){
        rightShiftOnMemory();
        memory[0] = outcome;
        if (decision==outcome){
            realScore++;
            virtualScore[strategyChosen]++;
        }
        else {
            virtualScore[strategyChosen]--;
        }
    }

    //todo: da testare
    private void rightShiftOnMemory(){
        for (int i=Parameters.M-2; i>=0; i--) {
            int current = memory[i];
            memory[i+1] = current;
        }
    }

}
