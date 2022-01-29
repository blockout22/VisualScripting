package visual.scripting;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.*;

public class TextureLoader {

    public static Texture loadTexture(String textureFile) throws IOException {
        BufferedImage image = loadImage(textureFile);

        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                int pixel = pixels[h * image.getWidth() + w];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -1);
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);

        Texture texture = new Texture();
        texture.ID = textureID;
        texture.width = image.getWidth();
        texture.height = image.getHeight();
        return texture;
    }

    private static BufferedImage loadImage(String fileName) throws IOException {
        InputStream in = null;
        BufferedImage image;
        File file = new File(fileName);

        if(!file.exists()) {
            in = TextureLoader.class.getResourceAsStream("/" + fileName);
            //if filename starts with http ... try to load from URL if image isn't found in file system
            if(fileName.startsWith("http")){
                URL url = new URL(fileName);
                String urlFileName = url.getFile().substring(url.getFile().lastIndexOf("/") + 1);

                //check if saved image already exists
                File urlFile = new File(urlFileName);
                if(!urlFile.exists()) {
                    image = ImageIO.read(url);
                    ImageIO.write(image, "png", new FileOutputStream(urlFile));
                }else{
                    image = ImageIO.read(urlFile);
                }

            }else {
                image = ImageIO.read(in);
            }
        }else{
            image = ImageIO.read(file);
        }


        if(image == null){
            throw new IOException("Image Not Found!");
        }

        if (in != null) {
            in.close();
        }
        return image;
    }
}
