import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Simulation {
    private String name;
    private List<State> states;
    private List<Transition> transitions;
    private List<String> events;
    private State startState;
    private State endState;

    @JsonCreator
    public Simulation(
            @JsonProperty("name") String name,
            @JsonProperty("states") List<State> states,
            @JsonProperty("transitions") List<Transition> transitions,
            @JsonProperty("events") List<String> events,
            @JsonProperty("startState") State startState,
            @JsonProperty("endState") State endState) {
        this.name = name;
        this.states = states;
        this.transitions = transitions;
        this.events = events;
        this.startState = startState;
        this.endState = endState;
    }

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
