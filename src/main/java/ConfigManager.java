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
            // Check if the file already exists
            File folder = new File("Existing Simulations");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File[] files = folder.listFiles((dir, name) -> name.equals(fileName));
            if (files != null && files.length > 0) {
                System.out.println("A configuration file with the same name already exists.");
                System.out.println("Do you want to overwrite it? (yes/no)");
                String overwriteChoice = scanner.nextLine().trim().toLowerCase();
                if (!overwriteChoice.equals("yes")) {
                    System.out.println("Configurations not saved.");
                    return;
                }
            }

            // Write configurations to a file named simulation_<simulationCode>.json
            File configFile = new File(folder, fileName);
            objectMapper.writeValue(configFile, configuration);
            System.out.println("Configurations saved to " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration readFromJson(String fileName) {
        try {
            return objectMapper.readValue(new File("Existing Simulations", fileName), Configuration.class);
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
            return null;
        }

        // Display available configuration files to the user
        System.out.println("Select previous configuration files:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ") " + files[i].getName());
        }

        // Prompt the user to select configuration files
        System.out.print("Enter the numbers of the configuration files to load (comma-separated, 0 to skip): ");
        String input = scanner.nextLine().trim();
        if (input.equals("0")) {
            System.out.println("Skipping loading previous configuration files.");
            return null;
        }

        String[] selections = input.split(",");
        List<String> selectedFiles = new ArrayList<>();
        for (String selection : selections) {
            try {
                int choice = Integer.parseInt(selection.trim());
                if (choice >= 1 && choice <= files.length) {
                    selectedFiles.add(files[choice - 1].getName());
                } else {
                    System.out.println("Invalid choice: " + choice);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: " + selection);
            }
        }
        return selectedFiles;
    }

    public static void updateJsonFile(Configuration configuration, String fileName) {
        try {
            // Write updated configurations to the existing JSON file
            File configFile = new File("Existing Simulations", fileName);
            objectMapper.writeValue(configFile, configuration);
            System.out.println("Configurations updated and saved to " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
