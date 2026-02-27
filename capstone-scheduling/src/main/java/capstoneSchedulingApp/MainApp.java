package capstoneSchedulingApp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainApp extends Application {
    private final TextArea output = new TextArea();
    private File selectedCSV;

    @Override
    public void start(Stage stage){
        Button loadBtn = new Button("Load CSV");
        Button parseBtn = new Button("Parse");
        parseBtn.setDisable(true);
        Label status = new Label("No File Loaded.");
        loadBtn.setOnAction(e->{
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Schedule CSV");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
            File f = chooser.showOpenDialog(stage);
            if(f!=null){
                selectedCSV = f;
                status.setText("Loaded: " + f.getName());
                parseBtn.setDisable(false);
            }
        });

        parseBtn.setOnAction(e->{
            if(selectedCSV == null)
                return;
            try{
                Schedule sched = Parser.parseFile(selectedCSV.getAbsolutePath(), ",");
                output.setText(sched.toString());
                status.setText("Parsed Successful: " + selectedCSV.getName());
            }
            catch(Exception ex){
                output.setText("ERROR:\n" + ex);
                status.setText("Parsing Failed.");
            }
        });
        HBox toolbar = new HBox(10, loadBtn, parseBtn, status);
        toolbar.setStyle("-fx-padding: 10;");
        output.setEditable(false);
        BorderPane root = new BorderPane(output);
        MenuBar menuBar = new MenuBar();
        Menu helpMenu = new Menu("Help");
        MenuItem viewHelp = new MenuItem("View Help");
        viewHelp.setOnAction(e->showHelpWindow());
        helpMenu.getItems().add(viewHelp);
        menuBar.getMenus().add(helpMenu);
        VBox topContainer = new VBox(menuBar, toolbar);
        root.setTop(topContainer);
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Scheduling Validation Tool (Capstone WIP)");
        stage.setScene(scene);
        stage.show();
    }

    private void showHelpWindow(){
        try{
            InputStream i = getClass().getResourceAsStream("/help.txt");
            if(i == null)
                throw new RuntimeException("help.txt not found in resources folder.");
            String text = new String(i.readAllBytes(), StandardCharsets.UTF_8);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Help");
            alert.setHeaderText("Capstone Scheduling Help");
            alert.getDialogPane().setContent(new TextArea(text));
            alert.setResizable(true);
            alert.showAndWait();
        }
        catch(Exception ex){
            Alert error = new Alert(AlertType.ERROR, "Unable to load help file.");
            error.showAndWait();
        }
    }
    public static void main(String [] args){
        launch(args);
    }
}

