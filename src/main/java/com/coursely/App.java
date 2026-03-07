package com.coursely;

import com.coursely.db.DatabaseInitializer;

public class App {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        System.out.println("Coursely starting...");
        // Later: launch Swing UI
    }
}