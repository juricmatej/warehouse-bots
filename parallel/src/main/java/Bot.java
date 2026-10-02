import Util.Logger;

import java.util.ArrayList;

public class Bot implements  Runnable{
    String id;
    boolean carrying;
    ArrayList<Mission> tasks = new ArrayList<>();
    BotState botState = BotState.GOING_TO;//starno stanje za bota
    String carrid;
    Location location;
    Location nasledna;



    ArrayList<Location> pot = new ArrayList<>();

    int steps;

    public synchronized void incSteps(){
        steps++;
    }

    public synchronized int getSteps(){
        return steps;
    }

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

    /*Breath First Search Alogirtem generiran z AI. Spodnji del torej ta findPath je generiran z
    pomocjo AI. Probal sem z lastnim alogirtmom ampak se je znaslo da je prislo do ogromno deadlockov.

     */
    //jaz dodelov za parralle vesrion
    ArrayList<Location> findPath(Location start, Location end) {
        Location[][] parent = new Location[GameState.pravi_width][GameState.pravi_height];
        boolean[][] visited = new boolean[GameState.pravi_width][GameState.pravi_height];

        ArrayList<Location> queue = new ArrayList<>();
        queue.add(start);
        visited[start.getX()][start.getY()] = true;

        while (!queue.isEmpty()) {
            Location current = queue.remove(0);
            if (current.getX() == end.getX() && current.getY() == end.getY()) {
                ArrayList<Location> pot = new ArrayList<>();
                Location c = end;
                while (!(c.getX() == start.getX() && c.getY() == start.getY())) {
                    pot.add(0, c);
                    c = parent[c.getX()][c.getY()];
                }
                return pot;
            }
            int[] dx = {1, -1, 0, 0};
            int[] dy = {0, 0, 1, -1};
            for (int i = 0; i < 4; i++) {
                int nx = current.getX() + dx[i];
                int ny = current.getY() + dy[i];
                if (nx >= 0 && ny >= 0
                        && nx < GameState.pravi_width
                        && ny < GameState.pravi_height
                        && !visited[nx][ny]
                        && !(nx % 2 != 0 && ny % 2 != 0)
                        && GameState.jeProsto(new Location(nx, ny), this)) {
                    visited[nx][ny] = true;
                    parent[nx][ny] = current;
                    queue.add(new Location(nx, ny));
                }
            }
        }
        return null;
    }
    //***** DO TE TOCKE JE BILA KODA AI GENERIRANA******



    public void doMove() {
        if (tasks.isEmpty()) {
            setBotState(BotState.FINITO);
            nasledna = null;

            GameState.clear(getLocation(), this);
            return;
        }
        Mission mission = tasks.get(0);

        switch (getBotState()) {
            case GOING_TO:
                Location tarca1 = sosed(mission.poberi);
                if (pot == null || pot.isEmpty()) {
                    pot = findPath(getLocation(), tarca1);
                    if (pot == null) {
                        Logger.warn(id + " blokiran, tarca " + tarca1.getX() + "," + tarca1.getY() + " drzi: " + (GameState.rezervirano[tarca1.getX()][tarca1.getY()] == null ? "nihce" : GameState.rezervirano[tarca1.getX()][tarca1.getY()].id));
                    }
                }
                if (pot != null && !pot.isEmpty()) {
                    Location mybe = pot.get(0);
                    if (GameState.rezerviraj(mybe, this)) {
                        nasledna = pot.remove(0);
                    } else {
                        nasledna = null;
                        pot = null;
                    }

                }
                if (getLocation().getX() == tarca1.getX() && getLocation().getY() == tarca1.getY()) {
                    nasledna = null;
                    pot = null;
                    setBotState(BotState.PICKING_UP);



                }
                break;

            case PICKING_UP:
                nasledna = null;
                setCarrying(true);
                GameState.take(this, mission);
                setBotState(BotState.DROPPING_OFF);
                pot = null;
                break;

            case DROPPING_OFF:
                Location tarca2 = sosed(mission.daj);
                if (pot == null || pot.isEmpty()) {
                    pot = findPath(getLocation(), tarca2);
                    if(pot == null) {
                        Logger.warn(id + " blokiran, tarca " + tarca2.getX() + "," + tarca2.getY() + " drzi: " + (GameState.rezervirano[tarca2.getX()][tarca2.getY()] == null ? "nihce" : GameState.rezervirano[tarca2.getX()][tarca2.getY()].id));
                    }
                }
                if (pot != null && !pot.isEmpty()) {
                    Location mybe = pot.get(0);
                    if (GameState.rezerviraj(mybe, this)) {
                        nasledna = pot.remove(0);
                    } else { // tile ma nekdo drug, bot stoji en tick
                        nasledna = null;
                        pot = null;
                    }

                }
                if (getLocation().getX() == tarca2.getX() && getLocation().getY() == tarca2.getY()) {
                    setBotState(BotState.PUTING_IN);
                    nasledna = null;
                    pot = null;
                }
                break;

            case PUTING_IN:
                nasledna = null;
                setCarrying(false);
                GameState.odlozi(this, mission);
                tasks.remove(0);
                pot = null;
                setBotState(tasks.isEmpty() ? BotState.FINITO : BotState.GOING_TO);
                break;
        }
    }
    public void move(){
        if (nasledna != null){

            Location stara = getLocation();
            setLocation(nasledna);
            nasledna = null;
            incSteps();

            GameState.clear(stara, this);
        }
    }


    Location sosed(Location polica){
        //Prvi del tukaj ni bil napisan z AI bil pa je inspiriran iz prejsne AI kode za pathFind
        int[] px = {1,-1,0,0};
        int[] py = {0,0,1,-1};

        Location prvi_valid = null;
        for (int i = 0; i < 4; i++) {
            int pxx = polica.getX() + px[i];
            int pyy = polica.getY() + py[i];
            Location mozen_plac = new  Location(pxx, pyy);
            //malo dolgo ampak sam preveri ali je izven mape in ali je polica
            if (!(mozen_plac.getX() < 0 || mozen_plac.getY() < 0 || mozen_plac.getX() >= GameState.pravi_width || mozen_plac.getY() >= GameState.pravi_height || (mozen_plac.getX() % 2 != 0 && mozen_plac.getY() % 2 != 0)))  {
                if (prvi_valid == null) {
                    prvi_valid = mozen_plac;
                }
                if (GameState.jeProsto(mozen_plac, this)) {
                    return mozen_plac;
                }
            }
        }
        return prvi_valid;
    }

    @Override
    public void run() {
        while (getBotState() != BotState.FINITO){
            doMove();
            move();
            try {
                Thread.sleep(10);
            }
            catch (InterruptedException e){
                return;
            }

        }


        GameState.clear(getLocation(), this);


        Logger.info(id + " finished in: " + getSteps());
    }
}



