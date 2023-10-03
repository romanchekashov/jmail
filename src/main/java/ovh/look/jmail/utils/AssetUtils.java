package ovh.look.jmail.utils;

import ovh.look.jmail.JMail;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class AssetUtils {
    public static BufferedImage getBufferedImage(String image) throws IOException {
        return ImageIO.read(JMail.class.getResourceAsStream("/" + image));
    }
}
