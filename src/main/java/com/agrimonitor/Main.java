package com.agrimonitor;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.gui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        MongoDBConnection.getDatabase();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}