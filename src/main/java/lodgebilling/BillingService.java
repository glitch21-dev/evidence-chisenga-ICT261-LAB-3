package lodgebilling;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Service: holds the room prices, the billing rules and the saved bills.
 * Rules: 5% off the room charge for 5 or more nights; extras are not discounted; no tax.
 */
public class BillingService {

    private static final int DISCOUNT_NIGHTS = 5;
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    private final Map<String, BigDecimal> prices = new LinkedHashMap<>();
    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private int nextNumber = 1;

    /** Creates the service with the practice room prices. */
    public BillingService() {
        prices.put("Standard", new BigDecimal("350.00"));
        prices.put("Deluxe", new BigDecimal("550.00"));
        prices.put("Family", new BigDecimal("750.00"));
    }

    /** @return the room type names in display order */
    public List<String> getRoomTypes() {
        return new ArrayList<>(prices.keySet());
    }

    /** @return one text line per room, e.g. "Standard: K 350.00 per night" */
    public List<String> getPriceLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : prices.entrySet()) {
            lines.add(entry.getKey() + ": " + Bill.money(entry.getValue()) + " per night");
        }
        return lines;
    }

    /** @return the observable list shown in the table */
    public ObservableList<Bill> getBills() {
        return bills;
    }

    /**
     * Works out a bill from the form values (does not save it).
     *
     * @param guest  guest name
     * @param room   room type
     * @param nights number of nights (at least 1)
     * @param extras extras amount (not negative)
     * @param paid   amount paid (not negative, not more than the total)
     * @param notes  optional remarks
     * @return the calculated bill
     * @throws IllegalArgumentException if any value breaks the rules
     */
    public Bill createBill(String guest, String room, int nights,
                           BigDecimal extras, BigDecimal paid, String notes) {
        BigDecimal rate = prices.get(room);
        if (rate == null) {
            throw new IllegalArgumentException("Choose a room type.");
        }
        if (nights < 1) {
            throw new IllegalArgumentException("Nights must be at least 1.");
        }
        if (extras.signum() < 0 || paid.signum() < 0) {
            throw new IllegalArgumentException("Amounts cannot be negative.");
        }

        BigDecimal roomCharge = rate.multiply(BigDecimal.valueOf(nights))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = nights >= DISCOUNT_NIGHTS
                ? roomCharge.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal cleanExtras = extras.setScale(2, RoundingMode.HALF_UP);
        BigDecimal cleanPaid = paid.setScale(2, RoundingMode.HALF_UP);

        Bill bill = new Bill(guest, room, nights, roomCharge, discount,
                cleanExtras, cleanPaid, notes);
        if (cleanPaid.compareTo(bill.getTotal()) > 0) {
            throw new IllegalArgumentException("Amount paid is more than the total ("
                    + Bill.money(bill.getTotal()) + ").");
        }
        return bill;
    }

    /**
     * Gives the bill a number (B001, B002...) and adds it to the saved list.
     *
     * @param bill the calculated bill
     * @return the same bill, now numbered
     */
    public Bill save(Bill bill) {
        bill.setId(String.format("B%03d", nextNumber++));
        bills.add(bill);
        return bill;
    }

    /**
     * Removes a saved bill.
     *
     * @param bill the bill to remove (must not be null)
     * @throws IllegalArgumentException if no bill is given
     */
    public void delete(Bill bill) {
        if (bill == null) {
            throw new IllegalArgumentException("Select a bill first.");
        }
        bills.remove(bill);
    }
}
