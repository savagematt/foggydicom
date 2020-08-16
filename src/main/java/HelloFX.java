import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        DirectoryChooser fileChooser = new DirectoryChooser();
        Label from = new Label(" ");
        Button fromButton = new Button("Read images from (folder or CD)");
        fromButton.setOnAction(e -> {
            File selectedFile = fileChooser.showDialog(stage);
            from.setText(selectedFile.getAbsolutePath());
        });

        Label to = new Label(" ");
        Button toButton = new Button("Save to directory");
        toButton.setOnAction(e -> {
            File selectedFile = fileChooser.showDialog(stage);
            to.setText(selectedFile.getAbsolutePath());
        });

        Button goButton = new Button("GO");

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