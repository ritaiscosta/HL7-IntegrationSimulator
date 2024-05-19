import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigManager {

    private static Scanner scanner = new Scanner(System.in);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String generateSimulationCode() {
        // Get the current date and time
        Date currentDate = new Date();

        // Format the date and time to include milliseconds
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        // Generate the simulation code using formatted date and time
        return dateFormat.format(currentDate);
    }

    public static void writeToJson(Configuration configuration, String fileName) {
        try {
            // Create the folder if it doesn't exist
            File folder = new File("Existing Simulations");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // Check if the file already exists
            File configFile = new File(folder, fileName);
            if (configFile.exists()) {
                System.out.println("A configuration file with the same name already exists.");
                System.out.println("Do you want to overwrite it? (yes/no)");
                String overwriteChoice = scanner.nextLine().trim().toLowerCase();
                if (!overwriteChoice.equals("yes")) {
                    System.out.println("Configurations not saved.");
                    return;
                }
            }

            // Write configurations to the file
            objectMapper.writeValue(configFile, configuration);
            System.out.println("Configurations saved to " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration readFromJson(String fileName) {
        try {
            File configFile = new File("Existing Simulations", fileName);
            if (!configFile.exists()) {
                System.err.println("Configuration file not found: " + fileName);
                return null;
            }
            return objectMapper.readValue(configFile, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading configurations from JSON file: " + fileName);
            return null;
        }
    }

    public static List<String> selectConfigurationFiles() {
        // List available configuration files in the "Existing Simulations" folder
        File folder = new File("Existing Simulations");
        File[] files = folder.listFiles((dir, name) -> name.startsWith("simulation_") && name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("No previous configuration files found.");
            return new ArrayList<>();
        }

        // Display available configuration files to the user
        System.out.println("Select previous configuration files (separated by comma, e.g., 1,2,3):");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ") " + files[i].getName());
        }

        // Prompt the user to select configuration files
        System.out.print("Enter the numbers of the configuration files to load (0 to skip): ");
        String input = scanner.nextLine().trim();
        if (input.equals("0")) {
            System.out.println("Skipping loading previous configuration files.");
            return new ArrayList<>();
        }

        String[] inputNumbers = input.split(",");
        List<String> selectedFiles = new ArrayList<>();
        for (String number : inputNumbers) {
            int choice;
            try {
                choice = Integer.parseInt(number.trim());
                if (choice >= 1 && choice <= files.length) {
                    selectedFiles.add(files[choice - 1].getName());
                } else {
                    System.out.println("Invalid selection: " + number);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + number);
            }
        }
        return selectedFiles;
    }

    public static void updateJsonFile(Configuration configuration, String fileName) {
        try {
            // Write updated configurations to the existing JSON file
            File folder = new File("Existing Simulations");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File configFile = new File(folder, fileName);
            objectMapper.writeValue(configFile, configuration);
            System.out.println("Configurations updated and saved to " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
