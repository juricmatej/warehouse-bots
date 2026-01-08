import Util.Logger;

import java.io.IOException;
import java.util.SimpleTimeZone;

public class Main {
    static void main(String[] args) throws IOException {

        Logger.info("------------------------------");
        Logger.info("|       Warehouse Bots       |");
        Logger.info("------------------------------");

        Reader.reader();
        Logger.warn(GameState.WIDTH + " " + GameState.HEIGHT + " " + GameState.num_of_bots);
        Gui gui = new Gui();

    }
}
