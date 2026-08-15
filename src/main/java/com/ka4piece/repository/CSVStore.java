package com.ka4piece.repository;

import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

abstract class CSVStore {
    public List<String> readLines(String path) {
        Path filePath = Path.of(path);
        if (!Files.exists(filePath)) {
            //File not found: return empty list
            return new ArrayList<>();
        }

        try {
            //Read all lines from file and return as a list of strings
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            //Handle the exception by throwing a runtime exception with a message
            throw new RuntimeException("Error reading file: " + path, e);
        }
    
    }

    public void writeLines(String path, List<String> lines) {
        try {
            // Write all lines to file
            Files.write(Path.of(path), lines);
        } catch (IOException e) {
            // Handle the exception by throwing a runtime exception with a message
            throw new RuntimeException("Error writing to file: " + path, e);
        }
    }

    public void appendLine(String path, String line) {
        try {
            Files.writeString(Path.of(path), // call write file
            line + System.lineSeparator(), // add new line
            StandardOpenOption.CREATE, // create if file does not exist
            StandardOpenOption.APPEND); // Append to end of file
        } catch (IOException e) {
            throw new RuntimeException("Error appending to file: " + path, e);
        }
    }

    public String generateID(String prefix){
        // Generate a unique ID by combining the prefix with a random UUID
        return prefix.toUpperCase() + "_" + UUID.randomUUID().toString().substring(0,8);
    }
    
}
