import Util.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        Logger.info("------------------------------");
        Logger.info("|       Warehouse Bots       |");
        Logger.info("------------------------------");

        Reader.reader();
        Logger.warn(GameState.WIDTH + " " + GameState.HEIGHT + " " + GameState.num_of_bots);
        GameState.init();

        for (int i = 0; i < GameState.bots.size(); i++) {
            Bot b = GameState.bots.get(i);
            for (int j = 0; j < b.tasks.size(); j++) {
                Mission m = b.tasks.get(j);
                GameState.najdShelf(m.poberi).police[m.poberiPolica / 8][m.poberiPolica % 8] = "item-" + b.id + "-" + j;
            }
        }


        Gui gui = new Gui();
        int ticks = 0;
        long startTime = System.currentTimeMillis();

        new Thread(gui).start();


        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < GameState.bots.size(); i++) {
            Bot bot = GameState.bots.get(i);
            Thread t = new Thread(bot, bot.id);
            threads.add(t);
            t.start();
        }

        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).join();//caka da so vsi boti fertik
        }

        int skupaj = 0;
        int max = 0;
        long elapsedTime = System.currentTimeMillis() - startTime;
        Logger.info("Time: " + elapsedTime + " ms");


        for (int i = 0; i < GameState.bots.size(); i++) {
            int t = GameState.bots.get(i).getSteps();
            skupaj += t;
            if (t > max) max = t;
        }
        Logger.info("Skupaj steps: " + skupaj + " max: " + max);
    }
}
