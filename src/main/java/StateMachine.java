import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class StateMachine {
    private final Simulation simulation;
    private final Map<State, Queue<Person>> stateQueues;
    private final Scanner scanner;
    private final HL7messageGenerator hl7messageGenerator = new HL7messageGenerator(); // Initialize HL7 message generator
    private boolean returnToMainMenuFlag = false; // Flag to indicate returning to the main menu

    public StateMachine(Simulation simulation, Scanner scanner) {
        this.simulation = simulation;
        this.scanner = scanner;
        this.stateQueues = new HashMap<>();

        // Initialize queues for all states in the simulation, including start and end states
        Set<State> uniqueStates = new HashSet<>(simulation.getStates());
        uniqueStates.add(simulation.getStartState());
        uniqueStates.add(simulation.getEndState());

        for (State state : uniqueStates) {
            stateQueues.put(state, new LinkedList<>());
        }

        // Debug: Print all initialized state queues
        System.out.println("\nStarting state machine for simulation: " + simulation.getName());
        System.out.println("\nState queues initialized for states: " + stateQueues.keySet().stream().map(State::getName).collect(Collectors.joining(", ")));

        // Initialize people in the start state based on the start transition
        Transition startTransition = findStartTransition();
        if (startTransition != null) {
            executeStartTransition(startTransition);
        }
    }

    public void run() {
        printStateQueues();

        while (!allPeopleInEndState() && !returnToMainMenuFlag) {
            List<Transition> possibleTransitions = getPossibleTransitions();
            for (Transition transition : possibleTransitions) {
                executeTransition(transition);
                if (returnToMainMenuFlag) break; // Check flag after each transition
            }
            if (!returnToMainMenuFlag) {
                printStateQueues();
                promptToContinue();
            }
        }

        if (!returnToMainMenuFlag) {
            System.out.println("\nAll people have reached the end state.");
        }
    }

    private Transition findStartTransition() {
        for (Transition transition : simulation.getTransitions()) {
            if (transition.getSource() == null) {
                return transition;
            }
        }
        return null;
    }

    private void executeStartTransition(Transition transition) {
        State target = transition.getTarget();
        if (target == null) {
            System.out.println("\nError: Target state is null in executeStartTransition.");
            return;
        }
        Queue<Person> targetQueue = stateQueues.get(target);

        // Create person information
        String firstName = PersonInfo.generateRandomFirstName();
        String lastName = PersonInfo.generateRandomLastName();
        String id = PersonInfo.generatePatientInternalID();
        PersonInfo personInfo = new PersonInfo(firstName, lastName, id);

        // Using the constructor with three parameters: firstName, lastName, id
        Person person = new Person(firstName, lastName, id);
        targetQueue.add(person);
        System.out.println("\n" + person.getFirstName() + " " + person.getLastName() + " (ID:" + person.getId() + ") entered the simulation");

        String hl7Event = transition.getHL7Event();
        if (hl7Event != null) {
            String cleanedEvent = cleanEventString(hl7Event);
            String hl7Message = generateHL7Message(cleanedEvent, personInfo);
            System.out.println("\nEvent: " + hl7Event);
            System.out.println(hl7Message);
            System.out.println("\n");
        } else {
            System.out.println("\nNO EVENT");
        }
    }

    private void executeTransition(Transition transition) {
        State source = transition.getSource();
        State target = transition.getTarget();

        String sourceName = (source != null) ? source.getName() : "Simulation Entry";
        String targetName = (target != null) ? target.getName() : "Simulation Exit";

        if (source == null) {
            // Special case: Adding people to the simulation
            Queue<Person> targetQueue = stateQueues.get(target);
            if (targetQueue != null) {
                // Create person information
                String firstName = PersonInfo.generateRandomFirstName();
                String lastName = PersonInfo.generateRandomLastName();
                String id = PersonInfo.generatePatientInternalID();
                PersonInfo personInfo = new PersonInfo(firstName, lastName, id);

                // Using the constructor with three parameters: firstName, lastName, id
                Person newPerson = new Person(firstName, lastName, id);
                targetQueue.add(newPerson);
                System.out.println("\nAdded " + newPerson.getFirstName() + " " + newPerson.getLastName() + " (ID:" + newPerson.getId() + ") to " + targetName);

                // Generate HL7 message for the new person
                String hl7Event = transition.getHL7Event();
                if (hl7Event != null) {
                    String cleanedEvent = cleanEventString(hl7Event);
                    String hl7Message = generateHL7Message(cleanedEvent, personInfo);
                    System.out.println("HL7 Event: " + hl7Event);
                    System.out.println(hl7Message);
                    System.out.println("\n");
                } else {
                    System.out.println("\nNO EVENT");
                }
            } else {
                System.err.println("Error: Target queue for state " + targetName + " is null.");
                System.out.println("\n");
            }
        } else if (target == null) {
            // Special case: Removing people from the simulation
            Queue<Person> sourceQueue = stateQueues.get(source);
            if (sourceQueue != null) {
                Person person = sourceQueue.poll();
                if (person != null) {
                    System.out.println("\nRemoved " + person.getFirstName() + " " + person.getLastName() + " (ID:" + person.getId() + ") from " + sourceName);

                    // Generate HL7 message for the removed person
                    PersonInfo personInfo = new PersonInfo(person.getFirstName(), person.getLastName(), person.getId());
                    String hl7Event = transition.getHL7Event();
                    if (hl7Event != null) {
                        String cleanedEvent = cleanEventString(hl7Event);
                        String hl7Message = generateHL7Message(cleanedEvent, personInfo);
                        System.out.println("HL7 Event: " + hl7Event);
                        System.out.println(hl7Message);
                        System.out.println("\n");
                    } else {
                        System.out.println("\nNO EVENT");
                    }
                } else {
                    System.err.println("Error: Source queue for state " + sourceName + " is empty.");
                    System.out.println("\n");
                }
            } else {
                System.err.println("Error: Source queue for state " + sourceName + " is null.");
                System.out.println("\n");
            }
        } else {
            // Regular transition
            Queue<Person> sourceQueue = stateQueues.get(source);
            Queue<Person> targetQueue = stateQueues.get(target);

            if (sourceQueue != null && targetQueue != null) {
                while (!sourceQueue.isEmpty() && targetQueue.size() < target.getMaxCapacity()) {
                    Person person = sourceQueue.poll();
                    targetQueue.add(person);
                    System.out.println("\nMoved " + person.getFirstName() + " " + person.getLastName() + " (ID:" + person.getId() + ") from " + sourceName + " to " + targetName);

                    // Generate HL7 message for the moved person
                    PersonInfo personInfo = new PersonInfo(person.getFirstName(), person.getLastName(), person.getId());
                    String hl7Event = transition.getHL7Event();
                    if (hl7Event != null) {
                        String cleanedEvent = cleanEventString(hl7Event);
                        String hl7Message = generateHL7Message(cleanedEvent, personInfo);
                        System.out.println("HL7 Event: " + hl7Event);
                        System.out.println(hl7Message);
                        System.out.println("\n");
                    } else {
                        System.out.println("\nNO EVENT");
                    }
                }
            } else {
                System.err.println("Error: Source queue or target queue is null. Source: " + sourceName + ", Target: " + targetName);
                System.out.println("\n");
            }
        }
    }

    private String generateHL7Message(String hl7Event, PersonInfo personInfo) {
        String methodName = HL7messageGenerator.HL7EventMethodMapper.getMethodName(hl7Event);
        if (methodName != null) {
            try {
                Method method = HL7messageGenerator.class.getMethod(methodName, PersonInfo.class);
                return (String) method.invoke(hl7messageGenerator, personInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error generating HL7 message: " + e.getMessage();
            }
        } else {
            return "No HL7 message associated with this event";
        }
    }

    private String cleanEventString(String event) {
        if (event == null) {
            return "";
        }
        return event.split(" ")[0];
    }

    private void printStateQueues() {
        System.out.println("\nCurrent state of the simulation:");
        for (State state : simulation.getStates()) {
            System.out.println(state.getName() + ": " + stateQueues.get(state).stream().map(Person::toString).collect(Collectors.joining(", ")));
        }
        System.out.println();
    }

    private void promptToContinue() {
        System.out.print("\nPress Enter to continue, or type 0 to exit to the main menu:");
        System.out.println("\n");
        String input = scanner.nextLine().trim();

        if ("0".equals(input)) {
            // Handle the exit to main menu logic
            returnToMainMenu();
        }
    }

    private void returnToMainMenu() {
        returnToMainMenuFlag = true;
        System.out.println("Simulation Ended.");
        System.out.println("Returning to the main menu...");
    }

    private boolean allPeopleInEndState() {
        State endState = simulation.getEndState();
        return stateQueues.get(endState).size() == stateQueues.values().stream().mapToInt(Queue::size).sum();
    }

    private List<Transition> getPossibleTransitions() {
        List<Transition> possibleTransitions = new ArrayList<>();
        for (Transition transition : simulation.getTransitions()) {
            State source = transition.getSource();
            if (source == null || (stateQueues.containsKey(source) && !stateQueues.get(source).isEmpty())) {
                possibleTransitions.add(transition);
            }
        }
        return possibleTransitions;
    }
}
