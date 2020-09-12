package science.giraffe.foggydicom;

import javafx.scene.image.Image;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageConverter {
    private static final ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();
    private static final ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("JPEG").next();


    public static Image javafx(java.awt.Image image) throws IOException {
        if (!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
                image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();

            image = bufferedImage;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new Image(in);
    }

    public synchronized static BufferedImage readImage(ImageInputStream iis) throws IOException {
        imageReader.setInput(iis);
        return imageReader.read(0, imageReader.getDefaultReadParam());
    }

    public synchronized static void writeImage(File file, BufferedImage bi) {
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
            imageWriter.setOutput(ios);
            imageWriter.write(
                null,
                new IIOImage(bi, null, null),
                imageWriter.getDefaultWriteParam());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
