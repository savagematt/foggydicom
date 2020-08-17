package giraffe.science;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.dcm4che3.tool.dcm2jpg.Dcm2Jpg;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends Application {

    File input;
    File output;

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        DirectoryChooser fileChooser = new DirectoryChooser();
        Label from = new Label(" ");
        Button fromButton = new Button("Read images from (folder or CD)");
        fromButton.setOnAction(e -> {
            input = fileChooser.showDialog(stage);
            if (input != null) from.setText(input.getAbsolutePath());
        });

        Label to = new Label(" ");
        Button toButton = new Button("Save to directory");
        toButton.setOnAction(e -> {
            output = fileChooser.showDialog(stage);
            if (output != null) to.setText(output.getAbsolutePath());
        });

        Label error = new Label(" ");
        Button goButton = new Button("GO");
        goButton.setOnAction(e -> {
            try {
                Dcm2Jpg.main(new String[]{input.getAbsolutePath(), output.getAbsolutePath()});
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                error.setText(sw.toString());
            }
        });

        VBox box = new VBox(
            fromButton, from,
            toButton,
            to,
            goButton);
        box.setSpacing(10);
        box.setPadding(new Insets(10));
        Scene scene = new Scene(box, 640, 480);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}