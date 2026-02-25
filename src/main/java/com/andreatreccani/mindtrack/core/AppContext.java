package com.andreatreccani.mindtrack.core;

import com.andreatreccani.mindtrack.io.CsvStorage;
import com.andreatreccani.mindtrack.model.Booking;
import com.andreatreccani.mindtrack.model.Objective;
import com.andreatreccani.mindtrack.model.User;
import com.andreatreccani.mindtrack.ui.ConsoleIO;

import java.util.ArrayList;
import java.util.List;

public class AppContext {

    private final ConsoleIO io;
    private final CsvStorage storage;

    private final List<User> users = new ArrayList<>();
    private final List<Objective> objectives = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public AppContext(ConsoleIO io, CsvStorage storage) {
        this.io = io;
        this.storage = storage;
    }

    public ConsoleIO io() { return io; }
    public CsvStorage storage() { return storage; }

    public List<User> users() { return users; }
    public List<Objective> objectives() { return objectives; }
    public List<Booking> bookings() { return bookings; }

    public void loadAll() throws Exception {
        users.clear();
        objectives.clear();
        bookings.clear();

        users.addAll(storage.readUsers());
        objectives.addAll(storage.readObjectives());
        bookings.addAll(storage.readBookings());

        // Normalize: if there is ANY booking for an objective => objective must be unavailable
        boolean changed = normalizeAvailabilityFromBookings();

        // Save only if normalization changed something
        if (changed) {
            saveObjectives();
        }
    }

    private boolean normalizeAvailabilityFromBookings() {
        boolean changed = false;

        for (Booking b : bookings) {
            Objective o = findObjectiveById(b.objectiveId());
            if (o == null) {
                io.println("WARNING: booking " + b.id() + " references missing objective id " + b.objectiveId());
                continue;
            }
            if (o.isAvailable()) {
                o.setAvailable(false);
                changed = true;
            }
        }

        return changed;
    }

    public void saveUsers() throws Exception { storage.writeUsers(users); }
    public void saveObjectives() throws Exception { storage.writeObjectives(objectives); }
    public void saveBookings() throws Exception { storage.writeBookings(bookings); }

    public void saveAll() throws Exception {
        saveUsers();
        saveObjectives();
        saveBookings();
    }

    public User findUserById(int id) {
        return users.stream().filter(u -> u.id() == id).findFirst().orElse(null);
    }

    public Objective findObjectiveById(int id) {
        return objectives.stream().filter(o -> o.id() == id).findFirst().orElse(null);
    }

    public Booking findBookingById(int id) {
        return bookings.stream().filter(b -> b.id() == id).findFirst().orElse(null);
    }

    public boolean emailExists(String email) {
        if (email == null) return false;
        return users.stream().anyMatch(u -> u.email().equalsIgnoreCase(email.trim()));
    }

    public boolean bookingExistsForUserAndObjective(int userId, int objectiveId) {
        return bookings.stream().anyMatch(b -> b.userId() == userId && b.objectiveId() == objectiveId);
    }

    public int nextBookingId() {
        return bookings.stream().mapToInt(Booking::id).max().orElse(0) + 1;
    }

    public int nextUserId() {
        return users.stream().mapToInt(User::id).max().orElse(0) + 1;
    }
}

