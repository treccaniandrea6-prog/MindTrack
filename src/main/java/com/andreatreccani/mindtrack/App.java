package com.andreatreccani.mindtrack;

import com.andreatreccani.mindtrack.core.AppContext;
import com.andreatreccani.mindtrack.core.MenuController;
import com.andreatreccani.mindtrack.io.CsvStorage;
import com.andreatreccani.mindtrack.ui.ConsoleIO;

import java.nio.file.Path;

public class App {

    public static void main(String[] args) {

        ConsoleIO io = new ConsoleIO();

        Path dataDir = Path.of("src", "main", "resources", "data");
        Path usersCsv = dataDir.resolve("users.csv");
        Path objectivesCsv = dataDir.resolve("objectives.csv");
        Path bookingsCsv = dataDir.resolve("bookings.csv");

        CsvStorage storage = new CsvStorage(usersCsv, objectivesCsv, bookingsCsv);
        AppContext context = new AppContext(io, storage);

        try {
            context.loadAll();
            new MenuController(context).run();
        } catch (Exception e) {
            io.println("FATAL ERROR: " + e.getMessage());
        }
    }
}