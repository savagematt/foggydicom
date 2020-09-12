package science.giraffe.foggydicom;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static science.giraffe.foggydicom.ImageConverter.readImage;

@ViewController(value = "/fxml/ui/Sessions.fxml", title = "Select imaging")
public class SessionsController {
    @FXML
    @ActionTrigger("open")
    private JFXButton open;

    @FXML
    @ActionTrigger("previous")
    private JFXButton previous;

    @FXML
    @ActionTrigger("next")
    private JFXButton next;

    @FXML
    @ActionTrigger("save")
    private JFXButton save;

    @FXML
    @ActionTrigger("save")
    private JFXSlider slice;

    @FXML
    private VBox image;
    @FXML
    private StackPane root;
    @FXML
    private ScrollPane thumbnails;
    @FXML
    private Label title;
    @FXML
    private HBox controls;

    @Inject
    private Stage stage;

    @FXMLViewFlowContext
    private ViewFlowContext context;

    private final DirectoryChooser dirChooser = new DirectoryChooser();
    private final FileChooser fileChooser = new FileChooser();
    private List<Series> series = Collections.emptyList();
    private Selection selection = new Selection(0, 0);

    static class Selection {
        public final int series;
        public final int slice;

        public Selection(int series, int slice) {
            this.series = series;
            this.slice = slice;
        }

        public Selection nextSlice() {
            return slice(this.slice + 1);
        }

        public Selection previousSlice() {
            return slice(this.slice - 1);
        }

        public Selection slice(int slice) {
            return new Selection(this.series, slice);
        }

        public Selection series(int i) {
            if (i == this.series) return this;
            return new Selection(i, 0);
        }
    }

    @PostConstruct
    public void init() {
        dirChooser.setTitle("Open CD or folder");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JPEG", "jpg", "jpeg"));
        fileChooser.setTitle("Export image as JPEG");
        thumbnails.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        image.setOnScroll(e -> {
            if (e.getTextDeltaYUnits() == ScrollEvent.VerticalTextScrollUnits.LINES) {
                slice.setValue(Math.max(0, Math.min(slice.getMax(), slice.getValue() + e.getTextDeltaY())));
            } else {
                double percent = e.getDeltaY() / slice.getHeight();
                double delta = percent * slice.getMax();

                System.out.println();
                System.out.println(format("%s / %s = %s", e.getDeltaY(), slice.getHeight(), percent));
                System.out.println(format("%s * %s = %s", percent, slice.getMax(), delta));

                double v = delta > 0
                    ? delta < 1 ? 1 : delta
                    : delta > -1 ? -1 : delta;
                System.out.println(delta);

                double newValue = Math.max(0, Math.min(slice.getMax(), slice.getValue() - v));
                System.out.println(format("from %s to %s", slice.getValue(), newValue));
                slice.setValue(newValue);
            }
        });

        save.setOnAction(event -> {
            Series.Image image = this.series.get(selection.series).slices.get(selection.slice);
            String safeDescription = image.seriesDescription.replaceAll("[^A-Za-z0-9_\\-.]", "-");
            fileChooser.setInitialFileName(format("%s[%s].jpg", safeDescription, selection.slice + 1));

            File output = fileChooser.showSaveDialog(stage);
            if (output == null) return;

            ImageConverter.writeImage(output, image.bufferedImage);
        });
        open.setOnAction(event -> {
            File input = dirChooser.showDialog(stage);
            if (input == null) return;
            Stream<File> files = allFiles(input);
            Map<String, List<Series.Image>> bySeries = files
                .map(src -> {
                    try (ImageInputStream iis = ImageIO.createImageInputStream(src)) {
                        BufferedImage image = readImage(iis);
                        MetaData metaData = MetaData.parse(src);
                        return new Series.Image(metaData, image, ImageConverter.javafx(image));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(x -> x.seriesDescription));

            if (bySeries.isEmpty())
                return;
            this.series = bySeries
                .entrySet()
                .stream()
                .map(e -> new Series(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

            List<Node> thumbnails = IntStream.range(0, this.series.size())
                .mapToObj(i -> {
                    Series s = this.series.get(i);

                    ImageView v = new ImageView(s.slices.get(0).image);
                    v.setFitWidth(150);
                    v.setPreserveRatio(true);
                    v.setOnMouseClicked(($) -> select(selection.series(i)));

                    Label l = new Label(s.name);
                    l.setMaxWidth(150);
                    l.setWrapText(true);
                    l.setOnMouseClicked(($) -> select(selection.series(i)));

                    VBox box = new VBox(v, l);
                    box.setAlignment(Pos.CENTER);
                    box.setStyle("-fx-padding: 7 0 7 0;");
                    return box;

                })
                .collect(Collectors.toList());

            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(thumbnails);

            HBox hbox = new HBox(vbox);
            vbox.setMinWidth(200);
            hbox.setAlignment(Pos.CENTER);
            hbox.setStyle("-fx-background-color: white;");

            this.thumbnails.setContent(hbox);

            select(new Selection(0, 0));
        });

        slice.valueProperty().addListener(
            e -> {
                select(selection.slice((int) Math.round(slice.getValue())));
            }
        );
        next.setOnAction(event -> {
            select(selection.nextSlice());
        });
        previous.setOnAction(event -> {
            select(selection.previousSlice());
        });
    }

    private void select(Selection selection) {
        Series series = this.series.get(selection.series);
        slice.setMin(0);
        slice.setMax(series.size() - 1);
        slice.setValue(selection.slice);
        displayImage(series.slices.get(selection.slice));
        previous.setDisable(selection.slice <= 0);
        next.setDisable(selection.slice >= series.size() - 1);
        this.selection = selection;
    }

    private void displayImage(Series.Image i) {
        image.getChildren().clear();
        ImageView view = new WrappedImageView(i.image);
//        view.fitHeightProperty().bind(image.heightProperty());
//        view.fitWidthProperty().bind(image.widthProperty());
        view.setPreserveRatio(true);
        view.setStyle("-fx-background-color: blue;");
        this.image.getChildren().add(view);
    }

    private Stream<File> allFiles(File input) {
        if (input.isDirectory()) {
            File[] files = input.listFiles();
            if (files == null) return Stream.empty();
            return stream(files).map(this::allFiles).flatMap(it -> it);
        } else {
            return Stream.of(input);
        }
    }
}
