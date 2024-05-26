import java.util.*;
import java.util.stream.Collectors;

public class StateMachine {
    private Simulation simulation;
    private Map<State, Queue<Person>> stateQueues;
    private Scanner scanner;
    private HL7messageGenerator hl7messageGenerator = new HL7messageGenerator(); // Initialize HL7 message generator

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

        while (!allPeopleInEndState()) {
            List<Transition> possibleTransitions = getPossibleTransitions();
            for (Transition transition : possibleTransitions) {
                executeTransition(transition);
            }
            printStateQueues();
            promptToContinue();
        }

        System.out.println("\nAll people have reached the end state.");
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

        // Generate HL7 message
        String hl7Message = hl7messageGenerator.generateHL7Message(personInfo);

        Person person = new Person(firstName + " " + lastName);
        targetQueue.add(person);
        System.out.println("\n" + person + " entered the simulation");
        if (transition.getHL7Event() != null) {
            System.out.println("\nEvent: " + transition.getHL7Event());
        } else {
            System.out.println("NO EVENT");
        }
        System.out.println(hl7Message);
    }

    private void executeTransition(Transition transition) {
        State source = transition.getSource();
        State target = transition.getTarget();

        if (source == null) {
            executeStartTransition(transition);
        } else {
            Queue<Person> sourceQueue = stateQueues.get(source);
            Queue<Person> targetQueue = stateQueues.get(target);

            if (target == null) {
                System.out.println("\n Error: Target state is null for transition from " + source.getName());
                return;
            }

            // Ensure the target queue is initialized
            if (targetQueue == null) {
                targetQueue = new LinkedList<>();
                stateQueues.put(target, targetQueue);
                System.out.println("\nInitialized target queue for state: " + target.getName());
            }

            int peopleToMove = Math.min(sourceQueue.size(), transition.getFrequency());
            for (int i = 0; i < peopleToMove; i++) {
                Person person = sourceQueue.poll();
                if (Math.random() <= transition.getProbability()) {
                    if (targetQueue.size() < target.getMaxCapacity()) {
                        targetQueue.add(person);
                        System.out.println("\n" + person + " moved from " + source.getName() + " to " + target.getName() + " with event " + transition.getHL7Event());
                        PersonInfo personInfo = new PersonInfo(person.toString().split(" ")[0], person.toString().split(" ")[1], PersonInfo.generatePatientInternalID()); // Adjust as necessary
                        String hl7Message = hl7messageGenerator.generateHL7Message(personInfo);
                        if (transition.getHL7Event() != null) {
                            System.out.println("Event: " + transition.getHL7Event());
                        } else {
                            System.out.println("NO EVENT");
                        }
                        System.out.println("\n HL7 Message: " + hl7Message);
                    } else {
                        sourceQueue.add(person); // Re-add person to source queue if target is full
                        System.out.println("\n" + person + " could not move from " + source.getName() + " to " + target.getName() + " because " + target.getName() + " is full");
                    }
                } else {
                    sourceQueue.add(person); // Re-add person to source queue if transition didn't happen
                }
            }
        }
    }

    private void printStateQueues() {
        System.out.println("\nCurrent state of the simulation:");
        for (State state : simulation.getStates()) {
            System.out.println(state.getName() + ": " + stateQueues.get(state).stream().map(Person::toString).collect(Collectors.joining(", ")));
        }
        System.out.println();
    }

    private void promptToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
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
