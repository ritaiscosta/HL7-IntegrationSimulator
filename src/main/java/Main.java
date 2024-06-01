import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static List<Simulation> simulations = new ArrayList<>();

    public static void main(String[] args) {
        // Load previous configuration files, if available
        List<String> selectedFiles = Configuration.selectConfigurationFiles();
        Configuration configuration = null;
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (String selectedFile : selectedFiles) {
                configuration = Configuration.readFromJson(selectedFile);
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
        System.out.print("\nEnter a name for the simulation: ");
        String simulationName = scanner.nextLine();

        // Create lists to store states and transitions
        ArrayList<State> states = new ArrayList<>();
        ArrayList<Transition> transitions = new ArrayList<>();

        // Prompt the user to enter states
        System.out.println("\nCREATING STATES for Simulation: " + simulationName);
        System.out.println("\nPlease create states. Enter an empty name to finish.");

        int stateNumber = 1; // Counter for state numbers
        while (true) {
            System.out.println("\nState number " + stateNumber);
            System.out.print("State name: ");
            String name = scanner.nextLine();
            if (name.isEmpty()) {
                break; // Exit the loop if the name is empty
            } else {
                System.out.print("Enter maximum capacity for this state: ");
                int maxCapacity;
                try {
                    maxCapacity = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Maximum capacity must be a number.");
                    continue; // Restart the loop
                }

                int uniqueID = State.StateIDGenerator.generateUniqueID();

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
        int startFrequency = getFrequency("Enter frequency for the start state transition: ");
        double startProbability = getProbability("Enter probability for the start state transition (0-1): ");
        Event startEvent = selectEventForTransition("Select event for the start state transition: ");

        Transition startTransition = new Transition(null, startState, startEvent, startProbability, startFrequency);
        transitions.add(startTransition);

        // End state transition
        int endFrequency = getFrequency("\nEnter frequency for the end state transition: ");
        double endProbability = getProbability("Enter probability for the end state transition (0-1): ");
        Event endEvent = selectEventForTransition("Select event for the end state transition: ");

        Transition endTransition = new Transition(endState, null, endEvent, endProbability, endFrequency);
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
            System.out.print("Enter the number of the source state: ");
            int sourceStateNumber = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Prompt the user to select the target state
            System.out.print("Enter the number of the target state: ");
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
                double probability = getProbability("Enter probability of transitioning from '" + sourceState.getName() + "' to '" + targetState.getName() + "' (0-1): ");
                int frequency = getFrequency("Enter transition frequency from '" + sourceState.getName() + "' to '" + targetState.getName() + "' (positive number):");

                // Select an event for the transition
                Event selectedEvent = selectEventForTransition("Select event for the transition: ");

                // Add the transition between the selected states with correct probability, frequency, and event
                Transition newTransition = new Transition(sourceState, targetState, selectedEvent, probability, frequency);
                transitions.add(newTransition);

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
                    Simulation simulation = new Simulation(simulationName, states, transitions, null, startState, endState);
                    simulations.add(simulation);

                    // Display transitions
                    System.out.println("\nCONFIGURATIONS CREATED for Simulation: " + simulationName);
                    for (State state : states) {
                        System.out.println("State: " + state.getName() + " (ID: " + state.getStateID() + ")");
                    }
                    System.out.println("Start State: " + startState.getName());
                    System.out.println("End State: " + endState.getName());
                    for (Transition transition : transitions) {
                        System.out.println((transition.getSource() == null ? "null" : transition.getSource().getName()) + " -> " +
                                (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + " (HL7 Message: " + transition.getEvent().getEventName() + ")");
                        System.out.println("Frequency (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " + (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getFrequency());
                        System.out.println("Probability (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " + (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getProbability());
                    }

                    // Save configurations to a JSON file
                    Configuration config = new Configuration(simulations);
                    String fileName = "simulation_" + Configuration.generateSimulationCode() + ".json"; // Unique file name using timestamp
                    Configuration.writeToJson(config, fileName);

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
                    .filter(t -> t.getSource() != null && t.getSource().equals(current))
                    .map(Transition::getTarget)
                    .filter(Objects::nonNull)
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
        for (int i = 0; i < simulations.size(); i++) {
            Simulation simulation = simulations.get(i);
            System.out.println((i + 1) + ") " + simulation.getName());
        }
        System.out.println((simulations.size() + 1) + ") Return to Main Menu");

        // Prompt the user to select a simulation to modify
        System.out.print("\nEnter the number of the simulation to modify: ");
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
            System.out.println("Start State: " + selectedSimulation.getStartState().getName());
            System.out.println("End State: " + selectedSimulation.getEndState().getName() + "\n");
            System.out.println("\nTRANSITIONS:");
            for (Transition transition : selectedSimulation.getTransitions()) {
                System.out.println(
                        (transition.getSource() == null ? "null" : transition.getSource().getName()) + " -> " +
                                (transition.getTarget() == null ? "null" : transition.getTarget().getName()) +
                                " (HL7 Message: " + transition.getEvent().getEventName() + ")"
                );
                System.out.println("Frequency (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " +
                        (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getFrequency());
                System.out.println("Probability (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " +
                        (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getProbability() + "\n");
            }

            // Prompt the user to confirm modification
            System.out.println("\nDo you want to modify this configuration? (yes/no)");
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (confirm.equals("yes")) {
                // Modify simulation
                System.out.println("\nEnter new configurations for the simulation (leave blank to keep current values):");

                // Modify simulation name
                System.out.print("Enter a new name for the simulation (current: " + selectedSimulation.getName() + "): ");
                String newName = scanner.nextLine().trim();
                if (!newName.isEmpty()) {
                    selectedSimulation.setName(newName);
                }

                // Modify states
                System.out.println("Modify states:");
                for (State state : selectedSimulation.getStates()) {
                    System.out.print("Enter new name for state '" + state.getName() +
                            "' (current: " + state.getName() + "): ");
                    String newStateName = scanner.nextLine().trim();
                    if (!newStateName.isEmpty()) {
                        state.setName(newStateName);
                    }
                    System.out.print("Enter new maximum capacity for state '" + state.getName() +
                            "' (current: " + state.getMaxCapacity() + "): ");
                    String maxCapacityInput = scanner.nextLine().trim();
                    if (!maxCapacityInput.isEmpty()) {
                        int newMaxCapacity = Integer.parseInt(maxCapacityInput);
                        state.setMaxCapacity(newMaxCapacity);
                    }
                }

                // Modify start and end states
                System.out.println("Modify start and end states:");
                System.out.println("\nSelect the start state:");
                for (int i = 0; i < selectedSimulation.getStates().size(); i++) {
                    System.out.println((i + 1) + ") " + selectedSimulation.getStates().get(i).getName());
                }
                System.out.print("Enter the number of the start state: ");
                int startStateIndex = scanner.nextInt() - 1;
                scanner.nextLine(); // Consume newline
                selectedSimulation.setStartState(selectedSimulation.getStates().get(startStateIndex));

                System.out.println("\nSelect the end state:");
                for (int i = 0; i < selectedSimulation.getStates().size(); i++) {
                    System.out.println((i + 1) + ") " + selectedSimulation.getStates().get(i).getName());
                }
                System.out.print("Enter the number of the end state: ");
                int endStateIndex = scanner.nextInt() - 1;
                scanner.nextLine(); // Consume newline
                selectedSimulation.setEndState(selectedSimulation.getStates().get(endStateIndex));

                // Modify transitions
                List<Transition> transitions = selectedSimulation.getTransitions();
                System.out.println("Modify transitions:");
                for (Transition transition : transitions) {
                    System.out.println("\nTransition from " + (transition.getSource() == null ? "null" : transition.getSource().getName()) +
                            " to " + (transition.getTarget() == null ? "null" : transition.getTarget().getName()));

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
                        System.out.print("Enter the number of the new source state for this transition (current: " +
                                (transition.getSource() == null ? "null" : transition.getSource().getName()) + "): ");
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
                        System.out.print("Enter the number of the new target state for this transition (current: " +
                                (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): ");
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
                        List<Event> availableEvents = Event.getAvailableEvents();
                        System.out.println("Available HL7 Events:");
                        for (int i = 0; i < availableEvents.size(); i++) {
                            System.out.println((i + 1) + ") " + availableEvents.get(i).getEventName());
                        }

                        // Prompt the user to select an event
                        System.out.print("Enter the number of the HL7 event to associate with this transition: ");
                        int eventNumber = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        // Validate the user's input
                        if (eventNumber >= 1 && eventNumber <= availableEvents.size()) {
                            // Get the selected event
                            Event selectedEvent = availableEvents.get(eventNumber - 1);

                            // Set the selected event for the transition
                            transition.setEvent(selectedEvent);
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

                // Display new configurations
                System.out.println("\nNew configurations:");
                System.out.println("\nName: " + selectedSimulation.getName());
                System.out.println("\nStates:");
                for (State state : selectedSimulation.getStates()) {
                    System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                }
                System.out.println("Start State: " + selectedSimulation.getStartState().getName());
                System.out.println("End State: " + selectedSimulation.getEndState().getName());
                System.out.println("\nTransitions:");
                for (Transition transition : selectedSimulation.getTransitions()) {
                    System.out.println((transition.getSource() == null ? "null" : transition.getSource().getName()) + " -> " +
                            (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + " (HL7 Message: " + transition.getEvent().getEventName() + ")");
                    System.out.println("Frequency (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " +
                            (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getFrequency());
                    System.out.println("Probability (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " +
                            (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getProbability() + "\n");
                }
                String newFileName = selectedFiles.get(0).replace(".json", "(MODIFIED).json");
                Configuration.updateJsonFile(configuration, newFileName);

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
                    System.out.println("\nName: " + selectedSimulation.getName());
                    System.out.println("\nStates:");
                    for (State state : selectedSimulation.getStates()) {
                        System.out.println(state.getName() + " (Max Capacity: " + state.getMaxCapacity() + ")");
                    }
                    System.out.println("\nStart State: " + selectedSimulation.getStartState().getName());
                    System.out.println("End State: " + selectedSimulation.getEndState().getName());
                    System.out.println("\nTransitions:");
                    for (Transition transition : selectedSimulation.getTransitions()) {
                        System.out.println((transition.getSource() == null ? "null" : transition.getSource().getName()) + " -> " +
                                (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + " (HL7 Message: " + transition.getEvent().getEventName() + ")");
                        System.out.println("Frequency (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " +
                                (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getFrequency());
                        System.out.println("Probability (" + (transition.getSource() == null ? "null" : transition.getSource().getName()) + " to " +
                                (transition.getTarget() == null ? "null" : transition.getTarget().getName()) + "): " + transition.getProbability());
                    }

                    System.out.println("\nPress Enter to view another simulation or 0 to return to the main menu.");
                    String input = scanner.nextLine();
                    if (input.equals("0")) {
                        return;
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
            System.out.println("\nStarting simulation: " + selectedSimulation.getName());

            // Debug: Print start and end state details
            System.out.println("\nStart State: " + selectedSimulation.getStartState().getName());
            System.out.println("End State: " + selectedSimulation.getEndState().getName());

            System.out.print("\nPress Enter to continue or 0 to exit:");
            String input = scanner.nextLine().trim();

            if ("0".equals(input)) {
                return; // Exit simulation and return to main menu
            }

            runStateMachine(selectedSimulation);

        } else {
            System.out.println("\nNo simulation selected. Returning to main menu.");
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
        StateMachine stateMachine = new StateMachine(simulation);
        stateMachine.run();
    }

    private static void exitProgram() {
        System.out.println("\nClosing Session...");
        System.out.println("\nHL7 Simulator");
        System.out.println("Rita Costa 1171445");
        scanner.close();
        System.exit(0);
    }

    private static int getFrequency(String prompt) {
        int input;
        while (true) {
            System.out.print(prompt);
            try {
                input = Integer.parseInt(scanner.nextLine().trim());
                if (input <= 0) {
                    throw new NumberFormatException();
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a positive number.");
            }
        }
        return input;
    }

    private static double getProbability(String prompt) {
        double input;
        while (true) {
            System.out.print(prompt);
            try {
                input = Double.parseDouble(scanner.nextLine().trim());
                if (input < 0 || input > 1) {
                    throw new NumberFormatException();
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 0 and 1.");
            }
        }
        return input;
    }

    private static Event selectEventForTransition(String prompt) {
        List<Event> availableEvents = Event.getAvailableEvents();
        System.out.println(prompt);
        for (int i = 0; i < availableEvents.size(); i++) {
            System.out.println((i + 1) + ") " + availableEvents.get(i).getEventName());
        }

        System.out.print("Enter the number of the event): ");
        int eventNumber = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (eventNumber >= 1 && eventNumber <= availableEvents.size()) {
            return availableEvents.get(eventNumber - 1);
        } else if (eventNumber == 0) {
            return null;
        } else {
            System.out.println("Invalid event number. No event will be associated.");
            return null;
        }
    }
}
