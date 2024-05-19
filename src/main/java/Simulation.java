import java.util.List;

public class Simulation {
    private String name;
    private List<State> states;
    private List<Transition> transitions;
    private List<String> events;
    private State startState;
    private State endState;

    // Default constructor needed for Jackson
    public Simulation() {
    }

    // Constructor with parameters
    public Simulation(String name, List<State> states, List<Transition> transitions, List<String> events, State startState, State endState) {
        this.name = name;
        this.states = states;
        this.transitions = transitions;
        this.events = events;
        this.startState = startState;
        this.endState = endState;
    }

    // Getters and setters for all properties
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public State getStartState() {
        return startState;
    }

    public void setStartState(State startState) {
        this.startState = startState;
    }

    public State getEndState() {
        return endState;
    }

    public void setEndState(State endState) {
        this.endState = endState;
    }
}
