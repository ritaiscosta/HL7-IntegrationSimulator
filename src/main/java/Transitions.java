public class Transitions {
    private States source;
    private States target;
    private String hl7Event;
    private String simulationName;


    public Transitions(States source, States target, String simulationName) {
        this.source = source;
        this.target = target;
        this.simulationName = simulationName;
    }

    public States getSource() {
        return source;
    }

    public States getTarget() {
        return target;
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


}
