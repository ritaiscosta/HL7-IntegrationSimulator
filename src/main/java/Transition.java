import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transition {
    private State source;
    private State target;
    private String hl7Event;
    private String simulationName;
    private double probability;
    private int frequency;

    @JsonCreator
    public Transition(
            @JsonProperty("source") State source,
            @JsonProperty("target") State target,
            @JsonProperty("hl7Event") String hl7Event,
            @JsonProperty("simulationName") String simulationName,
            @JsonProperty("probability") double probability,
            @JsonProperty("frequency") int frequency) {
        this.source = source;
        this.target = target;
        this.hl7Event = hl7Event;
        this.simulationName = simulationName;
        this.probability = probability;
        this.frequency = frequency;
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

    public String getHL7Event() {
        return hl7Event;
    }

    public void setHL7Event(String hl7Event) {
        this.hl7Event = hl7Event;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
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

    public boolean hasEvent() {
        return hl7Event != null && !hl7Event.isEmpty();
    }
}
