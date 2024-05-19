import java.util.*;
import java.util.stream.Collectors;

public class StateMachine {
    private Simulation simulation;
    private Map<State, Queue<Person>> stateQueues;
    private Scanner scanner;

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
        System.out.println("State queues initialized for states: " + stateQueues.keySet().stream().map(State::getName).collect(Collectors.joining(", ")));

        // Initialize people in the start state based on the start transition
        Transition startTransition = findStartTransition();
        if (startTransition != null) {
            executeStartTransition(startTransition);
        }
    }

    public void run() {
        System.out.println("Starting state machine for simulation: " + simulation.getName());
        printStateQueues();

        while (!allPeopleInEndState()) {
            List<Transition> possibleTransitions = getPossibleTransitions();
            for (Transition transition : possibleTransitions) {
                executeTransition(transition);
            }
            printStateQueues();
            promptToContinue();
        }

        System.out.println("All people have reached the end state.");
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
        Queue<Person> targetQueue = stateQueues.get(target);

        // Debug: Check target state and queue
        System.out.println("Executing start transition to state: " + target.getName());

        if (targetQueue == null) {
            throw new NullPointerException("Target queue for state " + target.getName() + " is not initialized.");
        }

        int peopleToMove = Math.min(transition.getFrequency(), target.getMaxCapacity() - targetQueue.size());
        for (int i = 0; i < peopleToMove; i++) {
            if (Math.random() <= transition.getProbability()) {
                Person newPerson = new Person("Person_" + Person.getIdCounter());
                targetQueue.add(newPerson);
                System.out.println(newPerson + " entered the simulation at " + target.getName() + " with event " + transition.getHL7Event());
            }
        }
    }

    private boolean allPeopleInEndState() {
        for (State state : simulation.getStates()) {
            if (!state.equals(simulation.getEndState()) && !stateQueues.get(state).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private List<Transition> getPossibleTransitions() {
        List<Transition> possibleTransitions = new ArrayList<>();

        for (Transition transition : simulation.getTransitions()) {
            if (transition.getSource() == null && stateQueues.get(transition.getTarget()).size() < transition.getTarget().getMaxCapacity()) {
                possibleTransitions.add(transition);
            } else if (transition.getSource() != null && !stateQueues.get(transition.getSource()).isEmpty()) {
                possibleTransitions.add(transition);
            }
        }

        return possibleTransitions;
    }

    private void executeTransition(Transition transition) {
        State source = transition.getSource();
        State target = transition.getTarget();

        if (source == null) {
            // Handle special start state transition
            Queue<Person> targetQueue = stateQueues.get(target);

            int peopleToMove = Math.min(transition.getFrequency(), target.getMaxCapacity() - targetQueue.size());
            for (int i = 0; i < peopleToMove; i++) {
                if (Math.random() <= transition.getProbability()) {
                    Person newPerson = new Person("Person_" + Person.getIdCounter());
                    targetQueue.add(newPerson);
                    System.out.println(newPerson + " entered the simulation at " + target.getName() + " with event " + transition.getHL7Event());
                }
            }
        } else if (target == null) {
            // Handle special end state transition
            Queue<Person> sourceQueue = stateQueues.get(source);

            int peopleToMove = Math.min(sourceQueue.size(), transition.getFrequency());
            for (int i = 0; i < peopleToMove; i++) {
                Person person = sourceQueue.poll();
                if (Math.random() <= transition.getProbability()) {
                    System.out.println(person + " exited the simulation from " + source.getName() + " with event " + transition.getHL7Event());
                } else {
                    sourceQueue.add(person); // Re-add person to source queue if transition didn't happen
                }
            }
        } else {
            // Handle regular transition
            Queue<Person> sourceQueue = stateQueues.get(source);
            Queue<Person> targetQueue = stateQueues.get(target);

            int peopleToMove = Math.min(sourceQueue.size(), transition.getFrequency());
            for (int i = 0; i < peopleToMove; i++) {
                Person person = sourceQueue.poll();
                if (Math.random() <= transition.getProbability()) {
                    if (targetQueue.size() < target.getMaxCapacity()) {
                        targetQueue.add(person);
                        System.out.println(person + " moved from " + source.getName() + " to " + target.getName() + " with event " + transition.getHL7Event());
                    } else {
                        sourceQueue.add(person); // Re-add person to source queue if target is full
                        System.out.println(person + " could not move from " + source.getName() + " to " + target.getName() + " because " + target.getName() + " is full");
                    }
                } else {
                    sourceQueue.add(person); // Re-add person to source queue if transition didn't happen
                }
            }
        }
    }

    private void printStateQueues() {
        System.out.println("Current state of the simulation:");
        for (State state : simulation.getStates()) {
            System.out.println(state.getName() + ": " + stateQueues.get(state).stream().map(Person::toString).collect(Collectors.joining(", ")));
        }
        System.out.println();
    }

    private void promptToContinue() {
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}
