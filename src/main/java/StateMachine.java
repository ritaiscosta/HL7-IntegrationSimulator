import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StateMachine {
    private final Simulation simulation;
    private final Random random;
    private final TimeSimulator timer;
    private int personCount = 0;
    private final Map<String, List<Person>> stateLists;
    private BufferedWriter logWriter;
    private String logFilePath;
    private Alert initialAlert;
    private volatile boolean userStoppedSimulation = false;
    private static final AtomicInteger actionCounter = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private StateMachineVisualizer visualizer;

    public StateMachine(Simulation simulation, double timeMultiplier, StateMachineVisualizer visualizer) {
        this.simulation = simulation;
        this.random = new Random();
        this.timer = new TimeSimulator((long) timeMultiplier);
        this.stateLists = new HashMap<>();
        this.visualizer = visualizer;

        Set<State> uniqueStates = new HashSet<>(simulation.getStates());
        uniqueStates.add(simulation.getStartState());
        uniqueStates.add(simulation.getEndState());

        for (State state : uniqueStates) {
            stateLists.put(state.getName(), Collections.synchronizedList(new ArrayList<>()));
        }

        initializeLogFile();

        log("Starting state machine for simulation: " + simulation.getName());
        log("State lists initialized for states: " + String.join(", ", stateLists.keySet()));

        Transition startTransition = findStartTransition();
        if (startTransition != null) {
            executeStartTransition(startTransition);
        }

        personCount = stateLists.values().stream().mapToInt(List::size).sum();

        Platform.runLater(() -> visualizer.initialize(simulation.getStates(), simulation.getTransitions(), simulation.getStartState(), simulation.getEndState()));

        scheduleFrequencyBasedMovement();
    }

    private void scheduleFrequencyBasedMovement() {
        for (Transition transition : simulation.getTransitions()) {
            if (transition.getFrequency() <= 0) {
                log("Skipping transition with zero or negative frequency: " + transition.getSource() + " -> " + transition.getTarget());
                continue;
            }

            long period = (long) ((60.0 / transition.getFrequency()) * 1000); // Convert frequency to milliseconds
            long adjustedPeriod = Math.max((long) (period / timer.timeMultiplier), 1); // Adjust for time multiplier and ensure it's at least 1 millisecond

            scheduler.scheduleAtFixedRate(() -> {
                if (userStoppedSimulation) {
                    scheduler.shutdown(); // Cancel the scheduler if the simulation is stopped by the user
                    return;
                }
                if (allPeopleInEndState()) {
                    executeRemovalTransition(); // Initiate the removal transition
                    return;
                }
                if (shouldTransitionOccur(transition.getProbability()) && canExecuteTransition(transition)) {
                    executeTransition(transition);
                }
                timer.advanceSimulatedTime(adjustedPeriod); // Advance simulated time by the original period, not the adjusted one
            }, 0, adjustedPeriod, TimeUnit.MILLISECONDS);
        }
    }


    private boolean shouldTransitionOccur(double probability) {
        return random.nextDouble() <= probability;
    }

    private synchronized boolean canExecuteTransition(Transition transition) {
        State source = transition.getSource();
        State target = transition.getTarget();

        if (source == null) {
            return target != null && stateLists.containsKey(target.getName()) && stateLists.get(target.getName()).size() < target.getMaxCapacity();
        }

        if (target == null) {
            return source != null && stateLists.containsKey(source.getName()) && !stateLists.get(source.getName()).isEmpty();
        }

        List<Person> sourceList = stateLists.get(source.getName());
        List<Person> targetList = stateLists.get(target.getName());

        boolean canExecute = sourceList != null && (targetList == null || targetList.size() < target.getMaxCapacity());
        if (!canExecute && targetList != null && targetList.size() >= target.getMaxCapacity()) {
            log("Max capacity reached for state: " + target.getName());
        }
        return canExecute;
    }

    private synchronized void executeTransition(Transition transition) {
        if (userStoppedSimulation) return;

        State source = transition.getSource();
        State target = transition.getTarget();

        String sourceName = (source != null) ? source.getName() : "Simulation Entry";
        String targetName = (target != null) ? target.getName() : "Simulation Exit";

        String simulatedTimestamp = timer.getFormattedSimulatedTimestamp();

        if (source == null) {
            List<Person> targetList = stateLists.get(target.getName());
            if (targetList != null) {
                PatientInfo patientInfo = createNewPatientInfo();
                targetList.add(patientInfo);
                personCount++;
                log( patientInfo.getFirstName() + " " + patientInfo.getLastName() + " (ID:" + patientInfo.getId() + ") entered the simulation");

                Event event = transition.getEvent();
                if (event != null) {
                    String cleanedEvent = cleanEventString(event.getEventName());
                    String message = generateMessage(cleanedEvent, patientInfo, simulatedTimestamp);
                    log("HL7 Event: " + event.getEventName());
                    log( message);
                }
            } else {
                log( "Error: Target list for state " + targetName + " is null.");
            }
        } else if (target == null) {
            List<Person> sourceList = stateLists.get(source.getName());
            if (sourceList != null && !sourceList.isEmpty()) {
                Person person = sourceList.remove(0);
                personCount--;
                log(  person.getFirstName() + " " + person.getLastName() + " (ID:" + person.getId() + ") left the simulation");

                if (person instanceof PatientInfo) {
                    PatientInfo patientInfo = (PatientInfo) person;
                    Event event = transition.getEvent();
                    if (event != null) {
                        String cleanedEvent = cleanEventString(event.getEventName());
                        String message = generateMessage(cleanedEvent, patientInfo, simulatedTimestamp);
                        log("HL7 Event: " + event.getEventName());
                        log(message);
                    }
                }
            } else {
                log( "Error: Source list for state " + sourceName + " is null or empty.");
            }
        } else {
            List<Person> sourceList = stateLists.get(source.getName());
            List<Person> targetList = stateLists.get(target.getName());

            if (sourceList != null && targetList != null) {
                while (!sourceList.isEmpty() && targetList.size() < target.getMaxCapacity()) {
                    Person person = sourceList.remove(0);
                    targetList.add(person);
                    log( person.getFirstName() + " " + person.getLastName() +  " (ID:" + person.getId() + ")" + " moved from " + sourceName + " to " + targetName);

                    if (person instanceof PatientInfo) {
                        PatientInfo patientInfo = (PatientInfo) person;
                        Event event = transition.getEvent();
                        if (event != null) {
                            String cleanedEvent = cleanEventString(event.getEventName());
                            String message = generateMessage(cleanedEvent, patientInfo, simulatedTimestamp);
                            log("HL7 Event: " + event.getEventName());
                            log( message);
                        }
                    }
                }
                if (targetList.size() >= target.getMaxCapacity()) {
                    log( "Max capacity reached for state: " + target.getName());
                }
            } else {
                log( "Error: Source list or target list is null. Source: " + sourceName + ", Target: " + targetName);
            }
        }
        printStateLists();
        updatePersonCount();
        updateVisualizer();
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

        PatientInfo patientInfo = createNewPatientInfo();
        targetList.add(patientInfo);
        personCount++;


        log(  patientInfo.getFirstName() + " " + patientInfo.getLastName() + " (ID:" + patientInfo.getId() + ") entered the simulation");

        Event event = transition.getEvent();
        if (event != null) {
            String cleanedEvent = cleanEventString(event.getEventName());
            String message = generateMessage(cleanedEvent, patientInfo, timer.getFormattedSimulatedTimestamp());
            log( "Event: " + event.getEventName());
            log( message);
        }

        updateVisualizer();
    }

    private void executeRemovalTransition() {
        List<Transition> removalTransitions = getPossibleRemovalTransitions();
        for (Transition transition : removalTransitions) {
            if (userStoppedSimulation) return;
            if (canExecuteTransition(transition)) {
                executeTransition(transition);
            }
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

    private void updateVisualizer() {
        for (Map.Entry<String, List<Person>> entry : stateLists.entrySet()) {
            String stateName = entry.getKey();
            int count = entry.getValue().size();
            visualizer.updateState(stateName, count);
        }
    }

    private Transition findStartTransition() {
        for (Transition transition : simulation.getTransitions()) {
            if (transition.getSource() == null) {
                return transition;
            }
        }
        return null;
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
                String timestamp = timer.getFormattedSimulatedTime();
                String threadId = "Thread#" + Thread.currentThread().getId();
                logWriter.write(threadId + " - " + timestamp + " - " + message);
                logWriter.newLine();
                logWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        new Thread(() -> {
            while (!allPeopleInEndState() && !userStoppedSimulation) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (userStoppedSimulation) break;
                movePersons();
            }

            if (allPeopleInEndState() && !userStoppedSimulation) {
                log("All people have reached the end state.");
                while (!allPeopleRemoved() && !userStoppedSimulation) {
                    executeRemovalTransition();
                }
                if (!userStoppedSimulation) {
                    log("All people have been removed from the simulation.");
                }
            }

            timer.cancel();

            Platform.runLater(() -> {
                if (!userStoppedSimulation) {
                    showFinalAlert("The output of the simulation was saved on '" + logFilePath + "'");
                }
                visualizer.close();
            });
        }).start();
    }

    private void movePersons() {
        if (userStoppedSimulation) return;
        List<Transition> possibleTransitions = getPossibleTransitions();
        for (Transition transition : possibleTransitions) {
            if (userStoppedSimulation) return;
            if (shouldTransitionOccur(transition.getProbability()) && canExecuteTransition(transition)) {
                executeTransition(transition);
            }
        }
    }

    private PatientInfo createNewPatientInfo() {
        String sex = Person.generateRandomSex();
        String firstName = Person.generateRandomFirstName(sex);
        String lastName = Person.generateRandomLastName();
        String id = Person.generatePatientInternalID();
        String DOB = Person.generateRandomDOB();
        String street = Person.generateRandomStreet();
        String city = Person.generateRandomCity();
        String phoneNumber = Person.generateRandomPhoneNumber();
        String country = Person.generateRandomCountry();
        String admissionDateTime = ""; //to be set after
        String dischargeDateTime = ""; //to be set after

        return new PatientInfo(firstName, lastName, id, DOB, street, city, phoneNumber, country, sex, admissionDateTime, dischargeDateTime);
    }

    private String generateMessage(String eventName, PatientInfo patientInfo, String timestamp) {
        String methodName = Event.HL7EventMethodMapper.getMethodName(eventName);
        if (methodName != null) {
            try {
                Method method = Event.class.getMethod(methodName, PatientInfo.class, String.class);
                Event eventInstance = new Event(eventName, 0);
                String message = (String) method.invoke(eventInstance, patientInfo, timestamp);

                return message;
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
        synchronized (stateLists) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Current state of the simulation:\n");
            for (State state : simulation.getStates()) {
                String stateLog = state.getName() + ": " + stateLists.get(state.getName()).stream()
                        .map(person -> person.toString())
                        .collect(Collectors.joining(", "));
                logBuilder.append(stateLog).append("\n");
            }
            log(logBuilder.toString().trim());
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
        synchronized (stateLists) {
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

    private void updatePersonCount() {
        synchronized (stateLists) {
            personCount = simulation.getStates().stream()
                    .mapToInt(state -> stateLists.get(state.getName()).size())
                    .sum();
            log("Updated person count: " + personCount);
        }
    }

    public void stopSimulation() {
        userStoppedSimulation = true;
        log("Simulation was stopped by the user.");

        Platform.runLater(() -> {
            if (initialAlert != null) {
                initialAlert.close();
            }
            showFinalAlert("Simulation was stopped by the user.\nThe output of the simulation was saved on '" + logFilePath + "'");
            visualizer.close();
        });
    }

    private void showFinalAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Simulation Complete");
            alert.setHeaderText(null);

            Label label = new Label(message);
            label.setWrapText(true);

            ScrollPane scrollPane = new ScrollPane(label);
            scrollPane.setFitToWidth(true);

            VBox vbox = new VBox(scrollPane);
            vbox.setPadding(new Insets(10));
            alert.getDialogPane().setContent(vbox);

            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(new ButtonType("Return to Main Menu"));

            alert.getDialogPane().setMinWidth(400);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {

                Main.displayMenu();
            }

        });
    }
}
