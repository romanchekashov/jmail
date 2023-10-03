package ovh.look.jmail.utils;

import ovh.look.jmail.JMail;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class AssetUtils {

    public static String getCurrentPath() {
        try {
            return new File(AssetUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            return "";
        }
    }

    /**
     * @see <a href="https://www.baeldung.com/java-class-vs-classloader-getresource">Difference Between Class.getResource() and ClassLoader.getResource()</a>
     *
     * @param image
     * @return
     * @throws IOException
     */
    public static BufferedImage getBufferedImage(String image) throws IOException {
        return ImageIO.read(JMail.class.getResourceAsStream("/" + image));
    }

    public static BufferedReader getBufferedReader(String path) {
        InputStream is = JMail.class.getResourceAsStream(path);
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        return new BufferedReader(streamReader);
    }
}
