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
import com.vaadin.flow.component.*;
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
@PageTitle("Schedule Validation")
public class MainView extends AppLayout{

    //Main container for the page body below the navbar
    private final VerticalLayout mainContentLayout = new VerticalLayout();

    //Temporary directory that holds the SQLite database file
    private Path tempDatabaseDirectory;

    //Absolute path to the temporary SQLite databases file used for the queries
    private String databaseFilePath;

    //Parser generated row errors that's shown in the parsing errors dialog
    private ArrayList<String> parserErrors = new ArrayList<>();

    public MainView(){
        initializeTempDb();
        mainContentLayout.setSizeFull();
        setContent(mainContentLayout);
        addToNavbar(createHeader());
    }

    /*
        Creates a temporary SQLite database location for the current app session
        The database is recreated from uploaded CSV data and marked for deletion when the app exits
    */
    private void initializeTempDb(){
        try {
            Path systemTemp = Path.of(System.getProperty("java.io.tmpdir"));
            tempDatabaseDirectory = Files.createTempDirectory(systemTemp, ".schedule-validator-");
            Path dbFile = tempDatabaseDirectory.resolve("schedule.db");
            databaseFilePath = dbFile.toString();
            tempDatabaseDirectory.toFile().deleteOnExit();
            dbFile.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary database directory", e);
        }
    }

    /*
        Deletes the current temporary database file so a new one can be reuilt from the next uploaded CSV
     */
    private void resetTempDb(){
        try {
            Files.deleteIfExists(Path.of(databaseFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset database");
        }
    }

    /*
        Builds the persistent top navigation bar containing the app title and primary actions such as upload and help
    */
    private Component createHeader(){
        
        //Title Creation, Styling
        H1 pageTitle = new H1("Scheduling Validation Tool");
        pageTitle.getStyle().set("left", "var(--lumo-space-l)").set("font-size", "2rem").set("margin", "0")
                .set("position", "absolute").set("font-weight", "900");

        //Upload Button
        Button uploadButton = new Button("Upload", event->openUploadDialog());

        //Help Button
        Button helpButton = new Button("Help", event->openHelpDialog());

        //Header Creation
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.expand(pageTitle);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        Div leftSpacer = new Div();
        Div rightSpacer = new Div();
        header.add(pageTitle, leftSpacer, uploadButton, helpButton, rightSpacer);
        header.expand(rightSpacer, leftSpacer);
        return header;
    }

    /*
        Opens the CSV upload dialog, parses the uploaded file, runs all validation queries,
        and renders the resulting collisions in main content area
    */
    private void openUploadDialog(){
        mainContentLayout.removeAll();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Upload CSV");
        FileBuffer uploadBuffer = new FileBuffer();
        Upload csvUpload = new Upload(uploadBuffer);
        csvUpload.setAcceptedFileTypes(".csv");
        csvUpload.setMaxFiles(1);

        Paragraph instructions = new Paragraph("Upload your schedule CSV here.");

        csvUpload.addSucceededListener(event -> {
            try {
                String uploadedFileName = uploadBuffer.getFileName();
                resetTempDb();
                parserErrors = Parser.parseFile(databaseFilePath, uploadBuffer.getFileData().getFile().getAbsolutePath(), ",");
                ArrayList<Collision> validationResults = collectValidationResults();
                renderResults(uploadedFileName, validationResults);
                Notification notification = Notification.show("Upload and validation completed", 3000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();
            } catch (Exception e) {
                Notification notification = Notification.show("Validation Failed: " + e.getMessage(), 5000, Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        csvUpload.addFailedListener(event -> {
            Notification notification = Notification.show("Upload Failed", 4000, Notification.Position.TOP_END);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            dialog.close();
        });
        Button cancel = new Button("Cancel", event->dialog.close());
        VerticalLayout uploadDialogLayout = new VerticalLayout(instructions, csvUpload);
        uploadDialogLayout.setPadding(false);
        uploadDialogLayout.setSpacing(true);
        dialog.add(uploadDialogLayout);
        dialog.getFooter().add(cancel);
        dialog.open();
        }      
    
    /*
        Removes duplicate one hit collisions that may be returned by overlapping query logic
        This currently only deduplicates collisions that contain exactly one hit, grouped collisions are left unchanged
    */
    private void removeDuplicateCollisions(ArrayList<Collision> validationResults){
        for(int i = 0; i < validationResults.size(); i++){
            Collision a = validationResults.get(i);
            for(int j = i + 1; j < validationResults.size(); j++){
                Collision b = validationResults.get(j);
                if(areSameCollision(a, b)){
                    validationResults.remove(j);
                    j--;
                }
            }
        }
    }

    /*
        Builds the validation results grid, including the expand/collapse toggle,
        summary columns, and detail renderer for each collision row
    */
    private Grid<Collision> buildResultsGrid(ArrayList<Collision> validationResults){
        Grid<Collision> resultsGrid = new Grid<>(Collision.class, false);
        resultsGrid.setWidthFull();
        resultsGrid.setAllRowsVisible(true);
        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        resultsGrid.addComponentColumn(collision->buildRowToggleButton(resultsGrid, collision))
            .setHeader("")
            .setWidth("36px")
            .setFlexGrow(0);

        resultsGrid.addColumn(collision -> getRuleDisplayName(collision.getTypeSafe()))
            .setHeader("Rule")
            .setAutoWidth(true)
            .setFlexGrow(0);
       
        resultsGrid.addComponentColumn(this::buildImpactBadge)
            .setHeader("Impact")
            .setAutoWidth(true)
            .setFlexGrow(0);

        resultsGrid.addComponentColumn(collision -> buildWrappedTextSpan(formatCourse(collision.base)))
            .setHeader("Base Class")
            .setAutoWidth(false)
            .setFlexGrow(1);

        resultsGrid.addComponentColumn(collision -> buildWrappedTextSpan(formatConflictingCourses(collision)))
            .setHeader("Conflicting Class(es)")
            .setAutoWidth(false)
            .setFlexGrow(1);

        resultsGrid.setItemDetailsRenderer(buildCollisionDetailsRenderer());
        resultsGrid.setItems(validationResults);
        return resultsGrid;
    }

    private Span buildImpactBadge(Collision collision){
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
    }

    private Span buildWrappedTextSpan(String value){
        Span text = new Span(value);
        text.getStyle().set("white-space", "normal").set("line-height", "1.3").set("display", "block");
        return text;
    }

    private Button buildRowToggleButton(Grid<Collision> resultsGrid, Collision collision){
        Button toggleButton = new Button(resultsGrid.isDetailsVisible(collision) ? "▼" : "▶");
        toggleButton.getStyle().set("min-width", "18px").set("width", "18px").set("padding", "0").set("font-size", "0.9rem").set("font-weight", "700").set("line-height", "1").set("border", "none").set("background", "transparent").set("box-shadow", "none");
        toggleButton.addClickListener(event->{
            boolean currentlyVisible = resultsGrid.isDetailsVisible(collision);
            resultsGrid.setDetailsVisible(collision, !currentlyVisible);
            resultsGrid.getDataProvider().refreshItem(collision);
        });
        return toggleButton;
    }

    /*
        Creates the expandable detail section shown beneath each collision row.
        The detail layout includes a short rule description and a view of the base course and conflicting course
    */
    private ComponentRenderer<Component, Collision> buildCollisionDetailsRenderer(){
        return new ComponentRenderer<>(collision-> { 
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
        });
    }

    /*
        Determines whether two collisions should be treated as the same UI result
        Equality here is based on rule type plus the base course pairing, allowing reversed order comparisions.
    */
    private boolean areSameCollision(Collision firstCollision, Collision secondCollision){
        if(firstCollision == null || secondCollision == null || firstCollision.base == null || secondCollision.base == null)
            return false;
        if(!safeString(firstCollision.getTypeSafe()).equals(safeString(secondCollision.getTypeSafe())))
            return false;
        if(firstCollision.hits == null || secondCollision.hits == null || firstCollision.hits.size()!=1 || secondCollision.hits.size()!=1)
            return false;
        Course firstHit = firstCollision.hits.get(0);
        Course secondHit = secondCollision.hits.get(0);
        return (coursesMatch(firstCollision.base, secondCollision.base) && coursesMatch(firstHit, secondHit)) || (coursesMatch(firstCollision.base, secondHit) && coursesMatch(firstHit, secondCollision.base));
    }

    /*
        Compares two course records using the fields that matter for validation display and collision deduplicating
    */
    private boolean coursesMatch(Course firstCourse, Course secondCourse){
        if(firstCourse == null || secondCourse == null)
            return false;
        return firstCourse.clas_num == secondCourse.clas_num
            && firstCourse.course_num == secondCourse.course_num
            && safeString(firstCourse.type).equals(safeString(secondCourse.type))
            && safeString(firstCourse.days).equals(safeString(secondCourse.days))
            && safeString(firstCourse.start).equals(safeString(secondCourse.start))
            && safeString(firstCourse.end).equals(safeString(secondCourse.end))
            && safeString(firstCourse.room).equals(safeString(secondCourse.room))
            && safeString(firstCourse.instructor).equals(safeString(secondCourse.instructor));
    }
    
    /*
        Opens the help dialog and loads the help text from the help.txt file in resources folder
    */
    private void openHelpDialog(){
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

    /*
        Rebuilds the main content area to show the latest validation results
        for an uploaded schedule file.
    */
    private void renderResults(String fileName, ArrayList<Collision> validationResults){
        mainContentLayout.removeAll();

        //Title
        H3 resultsTitle = new H3("Validation Results");
        resultsTitle.getStyle().set("font-weight", "800").set("font-size", "1.5rem").set("line-height", "1.2").set("margin", "0");
        mainContentLayout.add(resultsTitle);

        validationResults.sort((first, second) -> Integer.compare(second.impact, first.impact));
        Grid<Collision> resultsGrid = buildResultsGrid(validationResults);
        HorizontalLayout resultsToolbar = buildResultsToolbar(fileName, validationResults, resultsGrid);

        if(validationResults.isEmpty()){
            mainContentLayout.add(resultsToolbar, new Paragraph("No rule violations were found."));
            return;
        }
        mainContentLayout.add(resultsToolbar, resultsGrid);
    }

    /*
        Builds the toolbar shown above the results grid.
        Displays the issue count, uploaded file name, parser error dialog, and expand/collapse actions when validation results exist
    */
    private HorizontalLayout buildResultsToolbar(String fileName, ArrayList<Collision> validationResults, Grid<Collision> resultsGrid){
        //Total Issues
        Paragraph totalIssues = new Paragraph("Total Issues Found: " + validationResults.size());
        totalIssues.getStyle().set("font-weight", "700").set("margin", "0").set("font-size", "1rem");
        
        //Name of file 
        Div uploadedFileBadge = new Div();
        uploadedFileBadge.setText("File: " + fileName);
        uploadedFileBadge.getStyle().set("padding", "0.45rem 0.85rem").set("border-radius", "10px").set("background", "var(--lumo-contrast-5pct)").set("font-weight", "600").set("font-size", "0.95rem").set("color", "var(--lumo-body-text-color)");

        HorizontalLayout toolbarLeftSection = new HorizontalLayout(totalIssues);
        toolbarLeftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbarLeftSection.setSpacing(true);

        HorizontalLayout toolbarRightSection = new HorizontalLayout();
        toolbarRightSection.setSpacing(true);
        toolbarRightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbarRightSection.add(uploadedFileBadge);

        //Show Parsing Errors Dialog Button
        if(!parserErrors.isEmpty()){
            Button parsingErrorsButton = new Button("Parsing Errors (" + parserErrors.size() + ")", event -> openParsingErrorsDialog());
            toolbarRightSection.add(parsingErrorsButton);
        }

        if(resultsGrid != null && !validationResults.isEmpty()){
            Button expandAllButton = new Button("Expand All", event->{
                for(Collision collision : validationResults){
                    resultsGrid.setDetailsVisible(collision, true);
                }
                resultsGrid.getDataProvider().refreshAll();
            });
            Button collapseAllButton = new Button("Collapse All", event->{
                for(Collision collision : validationResults){
                    resultsGrid.setDetailsVisible(collision, false);
                }
                resultsGrid.getDataProvider().refreshAll();
            });
            toolbarRightSection.add(expandAllButton, collapseAllButton);
        }

        HorizontalLayout resultsToolbar = new HorizontalLayout();
        resultsToolbar.setWidthFull();
        resultsToolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        resultsToolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        resultsToolbar.setSpacing(true);
        resultsToolbar.getStyle().set("padding", "0.75rem 0").set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        resultsToolbar.add(toolbarLeftSection, toolbarRightSection);
        return resultsToolbar;
    }

    /*
        Runs all active validation queries against the temporary database and returns
        the combined collision list shown in the results grid
        
        THIS METHOD SHOULD BE UPDATED ON ANY RULE ADDITIONS
    */
    private ArrayList<Collision> collectValidationResults(){
        ArrayList<Collision> validationResults = new ArrayList<>();

        validationResults.addAll(Query.queryLecCollision(databaseFilePath));
        validationResults.addAll(Query.queryRecCollision(databaseFilePath, 30));
        validationResults.addAll(Query.queryTeacherProximity(databaseFilePath, 30));
        validationResults.addAll(Query.queryTestCrossRoom(databaseFilePath));
        validationResults.addAll(Query.queryCrossProf(databaseFilePath));
        validationResults.addAll(Query.queryRoomCollision(databaseFilePath));
        validationResults.addAll(Query.queryCrossTime(databaseFilePath));

        removeDuplicateCollisions(validationResults);
        return validationResults;
    }

    /*
        Formats a single course record into the compact summary shown in the main results grid
    */
    private String formatCourse(Course c){
        return c.clas_num + " | " + c.course_num + " | " + c.type + " | " + c.days + " | " + c.start + "-" + c.end + " | " + c.room + " | " + c.instructor;
    }

    /*
        Builds the summary text for all conflicting courses attached to a collision.

        Multiple hits are joined so they remain readable inside a single grid cell
    */
    private String formatConflictingCourses(Collision collision){
        StringBuilder formattedText = new StringBuilder();
        for(int i = 0; i < collision.hits.size(); i++){
            formattedText.append(formatCourse(collision.hits.get(i)));
            if(i < collision.hits.size() - 1)
                formattedText.append(" || ");
        }
        return formattedText.toString();
    }

    /*
        Builds the header row used in the expanded collision detail table
        This header stays consistent for both the base class and conflicting class shown beneath an expanded result row.
    */
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

    /*
        Builds a single row in the expanded collision detail table.

        The role label identifies whether the row represents the base class or a conflicting class,
        and the shaded flag is used to visually distinguish the base row from the others
    */
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
            detailCell(safeString(c.type), "80px"),
            detailCell(safeString(c.days), "70px"),
            detailCell(safeString(c.start), "120px"),
            detailCell(safeString(c.end), "120px"),
            detailCell(safeString(c.room), "130px"),
            detailCell(safeString(c.instructor), null)
        );

        row.expand(row.getComponentAt(row.getComponentCount() - 1));
        return row;
    }

    /*
        Creates a styled header cell for the expanded detail table
        Fixed widths are applied where needed so the detail rows stay aligned
    */
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

    /*
        Opens a dialog listing parser errors for CSV rows that could not be used during validation

        These errors typically represent missing required values or invalid row data
    */
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
        for(String error: parserErrors)
            errorsList.add(buildParsingErrorCard(error));
        layout.add(summary, errorsList);
        dialog.add(layout);
        dialog.setWidth("800px");
        dialog.setHeight("500px");
        dialog.open();
    }

    /*
        Converts a raw parser error string into a styled card for display in the parsing errors dialog.
        The parser error text is split into row, data, and problem sections so the user can more easily understand what was skipped and why
    */
    private Div buildParsingErrorCard(String rawError){
        Div errorCard = new Div();
        errorCard.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "8px").set("padding", "0.75rem").set("background", "var(--lumo-base-color)").set("white-space", "normal");
        String [] lines = rawError.split("\\n");
        StringBuilder problem = new StringBuilder();
        for (String line : lines){
            problem.append("<br>");
            problem.append(escapeHtml(line));
        }
        //Formats HTML so the line breaks and labels display cleanly in the dialog
        errorCard.getElement().setProperty("innerHTML", "<b>" + problem + "</b> ");
        return errorCard;
    }

    /*
        Escapes basic HTML sensitive characters before user facing text is put into HTML rendered content
    */
    private String escapeHtml(String text){
        if(text==null)
            return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /*
        Normalizes raw CSV row data for display in parsing error cards.
        Empty trailing columns are removed, and blank values are displayed as "null"
        so skipped row data is easier to inspect
    */
    private String formatParsingData(String rawData){
        String [] csvColumns = rawData.split(",", -1);
        int lastNonEmpty = csvColumns.length-1;
        while(lastNonEmpty>=0 && csvColumns[lastNonEmpty].trim().equals(""))
            lastNonEmpty--;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<=lastNonEmpty; i++){
            String value = csvColumns[i].trim();
            if(i>0)
                sb.append(", ");
            sb.append(value.isEmpty() ? "null" : value);
        }
        return sb.toString();
    }

    /*
        Creates a styled data cell for the expanded collision detail table
        Cells use wrapping so long values such as instructor names or room labels
        do not overflow the layout
    */
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

    /*
        Returns a non null string value for safer comparisons and UI rendering
    */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /*
        Maps the backend rule identifiers to shorter, user friendly labels that are shown 
        in the results grid and detail section headers
    */
    private String getRuleDisplayName(String ruleType){
    switch(safeString(ruleType)){
        case "Lecture overlaps with other lecture sections of the same course":
            return "Lecture Section Overlap";
        case "Lecture overlaps with its own recitation":
            return "Lecture/Recitation Overlap";
        case "Recitation of same section overlap":
            return "Same Section Recitation Overlap";
        case "Time between recitations of the same section is within 30 minutes":
            return "Same Section Recitation Spacing";
        case "Recitation of any section overlap":
            return "Cross-Section Recitation Overlap";
        case "Time between recitations of the any section is within 30 minutes":
            return "Cross-Section Recitation Spacing";
        case "TIME BETWEEN CHECK":
            return "Instructor Proximity Check";
        case "CROSS-LISTED ROOM MISMATCH":
            return "Cross-Listed Room Mismatch";
        case "CROSS-LISTED INSTRUCTOR MISMATCH":
            return "Cross-Listed Instructor Mismatch";
        case "ROOM COLLISION":
            return "Room Collision";
        default:
            return safeString(ruleType);
    }
}
    /*
        Maps backend rule identifiers to descriptive explanations shown in the expanded collision detail row
    */
    private String getRuleIssueDescription(String ruleType){
        switch(safeString(ruleType)){
            case "Lecture overlaps with other lecture sections of the same course":
                return "These lecture sections for the same course overlap in time on at least one shared meeting day.";
            case "Lecture overlaps with its own recitation":
                return "This lecture overlaps in time with one of its associated recitation meetings.";
            case "Recitation of same section overlap":
                return "These recitation meetings from the same section overlap in time on at least one shared day.";
            case "Time between recitations of the same section is within 30 minutes":
                return "These recitation meetings from the same section are scheduled too close together and leave less than 30 minutes between meetings.";
            case "Recitation of any section overlap":
                return "These recitation meetings overlap in time on at least one shared meeting day.";
            case "Time between recitations of the any section is within 30 minutes":
                return "These recitation meetings are scheduled too close together and leave less than 30 minutes between meetings.";
            case "TIME BETWEEN CHECK":
                return "These instructor assignments are too close together and do not leave enough time between meetings.";
            case "CROSS-LISTED ROOM MISMATCH":
                return "These cross-listed sections are assigned to different rooms when they should remain aligned.";
            case "CROSS-LISTED INSTRUCTOR MISMATCH":
                return "These cross-listed sections are assigned to different instructors when they should remain aligned.";
            case "ROOM COLLISION":
                return "These classes are assigned to the same room at overlapping times on at least one shared meeting day.";
            default:
                return "This entry violates the stated rule.";
        }
    }
}


