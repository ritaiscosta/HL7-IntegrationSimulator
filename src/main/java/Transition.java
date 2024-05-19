import java.util.Objects;

public class Transition {
    private State source;
    private State target;
    private String hl7Event;
    private String simulationName;
    private double probability;
    private int frequency;

    public Transition() {
        this.source = new State();
        this.target = new State();
        this.hl7Event = "";
        this.simulationName = "";
        this.probability = 0.0;
        this.frequency = 0;
    }

    public Transition(State source, State target, String hl7Event, String simulationName, double probability, int frequency) {
        this.source = Objects.requireNonNull(source, "Source state cannot be null");
        this.target = Objects.requireNonNull(target, "Target state cannot be null");
        this.hl7Event = hl7Event != null ? hl7Event : "";
        this.simulationName = simulationName != null ? simulationName : "";
        setProbability(probability);
        setFrequency(frequency);
    }

    public State getSource() {
        return source;
    }

    public void setSource(State source) {
        this.source = Objects.requireNonNull(source, "Source state cannot be null");
    }

    public State getTarget() {
        return target;
    }

    public void setTarget(State target) {
        this.target = Objects.requireNonNull(target, "Target state cannot be null");
    }

    public String getHL7Event() {
        return hl7Event;
    }

    public void setHL7Event(String hl7Event) {
        this.hl7Event = hl7Event != null ? hl7Event : "";
    }

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName != null ? simulationName : "";
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("Probability must be between 0.0 and 1.0");
        }
        this.probability = probability;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        if (frequency < 0) {
            throw new IllegalArgumentException("Frequency cannot be negative");
        }
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return Double.compare(that.probability, probability) == 0 &&
                frequency == that.frequency &&
                Objects.equals(source, that.source) &&
                Objects.equals(target, that.target) &&
                Objects.equals(hl7Event, that.hl7Event) &&
                Objects.equals(simulationName, that.simulationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, hl7Event, simulationName, probability, frequency);
    }

    @Override
    public String toString() {
        return "Transition{" +
                "source=" + source +
                ", target=" + target +
                ", hl7Event='" + hl7Event + '\'' +
                ", simulationName='" + simulationName + '\'' +
                ", probability=" + probability +
                ", frequency=" + frequency +
                '}';
    }
}
