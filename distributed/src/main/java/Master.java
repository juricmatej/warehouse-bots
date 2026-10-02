import Util.Logger;
import mpi.MPI;
import mpi.Status;

public class Master {

    public static void go(int stBotov) throws Exception {



        Reader.reader();


        if (GameState.num_of_bots != stBotov) {
            Logger.error("Ni enako stevilo botov, runnaj z enakim stevilom ");



            System.exit(1);
        }


        GameState.init();

        for (int i = 0; i < GameState.bots.size(); i++) {
            Bot b = GameState.bots.get(i);
            for (int j = 0; j < b.tasks.size(); j++) {
                Mission a = b.tasks.get(j);
                GameState.najdShelf(a.poberi).police[a.poberiPolica / 8][a.poberiPolica % 8] = "item-" + b.id + "-" + j;
            }
        }


        Gui gui = new Gui();

        new Thread(gui).start();


        for (int i = 0; i < stBotov; i++ ){
            Bot bot = GameState.bots.get(i);

            int[] header = new int[5];

            header[0] = bot.getLocation().getX();
            header[1] = bot.getLocation().getY();
            header[2] = GameState.pravi_width;
            header[3] = GameState.pravi_height;
            header[4] = bot.tasks.size();

            MPI.COMM_WORLD.Send(header, 0, 5, MPI.INT, i+1, Protokol.INIT);

            int[] tasks = new int[bot.tasks.size() * 6]; // 6 je stevilka za kolko parametro je treba za eno mission



            for (int j = 0; j < bot.tasks.size(); j++) {
                Mission a = bot.tasks.get(j);
                tasks[j * 6] = a.poberi.getX();
                tasks[j * 6+1] = a.poberi.getY();
                tasks[j * 6+2] = a.poberiPolica;
                tasks[j * 6+3] = a.daj.getX();
                tasks[j * 6+4] = a.daj.getY();
                tasks[j * 6+5] = a.dajPolica;
            }

            MPI.COMM_WORLD.Send(tasks, 0, tasks.length, MPI.INT, i+1, Protokol.INIT);



        }

        Logger.debug("botov je:" + stBotov);
        long startTime = System.currentTimeMillis();

        int done = 0;
        int[] req = new int[3];




        while(done < stBotov) {
            Status s = MPI.COMM_WORLD.Recv(req, 0, 3, MPI.INT, MPI.ANY_SOURCE, Protokol.REQ);
            int rank = s.source;
            Bot bot = GameState.bots.get(rank -1);


            int[] odg = {0};


            switch (req[0]) {
                case Protokol.REZERV:
                    boolean uspelo = GameState.rezerviraj(new Location(req[1], req[2]), bot);
                    if (uspelo) {
                        odg[0] = 1;
                    } else {
                        odg[0] = 0;
                    }
                    break;

                case Protokol.RELEASE:
                    GameState.clear(new Location(req[1], req[2]), bot);
                    odg[0] = 1;
                    break;

                case Protokol.MOVE:
                    bot.setLocation(new Location(req[1], req[2]));
                    odg[0] = 1;
                    break;

                case Protokol.GOTIT:
                    Mission m1 = bot.tasks.get(0);
                    Shelf p1 = GameState.najdShelf(m1.poberi);
                    bot.carrid = p1.police[m1.poberiPolica / 8][m1.poberiPolica % 8];
                    p1.police[m1.poberiPolica / 8][m1.poberiPolica % 8] = null;
                    bot.setCarrying(true);
                    odg[0] = 1;
                    break;

                case Protokol.DROPIT:
                    Mission m2 = bot.tasks.get(0);
                    Shelf p2 = GameState.najdShelf(m2.daj);
                    p2.police[m2.dajPolica / 8][m2.dajPolica % 8] = bot.carrid;
                    Logger.info(bot.id + " odlozil " + bot.carrid);
                    bot.carrid = null;
                    bot.tasks.remove(0);
                    bot.setCarrying(false);
                    odg[0] = 1;
                    break;


                case Protokol.FINITO:
                    bot.setBotState(BotState.FINITO);
                    GameState.clear(bot.getLocation(), bot);
                    done++;
                    Logger.info(bot.id + " koncal " + done + " "+ stBotov);
                    odg[0] = 1;
                    break;

                case Protokol.REZERVACIJE:
                    int[] mapa = GameState.rezervacije();
                    MPI.COMM_WORLD.Send(mapa, 0, mapa.length, MPI.INT, rank, Protokol.ODG);
                    break;

                default:
                    break;
            }

            if (req[0] != Protokol.REZERVACIJE) {
                MPI.COMM_WORLD.Send(odg, 0, 1, MPI.INT, rank, Protokol.ODG);
            }

        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        int[] sendbuf = {0};
        int[] recvbuf = new int[1];



        MPI.COMM_WORLD.Reduce(sendbuf, 0, recvbuf, 0, 1, MPI.INT, MPI.SUM, 0);
        Logger.info("------------------");
        Logger.info("Vsi boti koncali");
        Logger.info("Skupaj korakov: " + recvbuf[0]);
        Logger.info("Time: " + elapsedTime + " ms");
        Logger.info("------------------");



    }

}
