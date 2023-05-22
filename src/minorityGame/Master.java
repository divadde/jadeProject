package minorityGame;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

//Il master si occupa di creare manager e giocatori e di avviarli.
public class Master extends Agent {

    protected void setup(){
        System.out.println("Creazione del manager e dei giocatori.");
        ContainerController cc = null;
        AgentController ac = null;
        try {
            cc = getContainerController();
            ac = cc.createNewAgent("manager","minorityGame.Manager",null);
            ac.start();
            for (int i=0; i<Parameters.N; i++){
                ac = cc.createNewAgent("player"+i,"minorityGame.Player",new Object[]{i});
                ac.start();
            }
        } catch (Exception e) {
            System.out.println("Errore durante la creazione di manager e giocatori.");
        }
        System.out.println("Creazione del manager e dei giocatori andata a buon fine, bye by Master.");
        doDelete();
    }

}
