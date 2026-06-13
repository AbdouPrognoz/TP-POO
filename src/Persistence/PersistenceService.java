package Persistence;

import Alerts.Alert;
import Zone.Zone;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceService {

    private static final String DATA_DIR = "data";
    private static final String ZONES_FILE = DATA_DIR + "/farm_zones.txt";
    private static final String ALERTS_FILE = DATA_DIR + "/farm_alerts.txt";

    /**
     * Saves the complete state of the farm (zones and alerts).
     * By serializing the zones map, we cascade and save all nested Crops, Animals, Sensors, and Readings.
     */
    public static void saveState(Map<String, Zone> zones, List<Alert> alerts) {
        ensureDataDirectoryExists();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ZONES_FILE))) {
            oos.writeObject(zones);
            System.out.println("Zones state saved successfully to " + ZONES_FILE);
        } catch (IOException e) {
            System.err.println("Error saving zones state: " + e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ALERTS_FILE))) {
            oos.writeObject(alerts);
            System.out.println("Alerts state saved successfully to " + ALERTS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving alerts state: " + e.getMessage());
        }
    }

    /**
     * Loads the map of zones from the persistence file.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Zone> loadZones() {
        File file = new File(ZONES_FILE);
        if (!file.exists()) {
            return new HashMap<>(); // Return empty if no previous state exists
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Zone>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading zones state. Starting with empty state. Details: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Loads the list of alerts from the persistence file.
     */
    @SuppressWarnings("unchecked")
    public static List<Alert> loadAlerts() {
        File file = new File(ALERTS_FILE);
        if (!file.exists()) {
            return new ArrayList<>(); // Return empty if no previous state exists
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Alert>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading alerts state. Starting with empty state. Details: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void ensureDataDirectoryExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
