package se.lu.ics.data;

import java.io.File;
import java.io.IOException;
import java.awt.Desktop;

public class ExcelOpener {

    public void openExcelFile(String filePath) {
        File file = new File(filePath); // Create a new file object

        if (!file.exists()) {
            System.out.println("The file does not exist: " + filePath);
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            System.out.println("Desktop is not supported on this platform.");
            return;
        }

        Desktop desktop = Desktop.getDesktop(); // Get the desktop object
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}