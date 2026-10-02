import java.util.ArrayList;
import java.util.Random;

public class Bot {
    String id;
    boolean carrying;
    ArrayList<Mission> tasks = new ArrayList<>();
    BotState botState = BotState.GOING_TO;//starno stanje za bota
    String carrid;
    Location location;
    Location nasledna;

    ArrayList<Location> pot = new ArrayList<>();

    int cakaj = 0;
    Random r = new Random();


    /*Breath First Search Alogirtem generiran z AI. Spodnji del torej ta findPath je generiran z
    pomocjo AI. Probal sem z lastnim alogirtmom ampak se je znaslo da je prislo do ogromno deadlockov.
    Do za Paralelno izdajo bom nasel alogirtem, ki ga bom implementiral sam ter probal pogruntati neko razmerje
    med velikostja grida in st botov, da ne pride do deadlocka.
     */
    ArrayList<Location> findPath(Location start, Location end, boolean[][] zasedn) {
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
                        && !zasedn[nx][ny]) {
                    visited[nx][ny] = true;
                    parent[nx][ny] = current;
                    queue.add(new Location(nx, ny));
                }
            }
        }
        return null;
    }
    //***** DO TE TOCKE JE BILA KODA AI GENERIRANA******



    public void doMove(boolean[][] zasedn) {

        if (cakaj > 0) {
            nasledna = null;
            cakaj--;
            return;
        }


        if (tasks.isEmpty()) {
            botState = BotState.FINITO;
            nasledna = null;
            return;
        }
        Mission mission = tasks.get(0);

        switch (botState) {
            case GOING_TO:
                Location tarca1 = sosed(mission.poberi, zasedn);
                if (pot != null && !pot.isEmpty()) {
                    Location nasledni = pot.get(0);
                    if (zasedn[nasledni.getX()][nasledni.getY()]) {
                        pot = null;
                    }
                }
                if (pot == null || pot.isEmpty()) {
                    pot = findPath(location, tarca1, zasedn);
                }
                if (pot != null && !pot.isEmpty()) {
                    nasledna = pot.remove(0);
                } else {
                    //ta vrstica je bila tudi kasneje dodana z AI, saj nisem mogel ugotoviti zakaj pride do dolocenga deadlocka
                    nasledna = null;
                    cakaj = r.nextInt(10); // dodal sem potem tole sam ko sem pogrtunal da je prsilo tudi se do livelocka
                }
                if (location.getX() == tarca1.getX() && location.getY() == tarca1.getY()) {
                    nasledna = null;
                    pot = null;
                    botState = BotState.PICKING_UP;



                }
                break;

            case PICKING_UP:
                nasledna = null;
                carrying = true;
                GameState.take(this, mission);
                botState = BotState.DROPPING_OFF;
                pot = null;
                break;

            case DROPPING_OFF:
                Location tarca2 = sosed(mission.daj, zasedn);
                if (pot != null && !pot.isEmpty()) {
                    Location nasledni = pot.get(0);
                    if (zasedn[nasledni.getX()][nasledni.getY()]) {
                        pot = null;
                    }
                }
                if (pot == null || pot.isEmpty()) {
                    pot = findPath(location, tarca2, zasedn);
                }
                if (pot != null && !pot.isEmpty()) {
                    nasledna = pot.remove(0);
                }else {
                    nasledna = null;//isto dodana z AI za resevanje tezave, ki je sam nisem ugotvil oz. videl
                    cakaj = r.nextInt(10);
                }
                if (location.getX() == tarca2.getX() && location.getY() == tarca2.getY()) {
                    botState = BotState.PUTING_IN;
                    nasledna = null;
                    pot = null;
                }
                break;

            case PUTING_IN:
                nasledna = null;
                carrying = false;
                GameState.odlozi(this, mission);
                tasks.remove(0);
                pot = null;
                botState = tasks.isEmpty() ? BotState.FINITO : BotState.GOING_TO;
                break;
        }
    }
    public void move(){
        if (nasledna != null){
            location = nasledna;
            nasledna = null;
        }
    }


    Location sosed(Location polica, boolean[][] zasedn){
        //Prvi del tukaj ni bil napisan z AI bil pa je inspiriran iz prejsne AI kode za pathFind
        int[] px = {1,-1,0,0};
        int[] py = {0,0,1,-1};


        if (Math.abs(polica.getX() - location.getX()) + Math.abs(polica.getY() - location.getY()) == 1) {
            return location;
        }

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
                if (!zasedn[pxx][pyy] || (pxx == location.getX() && pyy == location.getY())) {
                    return mozen_plac;//prost sosed
                }
            }
        }
        return prvi_valid;
    }
}



