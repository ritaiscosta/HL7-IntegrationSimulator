import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class State {
    private String name;
    private int maxCapacity;
    private int stateID;
    @JsonIgnore
    private List<Person> personList;


    @JsonCreator
    public State(@JsonProperty("name") String name, @JsonProperty("maxCapacity") int maxCapacity,  @JsonProperty("stateID") int stateID) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.stateID = stateID;
        this.personList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }


    public int getStateID() {
        return stateID;
    }


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

    @Override
    public String toString() {
        return name + " (ID: " + stateID + ", Max Capacity: " + maxCapacity + ")";
    }
}
