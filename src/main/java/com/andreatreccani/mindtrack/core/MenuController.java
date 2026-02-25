package com.andreatreccani.mindtrack.core;

import com.andreatreccani.mindtrack.model.Booking;
import com.andreatreccani.mindtrack.model.Objective;
import com.andreatreccani.mindtrack.model.User;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class MenuController {

    private final AppContext ctx;

    public MenuController(AppContext ctx) {
        this.ctx = ctx;
    }

    public void run() {
        while (true) {
            printMenu();
            int choice = ctx.io().readInt("Choose an option: ", 0, 5);

            switch (choice) {
                case 1 -> showObjectives();
                case 2 -> createBooking();
                case 3 -> cancelBooking();
                case 4 -> addUser();
                case 5 -> exportAvailable();
                case 0 -> {
                    ctx.io().println("Goodbye!");
                    return;
                }
            }
        }
    }

    private void printMenu() {
        ctx.io().println("\n=== MindTrack CLI ===");
        ctx.io().println("1) Show all objectives");
        ctx.io().println("2) Book an objective");
        ctx.io().println("3) Cancel booking");
        ctx.io().println("4) Add user");
        ctx.io().println("5) Export available objectives");
        ctx.io().println("0) Exit");
    }

    private void showObjectives() {
        List<Objective> list = ctx.objectives();
        if (list.isEmpty()) {
            ctx.io().println("No objectives found.");
            return;
        }

        for (Objective o : list) {
            ctx.io().println(
                    o.id() + " | " + o.title() + " | " +
                            o.period() + " | " + o.coins() + " | " +
                            (o.isAvailable() ? "YES" : "NO")
            );
        }
    }

    private void createBooking() {
        List<Objective> available = ctx.objectives().stream().filter(Objective::isAvailable).toList();
        if (available.isEmpty()) {
            ctx.io().println("No objectives available right now.");
            return;
        }

        ctx.io().println("Available objectives:");
        for (Objective o : available) {
            ctx.io().println(o.id() + " | " + o.title() + " | " + o.period() + " | " + o.coins());
        }

        int userId = ctx.io().readInt("User ID: ", 1, Integer.MAX_VALUE);
        User user = ctx.findUserById(userId);
        if (user == null) {
            ctx.io().println("User not found.");
            return;
        }

        int objectiveId = ctx.io().readInt("Objective ID: ", 1, Integer.MAX_VALUE);
        Objective objective = ctx.findObjectiveById(objectiveId);
        if (objective == null || !objective.isAvailable()) {
            ctx.io().println("Objective not available.");
            return;
        }

        // extra guard: avoid duplicate booking for same user & objective
        if (ctx.bookingExistsForUserAndObjective(userId, objectiveId)) {
            ctx.io().println("A booking for this user and objective already exists.");
            return;
        }

        LocalDate date = ctx.io().readDate("Booking date (YYYY-MM-DD): ");
        if (date.isBefore(LocalDate.now())) {
            ctx.io().println("Booking date cannot be in the past.");
            return;
        }

        int bookingId = ctx.nextBookingId();
        ctx.bookings().add(new Booking(bookingId, userId, objectiveId, date));
        objective.setAvailable(false);

        try {
            ctx.saveBookings();
            ctx.saveObjectives();
            ctx.io().println("Booking created with ID: " + bookingId);
        } catch (Exception e) {
            ctx.io().println("ERROR: could not save booking (" + e.getMessage() + "). Rolling back.");
            ctx.bookings().removeIf(b -> b.id() == bookingId);
            objective.setAvailable(true);
        }
    }

    private void cancelBooking() {
        if (ctx.bookings().isEmpty()) {
            ctx.io().println("No bookings found.");
            return;
        }

        int bookingId = ctx.io().readInt("Booking ID: ", 1, Integer.MAX_VALUE);
        Booking booking = ctx.findBookingById(bookingId);

        if (booking == null) {
            ctx.io().println("Booking not found.");
            return;
        }

        Objective obj = ctx.findObjectiveById(booking.objectiveId());

        ctx.bookings().remove(booking);
        if (obj != null) obj.setAvailable(true);

        try {
            ctx.saveBookings();
            ctx.saveObjectives();
            ctx.io().println("Booking cancelled.");
        } catch (Exception e) {
            ctx.io().println("ERROR: could not save cancellation (" + e.getMessage() + "). Rolling back.");
            ctx.bookings().add(booking);
            if (obj != null) obj.setAvailable(false);
        }
    }

    private void addUser() {
        String firstName = ctx.io().readNonEmptyString("First name: ");
        String lastName = ctx.io().readNonEmptyString("Last name: ");
        String email = ctx.io().readEmail("Email: ");

        if (ctx.emailExists(email)) {
            ctx.io().println("A user with this email already exists.");
            return;
        }

        int id = ctx.nextUserId();
        ctx.users().add(new User(id, firstName, lastName, email));

        try {
            ctx.saveUsers();
            ctx.io().println("User created with ID: " + id);
        } catch (Exception e) {
            ctx.io().println("ERROR: could not save user (" + e.getMessage() + "). Rolling back.");
            ctx.users().removeIf(u -> u.id() == id);
        }
    }

    private void exportAvailable() {
        try {
            Path out = ctx.storage().exportAvailableObjectives(ctx.objectives());
            ctx.io().println("Export created at: " + out.toAbsolutePath());
        } catch (Exception e) {
            ctx.io().println("ERROR: export failed (" + e.getMessage() + ")");
        }
    }
}

