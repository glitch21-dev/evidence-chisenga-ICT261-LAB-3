# Lodge Billing Lab

ICT261 Advanced Java Programming - Lecture 3 Lab

**Name:** Evidence Chisenga
**Student number:** 202308123

## About

A JavaFX app that calculates and saves lodge bills. Saved bills are kept in memory only
and are lost when the app closes.

## Billing rules

- Room prices per night: Standard K 350.00, Deluxe K 550.00, Family K 750.00
- 5 nights or more: 5% off the room charge
- Extras get no discount. No tax.
- Total = room charge - discount + extras. Balance = total - amount paid.

## Features

- Form: guest name, room type, nights, extras, amount paid, notes
- Calculate and Save bill buttons with input validation
- Saved bills table (bill number, guest, room, nights, total, paid, balance)
- Toolbar and menus: New guest, View receipt, Delete selected bill (with confirmation)
- Dark (Aurora) theme styled with CSS

## Structure

| File | Role |
| --- | --- |
| `App.java` | View: window, menus, toolbar, form, table |
| `BillingController.java` | Controller: validation, button actions, feedback |
| `BillingService.java` | Service: prices, billing rules, saved bills list |
| `Bill.java` | Model: one bill |
| `style.css` | Appearance |

## Run

From the folder containing `pom.xml` (JDK 17+):

```
mvn clean javafx:run
```

## Screenshots

See the `screenshots/` folder.
# evidence-chisenga-ICT261-LAB-3
