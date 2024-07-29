import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main extends Application {
    private static final List<Simulation> simulations = new ArrayList<>();
    private ListView<String> listView;
    private ObservableList<String> configFiles;
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 700;
    private static boolean isModified = false;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Select Previous Configuration Files");

        // Get configuration files
        List<String> files = Configuration.getConfigurationFiles();
        if (files.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No configurations", "No previous configuration files found.");
            primaryStage.close();
            displayMenu();  // Directly open the main window
            return;
        }

        configFiles = FXCollections.observableArrayList(files);

        // ListView for displaying configuration files
        listView = new ListView<>(configFiles);
        listView.getStyleClass().add("list-view");
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button loadButton = new Button("Select File");
        loadButton.getStyleClass().add("button");
        loadButton.setOnAction(event -> loadSelectedFiles(primaryStage));

        // Button to continue without selecting files
        Button continueButton = new Button("Continue Without Loading");
        continueButton.getStyleClass().add("button");
        continueButton.setOnAction(event -> {
            startApp(null);
            primaryStage.close();
        });

        // HBox layout for buttons with spacing and alignment
        HBox buttonBox = new HBox(5);
        buttonBox.setPadding(new Insets(5));
        buttonBox.setSpacing(5);
        buttonBox.setAlignment(Pos.CENTER);


        buttonBox.getChildren().addAll(loadButton,  continueButton);

        // Labels for title and subtitle
        Label titleLabel = new Label("Previous Configuration Files");
        titleLabel.getStyleClass().add("label-title");

        Label subtitleLabel = new Label("Choose the configuration files you want to load or proceed without loading a file");
        subtitleLabel.getStyleClass().add("label-subtitle");

        // VBox layout
        VBox vbox = new VBox(10, titleLabel, subtitleLabel, listView, buttonBox);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.show();
    }


    private void loadSelectedFiles(Stage stage) {
        List<String> selectedFiles = listView.getSelectionModel().getSelectedItems().stream().collect(Collectors.toList());
        if (selectedFiles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("No files selected");
            alert.setHeaderText("You have not selected any files.");
            alert.setContentText("Would you like to proceed without selecting files or go back to select files?");

            ButtonType buttonTypeProceed = new ButtonType("Proceed Without Selecting");
            ButtonType buttonTypeGoBack = new ButtonType("Go Back", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(buttonTypeProceed, buttonTypeGoBack);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeProceed) {
                startApp(null);
                stage.close();
            }
            // If Go Back is selected, do nothing and let the user make a selection
        } else {
            startApp(selectedFiles);
            stage.close();
        }
    }


    private static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void startApp(List<String> selectedFiles) {
        Configuration configuration = null;
        simulations.clear();  // Clear existing simulations to prevent conflicts

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            for (String selectedFile : selectedFiles) {
                try {
                    configuration = Configuration.readFromJson(selectedFile);
                    if (configuration != null) {
                        simulations.addAll(configuration.getSimulations());
                        showAlert(Alert.AlertType.INFORMATION, "Loaded", "Previous configurations loaded from " + selectedFile);
                    }
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error reading configurations from JSON file: " + selectedFile);
                }
            }
        }

        displayMenu();
    }

    public static void displayMenu() {
        Stage menuStage = new Stage();
        menuStage.setTitle("HL7 Simulator");

        Label welcomeLabel = new Label("Welcome to HL7 Simulator");
        welcomeLabel.getStyleClass().add("label-title");

        Label subtitleLabel = new Label("Please choose an option from the menu");
        subtitleLabel.getStyleClass().add("label-subtitle");

        Button createButton = new Button("Create a new Configuration");
        createButton.getStyleClass().add("button");
        Button modifyButton = new Button("Modify an existing Configuration");
        modifyButton.getStyleClass().add("button");
        Button listButton = new Button("List existing Configurations");
        listButton.getStyleClass().add("button");
        Button startButton = new Button("Start a Simulation");
        startButton.getStyleClass().add("button");
        Button loadPreviousConfigButton = new Button("Load Previous Configuration Files");
        loadPreviousConfigButton.getStyleClass().add("button");
        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("button");

        createButton.setOnAction(event -> {
            try {
                createNewSimulation(menuStage);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while creating a new configuration.");
            }
        });

        modifyButton.setOnAction(event -> {
            try {
                modifySimulationMenu(menuStage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        listButton.setOnAction(event -> {
            listSimulationsMenu(menuStage);
        });

        startButton.setOnAction(event -> {
            startSimulation(menuStage);
        });

        loadPreviousConfigButton.setOnAction(event -> {
            Main mainInstance = new Main();
            Stage primaryStage = new Stage();
            mainInstance.start(primaryStage);
            menuStage.close();
        });

        exitButton.setOnAction(event -> {
            exitProgram();
        });

        VBox vbox = new VBox(10, welcomeLabel, subtitleLabel, createButton, modifyButton, listButton, startButton, loadPreviousConfigButton, exitButton);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);  // Center the content
        Scene scene = new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        menuStage.setScene(scene);
        menuStage.setWidth(WINDOW_WIDTH);
        menuStage.setHeight(WINDOW_HEIGHT);
        menuStage.show();
    }

    private static void createNewSimulation(Stage ownerStage) throws IOException {
        ownerStage.close();  // Close the main window

        Stage stage = new Stage();
        stage.setTitle("Create New Configuration");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(ownerStage);

        // Step 1: Enter simulation name
        TextField simulationNameField = new TextField();
        Label simulationTitle = new Label("Simulation Name");
        simulationTitle.getStyleClass().add("label-title");

        Label simulationNameSubTitle = new Label("Write the name of the Simulation");
        simulationNameSubTitle.getStyleClass().add("label-subtitle");
        simulationNameField.setPromptText("Name of the Simulation");

        Button nextButton1 = new Button("Next");
        nextButton1.getStyleClass().add("button");

        // Button to go back to main menu
        Button returnButton1 = new Button("Return to Main Menu");
        returnButton1.getStyleClass().add("button");
        returnButton1.setOnAction(event -> showWarningDialog(stage));

        // HBox for main buttons
        HBox buttonBox1 = new HBox(10, nextButton1);
        buttonBox1.setAlignment(Pos.CENTER);

        // HBox for the return button
        HBox returnBox1 = new HBox(returnButton1);
        returnBox1.setAlignment(Pos.CENTER);

        // VBox for the main section
        VBox vbox1 = new VBox(10, simulationTitle, simulationNameSubTitle, simulationNameField, buttonBox1, returnBox1);
        vbox1.setPadding(new Insets(10));

        Scene scene1 = new Scene(vbox1, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene1.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        stage.setScene(scene1);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();

        nextButton1.setOnAction(event -> {
            String simulationName = simulationNameField.getText();
            if (simulationName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Simulation name cannot be empty.");
                return;
            }

            // Step 2: Create states
            Label stateTitle = new Label("Create States for the Simulation");
            stateTitle.getStyleClass().add("label-title");
            Label stateSubTitle = new Label("Write the name and the max capacity for the states of the Simulation");
            stateSubTitle.getStyleClass().add("label-subtitle");

            Label stateNameSubSubTitle = new Label("Enter the name of the state");
            stateNameSubSubTitle.getStyleClass().add("label-sub-subtitle");
            TextField stateNameField = new TextField();
            stateNameField.getStyleClass().add("text-field");
            stateNameField.setPromptText("State Name");

            Label stateCapacitySubSubTitle = new Label("Enter the max capacity of the state");
            stateCapacitySubSubTitle.getStyleClass().add("label-sub-subtitle");
            TextField stateCapacityField = new TextField();
            stateCapacityField.getStyleClass().add("text-field");
            stateCapacityField.setPromptText("Max Capacity");

            ListView<State> stateListView = new ListView<>();
            stateListView.getStyleClass().add("list-view");

            Button addStateButton = new Button("Add State");
            addStateButton.getStyleClass().add("button");
            Button nextButton2 = new Button("Next");
            nextButton2.getStyleClass().add("button");
            Button backButton2 = new Button("Back");
            backButton2.getStyleClass().add("button");
            nextButton2.setDisable(true);  // Initially disable the Next button

            // Button to go back to main menu
            Button returnButton2 = new Button("Return to Main Menu");
            returnButton2.getStyleClass().add("button");
            returnButton2.setOnAction(event2 -> showWarningDialog(stage));

            // HBox for main buttons
            HBox buttonBox2 = new HBox(10, backButton2, addStateButton, nextButton2);
            buttonBox2.setAlignment(Pos.CENTER);

            // HBox for the return button
            HBox returnBox2 = new HBox(returnButton2);
            returnBox2.setAlignment(Pos.CENTER);

            // VBox for the main section
            VBox vbox2 = new VBox(10, stateTitle, stateSubTitle, stateNameSubSubTitle, stateNameField, stateCapacitySubSubTitle, stateCapacityField, stateListView, buttonBox2, returnBox2);
            vbox2.setPadding(new Insets(10));

            Scene scene2 = new Scene(vbox2, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene2.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

            stage.setScene(scene2);

            List<State> states = new ArrayList<>();
            addStateButton.setOnAction(event2 -> {
                String stateName = stateNameField.getText();
                int maxCapacity;
                try {
                    maxCapacity = Integer.parseInt(stateCapacityField.getText());
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Max capacity must be a number.");
                    return;
                }

                if (states.stream().anyMatch(s -> s.getName().equals(stateName))) {
                    showAlert(Alert.AlertType.ERROR, "Error", "State name must be unique.");
                    return;
                }

                State newState = new State(stateName, maxCapacity, State.StateIDGenerator.generateUniqueID());
                states.add(newState);
                stateListView.getItems().add(newState);

                stateNameField.clear();
                stateCapacityField.clear();

                // Enable the Next button if at least two states are created
                if (states.size() >= 2) {
                    nextButton2.setDisable(false);
                }
            });

            backButton2.setOnAction(event2 -> {
                stage.setScene(scene1);
            });

            nextButton2.setOnAction(event2 -> {
                if (states.size() < 2) {
                    showAlert(Alert.AlertType.ERROR, "Error", "You must create at least two states.");
                    return;
                }

                // Step 3: Select start and end states
                Label startEndT = new Label("Start and End States");
                startEndT.getStyleClass().add("label-title");
                Label startEndSubT = new Label("Choose the Start and End state for your Simulation");
                startEndSubT.getStyleClass().add("label-subtitle");
                ChoiceBox<State> startStateChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(states));
                startStateChoiceBox.getStyleClass().add("combo-box");
                ChoiceBox<State> endStateChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(states));
                endStateChoiceBox.getStyleClass().add("combo-box");
                Button nextButton3 = new Button("Next");
                nextButton3.getStyleClass().add("button");
                Button backButton3 = new Button("Back");
                backButton3.getStyleClass().add("button");

                // Button to go back to main menu
                Button returnButton3 = new Button("Return to Main Menu");
                returnButton3.getStyleClass().add("button");
                returnButton3.setOnAction(event3 -> showWarningDialog(stage));

                // HBox for main buttons
                HBox buttonBox3 = new HBox(10, backButton3, nextButton3);
                buttonBox3.setAlignment(Pos.CENTER);

                // HBox for the return button
                HBox returnBox3 = new HBox(returnButton3);
                returnBox3.setAlignment(Pos.CENTER);

                // VBox for the main section
                VBox vbox3 = new VBox(10, startEndT, startEndSubT,
                        new Label("Select Start State"), startStateChoiceBox,
                        new Label("Select End State"), endStateChoiceBox, buttonBox3, returnBox3);
                vbox3.setPadding(new Insets(10));

                Scene scene3 = new Scene(vbox3, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene3.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                stage.setScene(scene3);

                backButton3.setOnAction(event3 -> {
                    stage.setScene(scene2);
                });

                nextButton3.setOnAction(event3 -> {
                    State startState = startStateChoiceBox.getValue();
                    State endState = endStateChoiceBox.getValue();
                    if (startState == null || endState == null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "You must select both a start state and an end state.");
                        return;
                    }
                    if (startState.equals(endState)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Start state and end state cannot be the same.");
                        return;
                    }

                    // Step 4: Configure special transitions
                    Label specialTransitionTitle = new Label("Start and End state Transitions");
                    specialTransitionTitle.getStyleClass().add("label-title");
                    Label specialTransitionSubTitle = new Label("Configure the special transitions for the Start State (People added to the Simulation) and");
                    specialTransitionSubTitle.getStyleClass().add("label-subtitle");
                    Label specialTransitionSubTitle2 = new Label("End State (People removed from the Simulation)");
                    specialTransitionSubTitle2.getStyleClass().add("label-subtitle");

                    TextField startFrequencyField = new TextField();
                    startFrequencyField.getStyleClass().add("text-field");
                    startFrequencyField.setPromptText("Frequency (per minute) for Start State");

                    Label startProbabilitySubTitle = new Label("Set the probability for the Start State (0.01 - 1)");
                    startProbabilitySubTitle.getStyleClass().add("label-sub-subtitle");

                    TextField startProbabilityField = new TextField();
                    startProbabilityField.getStyleClass().add("text-field");
                    startProbabilityField.setPromptText("Probability for Start State (0.01 - 1)");

                    TextField endFrequencyField = new TextField();
                    endFrequencyField.getStyleClass().add("text-field");
                    endFrequencyField.setPromptText("Frequency (per minute) for End State");

                    Label endProbabilitySubTitle = new Label("Set the probability for the End state (0.01 - 1)");
                    endProbabilitySubTitle.getStyleClass().add("label-sub-subtitle");

                    TextField endProbabilityField = new TextField();
                    endProbabilityField.getStyleClass().add("text-field");
                    endProbabilityField.setPromptText("Probability for End State (0.01 - 1)");

                    ChoiceBox<Event> startEventChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Event.getAvailableEvents()));
                    startEventChoiceBox.getStyleClass().add("combo-box");
                    ChoiceBox<Event> endEventChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Event.getAvailableEvents()));
                    endEventChoiceBox.getStyleClass().add("combo-box");

                    Button nextButton4 = new Button("Next");
                    nextButton4.getStyleClass().add("button");
                    Button backButton4 = new Button("Back");
                    backButton4.getStyleClass().add("button");

                    // Button to go back to main menu
                    Button returnButton4 = new Button("Return to Main Menu");
                    returnButton4.getStyleClass().add("button");
                    returnButton4.setOnAction(event4 -> showWarningDialog(stage));

                    // HBox for main buttons
                    HBox buttonBox4 = new HBox(10, backButton4, nextButton4);
                    buttonBox4.setAlignment(Pos.CENTER);

                    // HBox for the return button
                    HBox returnBox4 = new HBox(returnButton4);
                    returnBox4.setAlignment(Pos.CENTER);

                    // VBox for the main section
                    VBox vbox4 = new VBox(10, specialTransitionTitle, specialTransitionSubTitle,specialTransitionSubTitle2,
                            new Label("Set the frequency (per minute) of the Start State"), startFrequencyField, startProbabilitySubTitle, startProbabilityField,
                            new Label("Select Event for this Transition"), startEventChoiceBox,
                            new Label("Set the frequency (per minute) of the End State"), endFrequencyField, endProbabilitySubTitle, endProbabilityField,
                            new Label("Select Event for this Transition"), endEventChoiceBox, buttonBox4, returnBox4);
                    vbox4.setPadding(new Insets(10));

                    Scene scene4 = new Scene(vbox4, WINDOW_WIDTH, WINDOW_HEIGHT);
                    scene4.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                    stage.setScene(scene4);

                    backButton4.setOnAction(event4 -> {
                        stage.setScene(scene3);
                    });

                    nextButton4.setOnAction(event4 -> {
                        int startFrequency, endFrequency;
                        double startProbability, endProbability;
                        try {
                            startFrequency = Integer.parseInt(startFrequencyField.getText());
                            endFrequency = Integer.parseInt(endFrequencyField.getText());
                            startProbability = Double.parseDouble(startProbabilityField.getText());
                            endProbability = Double.parseDouble(endProbabilityField.getText());

                            if (startProbability < 0.01 || startProbability > 1.0 || endProbability < 0.01 || endProbability > 1.0) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Probability must be between 0.01 and 1.0.");
                                return;
                            }
                        } catch (NumberFormatException e) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Frequency must be an integer and probability must be a double between 0.01 and 1.0.");
                            return;
                        }

                        Event startEvent = startEventChoiceBox.getValue();
                        Event endEvent = endEventChoiceBox.getValue();

                        Transition startTransition = new Transition(null, startState, startEvent, startProbability, startFrequency);
                        Transition endTransition = new Transition(endState, null, endEvent, endProbability, endFrequency);

                        List<Transition> transitions = new ArrayList<>();
                        transitions.add(startTransition);
                        transitions.add(endTransition);

                        // Step 5: Add regular transitions
                        Label regularTransitionTitle = new Label("Add Transitions");
                        regularTransitionTitle.getStyleClass().add("label-title");
                        Label regularTransitionSubTitle = new Label("Add the Transitions between States by selecting the Source State (where it starts) and the Target State (when it ");
                        regularTransitionSubTitle.getStyleClass().add("label-subtitle");
                        Label regularTransitionSubTitle2 = new Label("ends), also adding a Frequency and Probability associated");
                        regularTransitionSubTitle2.getStyleClass().add("label-subtitle");

                        ChoiceBox<State> sourceStateChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(states));
                        sourceStateChoiceBox.getStyleClass().add("combo-box");
                        ChoiceBox<State> targetStateChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(states));
                        targetStateChoiceBox.getStyleClass().add("combo-box");

                        Label transitionFrequencySubTitle = new Label("Set the Frequency for the Transition");
                        transitionFrequencySubTitle.getStyleClass().add("label-sub-subtitle");
                        TextField transitionFrequencyField = new TextField();
                        transitionFrequencyField.getStyleClass().add("text-field");
                        transitionFrequencyField.setPromptText("Transition frequency (per minute)");

                        Label transitionProbabilitySubTitle = new Label("Set the probability for the Transition (0.01 - 1)");
                        transitionProbabilitySubTitle.getStyleClass().add("label-sub-subtitle");
                        TextField transitionProbabilityField = new TextField();
                        transitionProbabilityField.getStyleClass().add("text-field");
                        transitionProbabilityField.setPromptText("Transition probability (0.01 - 1)");

                        ChoiceBox<Event> transitionEventChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Event.getAvailableEvents()));
                        transitionEventChoiceBox.getStyleClass().add("combo-box");
                        ListView<Transition> transitionListView = new ListView<>();
                        transitionListView.getStyleClass().add("list-view");

                        Button addTransitionButton = new Button("Add Transition");
                        addTransitionButton.getStyleClass().add("button");
                        Button nextButton5 = new Button("Next");
                        nextButton5.getStyleClass().add("button");
                        Button backButton5 = new Button("Back");
                        backButton5.getStyleClass().add("button");

                        // Button to go back to main menu
                        Button returnButton5 = new Button("Return to Main Menu");
                        returnButton5.getStyleClass().add("button");
                        returnButton5.setOnAction(event5 -> showWarningDialog(stage));

                        // HBox for main buttons
                        HBox buttonBox5 = new HBox(10, backButton5, addTransitionButton, nextButton5);
                        buttonBox5.setAlignment(Pos.CENTER);

                        // HBox for the return button
                        HBox returnBox5 = new HBox(returnButton5);
                        returnBox5.setAlignment(Pos.CENTER);

                        // VBox for the main section
                        VBox vbox5 = new VBox(10, regularTransitionTitle, regularTransitionSubTitle,regularTransitionSubTitle2,
                                new Label("Choose the Source State"), sourceStateChoiceBox,
                                new Label("Choose the Target State"), targetStateChoiceBox,
                                transitionFrequencySubTitle, transitionFrequencyField,
                                transitionProbabilitySubTitle, transitionProbabilityField,
                                new Label("Select Event for Transition"), transitionEventChoiceBox,
                                transitionListView, buttonBox5, returnBox5);
                        vbox5.setPadding(new Insets(10));

                        Scene scene5 = new Scene(vbox5, WINDOW_WIDTH, WINDOW_HEIGHT);
                        scene5.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                        stage.setScene(scene5);

                        addTransitionButton.setOnAction(event5 -> {
                            State sourceState = sourceStateChoiceBox.getValue();
                            State targetState = targetStateChoiceBox.getValue();
                            if (sourceState == null || targetState == null) {
                                showAlert(Alert.AlertType.ERROR, "Error", "You must select both a source state and a target state.");
                                return;
                            }

                            int frequency;
                            double probability;
                            try {
                                frequency = Integer.parseInt(transitionFrequencyField.getText());
                                probability = Double.parseDouble(transitionProbabilityField.getText());

                                if (probability < 0.01 || probability > 1.0) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Probability must be between 0.01 and 1.0.");
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Frequency must be an integer and probability must be a double between 0.01 and 1.0.");
                                return;
                            }

                            if (isTransitionExists(transitions, sourceState, targetState)) {
                                showAlert(Alert.AlertType.ERROR, "Error", "This transitions already exists");
                                return;
                            }

                            Event transitionEvent = transitionEventChoiceBox.getValue();
                            Transition transition = new Transition(sourceState, targetState, transitionEvent, probability, frequency);
                            transitions.add(transition);
                            transitionListView.getItems().add(transition);
                        });

                        backButton5.setOnAction(event5 -> {
                            stage.setScene(scene4);
                        });

                        nextButton5.setOnAction(event5 -> {
                            if (transitions.size() < 2) { // At least the two special transitions should be there
                                showAlert(Alert.AlertType.ERROR, "Error", "You must add at least one regular transition.");
                                return;
                            }

                            if (!validateWorkflow(transitions, startState, endState)) {
                                showAlert(Alert.AlertType.ERROR, "Error", "The workflow does not lead from the start state to the end state.");
                                return;
                            }

                            // Step 6: Summary and save
                            Label summaryTitle = new Label("Configuration Details");
                            summaryTitle.getStyleClass().add("label-title");

                            // Create the visualizer and initialize it with the states and transitions
                            StateMachineVisualizer visualizer = new StateMachineVisualizer();
                            visualizer.initialize(states, transitions, startState, endState);

                            // Create a StackPane to hold the visualizer pane
                            StackPane visualizerPane = new StackPane();
                            visualizerPane.getChildren().add(visualizer.getPane());
                            visualizerPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT / 2); // Set preferred size for the visualizer


                            StringBuilder summary = new StringBuilder();
                            summary.append("Name: ").append(simulationName).append("\n\n");
                            summary.append("States:\n");
                            for (State state : states) {
                                summary.append(state.getName()).append(" (Max Capacity: ").append(state.getMaxCapacity()).append(")\n");
                            }
                            summary.append("\nStart State: ").append(startState.getName()).append("\n");
                            summary.append("End State: ").append(endState.getName()).append("\n\n");
                            summary.append("Transitions:\n");
                            for (Transition transition : transitions) {
                                summary.append((transition.getSource() == null ? "null" : transition.getSource().getName())).append(" -> ")
                                        .append((transition.getTarget() == null ? "null" : transition.getTarget().getName())).append(" (HL7 Message: ")
                                        .append(transition.getEvent().getEventName()).append(")\n");
                                summary.append("Frequency (").append(transition.getSource() == null ? "null" : transition.getSource().getName()).append(" to ")
                                        .append(transition.getTarget() == null ? "null" : transition.getTarget().getName()).append("): ").append(transition.getFrequency()).append("\n");
                                summary.append("Probability (").append(transition.getSource() == null ? "null" : transition.getSource().getName()).append(" to ")
                                        .append(transition.getTarget() == null ? "null" : transition.getTarget().getName()).append("): ").append(transition.getProbability()).append("\n\n");
                            }

                            Button finishButton = new Button("Finish");
                            finishButton.getStyleClass().add("button");
                            Button backButton6 = new Button("Back");
                            backButton6.getStyleClass().add("button");

                            TextArea textArea = new TextArea(summary.toString());
                            textArea.getStyleClass().add("text-area");
                            textArea.setEditable(false);
                            VBox.setVgrow(textArea, Priority.ALWAYS);

                            // Button to go back to main menu
                            Button returnButton6 = new Button("Return to Main Menu");
                            returnButton6.getStyleClass().add("button");
                            returnButton6.setOnAction(event6 -> showWarningDialog(stage));

                            // HBox for main buttons
                            HBox buttonBox6 = new HBox(10, backButton6, finishButton);
                            buttonBox6.setAlignment(Pos.CENTER);

                            // HBox for the return button
                            HBox returnBox6 = new HBox(returnButton6);
                            returnBox6.setAlignment(Pos.CENTER);

                            // VBox for the main section
                            VBox vbox6 = new VBox(10, summaryTitle, visualizerPane, textArea, buttonBox6, returnBox6);
                            vbox6.setPadding(new Insets(10));

                            Scene scene6 = new Scene(vbox6, WINDOW_WIDTH, WINDOW_HEIGHT);
                            scene6.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                            stage.setScene(scene6);

                            finishButton.setOnAction(event6 -> {
                                try {
                                    Simulation simulation = new Simulation(simulationName, states, transitions, startState, endState);
                                    simulations.add(simulation);
                                    Configuration config = new Configuration(simulations);
                                    String fileName = "simulation_" + Configuration.generateSimulationCode() + ".json";
                                    Configuration.writeToJson(config, fileName);
                                    showAlert(Alert.AlertType.INFORMATION, "Saved", "Configurations saved successfully to " + fileName);
                                    stage.close();
                                    displayMenu();  // Reopen the main menu
                                } catch (IOException e) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving the configuration.");
                                }
                            });

                            backButton6.setOnAction(event6 -> {
                                stage.setScene(scene5);
                            });
                        });
                    });
                });
            });
        });
    }

    private static boolean isTransitionExists(List<Transition> transitions, State source, State target) {
        return transitions.stream()
                .anyMatch(t -> t.getSource() != null && t.getSource().equals(source)
                        && t.getTarget() != null && t.getTarget().equals(target));
    }


    private static boolean validateWorkflow(List<Transition> transitions, State startState, State endState) {
        Map<Integer, List<Transition>> transitionMap = new HashMap<>();
        for (Transition transition : transitions) {
            if (transition.getSource() != null) {
                transitionMap.computeIfAbsent(transition.getSource().getStateID(), k -> new ArrayList<>()).add(transition);
            }
        }

        Set<Integer> visited = new HashSet<>();
        return isPathValid(transitionMap, visited, startState.getStateID(), endState.getStateID());
    }

    private static boolean isPathValid(Map<Integer, List<Transition>> transitionMap, Set<Integer> visited, int currentStateID, int endStateID) {
        if (currentStateID == endStateID) {
            return true;
        }

        visited.add(currentStateID);

        List<Transition> nextTransitions = transitionMap.get(currentStateID);
        if (nextTransitions == null) {
            return false;
        }

        for (Transition transition : nextTransitions) {
            if (!visited.contains(transition.getTarget().getStateID()) && isPathValid(transitionMap, visited, transition.getTarget().getStateID(), endStateID)) {
                return true;
            }
        }

        return false;
    }

    private static void modifySimulationMenu(Stage ownerStage) throws IOException {
        ownerStage.close();  // Close the main window

        if (simulations.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Configurations", "No configuration to modify.");
            displayMenu();  // Reopen the main menu
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Modify Existing Configuration");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(ownerStage);

        // Display existing configuration names
        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("list-view");
        for (Simulation simulation : simulations) {
            listView.getItems().add(simulation.getName());
        }
        Button selectButton = new Button("Select");
        selectButton.getStyleClass().add("button");
        Button returnButton = new Button("Return to Main Menu");
        returnButton.getStyleClass().add("button");

        Label titleLabel = new Label("Modify Existing Configuration");
        titleLabel.getStyleClass().add("label-title");
        Label subtitleLabel = new Label("Select a Configuration to Modify");
        subtitleLabel.getStyleClass().add("label-subtitle");

        // HBox for select button
        HBox buttonBox = new HBox(10, selectButton);
        buttonBox.setAlignment(Pos.CENTER);

        // HBox for return button
        HBox returnBox = new HBox(returnButton);
        returnBox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(10, titleLabel, subtitleLabel, listView, buttonBox, returnBox);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();

        selectButton.setOnAction(event -> {
            int selection = listView.getSelectionModel().getSelectedIndex();
            if (selection >= 0 && selection < simulations.size()) {
                Simulation selectedSimulation = simulations.get(selection);
                stage.close();
                modifyExistingSimulation(stage, selectedSimulation, selection);
            } else {
                showAlert(Alert.AlertType.ERROR, "Invalid Selection", "Please select a valid configuration.");
            }
        });

        returnButton.setOnAction(event -> {
            stage.close();
            displayMenu();
        });
    }

    private static void modifyExistingSimulation(Stage ownerStage, Simulation selectedSimulation, int simulationIndex) {
        Stage stage = new Stage();
        stage.setTitle("Modify Configuration: " + selectedSimulation.getName());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(ownerStage);

        String originalName = selectedSimulation.getName();
        List<State> originalStates = new ArrayList<>(selectedSimulation.getStates());
        List<Transition> originalTransitions = new ArrayList<>(selectedSimulation.getTransitions());
        State originalStartState = selectedSimulation.getStartState();
        State originalEndState = selectedSimulation.getEndState();

        // Step 1: Modify simulation name
        TextField simulationNameField = new TextField(selectedSimulation.getName());
        simulationNameField.getStyleClass().add("text-field");
        Button nextButton1 = new Button("Next");
        nextButton1.getStyleClass().add("button");

        // Button to go back to main menu
        Button returnButton1 = new Button("Return to Main Menu");
        returnButton1.getStyleClass().add("button");
        returnButton1.setOnAction(event -> showWarningDialog(stage));

        Label titleLabel1 = new Label("Simulation Name");
        titleLabel1.getStyleClass().add("label-title");
        Label subtitleLabel1 = new Label("Change the name of the simulation or click Next to keep the previous value");
        subtitleLabel1.getStyleClass().add("label-subtitle");

        Label subSubtitleLabel1 = new Label("Enter the new name for the simulation");
        subSubtitleLabel1.getStyleClass().add("label-sub-subtitle");

        // HBox for next button
        HBox buttonBox1 = new HBox(10, nextButton1);
        buttonBox1.setAlignment(Pos.CENTER);

        // HBox for return button
        HBox returnBox1 = new HBox(returnButton1);
        returnBox1.setAlignment(Pos.CENTER);

        VBox vbox1 = new VBox(10, titleLabel1, subtitleLabel1, subSubtitleLabel1, simulationNameField, buttonBox1, returnBox1);
        vbox1.setPadding(new Insets(10));
        Scene scene1 = new Scene(vbox1, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene1.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        stage.setScene(scene1);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();

        nextButton1.setOnAction(event -> {
            String simulationName = simulationNameField.getText();
            if (simulationName.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Simulation name cannot be empty.");
                return;
            }
            selectedSimulation.setName(simulationName);
            isModified = true;  // Mark as modified

            // Step 2: Modify states
            List<State> states = selectedSimulation.getStates();
            ObservableList<State> stateObservableList = FXCollections.observableArrayList(states);
            ListView<State> stateListView = new ListView<>(stateObservableList);
            stateListView.getStyleClass().add("list-view");
            Label subSubtitleLabel2 = new Label("Enter the new name for the State");
            subSubtitleLabel2.getStyleClass().add("label-sub-subtitle");
            TextField stateNameField = new TextField();
            stateNameField.getStyleClass().add("text-field");
            stateNameField.setPromptText("New State Name");
            Label subSubtitleLabel3 = new Label("Enter the new max capacity of the State");
            subSubtitleLabel3.getStyleClass().add("label-sub-subtitle");
            TextField stateCapacityField = new TextField();
            stateCapacityField.getStyleClass().add("text-field");
            stateCapacityField.setPromptText("New Max Capacity");
            Button updateStateButton = new Button("Update State");
            updateStateButton.getStyleClass().add("button");
            Button addStateButton = new Button("Add State");
            addStateButton.getStyleClass().add("button");
            Button nextButton2 = new Button("Next");
            nextButton2.getStyleClass().add("button");
            Button backButton2 = new Button("Back");
            backButton2.getStyleClass().add("button");

            // Button to go back to main menu
            Button returnButton2 = new Button("Return to Main Menu");
            returnButton2.getStyleClass().add("button");
            returnButton2.setOnAction(event2 -> showWarningDialog(stage));

            Label titleLabel2 = new Label("Modify States");
            titleLabel2.getStyleClass().add("label-title");
            Label subtitleLabel2 = new Label("Change the configuration of the States or click Next to keep the previous value");
            subtitleLabel2.getStyleClass().add("label-subtitle");

            // HBox for main buttons
            HBox buttonBox2 = new HBox(10, backButton2, updateStateButton, addStateButton, nextButton2);
            buttonBox2.setAlignment(Pos.CENTER);

            // HBox for return button
            HBox returnBox2 = new HBox(returnButton2);
            returnBox2.setAlignment(Pos.CENTER);

            VBox vbox2 = new VBox(10, titleLabel2, subtitleLabel2, subSubtitleLabel2, stateNameField, subSubtitleLabel3, stateCapacityField, stateListView, buttonBox2, returnBox2);
            vbox2.setPadding(new Insets(10));
            Scene scene2 = new Scene(vbox2, WINDOW_WIDTH, WINDOW_HEIGHT);
            scene2.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

            stage.setScene(scene2);

            // ChoiceBoxes for start and end states
            ObservableList<State> startStateList = FXCollections.observableArrayList(states);
            ObservableList<State> endStateList = FXCollections.observableArrayList(states);

            ChoiceBox<State> startStateChoiceBox = new ChoiceBox<>(startStateList);
            startStateChoiceBox.getStyleClass().add("combo-box");
            ChoiceBox<State> endStateChoiceBox = new ChoiceBox<>(endStateList);
            endStateChoiceBox.getStyleClass().add("combo-box");

            updateStateButton.setOnAction(event2 -> {
                String stateName = stateNameField.getText();
                int maxCapacity;
                try {
                    maxCapacity = Integer.parseInt(stateCapacityField.getText());
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Max capacity must be a number.");
                    return;
                }

                State selectedState = stateListView.getSelectionModel().getSelectedItem();
                if (selectedState == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "No state selected to update.");
                    return;
                }

                if (states.stream().anyMatch(s -> !s.equals(selectedState) && s.getName().equals(stateName))) {
                    showAlert(Alert.AlertType.ERROR, "Error", "State name must be unique.");
                    return;
                }

                selectedState.setName(stateName);
                selectedState.setMaxCapacity(maxCapacity);
                stateListView.refresh();
                stateObservableList.setAll(states); // Update observable list
                updateTransitions(selectedSimulation, stateObservableList);

                // Update ChoiceBoxes
                startStateList.setAll(states);
                endStateList.setAll(states);

                stateNameField.clear();
                stateCapacityField.clear();
                isModified = true;  // Mark as modified
            });

            addStateButton.setOnAction(event2 -> {
                String stateName = stateNameField.getText();
                int maxCapacity;
                try {
                    maxCapacity = Integer.parseInt(stateCapacityField.getText());
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Max capacity must be a number.");
                    return;
                }

                if (states.stream().anyMatch(s -> s.getName().equals(stateName))) {
                    showAlert(Alert.AlertType.ERROR, "Error", "State name must be unique.");
                    return;
                }

                State newState = new State(stateName, maxCapacity, State.StateIDGenerator.generateUniqueID());
                states.add(newState);
                stateObservableList.setAll(states); // Update observable list
                updateTransitions(selectedSimulation, stateObservableList);

                // Update ChoiceBoxes
                startStateList.setAll(states);
                endStateList.setAll(states);

                stateNameField.clear();
                stateCapacityField.clear();
                isModified = true;  // Mark as modified
            });

            backButton2.setOnAction(event2 -> {
                stage.setScene(scene1);
            });

            nextButton2.setOnAction(event2 -> {
                if (states.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "You must have at least one state.");
                    return;
                }

                startStateChoiceBox.setValue(selectedSimulation.getStartState());


                endStateChoiceBox.setValue(selectedSimulation.getEndState());
                Button nextButton3 = new Button("Next");
                nextButton3.getStyleClass().add("button");
                Button backButton3 = new Button("Back");
                backButton3.getStyleClass().add("button");

                // Button to go back to main menu
                Button returnButton3 = new Button("Return to Main Menu");
                returnButton3.getStyleClass().add("button");
                returnButton3.setOnAction(event3 -> showWarningDialog(stage));

                Label titleLabel3 = new Label("Modify Start and End State");
                titleLabel3.getStyleClass().add("label-title");
                Label subtitleLabel3 = new Label("Change the configuration of the Start and End State or click Next to keep the previous value");
                subtitleLabel3.getStyleClass().add("label-subtitle");

                // HBox for main buttons
                HBox buttonBox3 = new HBox(10, backButton3, nextButton3);
                buttonBox3.setAlignment(Pos.CENTER);

                // HBox for return button
                HBox returnBox3 = new HBox(returnButton3);
                returnBox3.setAlignment(Pos.CENTER);

                VBox vbox3 = new VBox(10, titleLabel3, subtitleLabel3,  new Label("Select a New Start State"), startStateChoiceBox, new Label("Select a New End State"), endStateChoiceBox, buttonBox3, returnBox3);
                vbox3.setPadding(new Insets(10));
                Scene scene3 = new Scene(vbox3, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene3.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                stage.setScene(scene3);

                backButton3.setOnAction(event3 -> {
                    stage.setScene(scene2);
                });

                nextButton3.setOnAction(event3 -> {
                    State startState = startStateChoiceBox.getValue();
                    State endState = endStateChoiceBox.getValue();
                    if (startState == null || endState == null) {
                        showAlert(Alert.AlertType.ERROR, "Error", "You must select both a start state and an end state.");
                        return;
                    }
                    selectedSimulation.setStartState(startState);
                    selectedSimulation.setEndState(endState);
                    startStateList.setAll(states);
                    endStateList.setAll(states);
                    isModified = true;  // Mark as modified

                    // Step 4: Modify transitions
                    ListView<Transition> transitionListView = new ListView<>(FXCollections.observableArrayList(selectedSimulation.getTransitions()));
                    transitionListView.getStyleClass().add("list-view");
                    Button modifyTransitionButton = new Button("Modify Transition");
                    modifyTransitionButton.getStyleClass().add("button");
                    Button addTransitionButton = new Button("Add Transition");
                    addTransitionButton.getStyleClass().add("button");
                    Button nextButton4 = new Button("Next");
                    nextButton4.getStyleClass().add("button");
                    Button backButton4 = new Button("Back");
                    backButton4.getStyleClass().add("button");

                    // Button to go back to main menu
                    Button returnButton4 = new Button("Return to Main Menu");
                    returnButton4.getStyleClass().add("button");
                    returnButton4.setOnAction(event4 -> showWarningDialog(stage));

                    Label titleLabel4 = new Label("Modify Transitions");
                    titleLabel4.getStyleClass().add("label-title");
                    Label subtitleLabel4 = new Label("Change the configuration of the Transitions or click Next to keep the previous value");
                    subtitleLabel4.getStyleClass().add("label-subtitle");

                    // HBox for main buttons
                    HBox buttonBox4 = new HBox(10, backButton4, modifyTransitionButton, addTransitionButton, nextButton4);
                    buttonBox4.setAlignment(Pos.CENTER);

                    // HBox for return button
                    HBox returnBox4 = new HBox(returnButton4);
                    returnBox4.setAlignment(Pos.CENTER);

                    VBox vbox4 = new VBox(10, titleLabel4, subtitleLabel4, transitionListView, buttonBox4, returnBox4);
                    vbox4.setPadding(new Insets(10));
                    Scene scene4 = new Scene(vbox4, WINDOW_WIDTH, WINDOW_HEIGHT);
                    scene4.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                    stage.setScene(scene4);

                    modifyTransitionButton.setOnAction(event4 -> {
                        Transition selectedTransition = transitionListView.getSelectionModel().getSelectedItem();
                        if (selectedTransition == null) {
                            showAlert(Alert.AlertType.ERROR, "Error", "No transition selected to modify.");
                            return;
                        }

                        Stage modifyTransitionStage = new Stage();
                        modifyTransitionStage.setTitle("Modify Transition");
                        modifyTransitionStage.initModality(Modality.APPLICATION_MODAL);
                        modifyTransitionStage.initOwner(stage);

                        ChoiceBox<State> sourceStateChoiceBox = new ChoiceBox<>(stateObservableList);
                        sourceStateChoiceBox.getStyleClass().add("combo-box");
                        ChoiceBox<State> targetStateChoiceBox = new ChoiceBox<>(stateObservableList);
                        targetStateChoiceBox.getStyleClass().add("combo-box");

                        if (selectedTransition.getSource() != null) {
                            sourceStateChoiceBox.setValue(selectedTransition.getSource());
                        } else {
                            sourceStateChoiceBox.setDisable(true);
                        }
                        if (selectedTransition.getTarget() != null) {
                            targetStateChoiceBox.setValue(selectedTransition.getTarget());
                        } else {
                            targetStateChoiceBox.setDisable(true);
                        }

                        Label titleLabel5 = new Label("Modify Transition");
                        titleLabel5.getStyleClass().add("label-title");

                        Label subSubtitleLabel6 = new Label("Set a new Frequency");
                        subSubtitleLabel6.getStyleClass().add("label-sub-subtitle");
                        TextField frequencyField = new TextField(String.valueOf(selectedTransition.getFrequency()));
                        frequencyField.getStyleClass().add("text-field");

                        Label subSubtitleLabel7 = new Label("Set a new Probability (0.01 - 1)");
                        subSubtitleLabel7.getStyleClass().add("label-sub-subtitle");
                        TextField probabilityField = new TextField(String.valueOf(selectedTransition.getProbability()));
                        probabilityField.getStyleClass().add("text-field");

                        Label subSubtitleLabel8 = new Label("Set a new type of Event");
                        subSubtitleLabel8.getStyleClass().add("label-sub-subtitle");
                        ChoiceBox<Event> eventChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Event.getAvailableEvents()));
                        eventChoiceBox.getStyleClass().add("combo-box");
                        eventChoiceBox.setValue(selectedTransition.getEvent());

                        Button saveButton = new Button("Save");
                        saveButton.getStyleClass().add("button");

                        VBox modifyVBox = new VBox(10, titleLabel5,
                                new Label("Source State"), sourceStateChoiceBox,
                                new Label("Target State"), targetStateChoiceBox,
                                subSubtitleLabel6, frequencyField,
                                subSubtitleLabel7, probabilityField,
                                subSubtitleLabel8, eventChoiceBox, saveButton);
                        modifyVBox.setPadding(new Insets(10));
                        Scene modifyScene = new Scene(modifyVBox, WINDOW_WIDTH, WINDOW_HEIGHT);
                        modifyScene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                        modifyTransitionStage.setScene(modifyScene);
                        modifyTransitionStage.setWidth(WINDOW_WIDTH);
                        modifyTransitionStage.setHeight(WINDOW_HEIGHT);
                        modifyTransitionStage.show();

                        saveButton.setOnAction(event5 -> {
                            State sourceState = sourceStateChoiceBox.getValue();
                            State targetState = targetStateChoiceBox.getValue();
                            int frequency;
                            double probability;

                            try {
                                frequency = Integer.parseInt(frequencyField.getText());
                                probability = Double.parseDouble(probabilityField.getText());

                                if (probability < 0.01 || probability > 1.0) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Probability must be between 0.01 and 1.0.");
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Frequency must be an integer and probability must be a double between 0.01 and 1.0.");
                                return;
                            }

                            Event transitionEvent = eventChoiceBox.getValue();
                            if (sourceStateChoiceBox.isDisabled()) {
                                targetState = targetStateChoiceBox.getValue();
                                selectedTransition.setTarget(targetState);
                            } else if (targetStateChoiceBox.isDisabled()) {
                                sourceState = sourceStateChoiceBox.getValue();
                                selectedTransition.setSource(sourceState);
                            } else {
                                selectedTransition.setSource(sourceState);
                                selectedTransition.setTarget(targetState);
                            }
                            selectedTransition.setFrequency(frequency);
                            selectedTransition.setProbability(probability);
                            selectedTransition.setEvent(transitionEvent);

                            transitionListView.refresh();
                            isModified = true;  // Mark as modified
                            modifyTransitionStage.close();
                        });
                    });

                    addTransitionButton.setOnAction(event4 -> {
                        Stage addTransitionStage = new Stage();
                        addTransitionStage.setTitle("Add Transition");
                        addTransitionStage.initModality(Modality.APPLICATION_MODAL);
                        addTransitionStage.initOwner(stage);

                        ChoiceBox<State> sourceStateChoiceBox = new ChoiceBox<>(stateObservableList);
                        sourceStateChoiceBox.getStyleClass().add("combo-box");
                        ChoiceBox<State> targetStateChoiceBox = new ChoiceBox<>(stateObservableList);
                        targetStateChoiceBox.getStyleClass().add("combo-box");

                        Label titleLabel6 = new Label("Add a New Transition");
                        titleLabel6.getStyleClass().add("label-title");

                        Label subSubtitleLabel9 = new Label("Set a new Frequency");
                        subSubtitleLabel9.getStyleClass().add("label-sub-subtitle");
                        TextField frequencyField = new TextField();
                        frequencyField.getStyleClass().add("text-field");
                        frequencyField.setPromptText("Transition Frequency (per minute)");

                        Label subSubtitleLabel10 = new Label("Set a new Probability (0.01 - 1)");
                        subSubtitleLabel10.getStyleClass().add("label-sub-subtitle");
                        TextField probabilityField = new TextField();
                        probabilityField.getStyleClass().add("text-field");
                        probabilityField.setPromptText("Transition Probability (0.01 - 1)");

                        Label subSubtitleLabel11 = new Label("Set a new type of Event");
                        subSubtitleLabel11.getStyleClass().add("label-sub-subtitle");
                        ChoiceBox<Event> eventChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Event.getAvailableEvents()));
                        eventChoiceBox.getStyleClass().add("combo-box");

                        Button saveButton = new Button("Save");
                        saveButton.getStyleClass().add("button");

                        VBox addVBox = new VBox(10, titleLabel6,
                                new Label("Source State"), sourceStateChoiceBox,
                                new Label("Target State"), targetStateChoiceBox,
                                subSubtitleLabel9, frequencyField,
                                subSubtitleLabel10, probabilityField,
                                subSubtitleLabel11, eventChoiceBox, saveButton);
                        addVBox.setPadding(new Insets(10));
                        Scene addScene = new Scene(addVBox, WINDOW_WIDTH, WINDOW_HEIGHT);
                        addScene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                        addTransitionStage.setScene(addScene);
                        addTransitionStage.setWidth(WINDOW_WIDTH);
                        addTransitionStage.setHeight(WINDOW_HEIGHT);
                        addTransitionStage.show();

                        saveButton.setOnAction(event5 -> {
                            State sourceState = sourceStateChoiceBox.getValue();
                            State targetState = targetStateChoiceBox.getValue();
                            int frequency;
                            double probability;

                            try {
                                frequency = Integer.parseInt(frequencyField.getText());
                                probability = Double.parseDouble(probabilityField.getText());

                                if (probability < 0.01 || probability > 1.0) {
                                    showAlert(Alert.AlertType.ERROR, "Error", "Probability must be between 0.01 and 1.0.");
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "Frequency must be an integer and probability must be a double between 0.01 and 1.0.");
                                return;
                            }

                            if (isTransitionExists(selectedSimulation.getTransitions(), sourceState, targetState)) {
                                showAlert(Alert.AlertType.ERROR, "Error", "This Transition already exists.");
                                return;
                            }

                            Event transitionEvent = eventChoiceBox.getValue();
                            Transition newTransition = new Transition(sourceState, targetState, transitionEvent, probability, frequency);
                            selectedSimulation.getTransitions().add(newTransition);
                            transitionListView.getItems().add(newTransition);
                            isModified = true;  // Mark as modified
                            addTransitionStage.close();
                        });
                    });

                    backButton4.setOnAction(event4 -> {
                        stage.setScene(scene3);
                    });

                    nextButton4.setOnAction(event4 -> {
                        if (!validateWorkflow(selectedSimulation.getTransitions(), selectedSimulation.getStartState(), selectedSimulation.getEndState())) {
                            showAlert(Alert.AlertType.ERROR, "Error", "The workflow does not lead from the start state to the end state.");
                            return;
                        }

                        Label titleLabel7 = new Label("Configuration Details After Modifications");
                        titleLabel7.getStyleClass().add("label-title");

                        // Step 5: Summary and save
                        StringBuilder summary = new StringBuilder();
                        summary.append("Name: ").append(selectedSimulation.getName()).append("\n\n");
                        summary.append("States:\n");
                        for (State state : states) {
                            summary.append(state.getName()).append(" (Max Capacity: ").append(state.getMaxCapacity()).append(")\n");
                        }
                        summary.append("\nStart State: ").append(selectedSimulation.getStartState().getName()).append("\n");
                        summary.append("End State: ").append(selectedSimulation.getEndState().getName()).append("\n\n");
                        summary.append("Transitions:\n");
                        for (Transition transition : selectedSimulation.getTransitions()) {
                            summary.append((transition.getSource() == null ? "null" : transition.getSource().getName())).append(" -> ")
                                    .append((transition.getTarget() == null ? "null" : transition.getTarget().getName())).append(" (HL7 Message: ")
                                    .append(transition.getEvent().getEventName()).append(")\n");
                            summary.append("Frequency (").append(transition.getSource() == null ? "null" : transition.getSource().getName()).append(" to ")
                                    .append(transition.getTarget() == null ? "null" : transition.getTarget().getName()).append("): ").append(transition.getFrequency()).append("\n");
                            summary.append("Probability (").append(transition.getSource() == null ? "null" : transition.getSource().getName()).append(" to ")
                                    .append(transition.getTarget() == null ? "null" : transition.getTarget().getName()).append("): ").append(transition.getProbability()).append("\n\n");
                        }


                        // Visualize the state machine
                        StateMachineVisualizer visualizer = new StateMachineVisualizer();
                        visualizer.initialize(states, selectedSimulation.getTransitions(), selectedSimulation.getStartState(), selectedSimulation.getEndState());
                        Pane visualizerPane = visualizer.getPane();
                        visualizerPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT / 2);

                        Button finishButton = new Button("Finish");
                        finishButton.getStyleClass().add("button");
                        Button backButton5 = new Button("Back");
                        backButton5.getStyleClass().add("button");

                        TextArea textArea = new TextArea(summary.toString());
                        textArea.getStyleClass().add("text-area");
                        textArea.setEditable(false);
                        VBox.setVgrow(textArea, Priority.ALWAYS);

                        // HBox for main buttons
                        HBox buttonBox5 = new HBox(10, backButton5, finishButton);
                        buttonBox5.setAlignment(Pos.CENTER);

                        // HBox for return button
                        HBox returnBox5 = new HBox(returnButton4);
                        returnBox5.setAlignment(Pos.CENTER);

                        VBox vbox5 = new VBox(10,visualizerPane, titleLabel7, textArea, buttonBox5, returnBox5);
                        vbox5.setPadding(new Insets(10));
                        Scene scene5 = new Scene(vbox5, WINDOW_WIDTH, WINDOW_HEIGHT);
                        scene5.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                        stage.setScene(scene5);

                        finishButton.setOnAction(event5 -> {
                            if (originalName.equals(selectedSimulation.getName()) &&
                                    originalStates.equals(selectedSimulation.getStates()) &&
                                    originalTransitions.equals(selectedSimulation.getTransitions()) &&
                                    originalStartState.equals(selectedSimulation.getStartState()) &&
                                    originalEndState.equals(selectedSimulation.getEndState()) && !isModified) {
                                showAlert(Alert.AlertType.WARNING, "No Changes", "No changes detected. No new file will be created.");
                                stage.close();
                                displayMenu();  // Reopen the main menu
                                return;
                            }

                            try {
                                Configuration config = new Configuration(simulations);
                                String fileName = "simulation_" + Configuration.generateSimulationCode() + "(MODIFIED).json";
                                Configuration.updateJsonFile(config, fileName);
                                showAlert(Alert.AlertType.INFORMATION, "Saved", "Configurations saved successfully to " + fileName);
                                stage.close();
                                displayMenu();  // Reopen the main menu
                            } catch (IOException e) {
                                showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while saving the simulation.");
                            }
                        });

                        backButton5.setOnAction(event5 -> {
                            stage.setScene(scene4);
                        });
                    });
                });
            });
        });
    }




    private static void updateTransitions(Simulation simulation, ObservableList<State> stateObservableList) {
        for (Transition transition : simulation.getTransitions()) {
            if (transition.getSource() != null) {
                State sourceState = stateObservableList.stream().filter(state -> state.getStateID() == transition.getSource().getStateID()).findFirst().orElse(null);
                transition.setSource(sourceState);
            }
            if (transition.getTarget() != null) {
                State targetState = stateObservableList.stream().filter(state -> state.getStateID() == transition.getTarget().getStateID()).findFirst().orElse(null);
                transition.setTarget(targetState);
            }
        }
    }

    private static void showWarningDialog(Stage currentStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning");
        alert.setHeaderText("Are you sure you want to close this configuration?");
        alert.setContentText("The changes will not be saved.");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            currentStage.close();
            displayMenu();
        }
    }

    private static void listSimulationsMenu(Stage ownerStage) {
        ownerStage.close(); // Close the main menu stage
        if (simulations.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Configurations", "No configurations found.");
            displayMenu();  // Reopen the main menu if no configurations are found
            return;
        }

        Stage listStage = new Stage();
        listStage.setTitle("Simulation Configurations");

        Label titleLabel = new Label("Simulation Configurations");
        titleLabel.getStyleClass().add("label-title");

        Label subtitleLabel = new Label("Select a configuration to view the details");
        subtitleLabel.getStyleClass().add("label-subtitle");

        ListView<Simulation> listView = new ListView<>(FXCollections.observableArrayList(simulations));
        listView.getStyleClass().add("list-view");
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Simulation simulation, boolean empty) {
                super.updateItem(simulation, empty);
                if (empty || simulation == null || simulation.getName() == null) {
                    setText(null);
                } else {
                    setText(simulation.getName());
                }
            }
        });

        Button viewDetailsButton = new Button("View Details");
        viewDetailsButton.getStyleClass().add("button");
        Button backButton = new Button("Return to Main Menu");
        backButton.getStyleClass().add("button");

        VBox vbox = new VBox(10, titleLabel, subtitleLabel, listView, viewDetailsButton, backButton);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        listStage.setScene(scene);
        listStage.setWidth(WINDOW_WIDTH);
        listStage.setHeight(WINDOW_HEIGHT);
        listStage.show();

        viewDetailsButton.setOnAction(event -> {
            Simulation selectedSimulation = listView.getSelectionModel().getSelectedItem();
            if (selectedSimulation != null) {
                listExistingSimulations(selectedSimulation);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a configuration to view details.");
            }
        });

        backButton.setOnAction(event -> {
            listStage.close();
            displayMenu();
        });
    }

    private static void listExistingSimulations(Simulation simulation) {
        Stage detailsStage = new Stage();
        Label titleLabel = new Label("Configuration Details");
        titleLabel.getStyleClass().add("label-title");

        Label subtitleLabel = new Label(simulation.getName());
        subtitleLabel.getStyleClass().add("label-subtitle");

        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(simulation.getName()).append("\n\n");
        details.append("States:\n");
        for (State state : simulation.getStates()) {
            details.append(state.getName()).append(" (Max Capacity: ").append(state.getMaxCapacity()).append(")\n");
        }
        details.append("\nStart State: ").append(simulation.getStartState().getName()).append("\n");
        details.append("End State: ").append(simulation.getEndState().getName()).append("\n\n");
        details.append("Transitions:\n");
        for (Transition transition : simulation.getTransitions()) {
            details.append((transition.getSource() == null ? "null" : transition.getSource().getName())).append(" -> ")
                    .append((transition.getTarget() == null ? "null" : transition.getTarget().getName())).append(" (HL7 Message: ")
                    .append(transition.getEvent().getEventName()).append(")\n");
            details.append("Frequency (").append(transition.getSource() == null ? "null" : transition.getSource().getName()).append(" to ")
                    .append(transition.getTarget() == null ? "null" : transition.getTarget().getName()).append("): ").append(transition.getFrequency()).append("\n");
            details.append("Probability (").append(transition.getSource() == null ? "null" : transition.getSource().getName()).append(" to ")
                    .append(transition.getTarget() == null ? "null" : transition.getTarget().getName()).append("): ").append(transition.getProbability()).append("\n\n");
        }

        TextArea textArea = new TextArea(details.toString());
        textArea.getStyleClass().add("text-area");
        textArea.setEditable(false);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        // Visualize the state machine
        StateMachineVisualizer visualizer = new StateMachineVisualizer();
        visualizer.initialize(simulation.getStates(), simulation.getTransitions(), simulation.getStartState(), simulation.getEndState());
        Pane visualizerPane = visualizer.getPane();
        visualizerPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT / 2);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        VBox vbox = new VBox(10, titleLabel,subtitleLabel, visualizerPane, textArea, backButton);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        detailsStage.setScene(scene);
        detailsStage.setWidth(WINDOW_WIDTH);
        detailsStage.setHeight(WINDOW_HEIGHT);
        detailsStage.show();

        backButton.setOnAction(event -> detailsStage.close());
    }


    public static void selectSimulationForStateMachine(Consumer<Simulation> callback) {
        Stage stage = new Stage();
        stage.setTitle("Select Configuration");

        if (simulations.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Configurations", "No configurations found.");
            displayMenu();
            return;
        }

        Label titleLabel = new Label("Select a Simulation");
        titleLabel.getStyleClass().add("label-title");

        Label subtitleLabel = new Label("Please select a Simulation to Start");
        subtitleLabel.getStyleClass().add("label-subtitle");

        ListView<Simulation> listView = new ListView<>(FXCollections.observableArrayList(simulations));
        listView.getStyleClass().add("list-view");
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Simulation simulation, boolean empty) {
                super.updateItem(simulation, empty);
                if (empty || simulation == null || simulation.getName() == null) {
                    setText(null);
                } else {
                    setText(simulation.getName());
                }
            }
        });

        Button selectButton = new Button("Select");
        selectButton.getStyleClass().add("button");
        Button backButton = new Button("Back to Main Menu");
        backButton.getStyleClass().add("button");

        VBox vbox = new VBox(10, titleLabel, subtitleLabel, listView, selectButton, backButton);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();

        selectButton.setOnAction(event -> {
            Simulation selectedSimulation = listView.getSelectionModel().getSelectedItem();
            if (selectedSimulation != null) {
                stage.close();
                callback.accept(selectedSimulation);
            } else {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a configuration to start.");
            }
        });

        backButton.setOnAction(event -> {
            stage.close();
            displayMenu();
        });

        stage.setOnCloseRequest(event -> displayMenu());
    }

    public static void startSimulation(Stage ownerStage) {
        ownerStage.close();
        selectSimulationForStateMachine(selectedSimulation -> {
            if (selectedSimulation != null) {
                Stage stage = new Stage();
                stage.setTitle("Starting Simulation: " + selectedSimulation.getName());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(ownerStage);

                ChoiceBox<Double> timeMultiplierBox = new ChoiceBox<>();
                timeMultiplierBox.getStyleClass().add("combo-box");
                timeMultiplierBox.getItems().addAll( 1.0, 2.0);
                timeMultiplierBox.setValue(1.0);

                Label timeTitle = new Label("Select Time Multiplier");
                timeTitle.getStyleClass().add("label-sub-title");

                Button continueButton = new Button("Continue");
                continueButton.getStyleClass().add("button");
                Button backButton = new Button("Back");
                backButton.getStyleClass().add("button");
                Button mainMenuButton = new Button("Back to Main Menu");
                mainMenuButton.getStyleClass().add("button");

                backButton.setOnAction(event -> {
                    stage.close();
                    startSimulation(ownerStage);
                });

                mainMenuButton.setOnAction(event -> {
                    stage.close();
                    displayMenu();
                });

                continueButton.setOnAction(event -> {
                    double selectedMultiplier = timeMultiplierBox.getValue();
                    stage.close(); // Close the selection menu stage

                    // Pass the selectedMultiplier directly to the StateMachine
                    runStateMachine(selectedSimulation, (long) selectedMultiplier);
                });

                // Visualize the state machine
                StateMachineVisualizer visualizer = new StateMachineVisualizer();
                visualizer.initialize(selectedSimulation.getStates(), selectedSimulation.getTransitions(), selectedSimulation.getStartState(), selectedSimulation.getEndState());
                Pane visualizerPane = visualizer.getPane();
                visualizerPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT / 2);

                // Title for the state machine visualization
                Label stateMachineTitle = new Label("Simulation");
                stateMachineTitle.getStyleClass().add("label-title");

                // Name of the simulation
                Label stateMachineName = new Label("Starting Simulation: " + selectedSimulation.getName());
                stateMachineName.getStyleClass().add("label-subtitle");

                VBox vbox = new VBox(10, stateMachineTitle, stateMachineName, visualizerPane, timeTitle, timeMultiplierBox);
                vbox.setPadding(new Insets(10));
                vbox.setAlignment(Pos.CENTER);

                HBox buttonBox = new HBox(10, backButton, continueButton);
                buttonBox.setAlignment(Pos.CENTER);

                VBox mainBox = new VBox(10, vbox, buttonBox, mainMenuButton);
                mainBox.setPadding(new Insets(10));
                mainBox.setAlignment(Pos.TOP_CENTER);

                Scene scene = new Scene(mainBox, WINDOW_WIDTH, WINDOW_HEIGHT);
                scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

                stage.setScene(scene);
                stage.setWidth(WINDOW_WIDTH);
                stage.setHeight(WINDOW_HEIGHT);
                stage.show();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "No Configuration Selected", "No configuration selected. Returning to main menu.");
                displayMenu();
            }
        });
    }


    private static void runStateMachine(Simulation simulation, long timeMultiplier) {
        // Launch the visualizer
        Platform.runLater(() -> {
            try {
                StateMachineVisualizer visualizer = new StateMachineVisualizer();
                visualizer.setSimulationName(simulation.getName());
                Stage visualizerStage = new Stage();
                visualizer.start(visualizerStage);

                StateMachine stateMachine = new StateMachine(simulation, timeMultiplier, visualizer);
                visualizer.setStateMachine(stateMachine);
                stateMachine.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void exitProgram() {
        showAlert(Alert.AlertType.INFORMATION, "Exit", "Closing Session...\nHL7 Simulator\nRita Costa 1171445");
        System.exit(0);
    }
}
