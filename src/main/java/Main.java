import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static List<Simulation> simulations = new ArrayList<>();

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
                    listExistingSimulations();
                    break;
                case 0:
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
        System.out.println("3) List existing simulations");
        System.out.println("0) Exit");
        System.out.print("\nChoose an option: ");
    }

    private static void listExistingSimulations() {
        System.out.println("\nEXISTING SIMULATIONS:");
        if (simulations.isEmpty()) {
            System.out.println("No simulations found.");
        } else {
            for (int i = 0; i < simulations.size(); i++) {
                Simulation simulation = simulations.get(i);
                System.out.println((i + 1) + ") " + simulation.getName());
            }
            // Prompt the user to select a simulation to view its details
            System.out.println("Enter the number of the simulation to view its details:");
            int selection = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (selection >= 1 && selection <= simulations.size()) {
                // Display details of the selected simulation
                Simulation selectedSimulation = simulations.get(selection - 1);
                System.out.println("\nSimulation Details:");
                System.out.println("Name: " + selectedSimulation.getName());
                System.out.println("States:");
                for (States state : selectedSimulation.getStates()) {
                    System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                }
                System.out.println("Transitions:");
                for (Transitions transition : selectedSimulation.getTransitions()) {
                    System.out.println(transition.getSource().getName() + " -> " +
                            transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                }
            } else {
                System.out.println("Invalid selection. Please enter a valid number.");
            }
        }
        // After displaying simulation details, offer the option to return to the main menu
        System.out.println("Press 0 to return to the main menu.");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        if (choice == 0) {
            return;
        } else {
            System.out.println("Invalid choice. Returning to the main menu.");
        }
    }

    private static void createNewSimulation() {
        System.out.println("\nCreating a new simulation...");

        // Prompt the user to enter a name for the simulation
        System.out.println("\nEnter a name for the new simulation:");
        String simulationName = scanner.nextLine();

        // Create lists to store states, transitions, and events
        List<States> states = new ArrayList<>();
        List<Transitions> transitions = new ArrayList<>();
        List<String> events = Events.getAvailableEvents();

        // Prompt the user to enter states
        System.out.println("\nCREATING STATES for Simulation: " + simulationName);
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

        // Prompt the user to enter transitions
        System.out.println("\nCREATING TRANSITIONS for Simulation: " + simulationName);
        System.out.println("\nEnter transitions. Enter empty source and target names to finish.");

        while (true) {
            // Display available states for selection
            System.out.println("Available States:");
            for (int i = 0; i < states.size(); i++) {
                System.out.println((i + 1) + ") " + states.get(i).getName());
            }

            // Prompt the user to select the source state
            System.out.println("Enter the number of the source state:");
            int sourceStateNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Prompt the user to select the target state
            System.out.println("Enter the number of the target state:");
            int targetStateNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Validate the user's input
            if (sourceStateNumber >= 1 && sourceStateNumber <= states.size() &&
                    targetStateNumber >= 1 && targetStateNumber <= states.size()) {
                // Get the selected source and target states
                States sourceState = states.get(sourceStateNumber - 1);
                States targetState = states.get(targetStateNumber - 1);

                // Add the transition between the selected states
                transitions.add(new Transitions(sourceState, targetState, simulationName));

                // Define available HL7 events
                List<String> availableEvents = Events.getAvailableEvents();

                // Display available events for selection
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
                System.out.println("Invalid state numbers. Please enter valid numbers.");
            }

            System.out.println("Do you want to add another transition? (yes/no)");
            String addAnotherTransition = scanner.nextLine();
            if (!addAnotherTransition.equalsIgnoreCase("yes")) {
                break;
            }
        }

        // Create a new Simulation object encapsulating all the created elements
        Simulation simulation = new Simulation(simulationName, states, transitions, events);
        simulations.add(simulation);

        // Display transitions
        System.out.println("\nTRANSITIONS CREATED for Simulation: " + simulationName);
        for (Transitions transition : transitions) {
            System.out.println(transition.getSource().getName() + " -> " +
                    transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
        }
    }

    private static void modifyExistingSimulation() {
        System.out.println("\nModifying an existing simulation...");

        if (simulations.isEmpty()) {
            System.out.println("No simulations to modify.");
            return;
        }

        // Display existing simulation names
        System.out.println("\nEXISTING SIMULATIONS:");
        for (int i = 0; i < simulations.size(); i++) {
            Simulation simulation = simulations.get(i);
            System.out.println((i + 1) + ") " + simulation.getName());
        }
        System.out.println((simulations.size() + 1) + ") Return to Main Menu");

        // Prompt the user to select a simulation to modify
        System.out.println("\nEnter the number of the simulation to modify:");
        int simulationNumber = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (simulationNumber >= 1 && simulationNumber <= simulations.size()) {
            // Get the selected simulation
            Simulation selectedSimulation = simulations.get(simulationNumber - 1);

            // Display selected simulation details
            System.out.println("\nSelected Simulation to Modify:");
            System.out.println(selectedSimulation.getName());

            // Display current values
            System.out.println("\nCurrent configurations:");
            System.out.println("\nName: " + selectedSimulation.getName());
            System.out.println("\nStates:");
            for (States state : selectedSimulation.getStates()) {
                System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
            }
            System.out.println("\nTransitions:");
            for (Transitions transition : selectedSimulation.getTransitions()) {
                System.out.println(transition.getSource().getName() + " -> " +
                        transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
            }

            // Prompt the user to confirm modification
            System.out.println("\nDo you want to modify this simulation? (yes/no)");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("yes")) {
                // Modify simulation
                System.out.println("\nEnter new configurations for the simulation (leave blank to keep current values):");

                // Modify simulation name
                System.out.println("Enter a new name for the simulation (current: " + selectedSimulation.getName() + "):");
                String newName = scanner.nextLine().trim();
                if (!newName.isEmpty()) {
                    selectedSimulation.setName(newName);
                }

                // Modify states
                System.out.println("Modify states:");
                for (States state : selectedSimulation.getStates()) {
                    System.out.println("Enter new name for state '" + state.getName() +
                            "' (current: " + state.getName() + "):");
                    String newStateName = scanner.nextLine().trim();
                    if (!newStateName.isEmpty()) {
                        state.setName(newStateName);
                    }
                    System.out.println("Enter new maximum capacity for state '" + state.getName() +
                            "' (current: " + state.getMaxCapacity() + "):");
                    String maxCapacityInput = scanner.nextLine().trim();
                    if (!maxCapacityInput.isEmpty()) {
                        int newMaxCapacity = Integer.parseInt(maxCapacityInput);
                        state.setMaxCapacity(newMaxCapacity);
                    }
                }

                // Modify transitions
            List<Transitions> transitions = selectedSimulation.getTransitions();
            System.out.println("Modify transitions:");
            for (Transitions transition : transitions) {
                System.out.println("\nTransition from " + transition.getSource().getName() + " to " + transition.getTarget().getName());

                // Display current event
                System.out.println("Current HL7 Event: " + transition.getHL7Event());

                // Display available events for selection
                List<String> availableEvents = Events.getAvailableEvents();
                System.out.println("Available HL7 Events:");
                for (int i = 0; i < availableEvents.size(); i++) {
                    System.out.println((i + 1) + ") " + availableEvents.get(i));
                }

                // Prompt the user to select an event
                System.out.println("Enter the number of the HL7 event to associate with this transition (leave blank to keep current event):");
                String eventNumberInput = scanner.nextLine().trim();
                if (!eventNumberInput.isEmpty()) {
                    try {
                        int eventNumber = Integer.parseInt(eventNumberInput);
                        if (eventNumber >= 1 && eventNumber <= availableEvents.size()) {
                            // Get the selected event
                            String selectedEvent = availableEvents.get(eventNumber - 1);
                            // Set the selected event for the transition
                            transition.setHL7Event(selectedEvent);
                        } else {
                            System.out.println("Invalid event number. Please enter a valid number.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid number.");
                    }
                }
            }

            // Display new configurations
            System.out.println("\nNew configurations:");

                System.out.println("\nName: " + selectedSimulation.getName());
                System.out.println("\nStates:");
                for (States state : selectedSimulation.getStates()) {
                    System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                }
                System.out.println("\nTransitions:");
                for (Transitions transition : selectedSimulation.getTransitions()) {
                    System.out.println(transition.getSource().getName() + " -> " +
                            transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                }


                System.out.println("\nSimulation modified successfully.");
            } else {
                System.out.println("\nModification canceled.");
            }

        } else if (simulationNumber == simulations.size() + 1) {
            System.out.println("\nReturning to Main Menu...");
        } else {
            System.out.println("Invalid simulation number. Please enter a valid number.");
        }

    }


    private static void exitProgram() {
        System.out.println("\nClosing Session...");
        System.out.println("\nHL7 Simulator");
        System.out.println("Rita Costa 1171445");
        scanner.close();
        System.exit(0);
    }

    private static States getStateByName(String name, List<States> states) {
        for (States state : states) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }
}
