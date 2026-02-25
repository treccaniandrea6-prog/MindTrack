package com.andreatreccani.mindtrack.io;

import com.andreatreccani.mindtrack.model.Booking;
import com.andreatreccani.mindtrack.model.Objective;
import com.andreatreccani.mindtrack.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvStorage {

    private static final String SEP = ";";

    private static final String USERS_HEADER = "id;first_name;last_name;email";
    private static final String OBJECTIVES_HEADER = "id;title;period;coins;available";
    private static final String BOOKINGS_HEADER = "id;user_id;objective_id;date";

    private final Path usersCsv;
    private final Path objectivesCsv;
    private final Path bookingsCsv;

    public CsvStorage(Path usersCsv, Path objectivesCsv, Path bookingsCsv) {
        this.usersCsv = usersCsv;
        this.objectivesCsv = objectivesCsv;
        this.bookingsCsv = bookingsCsv;
    }

    // -------------------------
    // READ
    // -------------------------

    public List<User> readUsers() throws IOException {
        ensureFile(usersCsv, USERS_HEADER);

        List<User> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(usersCsv)) {
            String header = br.readLine(); // header
            if (header == null || header.isBlank()) {
                // file vuoto: lo ripristino
                ensureFile(usersCsv, USERS_HEADER);
                return list;
            }

            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                String[] p = split(line, 4);
                if (p == null) {
                    warn("users.csv", row, line, "Expected 4 columns");
                    continue;
                }

                try {
                    int id = Integer.parseInt(p[0].trim());
                    String first = p[1].trim();
                    String last = p[2].trim();
                    String email = p[3].trim();

                    list.add(new User(id, first, last, email));
                } catch (Exception ex) {
                    warn("users.csv", row, line, ex.getMessage());
                }
            }
        }

        return list;
    }

    public List<Objective> readObjectives() throws IOException {
        ensureFile(objectivesCsv, OBJECTIVES_HEADER);

        List<Objective> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(objectivesCsv)) {
            String header = br.readLine(); // header
            if (header == null || header.isBlank()) {
                ensureFile(objectivesCsv, OBJECTIVES_HEADER);
                return list;
            }

            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                String[] p = split(line, 5);
                if (p == null) {
                    warn("objectives.csv", row, line, "Expected 5 columns");
                    continue;
                }

                try {
                    int id = Integer.parseInt(p[0].trim());
                    String title = p[1].trim();
                    String period = p[2].trim();
                    int coins = Integer.parseInt(p[3].trim());
                    boolean available = parseAvailable(p[4].trim());

                    list.add(new Objective(id, title, period, coins, available));
                } catch (Exception ex) {
                    warn("objectives.csv", row, line, ex.getMessage());
                }
            }
        }

        return list;
    }

    public List<Booking> readBookings() throws IOException {
        ensureFile(bookingsCsv, BOOKINGS_HEADER);

        List<Booking> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(bookingsCsv)) {
            String header = br.readLine(); // header
            if (header == null || header.isBlank()) {
                ensureFile(bookingsCsv, BOOKINGS_HEADER);
                return list;
            }

            String line;
            int row = 1;
            while ((line = br.readLine()) != null) {
                row++;
                if (line.isBlank()) continue;

                String[] p = split(line, 4);
                if (p == null) {
                    warn("bookings.csv", row, line, "Expected 4 columns");
                    continue;
                }

                try {
                    int id = Integer.parseInt(p[0].trim());
                    int userId = Integer.parseInt(p[1].trim());
                    int objectiveId = Integer.parseInt(p[2].trim());

                    LocalDate date;
                    try {
                        date = LocalDate.parse(p[3].trim());
                    } catch (DateTimeParseException dtpe) {
                        warn("bookings.csv", row, line, "Invalid date format (expected YYYY-MM-DD)");
                        continue;
                    }

                    list.add(new Booking(id, userId, objectiveId, date));
                } catch (Exception ex) {
                    warn("bookings.csv", row, line, ex.getMessage());
                }
            }
        }

        return list;
    }

    // -------------------------
    // WRITE
    // -------------------------

    public void writeUsers(List<User> users) throws IOException {
        ensureParentDir(usersCsv);
        try (BufferedWriter bw = Files.newBufferedWriter(usersCsv)) {
            bw.write(USERS_HEADER);
            bw.newLine();
            for (User u : users) {
                bw.write(u.id() + SEP + u.firstName() + SEP + u.lastName() + SEP + u.email());
                bw.newLine();
            }
        }
    }

    public void writeObjectives(List<Objective> objectives) throws IOException {
        ensureParentDir(objectivesCsv);
        try (BufferedWriter bw = Files.newBufferedWriter(objectivesCsv)) {
            bw.write(OBJECTIVES_HEADER);
            bw.newLine();
            for (Objective o : objectives) {
                bw.write(
                        o.id() + SEP + o.title() + SEP + o.period() + SEP + o.coins() + SEP + (o.isAvailable() ? "YES" : "NO")
                );
                bw.newLine();
            }
        }
    }

    public void writeBookings(List<Booking> bookings) throws IOException {
        ensureParentDir(bookingsCsv);
        try (BufferedWriter bw = Files.newBufferedWriter(bookingsCsv)) {
            bw.write(BOOKINGS_HEADER);
            bw.newLine();
            for (Booking b : bookings) {
                bw.write(b.id() + SEP + b.userId() + SEP + b.objectiveId() + SEP + b.date());
                bw.newLine();
            }
        }
    }

    // -------------------------
    // EXPORT
    // -------------------------

    public Path exportAvailableObjectives(List<Objective> objectives) throws IOException {
        Path exportDir = Path.of("exports");
        Files.createDirectories(exportDir);

        String filename = "available_objectives_" + LocalDate.now() + ".csv";
        Path out = exportDir.resolve(filename);

        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            // export "pulito": niente colonna available
            bw.write("id;title;period;coins");
            bw.newLine();

            for (Objective o : objectives) {
                if (!o.isAvailable()) continue;
                bw.write(o.id() + SEP + o.title() + SEP + o.period() + SEP + o.coins());
                bw.newLine();
            }
        }

        return out;
    }

    // -------------------------
    // HELPERS
    // -------------------------

    private void ensureFile(Path file, String header) throws IOException {
        ensureParentDir(file);

        if (!Files.exists(file)) {
            try (BufferedWriter bw = Files.newBufferedWriter(file)) {
                bw.write(header);
                bw.newLine();
            }
            return;
        }

        // Se esiste ma è vuoto, lo ripristino con header
        if (Files.size(file) == 0) {
            try (BufferedWriter bw = Files.newBufferedWriter(file)) {
                bw.write(header);
                bw.newLine();
            }
        }
    }

    private void ensureParentDir(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);
    }

    private String[] split(String line, int expectedCols) {
        String[] parts = line.split(SEP, -1);
        if (parts.length < expectedCols) return null;
        return parts;
    }

    private boolean parseAvailable(String raw) {
        // accepts legacy italian values too
        if (raw == null) return false;
        String v = raw.trim();
        if (v.equalsIgnoreCase("YES")) return true;
        if (v.equalsIgnoreCase("SI")) return true;
        if (v.equalsIgnoreCase("TRUE")) return true;
        return false; // NO / FALSE / qualsiasi altro
    }

    private void warn(String file, int row, String line, String reason) {
        System.out.println("[WARN] " + file + " line " + row + " skipped (" + reason + "): " + line);
    }
}