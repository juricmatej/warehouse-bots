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

        Path file = Paths.get("file.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
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
            for (int i = 0; i < num_of_bots; i++) {
                Bot bot = new Bot();
                bot.id = "B" + i+1;
                GameState.bots.add(bot);
            }


            while (reader.readLine() != null) {
                String navodila = reader.readLine();
                if (navodila == null) {
                    continue;
                }
                beri(navodila);

            }






        } catch (IOException e) {
            Logger.error("Error reading file: " + e.getMessage());
        }


    }

    private static void beri(String navodila) {
        String[] deli = navodila.split("\\|");
        String id = deli[0];
        String iz1 = deli[1];
        String v1 = deli[2];
        //izz
        String[] iz = iz1.split("-");
        int iz_x = Integer.parseInt(iz[0]);
        int iz_y = Integer.parseInt(iz[1]);
        String iz_HEx = iz[2];
        int iz_polica = Integer.parseInt(iz_HEx, 16);
        //v
        String[] v = v1.split("-");
        int vx = Integer.parseInt(v[0]);
        int vy = Integer.parseInt(v[1]);
        String vHex = v[2];
        int v_polica =  Integer.parseInt(vHex, 16);

        Mission delo = new Mission();
        // treba dodat aasdaspjdapsda'psokdadpok
    }


}
