package minorityGame;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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

    //todo: inserire aid per il manager?
    protected void setup(){
        System.out.println("Manager pronto.");
        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                System.out.println("Step numero: "+stepSim);
                myAgent.addBehaviour(new CommunicationBehaviour());
            }
            @Override
            public boolean done() {
                return stepSim==Parameters.T;
            }
        });
        //doDelete();
    }

    private class CommunicationBehaviour extends Behaviour{
        private int step=0;
        private int replies=0;
        private int numA=0; //se arriva 1 (A)
        private int numB=0; //se arriva 0 (B)
        private MessageTemplate decision;

        @Override
        public void action() {
            switch(step){
                case 0: //Ask to players
                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                    for (int i=0; i < Parameters.N; i++) { req.addReceiver(new AID("player"+i,AID.ISLOCALNAME)); }
                    req.setConversationId("asking-players"); //Id della conversazione
                    req.setReplyWith("req"+System.currentTimeMillis()); //todo: è giusto?
                    myAgent.send(req);
                    decision = MessageTemplate.and(MessageTemplate.MatchConversationId("decision"),
                            MessageTemplate.MatchInReplyTo(req.getReplyWith()));
                    step=1;break;
                case 1: //Read decision of players
                    ACLMessage reply = myAgent.receive(decision);
                    if (reply!=null) {
                        if (reply.getPerformative()==ACLMessage.INFORM) { //todo (capire meglio cosa succede se il performative non è corretto)
                            int choice = Integer.parseInt(reply.getContent());; //1==A, 0==B
                            if (choice==0) numB++;
                            else numA++;
                        }
                        replies++;
                        if (replies==Parameters.N) { step=2; }
                    }
                    else { block(); }
                    break;
                case 2: //Send players outcome (winner choice)
                    int winner=1;
                    if (numB<numA) winner=0;
                    ACLMessage outcome = new ACLMessage(ACLMessage.INFORM);
                    for (int i=0; i < Parameters.N; i++) { outcome.addReceiver(new AID("player"+i,AID.ISLOCALNAME)); }
                    outcome.setContent(Integer.toString(winner));
                    outcome.setConversationId("outcome-to-players"); //Id della conversazione
                    outcome.setReplyWith("outcome"+System.currentTimeMillis()); //todo: è giusto?
                    System.out.println("End of the step");
                    stepSim++;
                    step=3;break;
            }
        }

        @Override
        public boolean done() {
            return step==3;
        }

    }

}
