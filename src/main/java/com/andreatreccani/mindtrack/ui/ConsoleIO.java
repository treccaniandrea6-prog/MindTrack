package com.andreatreccani.mindtrack.ui;

import java.time.LocalDate;
import java.util.Scanner;

public class ConsoleIO {

    private final Scanner scanner = new Scanner(System.in);

    public void println(String msg) {
        System.out.println(msg);
    }

    public String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine());
                if (value >= min && value <= max) return value;
            } catch (Exception ignored) {}
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine());
            } catch (Exception ignored) {}
        }
    }

    public String readEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String email = scanner.nextLine();
            if (email.contains("@") && email.contains(".")) return email;
        }
    }
}