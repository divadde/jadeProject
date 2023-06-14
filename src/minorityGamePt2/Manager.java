package minorityGamePt2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Manager extends Agent {

    private int stepSim=0;
    private Observer observer;

    private int numA, numB, replies;

    protected void setup(){
        System.out.println("Manager pronto.");
        Object[] args = getArguments();
        observer = (Observer) args[0];
        addBehaviour(new WaitPlayers());
    }

    private class WaitPlayers extends Behaviour {

        private int replies=0;
        MessageTemplate ready = MessageTemplate.and(MessageTemplate.MatchConversationId("ready"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        @Override
        public void action() {
            ACLMessage reply = myAgent.receive(ready);
            if (reply!=null) {
                replies++;
            }
            else { block(); }
        }

        @Override
        public boolean done() {
            if (replies== Parameters.N) myAgent.addBehaviour(new SendRequest());
            return replies==Parameters.N;
        }
    }


    private class SendRequest extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.setConversationId("asking-players"); //Id della conversazione
            for (int i = 0; i < Parameters.N; i++) { req.addReceiver(new AID("player"+i,AID.ISLOCALNAME)); }
            myAgent.send(req);
            myAgent.addBehaviour(new WaitReplies());
        }
    }


    private class WaitReplies extends Behaviour {
        private MessageTemplate decision = MessageTemplate.and(MessageTemplate.MatchConversationId("decision"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        @Override
        public void action() {
            ACLMessage reply = myAgent.receive(decision);
            if (reply != null) {
                int choice = Integer.parseInt(reply.getContent()); //0==B, 1==A
                if (choice == 1) numA++;
                else numB++;
                replies++;
            } else {
                block();
            }
        }
        public boolean done() {
            if (replies== Parameters.N) myAgent.addBehaviour(new CalculateOutcome());
            return replies== Parameters.N;
        }
    }

    private class CalculateOutcome extends OneShotBehaviour {

        @Override
        public void action() {
            observer.updateSideA(numA);
            observer.updateUtility(numA);
            observer.calculateCommutationPerStep();
            ACLMessage outcome = new ACLMessage(ACLMessage.INFORM);
            for (int i = 0; i < Parameters.N; i++) { outcome.addReceiver(new AID("player"+i,AID.ISLOCALNAME)); }
            int winner=1;
            if (numB<numA) {
                winner=0;
                observer.calculateFsPerStep(numB);
            }
            else {
                observer.calculateFsPerStep(numA);
            }
            outcome.setContent(String.valueOf(winner));
            outcome.setConversationId("outcome-to-players"); //Id della conversazione
            myAgent.send(outcome);
            stepSim++;
            numA=0; numB=0; replies=0;
            System.out.println("Numero simulazione: "+stepSim);
            if (stepSim>= Parameters.T) {
                observer.getSideA();
                observer.getUtilty();
                System.out.println("End simulation: "+stepSim);
                System.out.println("Fs: "+observer.getFinalFs());
                System.out.println("Commutation rate: "+observer.getCommutationRate());
                myAgent.doDelete();
            }
            else {
                myAgent.addBehaviour(new SendRequest());
            }
        }

    }

}
