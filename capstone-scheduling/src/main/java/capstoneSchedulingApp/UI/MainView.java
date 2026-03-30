package capstoneSchedulingApp.UI;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.tabs.*;

import java.io.File;
import java.io.InputStream;

import com.nimbusds.jose.util.StandardCharset;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;




@Route("/")
@RouteAlias("main")
@RouteAlias("home")
@PageTitle("Schedule Validation")
public class MainView extends AppLayout{

    private VerticalLayout contentArea = new VerticalLayout();
    private String dbPath = System.getenv().getOrDefault("SQLITE_DB_PATH", "tmpData/schedule.db");

    public MainView(){

        contentArea.setSizeFull();
        setContent(contentArea);
        addToNavbar(createHeader());
    }

    private Component createHeader(){
        
        //Title Creation, Styling
        H1 title = new H1("Scheduling Validation Tool");
        title.getStyle().set("left", "var(--lumo-space-l)").set("font-size", "var(--lumo-font-size-xl)").set("margin", "0")
                .set("position", "absolute");

        //Upload Button
        Button uploadButton = new Button("Upload", event->UploadDialog());

        //Help Button
        Button helpButton = new Button("Help", event->HelpDialog());

        // Button viewData = new Button("View Dataset", event->viewData());
        Button viewData = new Button("View Data", event->viewData());

        //Header Creation
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.expand(title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        Div leftSpacer = new Div();
        Div rightSpacer = new Div();
        header.add(title, leftSpacer, uploadButton, rightSpacer, helpButton);
        header.expand(rightSpacer, leftSpacer);
        return header;
    }

    //Upload Dialog
    private void UploadDialog(){
        contentArea.removeAll();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Upload CSV");
        FileBuffer buffer = new FileBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".csv");
        upload.setMaxFiles(1);

        Paragraph instructions = new Paragraph("Upload your schedule CSV here.");

        upload.addSucceededListener(event -> {
                //ADD PARSER CALL
            contentArea.add(new Paragraph("Current Uploaded File: " + buffer.getFileName()));
            //Parser.parseFile(dbPath, buffer.getFileData().getFile().getAbsolutePath(), ",");
            //contentArea.add(new Pre(Parser.getClassesPreview(dbPath)));
            Notification notification = Notification.show("Upload Successful", 3000, Notification.Position.TOP_END);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            dialog.close();
        });
        upload.addFailedListener(event -> {
            Notification notification = Notification.show("Upload Failed", 4000, Notification.Position.TOP_END);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            dialog.close();
        });
        Button cancel = new Button("Cancel", event->dialog.close());
        VerticalLayout dialogLayout = new VerticalLayout(instructions, upload);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);
        dialog.add(dialogLayout);
        dialog.getFooter().add(cancel);
        dialog.open();
        }        
    
    private void HelpDialog(){
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Scheduling Validation Tool Help");
        Button exitButton = new Button("X", e->dialog.close());
        dialog.getHeader().add(exitButton);
        String helpText = "";
        try(InputStream in = getClass().getClassLoader().getResourceAsStream("help.txt")){
            if(in == null){
                throw new Exception();
            }
            helpText = new String(in.readAllBytes(), StandardCharset.UTF_8);
        } catch (Exception e) {
            Notification notification = Notification.show("Failed to Load Help File", 4000, Notification.Position.TOP_END);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        Pre text = new Pre(helpText);
        text.getStyle().set("white-space", "pre-wrap");
        dialog.add(text);
        dialog.setWidth("70%");
        dialog.setHeight("50%");
        dialog.open();
    }

    private void viewData(){
        contentArea.removeAll();
        Grid dataGrid = new Grid();

    }

}
