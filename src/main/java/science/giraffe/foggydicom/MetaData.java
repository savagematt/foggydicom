package science.giraffe.foggydicom;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.IOException;

public class MetaData {
    public final String seriesDescription;

    public MetaData(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public static MetaData parse(File src) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(src)) {
            Attributes attributes = dis.readDataset(-1, -1);
            return new MetaData(attributes.getString(528446));
        }
    }
}
