import Util.Logger;

import java.util.ArrayList;

public class Bot{
    String id;
    boolean carrying;
    ArrayList<Mission> tasks = new ArrayList<>();
    BotState botState = BotState.GOING_TO;//starno stanje za bota
    Location location;
    String carrid;
    Location nasledna;



    /*
    ArrayList<Location> pot = new ArrayList<>();


    int steps;

    public synchronized void incSteps(){
        steps++;
    }

    public synchronized int getSteps(){
        return steps;
    }


     */
    public synchronized Location getLocation(){
        return location;
    }

    public synchronized void setLocation(Location l ){
        location = l;
    }

    public synchronized BotState getBotState(){
        return botState;
    }

    public synchronized void setBotState(BotState s){
        botState = s;
    }

    public synchronized boolean isCarrying(){
        return carrying;
    }

    public synchronized void setCarrying(boolean carry) {
        carrying = carry;
    }





}


