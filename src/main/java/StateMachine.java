import java.util.List;

public class StateMachine {
    private Simulation simulation;
    private State currentState;

    public StateMachine(Simulation simulation) {
        this.simulation = simulation;
        this.currentState = simulation.getStartState();
    }

    public void run() {
        System.out.println("Starting state machine for simulation: " + simulation.getName());
        System.out.println("Initial State: " + currentState.getName());

        while (currentState != null && !currentState.equals(simulation.getEndState())) {
            List<Transition> possibleTransitions = getPossibleTransitions();
            if (possibleTransitions.isEmpty()) {
                System.out.println("No possible transitions from state: " + currentState.getName());
                break;
            }

            Transition transition = selectTransition(possibleTransitions);
            if (transition != null) {
                executeTransition(transition);
            } else {
                System.out.println("No valid transition found. Stopping simulation.");
                break;
            }
        }

        if (currentState.equals(simulation.getEndState())) {
            System.out.println("Reached the end state: " + currentState.getName());
        }
    }

    private List<Transition> getPossibleTransitions() {
        return simulation.getTransitions().stream()
                .filter(t -> t.getSource().equals(currentState))
                .toList();
    }

    private Transition selectTransition(List<Transition> transitions) {
        // For simplicity, we'll just select the first transition.
        // You can add more complex logic for selecting transitions here.
        return transitions.isEmpty() ? null : transitions.get(0);
    }

    private void executeTransition(Transition transition) {
        System.out.println("Transitioning from " + transition.getSource().getName() + " to " + transition.getTarget().getName() +
                " with HL7 event " + transition.getHL7Event());
        currentState = transition.getTarget();
    }
}
