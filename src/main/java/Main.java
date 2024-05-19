import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static List<Simulation> simulations = new ArrayList<>();

    public static void main(String[] args) {
        // Load previous configuration files, if available
        List<String> selectedFiles = ConfigManager.selectConfigurationFiles();
        Configuration configuration = null;
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (String selectedFile : selectedFiles) {
                configuration = ConfigManager.readFromJson(selectedFile);
                if (configuration != null) {
                    simulations.addAll(configuration.getSimulations());
                    System.out.println("Previous Simulation configurations loaded from " + selectedFile);
                }
            }
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
                    modifyExistingSimulation(configuration, selectedFiles);
                    break;
                case 3:
                    listExistingSimulations();
                    break;
                case 4:
                    startSimulation();
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
        ArrayList<State> states = new ArrayList<>();
        ArrayList<Transition> transitions = new ArrayList<>();
        ArrayList<String> events = new ArrayList<>(Event.getAvailableEvents());

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
                int maxCapacity;
                try {
                    maxCapacity = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Maximum capacity must be a number.");
                    continue; // Restart the loop
                }

                int uniqueID = StateIDGenerator.generateUniqueID();

                // Create new state with generated ID
                State newState = new State(name, maxCapacity);
                newState.setStateID(uniqueID);
                states.add(newState);
                stateNumber++; // Increment state number
            }
        }

        // Prompt the user to define the start state
        System.out.println("\nSelect the start state:");
        for (int i = 0; i < states.size(); i++) {
            System.out.println((i + 1) + ") " + states.get(i).getName());
        }
        System.out.print("Enter the number of the start state: ");
        int startStateIndex = scanner.nextInt() - 1;
        scanner.nextLine(); // Consume newline
        State startState = states.get(startStateIndex);

        // Prompt the user to define the end state
        System.out.println("\nSelect the end state:");
        for (int i = 0; i < states.size(); i++) {
            System.out.println((i + 1) + ") " + states.get(i).getName());
        }
        System.out.print("Enter the number of the end state: ");
        int endStateIndex = scanner.nextInt() - 1;
        scanner.nextLine(); // Consume newline
        State endState = states.get(endStateIndex);

        // Configure special transitions for start and end states
        System.out.println("\nCONFIGURING SPECIAL TRANSITIONS FOR START AND END STATES");

        // Start state transition
        System.out.print("Enter frequency for the start state transition: ");
        int startFrequency = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter probability for the start state transition (0-1): ");
        double startProbability = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        Transition startTransition = new Transition(null, startState, "Start Simulation", simulationName, startProbability, startFrequency);
        transitions.add(startTransition);

        // End state transition
        System.out.print("Enter frequency for the end state transition: ");
        int endFrequency = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter probability for the end state transition (0-1): ");
        double endProbability = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        Transition endTransition = new Transition(endState, null, "End Simulation", simulationName, endProbability, endFrequency);
        transitions.add(endTransition);

        // Prompt the user to enter regular transitions
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
                State sourceState = states.get(sourceStateNumber - 1);
                State targetState = states.get(targetStateNumber - 1);

                // Ensure no loops
                if (isLoopDetected(transitions, sourceState, targetState)) {
                    System.out.println("Creating this transition would result in a loop. Please enter a valid transition.");
                    continue;
                }

                // Prompt the user to enter probability and frequency
                System.out.print("Enter probability of transitioning from '" + sourceState.getName() + "' to '" + targetState.getName() + "' (0-1):");
                double probability;
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
                int frequency;
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
                Transition newTransition = new Transition(sourceState, targetState, "", simulationName, probability, frequency);
                transitions.add(newTransition);

                // Display available events for selection
                List<String> availableEvents = Event.getAvailableEvents();
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
                    Simulation simulation = new Simulation(simulationName, states, transitions, events, startState, endState);
                    simulations.add(simulation);

                    // Display transitions
                    System.out.println("\nCONFIGURATIONS CREATED for Simulation: " + simulationName);
                    for (State state : states) {
                        System.out.println("State: " + state.getName() + " (ID: " + state.getStateID() + ")");
                    }
                    System.out.println("Start State: " + startState.getName());
                    System.out.println("End State: " + endState.getName());
                    for (Transition transition : transitions) {
                        System.out.println(transition.getSource().getName() + " -> " +
                                transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                        System.out.println("Frequency (" + transition.getSource().getName() + " to " + transition.getTarget().getName() + "): " + transition.getFrequency());
                        System.out.println("Probability (" + transition.getSource().getName() + " to " + transition.getTarget().getName() + "): " + transition.getProbability());
                    }

                    // Save configurations to a JSON file
                    Configuration config = new Configuration(simulations);
                    String fileName = "simulation_" + ConfigManager.generateSimulationCode() + ".json"; // Unique file name using timestamp
                    ConfigManager.writeToJson(config, fileName);

                } else {
                    System.out.println("Configurations not saved.");
                }
                break;
            }
        }
    }

    private static boolean isLoopDetected(List<Transition> transitions, State source, State target) {
        Set<State> visited = new HashSet<>();
        Stack<State> stack = new Stack<>();
        stack.push(source);

        while (!stack.isEmpty()) {
            State current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (current.equals(target)) {
                return true;
            }
            List<State> adjacentStates = transitions.stream()
                    .filter(t -> t.getSource().equals(current))
                    .map(Transition::getTarget)
                    .collect(Collectors.toList());
            stack.addAll(adjacentStates);
        }
        return false;
    }

    private static void modifyExistingSimulation(Configuration configuration, List<String> selectedFiles) {
        System.out.println("\nModifying an existing simulation configuration...");

        if (simulations.isEmpty()) {
            System.out.println("No simulation configuration to modify.");
            return;
        }

        // Display existing simulation names
        System.out.println("\nEXISTING SIMULATION CONFIGURATION:");
        for (int i = 0; simulations.size() > i; i++) {
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
            System.out.print("\nSelected Simulation to Modify: " + selectedSimulation.getName());

            // Display current values
            System.out.println("\nCurrent configurations:");
            System.out.println("\nNAME: " + selectedSimulation.getName());
            System.out.println("\nSTATES:");
            for (State state : selectedSimulation.getStates()) {
                System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
            }
            System.out.println("\nStart State: " + selectedSimulation.getStartState().getName());
            System.out.println("\nEnd State: " + selectedSimulation.getEndState().getName());
            System.out.println("\nTRANSITIONS:");
            for (Transition transition : selectedSimulation.getTransitions()) {
                System.out.println(transition.getSource().getName() + " -> " +
                        transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                System.out.println("Frequency (" + transition.getSource().getName() + " to " + transition.getTarget().getName() + "): " + transition.getFrequency());
                System.out.println("Probability (" + transition.getSource().getName() + " to " + transition.getTarget().getName() + "): " + transition.getProbability() + "\n");
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
                for (State state : selectedSimulation.getStates()) {
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

                // Modify start and end states
                System.out.println("Modify start and end states:");
                System.out.println("\nCurrent start state: " + selectedSimulation.getStartState().getName());
                System.out.println("Available states:");
                for (int i = 0; i < selectedSimulation.getStates().size(); i++) {
                    System.out.println((i + 1) + ") " + selectedSimulation.getStates().get(i).getName());
                }
                System.out.print("Enter the number of the new start state (leave blank to keep current): ");
                String newStartStateInput = scanner.nextLine().trim();
                if (!newStartStateInput.isEmpty()) {
                    int newStartStateNumber = Integer.parseInt(newStartStateInput);
                    if (newStartStateNumber >= 1 && newStartStateNumber <= selectedSimulation.getStates().size()) {
                        State newStartState = selectedSimulation.getStates().get(newStartStateNumber - 1);
                        selectedSimulation.setStartState(newStartState);
                    } else {
                        System.out.println("Invalid start state number. The current start state will be kept.");
                    }
                }

                System.out.println("\nCurrent end state: " + selectedSimulation.getEndState().getName());
                System.out.print("Enter the number of the new end state (leave blank to keep current): ");
                String newEndStateInput = scanner.nextLine().trim();
                if (!newEndStateInput.isEmpty()) {
                    int newEndStateNumber = Integer.parseInt(newEndStateInput);
                    if (newEndStateNumber >= 1 && newEndStateNumber <= selectedSimulation.getStates().size()) {
                        State newEndState = selectedSimulation.getStates().get(newEndStateNumber - 1);
                        selectedSimulation.setEndState(newEndState);
                    } else {
                        System.out.println("Invalid end state number. The current end state will be kept.");
                    }
                }

                // Modify special transitions for start and end states
                System.out.println("Modify special transitions for start and end states:");

                // Start state transition
                Transition startTransition = selectedSimulation.getTransitions().stream()
                        .filter(t -> t.getSource() == null && t.getTarget().equals(selectedSimulation.getStartState()))
                        .findFirst().orElse(null);

                if (startTransition != null) {
                    System.out.print("Enter new frequency for the start state transition (current: " + startTransition.getFrequency() + "): ");
                    String newStartFrequencyInput = scanner.nextLine().trim();
                    if (!newStartFrequencyInput.isEmpty()) {
                        int newStartFrequency = Integer.parseInt(newStartFrequencyInput);
                        startTransition.setFrequency(newStartFrequency);
                    }

                    System.out.print("Enter new probability for the start state transition (0-1) (current: " + startTransition.getProbability() + "): ");
                    String newStartProbabilityInput = scanner.nextLine().trim();
                    if (!newStartProbabilityInput.isEmpty()) {
                        double newStartProbability = Double.parseDouble(newStartProbabilityInput);
                        if (newStartProbability >= 0 && newStartProbability <= 1) {
                            startTransition.setProbability(newStartProbability);
                        } else {
                            System.out.println("Invalid input. Probability must be a number between 0 and 1.");
                        }
                    }
                }

                // End state transition
                Transition endTransition = selectedSimulation.getTransitions().stream()
                        .filter(t -> t.getTarget() == null && t.getSource().equals(selectedSimulation.getEndState()))
                        .findFirst().orElse(null);

                if (endTransition != null) {
                    System.out.print("Enter new frequency for the end state transition (current: " + endTransition.getFrequency() + "): ");
                    String newEndFrequencyInput = scanner.nextLine().trim();
                    if (!newEndFrequencyInput.isEmpty()) {
                        int newEndFrequency = Integer.parseInt(newEndFrequencyInput);
                        endTransition.setFrequency(newEndFrequency);
                    }

                    System.out.print("Enter new probability for the end state transition (0-1) (current: " + endTransition.getProbability() + "): ");
                    String newEndProbabilityInput = scanner.nextLine().trim();
                    if (!newEndProbabilityInput.isEmpty()) {
                        double newEndProbability = Double.parseDouble(newEndProbabilityInput);
                        if (newEndProbability >= 0 && newEndProbability <= 1) {
                            endTransition.setProbability(newEndProbability);
                        } else {
                            System.out.println("Invalid input. Probability must be a number between 0 and 1.");
                        }
                    }
                }

                // Modify regular transitions
                List<Transition> transitions = selectedSimulation.getTransitions();
                System.out.println("Modify regular transitions:");
                for (Transition transition : transitions) {
                    if (transition.getSource() != null && transition.getTarget() != null) {
                        System.out.println("\nTransition from " + transition.getSource().getName() + " to " + transition.getTarget().getName());

                        // Prompt the user to modify the source and target states
                        System.out.print("Do you want to modify the source and target states for this transition? (yes/no): ");
                        String modifyStatesInput = scanner.nextLine().trim();
                        if (modifyStatesInput.equalsIgnoreCase("yes")) {
                            // Display available states for selection
                            System.out.println("Available States:");
                            for (int i = 0; i < selectedSimulation.getStates().size(); i++) {
                                State state = selectedSimulation.getStates().get(i);
                                System.out.println((i + 1) + ") " + state.getName());
                            }

                            // Prompt the user to select the source state
                            System.out.print("Enter the number of the new source state for this transition (current: " + transition.getSource().getName() + "): ");
                            String newSourceStateInput = scanner.nextLine().trim();
                            if (!newSourceStateInput.isEmpty()) {
                                int newSourceStateNumber = Integer.parseInt(newSourceStateInput);
                                if (newSourceStateNumber >= 1 && newSourceStateNumber <= selectedSimulation.getStates().size()) {
                                    // Get the selected source state
                                    State newSourceState = selectedSimulation.getStates().get(newSourceStateNumber - 1);
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
                                    State newTargetState = selectedSimulation.getStates().get(newTargetStateNumber - 1);
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
                            List<String> availableEvents = Event.getAvailableEvents();
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
                            }
                        }
                    }
                }

                // Display new configurations
                System.out.println("\nNew configurations:");
                System.out.println("\nName: " + selectedSimulation.getName());
                System.out.println("\nStates:");
                for (State state : selectedSimulation.getStates()) {
                    System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                }
                System.out.println("\nStart State: " + selectedSimulation.getStartState().getName());
                System.out.println("\nEnd State: " + selectedSimulation.getEndState().getName());
                System.out.println("\nTransitions:");
                for (Transition transition : selectedSimulation.getTransitions()) {
                    System.out.println(transition.getSource() + " -> " +
                            transition.getTarget() + " (HL7 Message: " + transition.getHL7Event() + ")");
                    System.out.println("Frequency (" + transition.getSource() + " to " + transition.getTarget() + "): " + transition.getFrequency());
                    System.out.println("Probability (" + transition.getSource() + " to " + transition.getTarget() + "): " + transition.getProbability() + "\n");
                }

                // Update JSON files
                for (String fileName : selectedFiles) {
                    ConfigManager.updateJsonFile(configuration, fileName);
                }

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
        while (true) {
            System.out.println("\nEXISTING CONFIGURATIONS:");
            if (simulations.isEmpty()) {
                System.out.println("No configurations found.");
                return;
            } else {
                for (int i = 0; i < simulations.size(); i++) {
                    Simulation simulation = simulations.get(i);
                    System.out.println((i + 1) + ") " + simulation.getName());
                }
                // Prompt the user to select a simulation to view its details
                System.out.print("Enter the number of the configuration to view its details (or 0 to return to the main menu): ");
                int selection = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                int stateNumber = 1;

                if (selection == 0) {
                    return;
                } else if (selection >= 1 && selection <= simulations.size()) {
                    // Display details of the selected simulation
                    Simulation selectedSimulation = simulations.get(selection - 1);
                    System.out.println("\nSimulation Details:");
                    System.out.println("Name: " + selectedSimulation.getName());
                    System.out.println("States:" + stateNumber);
                    for (State state : selectedSimulation.getStates()) {
                        System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                    }
                    System.out.println("Start State: " + selectedSimulation.getStartState().getName());
                    System.out.println("End State: " + selectedSimulation.getEndState().getName());
                    System.out.println("Transitions:");
                    for (Transition transition : selectedSimulation.getTransitions()) {
                        System.out.println(transition.getSource().getName() + " -> " +
                                transition.getTarget().getName() + " (HL7 Message: " + transition.getHL7Event() + ")");
                        System.out.println("Frequency (" + transition.getSource().getName() + " to " + transition.getTarget().getName() + "): " + transition.getFrequency());
                        System.out.println("Probability (" + transition.getSource().getName() + " to " + transition.getTarget().getName() + "): " + transition.getProbability() + "\n");
                    }
                } else {
                    System.out.println("Invalid selection. Please enter a valid number.");
                }
            }
        }
    }

    private static void startSimulation() {
        Simulation selectedSimulation = selectSimulationForStateMachine();
        if (selectedSimulation != null) {
            System.out.println("Starting simulation: " + selectedSimulation.getName());
            runStateMachine(selectedSimulation);
        } else {
            System.out.println("No simulation selected. Returning to main menu.");
        }
    }

    private static Simulation selectSimulationForStateMachine() {
        while (true) {
            System.out.println("\nSELECT A SIMULATION CONFIGURATION TO START:");
            if (simulations.isEmpty()) {
                System.out.println("No configurations found.");
                return null;
            } else {
                for (int i = 0; i < simulations.size(); i++) {
                    Simulation simulation = simulations.get(i);
                    System.out.println((i + 1) + ") " + simulation.getName());
                }
                // Prompt the user to select a simulation configuration
                System.out.print("Enter the number of the configuration to start (or 0 to return to the main menu): ");
                int selection = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (selection == 0) {
                    return null; // Return to main menu
                } else if (selection >= 1 && selection <= simulations.size()) {
                    return simulations.get(selection - 1); // Return the selected simulation
                } else {
                    System.out.println("Invalid selection. Please enter a valid number.");
                }
            }
        }
    }

    private static void runStateMachine(Simulation simulation) {
        StateMachine stateMachine = new StateMachine(simulation, scanner);
        stateMachine.run();
    }

    private static void exitProgram() {
        System.out.println("\nClosing Session...");
        System.out.println("\nHL7 Simulator");
        System.out.println("Rita Costa 1171445");
        scanner.close();
        System.exit(0);
    }
}
