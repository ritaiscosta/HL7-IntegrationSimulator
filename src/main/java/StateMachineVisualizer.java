import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StateMachineVisualizer extends Application {
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 700;
    private final Pane pane = new Pane();
    private final Map<String, Label> stateLabels = new HashMap<>();
    private final Map<String, Integer> stateCounts = new HashMap<>();
    private final Map<String, Circle> connectionPoints = new HashMap<>();
    private Stage stage;
    private StateMachine stateMachine;
    private String simulationName;

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }


    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        // Create the main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Create title and subtitle
        Label titleLabel = new Label("Simulation");
        titleLabel.getStyleClass().add("label-title");

        Label subtitleLabel = new Label("Starting Simulation: " + simulationName);
        subtitleLabel.getStyleClass().add("label-subtitle");

        // Add title and subtitle to the layout
        mainLayout.getChildren().addAll(titleLabel, subtitleLabel);

        // Add the state machine diagram pane
        pane.setPrefHeight(500);  // Adjust height if needed
        mainLayout.getChildren().add(pane);

        // Add Stop Button
        Button stopButton = new Button("Stop Simulation");
        stopButton.setId("stop-button");
        stopButton.setOnAction(event -> showConfirmationDialog());

        // Add Time Multiplier selection
        Label multiplierLabel = new Label("Select Time Multiplier");
        ComboBox<Double> multiplierComboBox = new ComboBox<>();
        multiplierComboBox.getItems().addAll(1.0, 2.0);
        multiplierComboBox.setValue(1.0);

        VBox multiplierBox = new VBox(5, multiplierLabel, multiplierComboBox);
        multiplierBox.setAlignment(Pos.CENTER);


        // Add button to a centered layout
        VBox bottomBox = new VBox(10, stopButton);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPrefHeight(100);
        mainLayout.getChildren().add(bottomBox);

        // Create the scene
        Scene scene = new Scene(mainLayout, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Simulation on Going");
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    private void showConfirmationDialog() {
        Platform.runLater(() -> {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Stop");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Are you sure you want to end the simulation?");
            confirmationAlert.getButtonTypes().clear();
            confirmationAlert.getButtonTypes().addAll(new ButtonType("Yes"), new ButtonType("No"));

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get().getText().equals("Yes")) {
                if (stateMachine != null) {
                    stateMachine.stopSimulation();
                }
            } else {
                confirmationAlert.close();
            }
        });
    }

    public Pane getPane() {
        return pane;
    }

    public void initialize(List<State> states, List<Transition> transitions, State startState, State endState) {
        Platform.runLater(() -> drawStateMachine(states, transitions, startState, endState));

        for (State state : states) {
            stateCounts.put(state.getName(), 0);
        }
    }

    private void drawStateMachine(List<State> states, List<Transition> transitions, State startState, State endState) {
        double centerX = pane.getWidth() / 2;
        double centerY = pane.getHeight() / 2;
        double totalWidth = states.size() * 150;

        // Draw states
        for (int i = 0; i < states.size(); i++) {
            createState(states.get(i).getName(), centerX - totalWidth / 2 + i * 150, centerY - 50);
        }

        // Draw transitions
        for (Transition transition : transitions) {
            if (transition.getSource() != null && transition.getTarget() != null) {
                createTransition(transition.getSource().getName(), transition.getTarget().getName());
            }
        }

        // Draw start and end transitions
        if (startState != null) {
            createStartTransition(startState.getName());
        }
        if (endState != null) {
            createEndTransition(endState.getName());
        }

        // Update state labels with initial counts
        updateStateLabels();
    }

    private void createState(String stateName, double x, double y) {
        Rectangle stateBox = new Rectangle(x, y, 100, 100); // Make it a square
        stateBox.setFill(Color.LIGHTBLUE);
        stateBox.setStroke(Color.BLACK); // Add border
        pane.getChildren().add(stateBox);

        Label stateLabel = new Label(stateName + "\n (0)");
        stateLabel.setLayoutX(x);
        stateLabel.setLayoutY(y + 40);
        stateLabel.setPrefWidth(100);
        stateLabel.setPrefHeight(50);
        stateLabel.setAlignment(Pos.CENTER);
        stateLabel.getStyleClass().add("state-label");

        stateLabels.put(stateName, stateLabel);
        pane.getChildren().add(stateLabel);

        // Add points on the middle of the right and left sides of the squares
        Circle leftPoint = new Circle(x, y + 50, 5, Color.BLACK);
        Circle rightPoint = new Circle(x + 100, y + 50, 5, Color.BLACK);
        leftPoint.setId(stateName + "_left");
        rightPoint.setId(stateName + "_right");
        connectionPoints.put(stateName + "_left", leftPoint);
        connectionPoints.put(stateName + "_right", rightPoint);
        pane.getChildren().addAll(leftPoint, rightPoint);


    }

    private void createTransition(String fromState, String toState) {
        Circle fromPoint = connectionPoints.get(fromState + "_right");
        Circle toPoint = connectionPoints.get(toState + "_left");
        if (fromPoint != null && toPoint != null) {
            double startX = fromPoint.getCenterX();
            double startY = fromPoint.getCenterY();
            double endX = toPoint.getCenterX();
            double endY = toPoint.getCenterY();

            double controlX1 = startX + (endX - startX) / 4;
            double controlY1 = startY - 50 - Math.abs(endX - startX) / 4;
            double controlX2 = startX + 3 * (endX - startX) / 4;
            double controlY2 = endY - 50 - Math.abs(endX - startX) / 4;

            CubicCurve transitionCurve = new CubicCurve(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
            transitionCurve.setStroke(Color.BLACK);
            transitionCurve.setFill(null);
            pane.getChildren().add(transitionCurve);

            // Add arrowhead
            addArrowHead(endX, endY, controlX2, controlY2);
        } else {
            System.err.println("Error: Unable to find connection points for states " + fromState + " and/or " + toState);
            System.err.println("From point: " + fromPoint + ", To point: " + toPoint);
        }
    }

    private void createStartTransition(String toState) {
        Circle toPoint = connectionPoints.get(toState + "_left");
        if (toPoint != null) {
            double startX = toPoint.getCenterX() - 50;
            double startY = toPoint.getCenterY();
            double endX = toPoint.getCenterX();
            double endY = toPoint.getCenterY();

            Line transitionLine = new Line(startX, startY, endX, endY);
            transitionLine.setStroke(Color.BLACK);  // Color the line red for visibility
            pane.getChildren().add(transitionLine);

            // Add arrowhead
            addArrowHead(endX, endY, startX, startY);
        } else {
            System.err.println("Error: Unable to find connection point for state " + toState);
            System.err.println("To point: " + toPoint);
        }
    }

    private void createEndTransition(String fromState) {
        Circle fromPoint = connectionPoints.get(fromState + "_right");
        if (fromPoint != null) {
            double startX = fromPoint.getCenterX();
            double startY = fromPoint.getCenterY();
            double endX = startX + 50;
            double endY = startY;

            Line transitionLine = new Line(startX, startY, endX, endY);
            transitionLine.setStroke(Color.BLACK);  // Color the line red for visibility
            pane.getChildren().add(transitionLine);

            // Add arrowhead
            addArrowHead(endX, endY, startX, startY);
        } else {
            System.err.println("Error: Unable to find connection point for state " + fromState);
            System.err.println("From point: " + fromPoint);
        }
    }

    private void addArrowHead(double x, double y, double controlX, double controlY) {
        double angle = Math.atan2(y - controlY, x - controlX);
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
                x, y,
                x - 10 * cos - 5 * sin, y - 10 * sin + 5 * cos,
                x - 10 * cos + 5 * sin, y - 10 * sin - 5 * cos
        );
        arrowHead.setFill(Color.BLUE);  // Color the arrowhead blue for visibility
        pane.getChildren().add(arrowHead);

    }

    public void updateState(String stateName, int count) {
        stateCounts.put(stateName, count);
        updateStateLabels();
    }

    private void updateStateLabels() {
        Platform.runLater(() -> {
            for (Map.Entry<String, Label> entry : stateLabels.entrySet()) {
                String stateName = entry.getKey();
                Label stateLabel = entry.getValue();
                int count = stateCounts.getOrDefault(stateName, 0);
                stateLabel.setText(stateName + "\n (" + count + ")");

            }
        });
    }


    public void close() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }
}
