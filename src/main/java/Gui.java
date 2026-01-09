import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Gui extends JPanel {

    private final BufferedImage floor = ImageIO.read(new File("./Assets/PNG/Tiles/platformPack_tile040.png"));
    private final BufferedImage police = ImageIO.read(new File("./Assets/PNG/crate_03.png"));
    private final BufferedImage botEmpty= ImageIO.read(new File("./Assets/PNG/robot_01.png"));
    // private final BufferedImage player = ImageIO.read(new File("./Assets/Player/player_01.png"));
    //private final BufferedImage coin = ImageIO.read(new File("./Assets/Environment/environment_11.png"));
   /* private final BufferedImage[] portals =  new BufferedImage[]{
            ImageIO.read(new File("./Assets/Environment/environment_01.png")),
            ImageIO.read(new File("./Assets/Environment/environment_04.png")),
            ImageIO.read(new File("./Assets/Environment/environment_07.png")),
            ImageIO.read(new File("./Assets/Environment/environment_09.png")),
            ImageIO.read(new File("./Assets/Environment/environment_13.png")),
    };
*/
    public Gui() throws IOException {
        JFrame frame = new JFrame("Warehouse Bots");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GameState.WIDTH * 64, insets().top + (GameState.HEIGHT * 64));
        frame.add(this);
        frame.setVisible(true);
    }



    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int tileWidth = (GameState.WIDTH*64 ) / GameState.WIDTH;
        int tileHeight = (GameState.HEIGHT*64) / GameState.HEIGHT;
        Random random = new Random();

        int xz = 2 * tileWidth;
        int yz = 2 * tileHeight;


        for (int col = 0; col < GameState.WIDTH; col++) {
            for (int row = 0; row < GameState.HEIGHT; row++) {
                int x = col * tileWidth;
                int y = row * tileHeight;
                g2d.drawImage(floor, x, y, tileWidth, tileHeight, null);

                if (col % 2 != 0 && row % 2 != 0) {
                    g2d.drawImage(police, x, y, 64, 64, null);

                    g2d.setColor(Color.WHITE);
                    g2d.drawString(col + "," + row, x + 5, y + 15);
                }



            }
        }
        g2d.drawImage(botEmpty, xz, yz,tileWidth,tileHeight, null);
       /*
        for (int portalIndex = 0; portalIndex < portals.length; portalIndex++) {
            int x = GameState.portals.get(portalIndex).getX() * tileWidth;
            int y = GameState.portals.get(portalIndex).getY() * tileHeight;

            g2d.drawImage(portals[portalIndex], x, y, tileWidth, tileHeight, null);

            g2d.setColor(Color.GREEN);
            g2d.drawString("" + portalIndex, x, y);
        }


        for (Location coinLocation : GameState.coins) {
            int x = coinLocation.getX() * tileWidth;
            int y = coinLocation.getY() * tileHeight;
            g2d.drawImage(coin, x, y, tileWidth, tileHeight, null);
        }


        int playerX = GameState.player.location.getX() * tileWidth;
        int playerY = GameState.player.location.getY() * tileHeight;
        g2d.drawImage(player, playerX, playerY, tileWidth, tileHeight, null);
        */
    }
}
