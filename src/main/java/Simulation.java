import java.util.ArrayList;
import java.util.List;

public class Simulation {
    private String name;
    private List<States> states;
    private List<Transitions> transitions;
    private List<String> events;

    // Default constructor for Jackson deserialization
    public Simulation() {
        // Initialize lists to avoid NullPointerException during deserialization
        this.states = new ArrayList<>();
        this.transitions = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public Simulation(String name, List<States> states, List<Transitions> transitions, List<String> events) {
        this.name = name;
        this.states = states;
        this.transitions = transitions;
        this.events = events;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<States> getStates() {
        return states;
    }

    public List<Transitions> getTransitions() {
        return transitions;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
}
