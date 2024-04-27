import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        // Generate the simulation code using formatted date and time
        return dateFormat.format(currentDate);
    }

    public static void writeToJson(Configurations configurations, String fileName) {
        try {
            // Create the "Existing Simulations" folder if it doesn't exist
            File folder = new File("Existing Simulations");
            if (!folder.exists()) {
                folder.mkdir();
            }
            String simulationCode = generateSimulationCode();
            System.out.println("Simulation code: " + simulationCode);


            // Write configurations to a file named simulation_<simulationCode>.json
            File configFile = new File(folder, "simulation_" + simulationCode + ".json");
            objectMapper.writeValue(configFile, configurations);
            System.out.println("Configurations saved to " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configurations readFromJson(String fileName) {
        try {
            return objectMapper.readValue(new File("Existing Simulations", fileName), Configurations.class);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading configurations from JSON file: " + fileName);
            return null;
        }
    }

    public static String selectConfigurationFile() {
        // List available configuration files in the "Existing Simulations" folder
        File folder = new File("Existing Simulations");
        File[] files = folder.listFiles((dir, name) -> name.startsWith("simulation_") && name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("No previous configuration files found.");
            return null;
        }

        // Display available configuration files to the user
        System.out.println("Select a previous configuration file:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ") " + files[i].getName());
        }

        // Prompt the user to select a configuration file
        System.out.print("Enter the number of the configuration file to load (0 to skip): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Validate user input and return the selected file name
        if (choice >= 1 && choice <= files.length) {
            return files[choice - 1].getName();
        } else if (choice == 0) {
            System.out.println("Skipping loading previous configuration file.");
            return null;
        } else {
            System.out.println("Invalid choice. Please enter a valid number.");
            return selectConfigurationFile(); // Recursive call to select again
        }
    }

    public static void updateJsonFile(Configurations configurations, String fileName) {
        try {
            // Write updated configurations to the existing JSON file
            File configFile = new File("Existing Simulations", fileName);
            objectMapper.writeValue(configFile, configurations);
            System.out.println("Configurations updated and saved to " + configFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
