import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class State {
    private String name;
    private int maxCapacity;
    private int stateID;
    private List<Person> personList;  // List to hold persons in the state

    @JsonCreator
    public State(@JsonProperty("name") String name, @JsonProperty("maxCapacity") int maxCapacity) {
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.personList = new ArrayList<>();  // Initialize the person list
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

    public void setStateID(int stateID) {
        this.stateID = stateID;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public void addPerson(Person person) {
        if (personList.size() < maxCapacity) {
            personList.add(person);  // Add person to the list if not at max capacity
            System.out.println("Added person: " + person.getFirstName() + " " + person.getLastName() + " to state: " + name);
        } else {
            throw new IllegalStateException("State is at maximum capacity");
        }
    }

    public void removePerson(Person person) {
        personList.remove(person);  // Remove person from the list
        System.out.println("Removed person: " + person.getFirstName() + " " + person.getLastName() + " from state: " + name);
    }

    public int getPersonCount() {
        System.out.println("State " + name + " has " + personList.size() + " people.");
        return personList.size();
    }

    public List<Person> getPeople() {
        System.out.println("Getting people from state: " + name + ", count: " + personList.size());
        return new ArrayList<>(personList);  // Return a copy of the person list
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
}
