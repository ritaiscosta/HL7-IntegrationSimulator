import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Simulation {
    private String name;
    private List<State> states;
    private List<Transition> transitions;
    private State startState;
    private State endState;


    @JsonCreator
    public Simulation(
            @JsonProperty("name") String name,
            @JsonProperty("states") List<State> states,
            @JsonProperty("transitions") List<Transition> transitions,
            @JsonProperty("startState") State startState,
            @JsonProperty("endState") State endState) {
        this.name = name;
        this.states = states;
        this.transitions = transitions;
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

    public List<Transition> getTransitions() {
        return transitions;
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
