import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static List<Simulation> simulations = new ArrayList<>();

    public static void main(String[] args) {

        // Load previous configuration file, if available
        String selectedFile = ConfigManager.selectConfigurationFile();
        Configurations configurations = null;
        if (selectedFile != null) {
            configurations = ConfigManager.readFromJson(selectedFile);
            simulations = configurations.getSimulations();
            System.out.println("Previous Simulation configurations loaded from " + selectedFile);
        }


        while (true) {
            displayMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    createNewSimulation();
                    break;
                case 2:
                    modifyExistingSimulation(configurations, selectedFile);
                    break;
                case 3:
                    listExistingSimulations();
                    break;
                case 4:
                    //StartSimulation();
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
        System.out.println("\n1) Create a new Simulation Configuration");
        System.out.println("2) Modify an existing Simulation Configuration");
        System.out.println("3) List existing Simulation Configurations");
        System.out.println("4) Start Simulation");
        System.out.println("0) Exit");
        System.out.print("\nChoose an option: ");
    }

    private static void createNewSimulation() {
        System.out.println("\nCreating a new simulation configuration...");

        // Prompt the user to enter a name for the simulation
        System.out.print("\nEnter a name for the simulation:");
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
            System.out.print("State name:");
            name = scanner.nextLine();
            if (name.isEmpty()) {
                break; // Exit the loop if the name is empty
            } else {
                System.out.print("Enter maximum capacity for this state:");
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
            System.out.print("Enter the number of the source state:");
            int sourceStateNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Prompt the user to select the target state
            System.out.print("Enter the number of the target state:");
            int targetStateNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Validate the user's input
            if (sourceStateNumber >= 1 && sourceStateNumber <= states.size() &&
                    targetStateNumber >= 1 && targetStateNumber <= states.size()) {
                // Get the selected source and target states
                States sourceState = states.get(sourceStateNumber - 1);
                States targetState = states.get(targetStateNumber - 1);


                // Prompt the user to enter probability and frequency
                System.out.print("Enter probability of transitioning from '" + sourceState.getName() + "' to '" + targetState.getName() + "' (0-1):");
                double probability = 0;
                try {
                    probability = Double.parseDouble(scanner.nextLine());
                    if (probability < 0 || probability > 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Probability must be a number between 0 and 1.");
                    continue; // Restart the loop
                }

                System.out.print("Enter transition frequency from '" + sourceState.getName() + "' to '" + targetState.getName() + " (positive number):");
                int frequency = 0;
                try {
                    frequency = Integer.parseInt(scanner.nextLine());
                    if (frequency <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Frequency must be a positive number.");
                    continue; // Restart the loop
                }

// Add the transition between the selected states with correct probability and frequency
                Transitions newTransition = new Transitions(sourceState, targetState, simulationName);

                newTransition.setProbability(probability);
                newTransition.setFrequency(frequency);
                transitions.add(newTransition);

                // Display available events for selection
                List<String> availableEvents = Events.getAvailableEvents();
                System.out.println("Available HL7 Events:");
                for (int i = 0; i < availableEvents.size(); i++) {
                    System.out.println((i + 1) + ") " + availableEvents.get(i));
                }

                // Prompt the user to select an event
                System.out.print("Enter the number of the HL7 event to associate with this transition: ");
                int eventNumber = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Validate the user's input
                if (eventNumber >= 1 && eventNumber <= availableEvents.size()) {
                    // Get the selected event
                    String selectedEvent = availableEvents.get(eventNumber - 1);

                    // Set the selected event for the transition
                    transitions.get(transitions.size() - 1).setHL7Event(selectedEvent);
                    newTransition.setHL7Event(selectedEvent);
                } else {
                    System.out.println("Invalid event number. Please enter a valid option.");
                }

            } else {
                System.out.println("Invalid state numbers. Please enter valid option.");
            }

            System.out.println("Do you want to add another transition? (yes/no)");
            String addAnotherTransition = scanner.nextLine();
            if (!addAnotherTransition.equalsIgnoreCase("yes")) {
                System.out.println("Do you want to save the configurations? (yes/no)");
                String saveConfigurations = scanner.nextLine();
                if (saveConfigurations.equalsIgnoreCase("yes")) {
                    // Create a new Simulation object encapsulating all the created elements
                    Simulation simulation = new Simulation(simulationName, states, transitions, events);
                    simulations.add(simulation);

                    // Display transitions
                  System.out.println("\nCONFIGURATIONS CREATED for Simulation: " + simulationName);
                   for (Transitions transition : transitions) {
                       System.out.println(transition.getSource().getName() + " -> " +
                              transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                      System.out.println("Frequency (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getFrequency());
                      System.out.println("Probability (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getProbability());
                    }

                   // Save configurations to a JSON file
                    Configurations configurations = new Configurations(simulations);
                    String fileName = "simulation_" + ConfigManager.generateSimulationCode() + ".json"; // Unique file name using timestamp
                   ConfigManager.writeToJson(configurations, fileName);


                } else {
                    System.out.println("Configurations not saved.");
                }
                break;
            }
        }



//        // Display transitions
//        System.out.println("\nCONFIGURATIONS for Simulation: " + simulationName);
//        for (Transitions transition : transitions) {
//            System.out.println(transition.getSource().getName() + " -> " +
//                    transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
//            System.out.println("Frequency (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getFrequency());
//            System.out.println("Probability (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getProbability());
//
//        }

//        // Save configurations to a JSON file
//        Configurations configurations = new Configurations(simulations);
//        String fileName = "simulation_" + System.currentTimeMillis() + ".json"; // Unique file name using timestamp
//        ConfigManager.writeToJson(configurations, fileName);
    }

    private static void modifyExistingSimulation(Configurations configurations, String selectedFile) {
        System.out.println("\nModifying an existing simulation configuration...");

        if (simulations.isEmpty()) {
            System.out.println("No simulation configuration to modify.");
            return;
        }

        // Display existing simulation names
        System.out.println("\nEXISTING SIMULATION CONFIGURATION:");
        for (int i = 0; i < simulations.size(); i++) {
            Simulation simulation = simulations.get(i);
            System.out.println((i + 1) + ") " + simulation.getName());
        }

        System.out.println((simulations.size() + 1) + ") Return to Main Menu");

        // Prompt the user to select a simulation to modify
        System.out.print("\nEnter the number of the simulation to modify:");
        int selection = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (selection >= 1 && selection <= simulations.size()) {
            Simulation selectedSimulation = simulations.get(selection - 1);

            // Display selected simulation details
            System.out.print("\nSelected Simulation to Modify: "+ selectedSimulation.getName());

            // Display current values
            System.out.println("\nCurrent configurations:");
            System.out.println("\nNAME: " + selectedSimulation.getName());
            System.out.println("\nSTATES:");
            for (States state : selectedSimulation.getStates()) {
                System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
            }
            System.out.println("\nTRANSITIONS:");
            for (Transitions transition : selectedSimulation.getTransitions()) {
                System.out.println(transition.getSource().getName() + " -> " +
                        transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                System.out.println("Frequency (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getFrequency());
                System.out.println("Probability (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getProbability() + "\n");

            }

            // Prompt the user to confirm modification
            System.out.println("\nDo you want to modify this configuration? (yes/no)");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("yes")) {
                // Modify simulation
                System.out.println("\nEnter new configurations for the simulation (leave blank to keep current values):");

                // Modify simulation name
                System.out.print("Enter a new name for the simulation (current: " + selectedSimulation.getName() + "):");
                String newName = scanner.nextLine().trim();
                if (!newName.isEmpty()) {
                    selectedSimulation.setName(newName);
                }

                // Modify states
                System.out.println("Modify states:");
                for (States state : selectedSimulation.getStates()) {
                    System.out.print("Enter new name for state '" + state.getName() +
                            "' (current: " + state.getName() + "):");
                    String newStateName = scanner.nextLine().trim();
                    if (!newStateName.isEmpty()) {
                        state.setName(newStateName);
                    }
                    System.out.print("Enter new maximum capacity for state '" + state.getName() +
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

                    // Prompt the user to modify the source and target states
                    System.out.print("Do you want to modify the source and target states for this transition? (yes/no): ");
                    String modifyStatesInput = scanner.nextLine().trim();
                    if (modifyStatesInput.equalsIgnoreCase("yes")) {
                        // Display available states for selection
                        System.out.println("Available States:");
                        for (int i = 0; i < selectedSimulation.getStates().size(); i++) {
                            States state = selectedSimulation.getStates().get(i);
                            System.out.println((i + 1) + ") " + state.getName());
                        }

                        // Prompt the user to select the source state
                        System.out.print("Enter the number of the new source state for this transition (current: " + transition.getSource().getName() + "): ");
                        String newSourceStateInput = scanner.nextLine().trim();
                        if (!newSourceStateInput.isEmpty()) {
                            int newSourceStateNumber = Integer.parseInt(newSourceStateInput);
                            if (newSourceStateNumber >= 1 && newSourceStateNumber <= selectedSimulation.getStates().size()) {
                                // Get the selected source state
                                States newSourceState = selectedSimulation.getStates().get(newSourceStateNumber - 1);
                                transition.setSource(newSourceState);
                            } else {
                                System.out.println("Invalid source state number. The current source state will be kept.");
                            }
                        }

                        // Prompt the user to select the target state
                        System.out.print("Enter the number of the new target state for this transition (current: " + transition.getTarget().getName() + "): ");
                        String newTargetStateInput = scanner.nextLine().trim();
                        if (!newTargetStateInput.isEmpty()) {
                            int newTargetStateNumber = Integer.parseInt(newTargetStateInput);
                            if (newTargetStateNumber >= 1 && newTargetStateNumber <= selectedSimulation.getStates().size()) {
                                // Get the selected target state
                                States newTargetState = selectedSimulation.getStates().get(newTargetStateNumber - 1);
                                transition.setTarget(newTargetState);
                            } else {
                                System.out.println("Invalid target state number. The current target state will be kept.");
                            }
                        }
                    }

                    // Prompt the user to modify the HL7 event
                    System.out.print("Do you want to modify the HL7 event for this transition? (yes/no): ");
                    String modifyEventInput = scanner.nextLine().trim();
                    if (modifyEventInput.equalsIgnoreCase("yes")) {
                        // Display available events for selection
                        List<String> availableEvents = Events.getAvailableEvents();
                        System.out.println("Available HL7 Events:");
                        for (int i = 0; i < availableEvents.size(); i++) {
                            System.out.println((i + 1) + ") " + availableEvents.get(i));
                        }

                        // Prompt the user to select an event
                        System.out.print("Enter the number of the HL7 event to associate with this transition: ");
                        int eventNumber = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        // Validate the user's input
                        if (eventNumber >= 1 && eventNumber <= availableEvents.size()) {
                            // Get the selected event
                            String selectedEvent = availableEvents.get(eventNumber - 1);

                            // Set the selected event for the transition
                            transition.setHL7Event(selectedEvent);
                        } else {
                            System.out.println("Invalid event number. The current event will be kept.");
                        }
                    }

                    // Prompt the user to modify the probability
                    System.out.print("Enter new probability for this transition (0-1) (current: " + transition.getProbability() + "): ");
                    String newProbabilityInput = scanner.nextLine().trim();
                    if (!newProbabilityInput.isEmpty()) {
                        double newProbability = Double.parseDouble(newProbabilityInput);
                        if (newProbability >= 0 && newProbability <= 1) {
                            transition.setProbability(newProbability);
                        } else {
                            System.out.println("Invalid input. Probability must be a number between 0 and 1.");
                            continue; // Restart the loop
                        }
                    }

                    // Prompt the user to modify the frequency
                    System.out.print("Enter new frequency for this transition (positive number) (current: " + transition.getFrequency() + "): ");
                    String newFrequencyInput = scanner.nextLine().trim();
                    if (!newFrequencyInput.isEmpty()) {
                        int newFrequency = Integer.parseInt(newFrequencyInput);
                        if (newFrequency > 0) {
                            transition.setFrequency(newFrequency);
                        } else {
                            System.out.println("Invalid input. Frequency must be a positive number.");
                            continue; // Restart the loop
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
                    System.out.println("Frequency (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getFrequency());
                    System.out.println("Probability (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getProbability() + "\n");

                }
                String newFileName = selectedFile.replace(".json", "(NEW).json");
                ConfigManager.updateJsonFile(configurations, newFileName);


                System.out.println("\nSimulation modified successfully.");
            } else {
                System.out.println("\nModification canceled.");
            }

        } else if (selection == simulations.size() + 1) {
            System.out.println("\nReturning to Main Menu...");
        } else {
            System.out.println("Invalid simulation number. Please enter a valid number.");
        }
    }


    private static void listExistingSimulations() {
        System.out.println("\nEXISTING CONFIGURATIONS:");
        if (simulations.isEmpty()) {
            System.out.println("No configurations found.");
        } else {
            for (int i = 0; i < simulations.size(); i++) {
                Simulation simulation = simulations.get(i);
                System.out.println((i + 1) + ") " + simulation.getName());
            }
            // Prompt the user to select a simulation to view its details
            System.out.print("Enter the number of the configuration to view its details:");
            int selection = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            int stateNumber=1;

            if (selection >= 1 && selection <= simulations.size()) {
                // Display details of the selected simulation
                Simulation selectedSimulation = simulations.get(selection - 1);
                System.out.println("\nSimulation Details:");
                System.out.println("Name: " + selectedSimulation.getName());
                System.out.println("States:" + stateNumber);
                for (States state : selectedSimulation.getStates()) {
                    System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                }
                System.out.println("Transitions:");
                for (Transitions transition : selectedSimulation.getTransitions()) {
                    System.out.println(transition.getSource().getName() + " -> " +
                            transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                    System.out.println("Frequency (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getFrequency());
                    System.out.println("Probability (" + transition.getSource().getName() + " to "+ transition.getTarget().getName() + "): " + transition.getProbability() + "\n");
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


    private static void exitProgram() {
        System.out.println("\nClosing Session...");
        System.out.println("\nHL7 Simulator");
        System.out.println("Rita Costa 1171445");
        scanner.close();
        System.exit(0);
    }

}
