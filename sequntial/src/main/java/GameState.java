import Util.Logger;

import java.util.ArrayList;
import java.util.Random;

public class GameState {

    public static int WIDTH = Reader.A;
    public static int HEIGHT = Reader.B;
    public static int num_of_bots = Reader.num_of_bots;
    public static ArrayList<Bot> bots = new ArrayList<Bot>();




    public static int pravi_width = 2 * WIDTH + 1;

    public static int pravi_height = 2 * HEIGHT + 1;

    public static Shelf najdShelf(Location l) {
        for (int i = 0; i < polices.size(); i++) {
            Shelf a = polices.get(i);
            if (a.location.getX() == l.getX() && a.location.getY() == l.getY()) {
                return a;
            }
        }
        return null;
    }



    public static ArrayList<Shelf> polices = new  ArrayList<>();
    public static void init() {
        for(int i = 0; i < pravi_width ; i++) {
            for(int j = 0; j < pravi_height ; j++) {
                if( i % 2 != 0 && j % 2 != 0) {//kordinata za shelf samo lihi i in j
                    Shelf shelf = new Shelf();
                    shelf.location = new Location(i,j);
                    shelf.police = new String[2][8];
                    polices.add(shelf);

                }
            }
        }
        //vsi tili, ki niso police in na katerih se lahko spawnajo roboti
        ArrayList<Location> potke = new ArrayList<>();
        for(int i = 0; i < pravi_width; i++) {
            for(int j = 0; j < pravi_height; j++) {
                if( i % 2 == 0 || j % 2 == 0) {
                    potke.add(new Location(i,j));
                }
            }
        }

        Random rand = new Random();
        for(int i = 0; i < num_of_bots; i++) {
            int r = rand.nextInt(potke.size());
            bots.get(i).location = potke.get(r);
            potke.remove(r);// da se ne ponovi random pozicija
        }

    }

    public static boolean[][] zasedene(){
        boolean[][] zasedn = new boolean[pravi_width][pravi_height];

        for (int i = 0; i < bots.size(); i++) {
            Bot bot = bots.get(i);//cez vse bote in na zasedn da vse tile ki so zasedeni
            zasedn[bot.location.getX()][bot.location.getY()] = true;

            if (bot.nasledna != null) {
                zasedn[bot.nasledna.getX()][bot.nasledna.getY()] = true;
            }
        }

        return zasedn;
    }

    public static boolean konec() {
        for (int i = 0; i <bots.size() ; i++) {
            Bot bot = bots.get(i);
            if(bot.botState != BotState.FINITO){
                return false;
            }

        }
        Logger.info("Game has finished !");
        return true;
    }



    public static void take(Bot bot, Mission a) {
        Shelf s = najdShelf(a.poberi);
        bot.carrid = s.police[a.poberiPolica / 8][a.poberiPolica % 8];
        s.police[a.poberiPolica / 8][a.poberiPolica % 8] = null;
    }

    public static void odlozi(Bot bot, Mission a) {
        Shelf s = najdShelf(a.daj);
        s.police[a.dajPolica / 8][a.dajPolica % 8] = bot.carrid;
        bot.carrid = null;
    }


    public static void checkTrk() {
        boolean[][] muje =  new boolean[pravi_width][pravi_height];
        for (int i = 0; i <bots.size() ; i++) {
            Bot bot = bots.get(i);
            if (bot.nasledna != null){
                int x = bot.nasledna.getX();
                int y = bot.nasledna.getY();
                if (muje[x][y]){
                    bot.nasledna = null;
                    bot.pot = null;
                    bot.cakaj = bot.r.nextInt(7);
                }else {
                    muje[x][y] = true;
                }
            }
        }
    }



}
