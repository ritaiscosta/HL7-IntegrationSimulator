import java.util.Arrays;
import java.util.List;

public class Events {
    private States source; // Source state of the transition
    private States target; // Target state of the transition
    private String hl7Event; // HL7 event associated with the transition

    // Define available HL7 events
    private static final List<String> availableEvents = Arrays.asList("Event A", "Event B", "Event C");

    // Constructor
    public Events(States source, States target) {
        this.source = source;
        this.target = target;
    }

    // Getter and setter methods
    public States getSource() {
        return source;
    }

    public void setSource(States source) {
        this.source = source;
    }

    public States getTarget() {
        return target;
    }

    public void setTarget(States target) {
        this.target = target;
    }

    public String getHL7Event() {
        return hl7Event;
    }

    public void setHL7Event(String hl7Event) {
        this.hl7Event = hl7Event;
    }

    // Getter for available HL7 events
    public static List<String> getAvailableEvents() {
        return availableEvents;
    }

    // Method to update the event value
    public void updateEvent(String oldEvent, String newEvent) {
        int index = availableEvents.indexOf(oldEvent);
        if (index != -1) {
            availableEvents.set(index, newEvent);
        } else {
            System.out.println("Event '" + oldEvent + "' not found.");
        }
    }
}
