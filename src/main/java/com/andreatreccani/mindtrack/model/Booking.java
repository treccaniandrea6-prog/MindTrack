package com.andreatreccani.mindtrack.model;

import java.time.LocalDate;

public record Booking(int id, int userId, int objectiveId, LocalDate date) {}