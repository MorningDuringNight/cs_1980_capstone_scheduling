package capstoneSchedulingApp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class DesktopLauncher extends Application {

    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        context = new SpringApplicationBuilder(WebLauncher.class).run();
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
    }
}