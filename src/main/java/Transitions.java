import java.util.Objects;

public class Transitions {
    private States source;
    private States target;
    private String hl7Event;
    private String simulationName;
    private double probability; // Probability of transitioning to the target state
    private int frequency; // Frequency of transitioning to the target state

    // Default constructor for Jackson deserialization
    public Transitions() {
        this.source = new States();
        this.target = new States();
        this.hl7Event = "";
        this.simulationName = "";

    }

    public Transitions(States source, States target, String simulationName) {
        this.source = source;
        this.target = target;
        this.simulationName = simulationName;
        this.frequency = frequency;
        this.probability = probability;
    }

    public States getSource() {
        return source;
    }

    public States setSource(States newSourceState){
        return source;
    }

    public States getTarget() {
        return target;
    }

    public States setTarget(States newTargetState){
        return target;
    }

    public double getProbability() {
        return probability;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }



    public void setHL7Event(String hl7Event) {
        this.hl7Event = hl7Event;
    }


    public String getHL7Event() {
        return hl7Event;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transitions that = (Transitions) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(target, that.target) &&
                Objects.equals(hl7Event, that.hl7Event) &&
                Objects.equals(simulationName, that.simulationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, hl7Event, simulationName);
    }
}
