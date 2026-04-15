/*
    Author: Nico Bartello
    Date: 4/8/26
    Description: The vaadin implementation for the frontend view webpage
*/
package capstoneSchedulingApp.UI;
import capstoneSchedulingApp.*;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;


import java.io.IOException;
import java.util.Comparator;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
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
import com.vaadin.flow.data.renderer.ComponentRenderer;
import java.io.InputStream;
import com.nimbusds.jose.util.StandardCharset;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;



@Route("/")
@RouteAlias("main")
@RouteAlias("home")
@PageTitle("Schedule Validation")
public class MainView extends AppLayout{

    private final VerticalLayout contentArea = new VerticalLayout();
    private Path tmpDbDir; //= System.getenv().getOrDefault("SQLITE_DB_PATH", "tmpData/schedule.db");
    private String dbPath;
    private ArrayList<String> parsingErrors = new ArrayList<>();


    public MainView(){
        initializeTempDb();
        contentArea.setSizeFull();
        setContent(contentArea);
        addToNavbar(createHeader());
    }

    private void initializeTempDb(){
        try {
            Path systemTemp = Path.of(System.getProperty("java.io.tmpdir"));
            tmpDbDir = Files.createTempDirectory(systemTemp, ".schedule-validator-");
            Path dbFile = tmpDbDir.resolve("schedule.db");
            dbPath = dbFile.toString();
            tmpDbDir.toFile().deleteOnExit();
            dbFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary database directory", e);
        }
    }

    private void resetTempDb(){
        try {
            Files.deleteIfExists(Path.of(dbPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset database");
        }
    }

    private Component createHeader(){
        
        //Title Creation, Styling
        H1 title = new H1("Scheduling Validation Tool");
        title.getStyle().set("left", "var(--lumo-space-l)").set("font-size", "var(--lumo-font-size-xl)").set("margin", "0")
                .set("position", "absolute").set("font-weight", "bold");

        //Upload Button
        Button uploadButton = new Button("Upload", event->UploadDialog());

        //Help Button
        Button helpButton = new Button("Help", event->HelpDialog());


        //Header Creation
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.expand(title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        Div leftSpacer = new Div();
        Div rightSpacer = new Div();
        header.add(title, leftSpacer, uploadButton, helpButton, rightSpacer);
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
            try {
                String currentUploadedFileName = buffer.getFileName();
                resetTempDb();
                parsingErrors = Parser.parseFile(dbPath, buffer.getFileData().getFile().getAbsolutePath(), ",");
                ArrayList<Collision> collisions = new ArrayList<>();

                //getting the stuff
                collisions.addAll(Query.queryLecCollision(dbPath));
                collisions.addAll(Query.queryRecCollision(dbPath, 30));
                collisions.addAll(Query.queryTeacherProximity(dbPath, 30));
                removeDuplicateCollisions(collisions);
                renderResults(currentUploadedFileName, collisions);
                Notification notification = Notification.show("Upload and validation completed", 3000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
            } catch (Exception e) {
                Notification notification = Notification.show("Validation Failed: " + e.getMessage(), 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
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
        
    private void removeDuplicateCollisions(ArrayList<Collision> collisions){
        for(int i = 0; i < collisions.size(); i++){
            Collision a = collisions.get(i);
            for(int j = i + 1; j < collisions.size(); j++){
                Collision b = collisions.get(j);
                if(areSameCollision(a, b)){
                    collisions.remove(j);
                    j--;
                }
            }
        }
    }

    private boolean areSameCollision(Collision a, Collision b){
        if(a == null || b == null || a.base == null || b.base == null)
            return false;
        if(!safe(a.getTypeSafe()).equals(safe(b.getTypeSafe())))
            return false;
        if(a.hits == null || b.hits == null || a.hits.size()!=1 || b.hits.size()!=1)
            return false;
        Course aHit = a.hits.get(0);
        Course bHit = b.hits.get(0);
        return (sameCourse(a.base, b.base) && sameCourse(aHit, bHit)) || (sameCourse(a.base, bHit) && sameCourse(aHit, b.base));
    }

    private boolean sameCourse(Course a, Course b){
        if(a == null || b == null)
            return false;
        return a.clas_num == b.clas_num
            && a.course_num == b.course_num
            && safe(a.type).equals(safe(b.type))
            && safe(a.days).equals(safe(b.days))
            && safe(a.start).equals(safe(b.start))
            && safe(a.end).equals(safe(b.end))
            && safe(a.room).equals(safe(b.room))
            && safe(a.instructor).equals(safe(b.instructor));
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



    private void renderResults(String fileName, ArrayList<Collision> collisions){
        contentArea.removeAll();
        H3 title = new H3("Validation Results");
        title.getStyle().set("font-weight", "700");
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setAlignItems(FlexComponent.Alignment.CENTER);
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Paragraph totalIssues = new Paragraph("Total Issues Found: " + collisions.size());
        totalIssues.getStyle().set("font-weight", "700");
        Paragraph uploadedFile = new Paragraph("File: " + fileName);
        uploadedFile.getStyle().set("margin", "0").set("font-size", "0.95rem").set("color", "var(--lumo-secondary-text-color)").set("font-weight", "700");
        HorizontalLayout rightSide = new HorizontalLayout();
        rightSide.setSpacing(true);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.add(uploadedFile);
        if(!parsingErrors.isEmpty()){
            Button parsingErrorsButton = new Button("Parsing Errors (" + parsingErrors.size() + ")", event -> openParsingErrorsDialog());
            rightSide.add(parsingErrorsButton);
        }
        topBar.add(totalIssues, rightSide);
        contentArea.add(title, topBar);
        if(collisions.isEmpty()){
            contentArea.add(new Paragraph("No rule violations were found."));
            return;
        }
        collisions.sort((a, b) -> Integer.compare(b.impact, a.impact));
        Grid<Collision> grid = new Grid<>(Collision.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(collision -> getRuleDisplayName(collision.getTypeSafe())).setHeader("Rule").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(collision -> {
            Span badge = new Span("Impact " + collision.impact);
            badge.getStyle().set("padding", "0.25rem 0.6rem").set("border-radius", "999px").set("font-weight", "600").set("font-size", "0.85rem").set("color", "white");
            switch(collision.impact){
                case 3: 
                    badge.getStyle().set("background-color", "#d32f2f");
                    break;
                case 2:
                    badge.getStyle().set("background-color", "#f9a825");
                    break;
                default:
                    badge.getStyle().set("background-color", "#1976d2");
            }
            return badge;
        }).setHeader("Impact").setAutoWidth(true).setFlexGrow(0);
        Grid.Column<Collision> baseClassColumn = grid.addComponentColumn(collision -> {
            Span text = new Span(formatCourse(collision.base));
            text.getStyle().set("white-space", "normal").set("line-height", "1.3").set("display", "block");
            return text;
        }).setHeader("Base Class").setAutoWidth(false).setFlexGrow(1);
        Grid.Column<Collision> conflictsColumn = grid.addComponentColumn(collision -> {
            Span text = new Span(formatHits(collision));
            text.getStyle().set("white-space", "normal").set("line-height", "1.3").set("display", "block");
            return text;
        }).setHeader("Conflicting Class(es)").setAutoWidth(false).setFlexGrow(1);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(collision -> {
            VerticalLayout detailsWrapper = new VerticalLayout();
            detailsWrapper.setPadding(false);
            detailsWrapper.setSpacing(false);
            detailsWrapper.getStyle()
                .set("margin", "0")
                .set("padding", "0.4rem 0.8rem 0.6rem 0.8rem")
                .set("background", "var(--lumo-contrast-5pct)");

            VerticalLayout table = new VerticalLayout();
            table.setPadding(false);
            table.setSpacing(false);
            table.setWidthFull();
            table.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("background", "white");

            HorizontalLayout headerRow = buildDetailHeaderRow();
            table.add(headerRow);

            table.add(buildDetailDataRow("Base Class", collision.base, true));

            for (int i = 0; i < collision.hits.size(); i++) {
                table.add(buildDetailDataRow("Conflicting Class", collision.hits.get(i), false));
            }
            Paragraph issueText = new Paragraph(getRuleDisplayName(collision.getTypeSafe()) + ": " + getRuleIssueDescription(collision.getTypeSafe()));
            issueText.getStyle().set("margin", "0 0 0.5rem 0").set("font-weight", "600").set("color", "var(--lumo-secondary-text-color)");
            detailsWrapper.add(issueText);
            detailsWrapper.add(table);
            return detailsWrapper;
        }));
        /*grid.setItemDetailsRenderer(new ComponentRenderer<>(collision -> {
            VerticalLayout details = new VerticalLayout();
            details.setPadding(false);
            details.setSpacing(false);
            details.getStyle().set("background", "var(--lumo-contrast-5pct)").set("padding", "0.5rem");
            Grid<CourseRow> detailGrid = new Grid<>(CourseRow.class, false);
            detailGrid.setWidthFull();
            detailGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
            detailGrid.setAllRowsVisible(true);

            detailGrid.addColumn(CourseRow::getLabel).setHeader("Role").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->String.valueOf(row.course.clas_num)).setHeader("Class #").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->String.valueOf(row.course.course_num)).setHeader("Course #").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->row.course.type).setHeader("Type").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->row.course.days).setHeader("Days").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->row.course.start).setHeader("Start").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->row.course.end).setHeader("End").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->row.course.room).setHeader("Room").setAutoWidth(true).setFlexGrow(0);
            detailGrid.addColumn(row->row.course.instructor == null ? "" : row.course.instructor).setHeader("Instructor").setFlexGrow(1);
            ArrayList<CourseRow> rows = new ArrayList<>();
            rows.add(new CourseRow("Base Class", collision.base));
            for(int i = 0; i < collision.hits.size(); i++){
                rows.add(new CourseRow("Conflicting Class", collision.hits.get(i)));
            }
            detailGrid.setItems(rows);
            details.add(detailGrid);
            return details;
        }));*/
        final Collision[] expandedItem = new Collision[1];
        grid.addItemClickListener(event -> {
            Grid.Column<Collision> clickedColumn = event.getColumn();
            Collision clickedItem = event.getItem();
            if(clickedColumn != baseClassColumn && clickedColumn != conflictsColumn)
                return;
            if(expandedItem[0] != null && expandedItem[0] != clickedItem){
                grid.setDetailsVisible(expandedItem[0], false);
                grid.getDataProvider().refreshItem(expandedItem[0]);
            }
            boolean currentlyVisible = grid.isDetailsVisible(clickedItem);
            grid.setDetailsVisible(clickedItem, !currentlyVisible);
            grid.getDataProvider().refreshItem(clickedItem);
            expandedItem[0] = grid.isDetailsVisible(clickedItem) ? clickedItem : null;
        });
        grid.setItems(collisions);
        contentArea.add(grid);
    }

    private String formatCourse(Course c){
        return c.clas_num + " | " + c.course_num + " | " + c.type + " | " + c.days + " | " + c.start + "-" + c.end + " | " + c.room + " | " + c.instructor;
    }

    private String formatHits(Collision collision){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < collision.hits.size(); i++){
            sb.append(formatCourse(collision.hits.get(i)));
            if(i < collision.hits.size() - 1)
                sb.append(" || ");
        }
        return sb.toString();
    }


    private HorizontalLayout buildDetailHeaderRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(false);
        row.setPadding(false);
        row.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        row.add(
            detailCellHeader("Role", "190px"),
            detailCellHeader("Class #", "90px"),
            detailCellHeader("Course #", "90px"),
            detailCellHeader("Type", "80px"),
            detailCellHeader("Days", "70px"),
            detailCellHeader("Start", "120px"),
            detailCellHeader("End", "120px"),
            detailCellHeader("Room", "130px"),
            detailCellHeader("Instructor", null)
        );

        row.expand(row.getComponentAt(row.getComponentCount() - 1));
        return row;
    }

    private HorizontalLayout buildDetailDataRow(String role, Course c, boolean shaded) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(false);
        row.setPadding(false);

        row.getStyle()
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("background", shaded ? "var(--lumo-contrast-5pct)" : "white");

        row.add(
            detailCell(role, "190px"),
            detailCell(String.valueOf(c.clas_num), "90px"),
            detailCell(String.valueOf(c.course_num), "90px"),
            detailCell(safe(c.type), "80px"),
            detailCell(safe(c.days), "70px"),
            detailCell(safe(c.start), "120px"),
            detailCell(safe(c.end), "120px"),
            detailCell(safe(c.room), "130px"),
            detailCell(safe(c.instructor), null)
        );

        row.expand(row.getComponentAt(row.getComponentCount() - 1));
        return row;
    }

    private Div detailCellHeader(String text, String width) {
        Div cell = new Div();
        cell.setText(text);
        cell.getStyle()
            .set("font-weight", "600")
            .set("padding", "0.45rem 0.75rem")
            .set("box-sizing", "border-box");

        if (width != null) {
            cell.setWidth(width);
            cell.setMinWidth(width);
            cell.setMaxWidth(width);
        }

        return cell;
    }

    private void openParsingErrorsDialog(){
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Parsing Errors");
        Button closeButton = new Button("Close", event -> dialog.close());
        dialog.getFooter().add(closeButton);
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidth("100%");
        Paragraph summary = new Paragraph("These rows were skipped because they were missing required data or contained invalid values needed for validation");
        summary.getStyle().set("margin", "0");
        VerticalLayout errorsList = new VerticalLayout();
        errorsList.setPadding(false);
        errorsList.setSpacing(true);
        errorsList.setWidthFull();
        for(String error: parsingErrors)
            errorsList.add(buildParsingErrorsDisplay(error));
        layout.add(summary, errorsList);
        dialog.add(layout);
        dialog.setWidth("800px");
        dialog.setHeight("500px");
        dialog.open();
    }

    private Div buildParsingErrorsDisplay(String rawError){
        Div block = new Div();
        block.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "8px").set("padding", "0.75rem").set("background", "var(--lumo-base-color)").set("white-space", "normal");
        String [] lines = rawError.split("\\n");
        String row = "";
        String data = "";
        StringBuilder problem = new StringBuilder();
        for (String line : lines){
            if(line.startsWith("Row:"))
                row = line.substring("Row:".length()).trim();
            else if(line.startsWith("Data:"))
                data = formatParsingData(line.substring("Data:".length()).trim());
            else if(line.startsWith("Problem(s):")){
                String text = line.substring("Problem(s):".length()).trim();
                if(!text.isEmpty()){
                    if(problem.length() > 0)
                        problem.append("<br>");
                    problem.append(escapeHtml(text));
                }
            }
            else if(line.startsWith("Problem:")){
                String text = line.substring("Problem:".length()).trim();
                if(!text.isEmpty()){
                    if(problem.length() > 0)
                        problem.append("<br>");
                    problem.append(escapeHtml(text));
                }
            }
            else if(!line.trim().isEmpty()){
                if(problem.length()>0)
                    problem.append("<br>");
                problem.append(escapeHtml(line.trim()));
            }
        }
        String problemLabel = problem.toString().contains("<br>") ? "Problems:" : "Problem:";
        block.getElement().setProperty("innerHTML", "<b>Row:</b> " + escapeHtml(row) + "<br>" + "<b>Data:</b> " + escapeHtml(data) + "<br>" + "<b>" + problemLabel + "</b> " + problem.toString());
        return block;
    }

    

    private String escapeHtml(String text){
        if(text==null)
            return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String formatParsingData(String rawData){
        String [] parts = rawData.split(",", -1);
        int lastNonEmpty = parts.length-1;
        while(lastNonEmpty>=0 && parts[lastNonEmpty].trim().equals(""))
            lastNonEmpty--;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<=lastNonEmpty; i++){
            String value = parts[i].trim();
            if(i>0)
                sb.append(", ");
            sb.append(value.isEmpty() ? "null" : value);
        }
        return sb.toString();
    }

    private Div detailCell(String text, String width) {
        Div cell = new Div();
        cell.setText(text);
        cell.getStyle()
            .set("padding", "0.45rem 0.75rem")
            .set("box-sizing", "border-box")
            .set("white-space", "normal")
            .set("overflow-wrap", "anywhere");

        if (width != null) {
            cell.setWidth(width);
            cell.setMinWidth(width);
            cell.setMaxWidth(width);
        }

        return cell;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String getRuleDisplayName(String ruleType){
        switch(safe(ruleType)){
            case "OVERLAP CHECK":
                return "Overlap Check";
            case "TIME BETWEEN CHECK":
                return "Time Between Check";
            case "PROXIMITY CHECK":
                return "Instructor Proximity Check";
            default:
                return safe(ruleType);
        }
    }

    private String getRuleIssueDescription(String ruleType){
        switch(safe(ruleType)){
            case "OVERLAP CHECK":
                return "These classes meet at overlapping times on at least one shared day.";
            case "TIME BETWEEN CHECK":
                return "These classes do not leave enough time between meetings.";
            case "PROXIMITY CHECK":
                return "These instructor assignments are too close together to allow reasonable travel and preparation time.";
            default:
                return "This entry violates the stated rule.";
        }
    }
}


