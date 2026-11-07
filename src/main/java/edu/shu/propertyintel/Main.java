package edu.shu.propertyintel;

import edu.shu.propertyintel.cli.ConsoleApplication;
import edu.shu.propertyintel.data.DatasetLoader;
import edu.shu.propertyintel.data.PropertyStore;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Path dataDirectory = args.length == 0 ? Path.of("data") : Path.of(args[0]);
        try {
            PropertyStore store = new DatasetLoader().load(dataDirectory);
            new ConsoleApplication(store, new Scanner(System.in), System.out).run();
        } catch (IOException | IllegalArgumentException | DateTimeException exception) {
            System.err.println("Unable to load property data: " + exception.getMessage());
            System.exit(1);
        }
    }
}
