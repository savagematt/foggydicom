package science.giraffe.foggydicom;

import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ResourceBundle;

public class Dcm2Jpg {
    private static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.dcm2jpg.messages");
    private String suffix;
    private final ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();
    private ImageWriter imageWriter;
    private ImageWriteParam imageWriteParam;

    public Dcm2Jpg() {
    }

    public void initImageWriter() {
        String formatName = "JPEG";
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(formatName);
        if (!imageWriters.hasNext()) {
            throw new IllegalArgumentException(MessageFormat.format(rb.getString("formatNotSupported"), formatName));
        } else {
            this.suffix = suffix != null ? suffix : formatName.toLowerCase();
            this.imageWriter = imageWriters.next();

            this.imageWriteParam = this.imageWriter.getDefaultWriteParam();
        }
    }


    public static void main(File src, File dest) {
        Dcm2Jpg main = new Dcm2Jpg();
        main.initImageWriter();

        dest.mkdirs();
        // TODO: check src exists and dest is a directory

        main.mconvert(src, dest);
    }

    private void mconvert(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            File[] files = src.listFiles();

            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                this.mconvert(file, new File(dest, file.isFile() ? this.suffix(file) : file.getName()));
            }

        } else {
            if (dest.isDirectory()) {
                dest = new File(dest, this.suffix(src));
            }

            try {
                this.convert(src, dest);
                System.out.println(MessageFormat.format(rb.getString("converted"), src, dest));
            } catch (Exception var7) {
                System.out.println(MessageFormat.format(rb.getString("failed"), src, var7.getMessage()));
                var7.printStackTrace(System.out);
            }

        }
    }

    public void convert(File src, File dest) throws IOException {

        try (ImageInputStream iis = ImageIO.createImageInputStream(src)) {
            BufferedImage bi = this.readImage(iis);
            bi = this.convert(bi);
            dest.delete();

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(dest)) {
                this.writeImage(ios, bi);
            }
        }

    }

    private BufferedImage convert(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        return cm.getNumComponents() == 3 ? BufferedImageUtils.convertToIntRGB(bi) : bi;
    }

    private BufferedImage readImage(ImageInputStream iis) throws IOException {
        this.imageReader.setInput(iis);
        return this.imageReader.read(0, this.readParam());
    }

    private ImageReadParam readParam() {
        DicomImageReadParam param = (DicomImageReadParam) this.imageReader.getDefaultReadParam();
        param.setAutoWindowing(true);
        param.setPreferWindow(true);
        param.setOverlayActivationMask(65535);
        param.setOverlayGrayscaleValue(65535);
        param.setOverlayRGBValue(16777215);
        return param;
    }

    private void writeImage(ImageOutputStream ios, BufferedImage bi) throws IOException {
        this.imageWriter.setOutput(ios);
        this.imageWriter.write(null, new IIOImage(bi, null, null), this.imageWriteParam);
    }

    private String suffix(File src) {
        return src.getName() + '.' + this.suffix;
    }
}
