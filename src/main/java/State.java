import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class State {

    private String name;
    private int stateID;
    private int maxCapacity;
    private List<Person> persons;
    private List<Transition> transitions;

    public State() {
        this("", 0);
    }

    public State(String name, int maxCapacity) {
        this.name = Objects.requireNonNull(name, "State name cannot be null");
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("Max capacity cannot be negative");
        }
        this.maxCapacity = maxCapacity;
        this.persons = new ArrayList<>();
        this.transitions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "State name cannot be null");
    }

    public int getStateID() {
        return stateID;
    }

    public void setStateID(int stateID) {
        this.stateID = stateID;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("Max capacity cannot be negative");
        }
        this.maxCapacity = maxCapacity;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = Objects.requireNonNull(persons, "Persons list cannot be null");
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = Objects.requireNonNull(transitions, "Transitions list cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return stateID == state.stateID &&
                maxCapacity == state.maxCapacity &&
                Objects.equals(name, state.name) &&
                Objects.equals(persons, state.persons) &&
                Objects.equals(transitions, state.transitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stateID, maxCapacity, persons, transitions);
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", stateID=" + stateID +
                ", maxCapacity=" + maxCapacity +
                ", persons=" + persons +
                ", transitions=" + transitions +
                '}';
    }
}
