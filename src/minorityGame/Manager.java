package minorityGame;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Manager extends Agent {
    //idea: un behavior a step sequenziali (chiedo, aspetto scelta, comunico outcome) in un behaviour a 1000 steps (più messaggio finale ai giocatori?)
    //behaviour classes come inner classes
    //un solo thread che gestisce tutti i behaviour
    //metodo action() di un behavior deve essere atomico, (no cicli while true) ogni azione deve avere un block()
    //diverse tipologie di behavior (one-shot, cyclic, generic, waker, ticker)
    //sfruttare il metodo receive (ACLMessage)
    //usare un array di giocatori (AID)? No, prova a riferirti col nome

    private int stepSim=0;
    private Observer observer;

    protected void setup(){
        System.out.println("Manager pronto.");
        Object[] args = getArguments();
        observer = (Observer) args[0];
        addBehaviour(new CommunicationBehaviour());
        /*
        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                //System.out.println("Step numero: "+stepSim);
                myAgent.addBehaviour(new CommunicationBehaviour());
                stepSim++;
            }
            @Override
            public boolean done() {
                return stepSim==Parameters.T;
            }
        });
         */
        //doDelete
    }

    private class CommunicationBehaviour extends CyclicBehaviour {
        private int step=0;
        private int replies=0;
        private int numA=0; //se arriva 1 (A)
        private int numB=0; //se arriva 0 (B)
        private MessageTemplate decision;

        @Override
        public void action() {
            switch(step){
                case 0: //Ask to players
                    //System.out.println("Manager asks to player"); //debug
                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                    for (int i=0; i < Parameters.N; i++) {
                        //System.out.println(i);
                        System.out.println("Aggiungo ricevitore: player"+i);
                        AID player = new AID(("player"+i),AID.ISLOCALNAME);
                        req.addReceiver(player);
                    }
                    req.setConversationId("asking-players"); //Id della conversazione
                    myAgent.send(req);
                    decision = MessageTemplate.and(MessageTemplate.MatchConversationId("decision"),
                                                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                    step=1;
                    break;
                case 1: //Read decision of players
                    //System.out.println("Manager read decisions"); //debug
                    ACLMessage reply = myAgent.receive(decision);
                    if (reply!=null) {
                        //System.out.println("Manager read decisions"); //debug
                        int choice = Integer.parseInt(reply.getContent());; //1==A, 0==B
                        if (choice==0) numB++;
                        else numA++;
                        replies++;
                        //System.out.println("Numero replies "+replies+ " allo step "+stepSim);
                        if (replies==Parameters.N) step=2;
                    }
                    else { block(); }
                    break;
                case 2: //Send players outcome (winner choice)
                    //System.out.println("Manager communicate outcome"); //debug
                    observer.updateSideA(numA);
                    observer.calculateCommutationPerStep();
                    int winner=1;
                    if (numB<numA) {
                        winner=0;
                        observer.calculateFsPerStep(numB);
                    }
                    else {
                        observer.calculateFsPerStep(numA);
                    }
                    ACLMessage outcome = new ACLMessage(ACLMessage.INFORM);
                    for (int i=0; i < Parameters.N; i++) { outcome.addReceiver(new AID("player"+i,AID.ISLOCALNAME)); }
                    outcome.setContent(Integer.toString(winner));
                    outcome.setConversationId("outcome-to-players"); //Id della conversazione
                    myAgent.send(outcome);
                    //System.out.println("End of the step");
                    stepSim++;
                    numA=0;
                    numB=0;
                    //if (stepSim<=Parameters.T) myAgent.addBehaviour(new CommunicationBehaviour()); //provo così
                    System.out.println("Numero simulazione: "+stepSim);
                    if (stepSim>=Parameters.T) {
                        System.out.println("End simulation: "+stepSim);
                        System.out.println("Fs: "+observer.getFinalFs());
                        System.out.println("Commutation rate: "+observer.getCommutationRate());
                        observer.getSideA();
                        doDelete();
                    }
                    replies=0;
                    step=0;
                    break;
            }
        }

        /*
        @Override
        public boolean done() {
            return step==3;
        }
        */

    }

}
