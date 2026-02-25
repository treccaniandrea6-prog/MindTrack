# MindTrack (Java CLI Application)

MindTrack is a command-line Java application designed to manage users, objectives, and bookings.

The application loads data from CSV files at startup and allows the user to perform operations through a structured CLI menu. All changes are persisted back to the CSV files.

---

## Features

- Automatic CSV loading at application startup
- Command Line Interface (CLI)
- Persistent data storage
- Input validation
- Export functionality
- Error handling and rollback mechanism

---

## CLI Menu

When the application starts, the following menu is displayed:

1. Show all objectives
2. Book an objective
3. Cancel booking
4. Add user
5. Export available objectives
6. Exit

---

## Functional Requirements Implemented

### 1. Show all objectives

Displays:

- id
- title
- period
- coins
- availability (YES/NO)

### 2. Book an objective

Validations:

- User must exist
- Objective must exist
- Objective must be available
- Booking date cannot be in the past
- Duplicate booking (same user + objective) is not allowed

On success:

- A new booking is created
- Objective becomes unavailable
- Data is immediately saved to CSV

### 3. Cancel booking

Validations:

- Booking must exist

On success:

- Booking is removed
- Objective becomes available again
- Data is persisted

### 4. Add user

Validations:

- Email must be unique

On success:

- User is created
- CSV file is updated immediately

### 5. Export available objectives

Creates a CSV file inside the `exports/` directory:

Example:
available_objectives_2026-02-24.csv

Export format:
id;title;period;coins

---

## Tech Stack

- Java 17+
- Apache Maven
- CSV file-based persistence

---

## Project Structure

MindTrack/
│
├── pom.xml
├── README.md
├── exports/
│
└── src/
└── main/
├── java/
│ └── com/andreatreccani/mindtrack/
│ ├── App.java
│ ├── core/
│ ├── io/
│ ├── model/
│ └── ui/
│
└── resources/
└── data/
├── users.csv
├── objectives.csv
└── bookings.csv

---

## CSV Format

All CSV files use ";" as delimiter.

### users.csv

Header:
id;first_name;last_name;email

Example:
1;Andrea;Treccani;andrea@example.com

---

### objectives.csv

Header:
id;title;period;coins;available

Example:
1;Daily meditation;daily;10;YES

---

### bookings.csv

Header:
id;user_id;objective_id;date

Example:
1;1;1;2026-02-26

---

## How to Run the Project

### Requirements

- Java 17 or higher
- Maven installed (mvn -v must work)

---

### 1. Build the project

From the project root directory:

mvn clean package

---

### 2. Run the application

java -jar target/MindTrack-1.0.0.jar

---

## Git Repository Rules

This repository contains only:

- Source code
- CSV files
- README

The following must NOT be committed:

- target/
- .class files
- generated .jar files
- IDE configuration folders

---

## Evaluation Notes

The project includes:

- Proper separation of responsibilities (core, model, io, ui)
- Object-Oriented design
- Error handling
- Input validation
- Data persistence
- CSV normalization
- Clean CLI structure

---

## Author

Andrea Treccani  
Full-Stack Development Student  
Project developed as part of Java course practice.
