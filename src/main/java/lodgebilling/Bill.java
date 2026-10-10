package lodgebilling;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Model class: one lodge bill. All money values are BigDecimal with 2 decimals.
 * Author: Evidence Chisenga (202308123)
 */
public class Bill {

    private String id;
    private final String guest;
    private final String room;
    private final int nights;
    private final BigDecimal roomCharge;
    private final BigDecimal discount;
    private final BigDecimal extras;
    private final BigDecimal total;
    private final BigDecimal paid;
    private final BigDecimal balance;
    private final String notes;

    /**
     * Creates a bill and works out the total and balance.
     *
     * @param guest      guest name
     * @param room       room type
     * @param nights     number of nights
     * @param roomCharge price per night multiplied by nights
     * @param discount   discount taken off the room charge
     * @param extras     extra charges (no discount)
     * @param paid       amount already paid
     * @param notes      optional remarks
     */
    public Bill(String guest, String room, int nights, BigDecimal roomCharge,
                BigDecimal discount, BigDecimal extras, BigDecimal paid, String notes) {
        this.guest = guest;
        this.room = room;
        this.nights = nights;
        this.roomCharge = roomCharge;
        this.discount = discount;
        this.extras = extras;
        this.paid = paid;
        this.notes = notes;
        this.total = roomCharge.subtract(discount).add(extras);
        this.balance = total.subtract(paid);
    }

    /**
     * Formats an amount in Zambian Kwacha, e.g. "K 2812.50".
     *
     * @param amount the amount to format
     * @return the formatted text
     */
    public static String money(BigDecimal amount) {
        return String.format(Locale.US, "K %.2f", amount);
    }

    /** @return the bill number, or null if not saved yet */
    public String getId() { return id; }

    /** @param id the bill number assigned when the bill is saved */
    public void setId(String id) { this.id = id; }

    /** @return guest name */
    public String getGuest() { return guest; }

    /** @return room type */
    public String getRoom() { return room; }

    /** @return number of nights */
    public int getNights() { return nights; }

    /** @return room charge before discount */
    public BigDecimal getRoomCharge() { return roomCharge; }

    /** @return discount amount */
    public BigDecimal getDiscount() { return discount; }

    /** @return extras amount */
    public BigDecimal getExtras() { return extras; }

    /** @return total amount due */
    public BigDecimal getTotal() { return total; }

    /** @return amount paid */
    public BigDecimal getPaid() { return paid; }

    /** @return outstanding balance */
    public BigDecimal getBalance() { return balance; }

    /** @return optional remarks */
    public String getNotes() { return notes; }

    /** @return total formatted for the table */
    public String getTotalText() { return money(total); }

    /** @return paid formatted for the table */
    public String getPaidText() { return money(paid); }

    /** @return balance formatted for the table */
    public String getBalanceText() { return money(balance); }

    /** @return a plain-text receipt for this bill */
    public String toReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Guest: ").append(guest).append('\n');
        sb.append("Room: ").append(room).append(" x ").append(nights).append(" night(s)\n");
        sb.append("Room charge: ").append(money(roomCharge)).append('\n');
        sb.append("Discount: ").append(money(discount)).append('\n');
        sb.append("Extras: ").append(money(extras)).append('\n');
        sb.append("Total: ").append(money(total)).append('\n');
        sb.append("Paid: ").append(money(paid)).append('\n');
        sb.append("Balance: ").append(money(balance));
        if (notes != null && !notes.isBlank()) {
            sb.append("\nNotes: ").append(notes.trim());
        }
        return sb.toString();
    }
}
