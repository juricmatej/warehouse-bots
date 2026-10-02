import Util.Logger;
import mpi.MPI;

import java.util.ArrayList;

public class BotMain {

    static int rank;
    static String id;
    static int sirina;//pravi_width
    static int visina;//pravi_height
    static Location location;
    static Location nasledna;
    static BotState botState = BotState.GOING_TO;
    static int steps;
    static int[] rezerved;


    static ArrayList<Mission> tasks = new ArrayList<>();
    static ArrayList<Location> pot = new ArrayList<>();


    public static void go(int rank2) throws Exception {
        rank = rank2;


        id = "B" + rank2;
        Thread.currentThread().setName(id);

        int[] header = new int[5];
                            //kam  prvi koliko   tip     //od koga     //kaksej prookol1
        MPI.COMM_WORLD.Recv(header, 0,    5,     MPI.INT,    0,        Protokol.INIT);
        location = new Location(header[0], header[1]);
        sirina = header[2];
        visina = header[3];
        int stMisij = header[4];


        int[] taskiRaw = new int[stMisij * 6];
        MPI.COMM_WORLD.Recv(taskiRaw, 0, taskiRaw.length, MPI.INT, 0, Protokol.INIT);
        for (int j = 0; j < stMisij; j++) {
            Mission m = new Mission();
            m.poberi = new Location(taskiRaw[j * 6], taskiRaw[j * 6 + 1]);
            m.poberiPolica = taskiRaw[j * 6 + 2];
            m.daj = new Location(taskiRaw[j * 6 + 3], taskiRaw[j * 6 + 4]);
            m.dajPolica = taskiRaw[j * 6 + 5];
            tasks.add(m);
        }
        Logger.info(id + " dobil " + stMisij + " taskov, start: " + location.getX() +  location.getY());


        while (botState != BotState.FINITO) {
            doMove();
            move();
            Thread.sleep(10);
        }

        askMastr(Protokol.FINITO, 0, 0);

        int[] sendbuf = {steps};
        int[] recvbuf = new int[1];
        MPI.COMM_WORLD.Reduce(sendbuf, 0, recvbuf, 0, 1, MPI.INT, MPI.SUM, 0);

        //Logger.error(id + " koncal: " + steps + " korakov");
    }

    static int askMastr(int ukaz, int x, int y) {
        int[] req = {ukaz, x, y};
        MPI.COMM_WORLD.Send(req, 0, 3, MPI.INT, 0, Protokol.REQ);
        int[] odg = new int[1];
        MPI.COMM_WORLD.Recv(odg, 0, 1, MPI.INT, 0, Protokol.ODG);
        return odg[0];
    }

    static void refresh() {
        int[] req = {Protokol.REZERVACIJE, 0, 0};
        MPI.COMM_WORLD.Send(req, 0, 3, MPI.INT, 0, Protokol.REQ);
        rezerved = new int[sirina * visina];
        MPI.COMM_WORLD.Recv(rezerved, 0, rezerved.length, MPI.INT, 0, Protokol.ODG);
    }

    static boolean jeProsto(int x, int y) {
        return rezerved[x * visina + y] == 0 || rezerved[x * visina + y] == rank;
    }

    static void doMove() {
        if (tasks.isEmpty()) {
            botState = BotState.FINITO;
            nasledna = null;
            return;
        }


        Mission mission = tasks.get(0);
        refresh();//refresha mapo


        switch (botState) {
            case GOING_TO:
                Location tarca1 = sosed(mission.poberi);
                if (pot == null || pot.isEmpty()) {
                    pot = findPath(location, tarca1);
                }
                if (pot != null && !pot.isEmpty()) {
                    Location mybe = pot.get(0);
                    if (askMastr(Protokol.REZERV, mybe.getX(), mybe.getY()) == 1) {
                        nasledna = pot.remove(0);
                    } else {
                        nasledna = null;
                        pot = null;
                    }
                }
                if (location.getX() == tarca1.getX() && location.getY() == tarca1.getY()) {
                    nasledna = null;
                    pot = null;
                    botState = BotState.PICKING_UP;
                }
                break;

            case PICKING_UP:
                nasledna = null;
                askMastr(Protokol.GOTIT, 0, 0);
                botState = BotState.DROPPING_OFF;
                pot = null;
                break;

            case DROPPING_OFF:
                Location tarca2 = sosed(mission.daj);
                if (pot == null || pot.isEmpty()) {
                    pot = findPath(location, tarca2);
                }
                if (pot != null && !pot.isEmpty()) {
                    Location mybe = pot.get(0);
                    if (askMastr(Protokol.REZERV, mybe.getX(), mybe.getY()) == 1) {
                        nasledna = pot.remove(0);
                    } else {
                        nasledna = null;
                        pot = null;
                    }
                }
                if (location.getX() == tarca2.getX() && location.getY() == tarca2.getY()) {
                    botState = BotState.PUTING_IN;
                    nasledna = null;
                    pot = null;
                }
                break;

            case PUTING_IN:
                nasledna = null;
                askMastr(Protokol.DROPIT, 0, 0);
                tasks.remove(0);
                pot = null;
                if (tasks.isEmpty()) {
                    botState = BotState.FINITO;
                } else {
                    botState = BotState.GOING_TO;
                }
                break;
        }
    }

    static void move() {
        if (nasledna != null) {
            Location stara = location;
            location = nasledna;
            nasledna = null;
            steps++;
            askMastr(Protokol.MOVE, location.getX(), location.getY());
            askMastr(Protokol.RELEASE, stara.getX(), stara.getY());
        }
    }


    //Kopirano iz Parallel/Sequnteila verzije in rocno dodlean
    /*Breath First Search Alogirtem generiran z AI. Spodnji del torej ta findPath je generiran z
    pomocjo AI. Probal sem z lastnim alogirtmom ampak se je znaslo da je prislo do ogromno deadlockov..
     */
    //seveda malo spremenjen za distrubuted verzijo s strani mene.
    static ArrayList<Location> findPath(Location start, Location end) {
        Location[][] parent = new Location[sirina][visina];
        boolean[][] visited = new boolean[sirina][visina];

        ArrayList<Location> queue = new ArrayList<>();
        queue.add(start);
        visited[start.getX()][start.getY()] = true;

        while (!queue.isEmpty()) {
            Location current = queue.remove(0);

            //ce je end se naredi tole drugace ne
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
                        && nx < sirina
                        && ny < visina
                        && !visited[nx][ny]
                        && !(nx % 2 != 0 && ny % 2 != 0)
                        && jeProsto(nx, ny)) {
                    visited[nx][ny] = true;
                    parent[nx][ny] = current;
                    queue.add(new Location(nx, ny));
                }
            }
        }
        return null;
    }

    static Location sosed(Location polica) {
        //Prvi del tukaj ni bil napisan z AI bil pa je inspiriran iz prejsne AI kode za pathFind
        int[] px = {1, -1, 0, 0};
        int[] py = {0, 0, 1, -1};

        Location prvi_veljaven = null;
        for (int i = 0; i < 4; i++) {
            int pxx = polica.getX() + px[i];
            int pyy = polica.getY() + py[i];
            Location mozen_plac = new Location(pxx, pyy);
            //malo dolgo ampak sam preveri ali je izven mape in ali je polica
            if (!(mozen_plac.getX() < 0 || mozen_plac.getY() < 0 || mozen_plac.getX() >= sirina || mozen_plac.getY() >= visina || (mozen_plac.getX() % 2 != 0 && mozen_plac.getY() % 2 != 0))) {
                if (prvi_veljaven == null) {
                    prvi_veljaven = mozen_plac;
                }
                if (jeProsto(pxx, pyy)) {
                    return mozen_plac;
                }
            }
        }
        return prvi_veljaven;
    }
}





