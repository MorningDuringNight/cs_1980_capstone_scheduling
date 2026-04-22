# CS 1980 Capstone Scheduling Validation Tool
A Java-based course schedule validation application that checks uploaded CSV schedule data for conflicts, inconsistencies, and other rule violations.

## Overview

The Scheduling Validation Tool is designed to help validate University of Pittsburgh course scheduling data before it becomes a harder to find and more difficult problem later on. Instead of generating schedules, this application analyzes an existing schedule export and flags issues such as overlapping classes, room conflicts, instructor mismatches, and cross-listed inconsistencies.

The current implementation provides a UI for uploading a CSV file, parsing it into a temporary SQLite database, running a set of validation rules, and displaying the results in an organized, severity weighted interface.

The application supports packaged installers for **Windows**, **Ubuntu**, and **macOS**.

## Features

- Upload a schedule CSV through an easy to use interface
- Parses CSV input into a temporary database
- Detect scheduling conflicts using rule-based validation
- Display issues with impact levels
- Expandable rows to inspect full conflict details
- Show CSV parsing errors separately from validation results
- Support validation of known cross-listed course pairs

## Current Validation Rules

The system currently checks for:

- Missing instructor for lecture / practice / seminar sections
- Lecture overlap with another lecture section of the same course
- Lecture overlap with its own recitation
- Recitation/lab overlap within the same section
- Recitation/lab sections that are too close together within the same section
- Recitation/lab overlap across different sections of the same course
- Recitation/lab sections that are too close together across different sections
- Instructor proximity issues
- Cross-listed room mismatches
- Cross-listed instructor mismatches
- Room collisions at the same time

## How It Works
1. A user uploads a CSV file through the UI.
2. The parser validates each row and skips invalid entries.
3. Valid rows are stored in a temporary SQLite database.
4. Additional derived tables are created for:
   - classes
   - courses
   - instructors
   - cross-listed relationships
5. The query engine runs validation rules against the stored schedule data.
6. Results are sorted by impact and displayed in the UI.

## Tech Stack

- **Java**
- **Vaadin** for the frontend
- **SQLite** for temporary schedule storage and querying
- **JDBC** for database access
- **Maven** for organization and consistency


## Important Files and Directories

### Core Application Files

- `src/main/java/capstoneSchedulingApp/UI/MainView.java`  
  Main user interface. Handles file upload, validation result display, and user interaction with detected issues.

- `src/main/java/capstoneSchedulingApp/Parser.java`  
  Parses uploaded CSV schedule data, validates row formatting, and inserts valid data into the database.

- `src/main/java/capstoneSchedulingApp/Query.java`  
  Contains the rule checking and conflict detection logic used to validate schedules.

- `src/main/java/capstoneSchedulingApp/Collision.java`  
  Data model for a detected validation issue, including the base class, conflicting classes, and issue metadata.

- `src/main/java/capstoneSchedulingApp/Course.java`  
  Represents course/class data after it has been parsed from the CSV.

### Resources and Configuration

- `src/main/resources/`  
  Stores application resources such as configuration files and static assets.

- `pom.xml`  
  Main Maven build file. Defines dependencies, plugins, and project build settings.

### Packaging and Distribution

- `packaging/`  
  Files used to package the application for distribution.

- `dist/`  
  Generated distribution/build output.