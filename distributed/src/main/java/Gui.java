import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static java.lang.Math.random;

public class Gui extends JPanel implements Runnable {

    private final BufferedImage floor = ImageIO.read(new File("./Assets/PNG/platformPack_tile040.png"));
    private final BufferedImage police = ImageIO.read(new File("./Assets/PNG/custom_crate010.png"));
    private final BufferedImage botEmpty= ImageIO.read(new File("./Assets/PNG/robot_01.png"));
    private final BufferedImage botCarrying= ImageIO.read(new File("./Assets/PNG/bot_carrying3.png"));

    public Gui() throws IOException {
        JFrame frame = new JFrame("Warehouse Bots");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((GameState.pravi_width * 64 )+ 18,  40+ (GameState.pravi_height * 64));
        frame.add(this);
        frame.setVisible(true);
    }



    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int tileWidth = (GameState.WIDTH*20 ) / GameState.WIDTH;
        int tileHeight = (GameState.HEIGHT*20) / GameState.HEIGHT;
        Random random = new Random();



        for (int col = 0; col < GameState.pravi_width; col++) {
            for (int row = 0; row < GameState.pravi_height; row++) {
                int x = col * tileWidth;
                int y = row * tileHeight;
                g2d.drawImage(floor, x, y, tileWidth, tileHeight, null);

                if (col % 2 != 0 && row % 2 != 0) {
                    g2d.drawImage(police, x, y, 20, 20, null);

                }



            }
        }


        for (int i = 0; i < GameState.bots.size() ; i++) {
            Bot bot = GameState.bots.get(i);
            if (bot.getBotState() == BotState.FINITO) {
                continue;
            }
            int x = bot.getLocation().getX() * tileWidth;
            int y = bot.getLocation().getY() * tileHeight;
            if(bot.isCarrying()){
                g2d.drawImage(botCarrying, x, y, tileWidth, tileHeight, null);
            } else {
                g2d.drawImage(botEmpty, x, y, tileWidth, tileHeight, null);
            }
        }


    }


    @Override
    public void run() {
        javax.swing.Timer timer = new javax.swing.Timer(50, e -> repaint());
        timer.start();
    }
}
