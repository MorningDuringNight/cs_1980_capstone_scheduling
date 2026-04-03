package capstoneSchedulingApp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DesktopLauncher extends Application {

    private ConfigurableApplicationContext context;
    private final String dbPath = System.getenv().getOrDefault("SQLITE_DB_PATH", "tmpData/");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        context = new SpringApplicationBuilder(WebLauncher.class).run();
        try {
            Files.createDirectory(Paths.get(dbPath));
        } catch (Exception e) {
            System.out.println("Data directory already exists");
        }
    }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        
        webView.getEngine().setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " + "AppleWebKit/605.1.15 (KHTML, like Gecko) " + "Version/17.0 Safari/605.1.15");
        webView.getEngine().load("http://127.0.0.1:8080/");

        Scene scene = new Scene(webView, 1200, 800);
        stage.setTitle("Schedule Validation Tool");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }

        try {
            FileUtils.deleteDirectory(new File(dbPath));
        } catch (Exception e) {
            System.out.println("Error cleaning data directory");
        }
    }
}