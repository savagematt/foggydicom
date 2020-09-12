package science.giraffe.foggydicom;


import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public class Series {
    public final String name;
    public final List<Image> slices;

    public Series(String name, List<Image> slices) {
        this.name = name;
        this.slices = Collections.unmodifiableList(slices);
    }

    public int size() {
        return slices.size();
    }

    public static class Image {
        public final String seriesDescription;
        public final BufferedImage bufferedImage;
        public final javafx.scene.image.Image image;

        public Image(MetaData metaData, BufferedImage bufferedImage, javafx.scene.image.Image image) {
            this.seriesDescription = metaData.seriesDescription;
            this.bufferedImage = bufferedImage;
            this.image = image;
        }
    }
}
