import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static List<States> states = new ArrayList<>();
    private static List<Transitions> transitions = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    createNewSimulation();
                    break;
                case 2:
                    modifyExistingSimulation();
                    break;
                case 3:
                    exitProgram();
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\nWelcome to HL7 Simulator");
        System.out.println("\n1) Create a new simulation");
        System.out.println("2) Modify an existing simulation");
        System.out.println("3) Exit");
        System.out.print("\nChoose an option: ");
    }

    private static void createNewSimulation() {
        System.out.println("\nCreating a new simulation...");

        // Prompt the user to enter states
        System.out.println("\nCREATING STATES");
        System.out.println("\nPlease create states. Enter an empty name to finish.");

        int stateNumber = 1; // Counter for state numbers
        String name = "";
        while (true) {
            System.out.println("\nState number " + stateNumber);
            System.out.println("State name:");
            name = scanner.nextLine();
            if (name.isEmpty()) {
                break; // Exit the loop if the name is empty
            } else {
                System.out.println("Enter maximum capacity for this state:");
                int maxCapacity = 0;
                try {
                    maxCapacity = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Maximum capacity must be a number.");
                    continue; // Restart the loop
                }
                states.add(new States(name, maxCapacity));
                stateNumber++; // Increment state number
            }
        }

        // Display states
        System.out.println("\nSTATES CREATED:");
        for (int i = 0; i < states.size(); i++) {
            States state = states.get(i);
            System.out.println("State number " + (i + 1) + ": " + state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
        }

        // Prompt the user to enter transitions
        System.out.println("\nCREATING TRANSITIONS");
        System.out.println("\nEnter transitions. Enter empty source and target names to finish.");

        while (true) {
            System.out.println("\nEnter source state name:");
            String sourceName = scanner.nextLine();
            if (sourceName.isEmpty()) {
                break; // Exit the loop if source name is empty
            }
            System.out.println("Enter target state name:");
            String targetName = scanner.nextLine();
            States sourceState = getStateByName(sourceName);
            States targetState = getStateByName(targetName);
            if (sourceState != null && targetState != null) {
                transitions.add(new Transitions(sourceState, targetState));

                // Define available HL7 events
                List<String> availableEvents = Events.getAvailableEvents();

                // Display available events to the user
                System.out.println("Available HL7 Events:");
                for (int i = 0; i < availableEvents.size(); i++) {
                    System.out.println((i + 1) + ") " + availableEvents.get(i));
                }

                // Prompt the user to select an event
                System.out.println("Enter the number of the HL7 event to associate with this transition:");
                int eventNumber = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Validate the user's input
                if (eventNumber >= 1 && eventNumber <= availableEvents.size()) {
                    // Get the selected event
                    String selectedEvent = availableEvents.get(eventNumber - 1);

                    // Set the selected event for the transition
                    transitions.get(transitions.size() - 1).setHL7Event(selectedEvent);

                } else {
                    System.out.println("Invalid event number. Please enter a valid number.");
                }

            } else {
                System.out.println("Invalid state names.");
            }
        }

        // Display transitions
        System.out.println("\nTRANSITIONS CREATED:");
        for (Transitions transition : transitions) {
            System.out.println(transition.getSource().getName() + " -> " +
                    transition.getTarget().getName() + " (HL7 Event: " + transition.getHL7Event() + ")");
        }

    }

    private static void modifyExistingSimulation() {
        System.out.println("Modifying an existing simulation...");
    }

    private static void exitProgram() {
        System.out.println("\nClosing Session...");
        System.out.println("\nHL7 Simulator");
        System.out.println("Rita Costa 1171445");
        scanner.close();
        System.exit(0);
    }

    private static States getStateByName(String name) {
        for (States state : states) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }
}
