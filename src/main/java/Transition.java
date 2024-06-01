import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transition {

    private State source;
    private State target;
    private Event event;
    private double probability;
    private int frequency;

    @JsonCreator
    public Transition(
            @JsonProperty("source") State source,
            @JsonProperty("target") State target,
            @JsonProperty("event") Event event,
            @JsonProperty("probability") double probability,
            @JsonProperty("frequency") int frequency) {
        this.source = source;
        this.target = target;
        this.event = event;
        this.probability = probability;
        this.frequency = frequency;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public State getSource() {
        return source;
    }

    public void setSource(State source) {
        this.source = source;
    }

    public State getTarget() {
        return target;
    }

    public void setTarget(State target) {
        this.target = target;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
