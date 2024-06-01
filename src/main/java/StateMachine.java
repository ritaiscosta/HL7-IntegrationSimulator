import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StateMachine {
    private final Simulation simulation;
    private final Map<String, List<Person>> stateLists; // Using state name as the key
    private final Random random;
    private final Timer timer;
    private boolean returnToMainMenuFlag = false; // Flag to indicate returning to the main menu
    private int personCount = 0;
    private BufferedWriter logWriter;
    private String logFilePath;

    public StateMachine(Simulation simulation) {
        this.simulation = simulation;
        this.stateLists = new HashMap<>();
        this.random = new Random();
        this.timer = new Timer();

        // Initialize lists for all states in the simulation, including start and end states
        Set<State> uniqueStates = new HashSet<>(simulation.getStates());
        uniqueStates.add(simulation.getStartState());
        uniqueStates.add(simulation.getEndState());

        for (State state : uniqueStates) {
            stateLists.put(state.getName(), new ArrayList<>());
        }

        // Initialize log file
        initializeLogFile();

        log("Starting state machine for simulation: " + simulation.getName());
        log("State lists initialized for states: " + String.join(", ", stateLists.keySet()));

        // Initialize people in the start state based on the start transition
        Transition startTransition = findStartTransition();
        if (startTransition != null) {
            executeStartTransition(startTransition);
        }

        // Initialize personCount based on people in the start state
        personCount = stateLists.values().stream().mapToInt(List::size).sum();

        // Schedule frequency-based movement
        scheduleFrequencyBasedMovement();
    }

    private void initializeLogFile() {
        try {
            File logDir = new File("Simulation Logs", simulation.getName());
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String logFileName = simulation.getName() + "_" + sdf.format(new Date()) + ".txt";
            logFilePath = new File(logDir, logFileName).getPath();
            logWriter = new BufferedWriter(new FileWriter(logFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        try {
            if (logWriter != null) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                logWriter.write(timestamp + " - " + message);
                logWriter.newLine();
                logWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (!allPeopleInEndState() && !returnToMainMenuFlag) {
            try {
                Thread.sleep(1000); // Wait for 1 second between checks
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!returnToMainMenuFlag) {
            log("All people have reached the end state.");
            while (!allPeopleRemoved()) { // Continue executing removal transitions until all people are removed
                executeRemovalTransition();
            }
            log("All people have been removed from the simulation.");
        }

        // Cancel the timer to stop further movements
        timer.cancel();

        System.out.println("The output of the simulation was saved on '" + logFilePath+"'");
    }

    private Transition findStartTransition() {
        for (Transition transition : simulation.getTransitions()) {
            if (transition.getSource() == null) {
                return transition;
            }
        }
        return null;
    }

    private void executeStartTransition(Transition transition) {
        State target = transition.getTarget();
        if (target == null) {
            log("Error: Target state is null in executeStartTransition.");
            return;
        }
        List<Person> targetList = stateLists.get(target.getName());

        if (targetList == null) {
            log("Error: Target list for state " + target.getName() + " is null.");
            return;
        }

        // Create person information
        String firstName = PatientInfo.generateRandomFirstName();
        String lastName = PatientInfo.generateRandomLastName();
        String id = PatientInfo.generatePatientInternalID();
        PatientInfo patientInfo = new PatientInfo(firstName, lastName, id);

        // Using the constructor with three parameters: firstName, lastName, id
        targetList.add(patientInfo);
        personCount++;  // Increment person count
        log(patientInfo.getFirstName() + " " + patientInfo.getLastName() + " (ID:" + patientInfo.getId() + ") entered the simulation");

        Event event = transition.getEvent();
        if (event != null) {
            String cleanedEvent = cleanEventString(event.getEventName());
            String message = generateMessage(cleanedEvent, patientInfo);
            log("Event: " + event.getEventName());
            log(message);
        }
        printStateLists();
    }

    private void scheduleFrequencyBasedMovement() {
        int interval = 2000; // Default interval in milliseconds

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (allPeopleInEndState()) {
                    timer.cancel(); // Cancel the timer if all people are in the end state
                    executeRemovalTransition(); // Initiate the removal transition
                    return;
                }
                movePersons();
            }
        }, 0, interval);
    }

    private void movePersons() {
        List<Transition> possibleTransitions = getPossibleTransitions();
        boolean moved = false;
        for (Transition transition : possibleTransitions) {
            if (shouldTransitionOccur(transition)) {
                for (int i = 0; i < transition.getFrequency(); i++) {
                    if (canExecuteTransition(transition)) {
                        executeTransition(transition);
                        moved = true;
                    }
                }
            }
        }
        if (moved) {
            printStateLists();
        }
    }

    private void executeRemovalTransition() {
        List<Transition> removalTransitions = getPossibleRemovalTransitions();
        boolean moved = false;
        for (Transition transition : removalTransitions) {
            if (canExecuteTransition(transition)) {
                executeTransition(transition);
                moved = true;
            }
        }
        if (moved) {
            printStateLists();
        }
    }

    private List<Transition> getPossibleRemovalTransitions() {
        List<Transition> removalTransitions = new ArrayList<>();
        for (Transition transition : simulation.getTransitions()) {
            if (transition.getTarget() == null) {
                removalTransitions.add(transition);
            }
        }
        return removalTransitions;
    }

    private boolean shouldTransitionOccur(Transition transition) {
        double probability = transition.getProbability();
        return random.nextDouble() <= probability;
    }

    private boolean canExecuteTransition(Transition transition) {
        State source = transition.getSource();
        State target = transition.getTarget();

        if (source == null) {
            return target != null && stateLists.containsKey(target.getName()) && stateLists.get(target.getName()).size() < target.getMaxCapacity();
        }

        if (target == null) {
            boolean canExecute = source != null && stateLists.containsKey(source.getName()) && !stateLists.get(source.getName()).isEmpty();
            return canExecute;
        }

        List<Person> sourceList = stateLists.get(source.getName());
        List<Person> targetList = stateLists.get(target.getName());

        return sourceList != null && (targetList == null || targetList.size() < target.getMaxCapacity());
    }

    private void executeTransition(Transition transition) {
        State source = transition.getSource();
        State target = transition.getTarget();

        String sourceName = (source != null) ? source.getName() : "Simulation Entry";
        String targetName = (target != null) ? target.getName() : "Simulation Exit";

        if (source == null) {
            // Handle adding people to the simulation
            List<Person> targetList = stateLists.get(target.getName());
            if (targetList != null) {
                String firstName = PatientInfo.generateRandomFirstName();
                String lastName = PatientInfo.generateRandomLastName();
                String id = PatientInfo.generatePatientInternalID();
                PatientInfo patientInfo = new PatientInfo(firstName, lastName, id);

                targetList.add(patientInfo);
                personCount++;
                log("Added " + patientInfo.getFirstName() + " " + patientInfo.getLastName() + " (ID:" + patientInfo.getId() + ") to " + targetName);

                Event event = transition.getEvent();
                if (event != null) {
                    String cleanedEvent = cleanEventString(event.getEventName());
                    String message = generateMessage(cleanedEvent, patientInfo);
                    log("HL7 Event: " + event.getEventName());
                    log(message);
                }
            } else {
                log("Error: Target list for state " + targetName + " is null.");
            }
        } else if (target == null) {
            // Special case: Removing people from the simulation
            List<Person> sourceList = stateLists.get(source.getName());
            if (sourceList != null && !sourceList.isEmpty()) {
                Person person = sourceList.remove(0);
                personCount--;
                log("Removed " + person.getFirstName() + " " + person.getLastName() + " (ID:" + person.getId() + ") from " + sourceName);

                PatientInfo patientInfo = new PatientInfo(person.getFirstName(), person.getLastName(), person.getId());
                Event event = transition.getEvent();
                if (event != null) {
                    String cleanedEvent = cleanEventString(event.getEventName());
                    String message = generateMessage(cleanedEvent, patientInfo);
                    log("HL7 Event: " + event.getEventName());
                    log(message);
                }
            } else {
                log("Error: Source list for state " + sourceName + " is null or empty.");
            }
        } else {
            // Regular transition
            List<Person> sourceList = stateLists.get(source.getName());
            List<Person> targetList = stateLists.get(target.getName());

            if (sourceList != null && targetList != null) {
                while (!sourceList.isEmpty() && targetList.size() < target.getMaxCapacity()) {
                    Person person = sourceList.remove(0);
                    targetList.add(person);
                    log("Moved " + person.getFirstName() + " " + person.getLastName() + " (ID:" + person.getId() + ") from " + sourceName + " to " + targetName);

                    // Generate HL7 message for the moved person
                    PatientInfo patientInfo = new PatientInfo(person.getFirstName(), person.getLastName(), person.getId());
                    Event event = transition.getEvent();
                    if (event != null) {
                        String cleanedEvent = cleanEventString(event.getEventName());
                        String message = generateMessage(cleanedEvent, patientInfo);
                        log("HL7 Event: " + event.getEventName());
                        log(message);
                    }
                }
            } else {
                log("Error: Source list or target list is null. Source: " + sourceName + ", Target: " + targetName);
            }
        }
    }

    private String generateMessage(String eventName, PatientInfo patientInfo) {
        String methodName = Event.HL7EventMethodMapper.getMethodName(eventName);
        if (methodName != null) {
            try {
                Method method = Event.class.getMethod(methodName, PatientInfo.class);
                Event eventInstance = new Event(eventName, 0); // Use appropriate event name and ID
                return (String) method.invoke(eventInstance, patientInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error generating HL7 message: " + e.getMessage();
            }
        } else {
            return "No HL7 message associated with this event";
        }
    }

    private String cleanEventString(String event) {
        if (event == null) {
            return "";
        }
        return event.split(" ")[0];
    }

    private void printStateLists() {
        log("Current state of the simulation:");
        for (State state : simulation.getStates()) {
            log(state.getName() + ": " + stateLists.get(state.getName()).stream()
                    .map(person -> person.toString())
                    .collect(Collectors.joining(", ")));
        }
    }

    private boolean allPeopleInEndState() {
        State endState = simulation.getEndState();
        return stateLists.get(endState.getName()).size() == personCount;
    }

    private boolean allPeopleRemoved() {
        return stateLists.values().stream().allMatch(List::isEmpty);
    }

    private List<Transition> getPossibleTransitions() {
        List<Transition> possibleTransitions = new ArrayList<>();
        for (Transition transition : simulation.getTransitions()) {
            State source = transition.getSource();
            if (source == null || (stateLists.containsKey(source.getName()) && !stateLists.get(source.getName()).isEmpty())) {
                possibleTransitions.add(transition);
            }
        }
        return possibleTransitions;
    }
}
