import java.util.Arrays;
import java.util.List;

public class Events {
    private States source; // Source state of the transition
    private States target; // Target state of the transition
    private String hl7Event; // HL7 event associated with the transition

    // Define available HL7 trigger events
    private static final List<String> availableEvents = Arrays.asList(
            "ADT_A01 - Admit/visit Notification", "ADT_A02 - Transfer a Patient", "ADT_A03 - Discharge/End Visit", "ORM_O01 - General Order ", "ORU_R01 - Unsolicited transmission of an observation message");

    // Constructor
    public Events(States source, States target, String hl7Event) {
        this.source = source;
        this.target = target;
        this.hl7Event = hl7Event;
    }


    // Getter for available HL7 trigger events
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
