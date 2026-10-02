import Util.Logger;

import java.io.IOException;

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

        while (!GameState.konec()){



            for (int i = 0; i < GameState.bots.size() ; i++) {
                boolean[][] zaseden = GameState.zasedene();
                GameState.bots.get(i).doMove(zaseden);

            }
            GameState.checkTrk();

            for (int i = 0; i < GameState.bots.size() ; i++) {
                Bot bot = GameState.bots.get(i);
                bot.move();

            }

            for (int i = GameState.bots.size() - 1; i >= 0; i--) {
                if(GameState.bots.get(i).botState == BotState.FINITO ){
                    GameState.bots.remove(i);//dodal ker se pri majnsih gridih in vec botoh lahko zgodi da ustavljenu boti "ujameo" enega in nemore koncati
                }
            }
            ticks++;
            gui.repaint();
            Thread.sleep(10);
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        Logger.info("------------------");
        Logger.info("Ticks " + ticks);
        Logger.info("Time: " + elapsedTime + " ms");
        Logger.info("------------------");
    }
}
