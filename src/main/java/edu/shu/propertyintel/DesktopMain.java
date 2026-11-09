package edu.shu.propertyintel;

import edu.shu.propertyintel.data.DatasetLoader;
import edu.shu.propertyintel.data.PropertyStore;
import edu.shu.propertyintel.ui.MainWindow;
import edu.shu.propertyintel.ui.UiTheme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;

public final class DesktopMain {
    private DesktopMain() {
    }

    public static void main(String[] args) {
        Path directory = args.length == 0 ? Path.of("data") : Path.of(args[0]);
        try {
            PropertyStore store = new DatasetLoader().load(directory);
            SwingUtilities.invokeLater(() -> {
                UiTheme.install();
                new MainWindow(store, directory).setVisible(true);
            });
        } catch (IOException | IllegalArgumentException | DateTimeException exception) {
            SwingUtilities.invokeLater(() -> {
                UiTheme.install();
                JOptionPane.showMessageDialog(null,
                        "The property dataset could not be loaded.\n\n"
                                + exception.getMessage() + "\n\n"
                                + "Check properties.csv, sales.csv and neighborhoods.csv in "
                                + directory.toAbsolutePath().normalize(),
                        "Unable to open Property Intelligence Engine",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }
}
