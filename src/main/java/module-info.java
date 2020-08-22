module giraffe.science.foggydicom {
    requires java.base;
    requires java.logging;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;

    requires dcm4che.tool.dcm2jpg;

    exports giraffe.science.foggydicom;
}
