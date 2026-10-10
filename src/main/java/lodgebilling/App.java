package lodgebilling;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class App extends Application {

    @Override
    public void start(Stage stage) {
        BillingService service = new BillingService();

        // --- Form fields ---
        TextField guestField = new TextField();
        ComboBox<String> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(service.getRoomTypes());
        roomBox.getSelectionModel().selectFirst();
        roomBox.setMaxWidth(Double.MAX_VALUE);
        TextField nightsField = new TextField("1");
        TextField extrasField = new TextField("0");
        TextField paidField = new TextField("0");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Optional guest remarks");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        // --- Buttons and status ---
        Button calculateButton = new Button("Calculate");
        Button saveButton = new Button("Save bill");
        saveButton.getStyleClass().add("primary");
        HBox formButtons = new HBox(10, calculateButton, saveButton);

        Label status = new Label();
        status.setWrapText(true);

        // --- Form grid (labels linked to fields for accessibility) ---
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        addRow(grid, 0, "Guest name", guestField);
        addRow(grid, 1, "Room type", roomBox);
        addRow(grid, 2, "Nights", nightsField);
        addRow(grid, 3, "Extras in K", extrasField);
        addRow(grid, 4, "Amount paid in K", paidField);
        addRow(grid, 5, "Notes", notesArea);
        grid.add(formButtons, 1, 6);

        // --- Right side price panel ---
        Label pricesHeading = new Label("Practice room prices");
        pricesHeading.getStyleClass().add("section-label");
        ListView<String> priceList = new ListView<>();
        priceList.getItems().addAll(service.getPriceLines());
        priceList.setPrefHeight(110);
        priceList.setFocusTraversable(false);
        VBox side = new VBox(8, pricesHeading, priceList,
                note("5 nights or more: 5% off the room charge."),
                note("Extras get no discount. No tax in this lab."),
                note("Saved bills last only until you close the app."));
        side.setPrefWidth(300);
        side.setMinWidth(300);

        HBox formArea = new HBox(24, grid, side);
        HBox.setHgrow(grid, Priority.ALWAYS);

        // --- Table ---
        Label billsHeading = new Label("Saved bills");
        billsHeading.getStyleClass().add("section-label");
        TableView<Bill> table = new TableView<>();
        table.setItems(service.getBills());
        table.setPlaceholder(new Label("No saved bills yet."));
        table.getColumns().add(column("Bill", "id", 70));
        table.getColumns().add(column("Guest", "guest", 200));
        table.getColumns().add(column("Room", "room", 110));
        table.getColumns().add(column("Nights", "nights", 80));
        table.getColumns().add(column("Total", "totalText", 120));
        table.getColumns().add(column("Paid", "paidText", 120));
        table.getColumns().add(column("Balance", "balanceText", 120));
        VBox.setVgrow(table, Priority.ALWAYS);

        // --- Controller wiring ---
        BillingController controller = new BillingController(guestField, roomBox,
                nightsField, extrasField, paidField, notesArea, status, table, service);
        calculateButton.setOnAction(event -> controller.handleCalculate());
        saveButton.setOnAction(event -> controller.handleSave());

        // --- Menu bar and toolbar ---
        MenuItem newItem = new MenuItem("New guest");
        newItem.setOnAction(event -> controller.handleNewGuest());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> Platform.exit());
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> controller.handleAbout());
        MenuBar menuBar = new MenuBar(
                new Menu("File", null, newItem, exitItem),
                new Menu("Help", null, aboutItem));

        Button newGuestButton = new Button("New guest");
        newGuestButton.setOnAction(event -> controller.handleNewGuest());
        Button receiptButton = new Button("View receipt");
        receiptButton.setOnAction(event -> controller.handleViewReceipt());
        Button deleteButton = new Button("Delete selected bill");
        deleteButton.setOnAction(event -> controller.handleDelete());
        ToolBar toolBar = new ToolBar(newGuestButton, receiptButton, deleteButton);

        // --- Page content ---
        Label title = new Label("Lodge Billing Lab");
        title.getStyleClass().add("title-label");
        Label subtitle = new Label(
                "ICT261 Lecture 3   Evidence Chisenga (202308123)   Mulungushi University");

        VBox content = new VBox(10, title, subtitle, formArea, status, billsHeading, table);
        content.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox root = new VBox(menuBar, toolBar, content);
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene scene = new Scene(root, 980, 780);
        if (App.class.getResource("/style.css") != null) {
            scene.getStylesheets().add(App.class.getResource("/style.css").toExternalForm());
        } else {
            System.err.println("style.css not found; using default look.");
        }

        stage.setTitle("Lodge Billing Lab - Evidence Chisenga (202308123)");
        stage.setScene(scene);
        stage.show();
        Platform.runLater(guestField::requestFocus);
    }

    /**
     * Adds a label and its input to one grid row.
     *
     * @param grid  the form grid
     * @param row   the row number
     * @param text  the label text
     * @param input the input control
     */
    private void addRow(GridPane grid, int row, String text, javafx.scene.control.Control input) {
        Label label = new Label(text);
        label.setLabelFor(input);
        label.setMinWidth(120);
        GridPane.setHgrow(input, Priority.ALWAYS);
        input.setMaxWidth(Double.MAX_VALUE);
        grid.add(label, 0, row);
        grid.add(input, 1, row);
    }

    /**
     * Creates a wrapped note label for the side panel.
     *
     * @param text the note text
     * @return the label
     */
    private Label note(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(300);
        return label;
    }

    /**
     * Creates a table column bound to a Bill property.
     *
     * @param title    column heading
     * @param property Bill property name
     * @param width    preferred width
     * @return the column
     */
    private TableColumn<Bill, Object> column(String title, String property, double width) {
        TableColumn<Bill, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
