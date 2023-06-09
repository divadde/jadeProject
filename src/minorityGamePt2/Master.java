package minorityGamePt2;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


//Il master si occupa di creare manager e giocatori e di avviarli.
public class Master extends Agent {

    protected void setup(){
        System.out.println("Creazione del manager e dei giocatori.");
        ContainerController cc;
        AgentController ac;
        Observer observer = new Observer();
        try {
            cc = getContainerController();
            ac = cc.createNewAgent("manager","minorityGamePt2.Manager",new Object[]{observer});
            ac.start();
            for (int i = 0; i< Parameters.N; i++){
                ac = cc.createNewAgent("player"+i,"minorityGamePt2.Player",new Object[]{i,observer});
                ac.start();
            }
        } catch (Exception e) {
            System.out.println("Errore durante la creazione di manager e giocatori.");
        }
        System.out.println("Creazione del manager e dei giocatori andata a buon fine, bye by Master.");
        doDelete();
    }

}
