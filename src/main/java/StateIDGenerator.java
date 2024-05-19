import java.io.*;

public class StateIDGenerator {
    private static final String COUNTER_FILE = "state_counter.txt";
    private static int stateCounter;

    static {
        // Load the counter value from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(COUNTER_FILE))) {
            stateCounter = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            stateCounter = 0; // Set counter to 0 if file reading fails
        }
    }

    public static synchronized int generateUniqueID() {
        stateCounter++;
        persistCounter();
        return stateCounter;
    }

    private static void persistCounter() {
        // Save the updated counter value to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNTER_FILE))) {
            writer.write(String.valueOf(stateCounter));
        } catch (IOException e) {
            e.printStackTrace(); // Handle file writing error
        }
    }
}
