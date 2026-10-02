import Util.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Reader{

    public static int A;
    public static int B;
    public static int num_of_bots;

    public static void reader() {

        Path file = Paths.get("testi/s3_tasks80.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader("testi/s3_tasks80.txt"))) {//fili za test so bili AI genererani torej instructions2.txt in instructions.txt
            String line;
            String[] AB;
            long st_instructions = (Files.lines(file).count()) - 2;
            String[] instructions = new String[Math.toIntExact(st_instructions)];
            num_of_bots = Integer.parseInt(reader.readLine());
            AB = (reader.readLine().split(" "));
            A = Integer.parseInt(AB[0]);
            B = Integer.parseInt(AB[1]);


            Logger.info("Bots: "+ num_of_bots +", "+ A+"*"+B);
            Logger.debug("Lines of instructions: " + st_instructions);
            //dodam v game state
            for (int i = 0; i < num_of_bots; i++) {
                Bot bot = new Bot();
                bot.id = "B" + (i+1);
                GameState.bots.add(bot);
            }

            String navodila;


            while((navodila = reader.readLine())  != null) {
                beri(navodila);
            }





        } catch (IOException e) {
            Logger.error("Error reading file: " + e.getMessage());
        }


    }

    private static void beri(String navodila) {

        try {
            String[] deli = navodila.split("\\|");
            String id = deli[0];
            String iz1 = deli[1];
            String v1 = deli[2];
            //izz
            String[] iz = iz1.split("-");
            int iz_x = Integer.parseInt(iz[0]);
            int iz_y = Integer.parseInt(iz[1]);
            String iz_HEx = iz[2];
            int iz_polica = Integer.parseInt(iz_HEx, 16);//potrebno ker imam naslove v hex
            //v
            String[] v = v1.split("-");
            int vx = Integer.parseInt(v[0]);
            int vy = Integer.parseInt(v[1]);
            String vHex = v[2];
            int v_polica = Integer.parseInt(vHex, 16);
            Mission delo = new Mission();
            delo.poberi = new Location(2 * iz_x - 1, 2 * iz_y - 1);
            delo.poberiPolica = iz_polica;
            delo.daj = new Location(2 * vx - 1, 2 * vy - 1);
            delo.dajPolica = v_polica;

            if (iz_x < 1 || iz_x > A || iz_y < 1 || iz_y > B) {
                Logger.error("Neveljavna from polica,vrsta");
                return;
            }
            if (vx < 1 || vx > A || vy < 1 || vy > B) {
                Logger.error("Neveljavna to polica,vrsta");
                return;
            }
            if (iz_polica < 0 || iz_polica > 15 || v_polica < 0 || v_polica > 15) {
                Logger.error("Neveljavna slot vrsta");
                return;
            }



            //v bota sopam navodila
            for (int i = 0; i < num_of_bots; i++) {
                Bot bot = GameState.bots.get(i);
                if (bot.id.equals(id)) {
                    bot.tasks.add(delo);
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
