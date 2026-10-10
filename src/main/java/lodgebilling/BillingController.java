package lodgebilling;

import java.math.BigDecimal;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller: reads and validates the form, calls the service and gives feedback.
 */
public class BillingController {

    private final TextField guestField;
    private final ComboBox<String> roomBox;
    private final TextField nightsField;
    private final TextField extrasField;
    private final TextField paidField;
    private final TextArea notesArea;
    private final Label status;
    private final TableView<Bill> table;
    private final BillingService service;

    /**
     * Creates the controller.
     *
     * @param guestField  guest name input
     * @param roomBox     room type list
     * @param nightsField nights input
     * @param extrasField extras input
     * @param paidField   amount paid input
     * @param notesArea   notes input
     * @param status      feedback label
     * @param table       saved bills table
     * @param service     billing service
     */
    public BillingController(TextField guestField, ComboBox<String> roomBox,
                             TextField nightsField, TextField extrasField,
                             TextField paidField, TextArea notesArea, Label status,
                             TableView<Bill> table, BillingService service) {
        this.guestField = guestField;
        this.roomBox = roomBox;
        this.nightsField = nightsField;
        this.extrasField = extrasField;
        this.paidField = paidField;
        this.notesArea = notesArea;
        this.status = status;
        this.table = table;
        this.service = service;
    }

    /** Shows the calculated total and balance without saving. */
    public void handleCalculate() {
        Bill bill = readBill();
        if (bill == null) {
            return;
        }
        showStatus("Room " + Bill.money(bill.getRoomCharge())
                + ", discount " + Bill.money(bill.getDiscount())
                + ", extras " + Bill.money(bill.getExtras())
                + ". Total: " + Bill.money(bill.getTotal())
                + ". Balance: " + Bill.money(bill.getBalance()), false);
    }

    /** Validates, calculates and saves the bill. */
    public void handleSave() {
        Bill bill = readBill();
        if (bill == null) {
            return;
        }
        service.save(bill);
        showStatus("Saved " + bill.getId() + ". Balance: "
                + Bill.money(bill.getBalance()), false);
    }

    /** Clears the form for the next guest. */
    public void handleNewGuest() {
        guestField.clear();
        roomBox.getSelectionModel().selectFirst();
        nightsField.setText("1");
        extrasField.setText("0");
        paidField.setText("0");
        notesArea.clear();
        status.setText("");
        guestField.requestFocus();
    }

    /** Shows the receipt of the selected bill. */
    public void handleViewReceipt() {
        Bill selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a bill first.", true);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt");
        alert.setHeaderText("Receipt " + selected.getId());
        alert.setContentText(selected.toReceipt());
        alert.showAndWait();
    }

    /** Asks for confirmation, then deletes the selected bill. */
    public void handleDelete() {
        Bill selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a bill first.", true);
            return;
        }
        ButtonType delete = new ButtonType("Delete");
        Alert ask = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete bill " + selected.getId() + " for " + selected.getGuest() + "?",
                delete, ButtonType.CANCEL);
        ask.setHeaderText("Confirm deletion");
        if (ask.showAndWait().orElse(ButtonType.CANCEL) == delete) {
            service.delete(selected);
            showStatus("Bill " + selected.getId() + " deleted.", false);
        } else {
            showStatus("Deletion cancelled.", false);
        }
    }

    /** Shows a short About dialog. */
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Lodge Billing Lab");
        alert.setContentText("ICT261 Lecture 3\nEvidence Chisenga (202308123)\n"
                + "Saved bills last only until you close the app.");
        alert.showAndWait();
    }

    /**
     * Reads and validates the form.
     *
     * @return the calculated bill, or null after showing an error message
     */
    private Bill readBill() {
        String guest = guestField.getText().trim();
        if (guest.isEmpty()) {
            showStatus("Enter the guest name.", true);
            guestField.requestFocus();
            return null;
        }

        String room = roomBox.getValue();
        if (room == null) {
            showStatus("Choose a room type.", true);
            roomBox.requestFocus();
            return null;
        }

        int nights;
        try {
            nights = Integer.parseInt(nightsField.getText().trim());
        } catch (NumberFormatException ex) {
            showStatus("Nights must be a whole number.", true);
            nightsField.requestFocus();
            return null;
        }
        if (nights < 1) {
            showStatus("Nights must be at least 1.", true);
            nightsField.requestFocus();
            return null;
        }

        BigDecimal extras = readMoney(extrasField, "Extras");
        if (extras == null) {
            return null;
        }
        BigDecimal paid = readMoney(paidField, "Amount paid");
        if (paid == null) {
            return null;
        }

        try {
            return service.createBill(guest, room, nights, extras, paid, notesArea.getText());
        } catch (IllegalArgumentException ex) {
            showStatus(ex.getMessage(), true);
            paidField.requestFocus();
            return null;
        }
    }

    /**
     * Reads a money field. A blank field counts as zero.
     *
     * @param field the input field
     * @param label the name used in error messages
     * @return the amount, or null after showing an error message
     */
    private BigDecimal readMoney(TextField field, String label) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text);
        } catch (NumberFormatException ex) {
            showStatus(label + " must be a number.", true);
            field.requestFocus();
            return null;
        }
        if (value.signum() < 0) {
            showStatus(label + " cannot be negative.", true);
            field.requestFocus();
            return null;
        }
        return value;
    }

    /**
     * Shows a message in the status label with an error or success style.
     *
     * @param message text to display
     * @param error   true for error style, false for success style
     */
    private void showStatus(String message, boolean error) {
        status.getStyleClass().removeAll("status-error", "status-success");
        status.getStyleClass().add(error ? "status-error" : "status-success");
        status.setText(message);
    }
}
