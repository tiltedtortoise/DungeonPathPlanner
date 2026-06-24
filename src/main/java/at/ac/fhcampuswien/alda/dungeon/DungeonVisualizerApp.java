package at.ac.fhcampuswien.alda.dungeon;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DungeonVisualizerApp extends Application {

    private enum InteractionMode {
        SELECT,
        ADD_ROOM,
        ADD_EDGE,
        DELETE_ROOM,
        DELETE_EDGE,
        EDIT_COST
    }

    private static final double ROOM_RADIUS = 22;
    private static final double GRAPH_WIDTH = 1100;
    private static final double GRAPH_HEIGHT = 580;

    private static final Color DEFAULT_CORRIDOR_COLOR = Color.GRAY;
    private static final Color HIGHLIGHT_CORRIDOR_COLOR = Color.ORANGERED;
    private static final Color DEFAULT_ROOM_FILL = Color.LIGHTBLUE;
    private static final Color HIGHLIGHT_ROOM_FILL = Color.LIGHTGREEN;
    private static final Color START_ROOM_FILL = Color.web("#bbdefb");
    private static final Color START_ROOM_BORDER = Color.web("#1565c0");
    private static final Color TARGET_ROOM_FILL = Color.web("#e1bee7");
    private static final Color TARGET_ROOM_BORDER = Color.web("#7b1fa2");
    private static final Color PENDING_ROOM_FILL = Color.web("#fff9c4");

    private DungeonGraph graph;
    private Pane graphPane;
    private TextArea outputArea;
    private Label modeLabel;
    private Map<String, double[]> roomPositions;

    private InteractionMode interactionMode = InteractionMode.SELECT;
    private Room pendingFirstRoom;

    private Room selectedStartRoom;
    private Room selectedTargetRoom;
    private final Set<Room> algorithmHighlightedRooms = new HashSet<>();
    private final Set<Corridor> algorithmHighlightedCorridors = new HashSet<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        graph = DemoDungeonFactory.createDemoDungeon();
        roomPositions = createRoomPositions();

        graphPane = new Pane();
        graphPane.setPrefSize(GRAPH_WIDTH, GRAPH_HEIGHT);
        graphPane.setStyle("-fx-background-color: #f5f5f5;");
        graphPane.setOnMouseClicked(this::handlePaneClick);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(10);

        modeLabel = new Label();
        modeLabel.setStyle("-fx-font-weight: bold;");

        Button selectModeButton = new Button("Select");
        Button addRoomModeButton = new Button("Add Room");
        Button addEdgeModeButton = new Button("Add Edge");
        Button deleteRoomModeButton = new Button("Delete Room");
        Button deleteEdgeModeButton = new Button("Delete Edge");
        Button editCostModeButton = new Button("Edit Cost");

        selectModeButton.setOnAction(event -> setInteractionMode(InteractionMode.SELECT));
        addRoomModeButton.setOnAction(event -> setInteractionMode(InteractionMode.ADD_ROOM));
        addEdgeModeButton.setOnAction(event -> setInteractionMode(InteractionMode.ADD_EDGE));
        deleteRoomModeButton.setOnAction(event -> setInteractionMode(InteractionMode.DELETE_ROOM));
        deleteEdgeModeButton.setOnAction(event -> setInteractionMode(InteractionMode.DELETE_EDGE));
        editCostModeButton.setOnAction(event -> setInteractionMode(InteractionMode.EDIT_COST));

        HBox modeBar = new HBox(8, modeLabel, selectModeButton, addRoomModeButton, addEdgeModeButton,
                deleteRoomModeButton, deleteEdgeModeButton, editCostModeButton);
        modeBar.setPadding(new Insets(10, 10, 0, 10));
        modeBar.setAlignment(Pos.CENTER_LEFT);

        Button resetButton = new Button("Reset View");
        Button resetDungeonButton = new Button("Reset Dungeon");
        Button clearSelectionButton = new Button("Clear Selection");
        Button dfsReachabilityButton = new Button("DFS Reachability");
        Button dfsPathButton = new Button("DFS Path");
        Button dijkstraPathButton = new Button("Dijkstra Path");
        Button primMstButton = new Button("Prim MST");

        resetButton.setOnAction(event -> resetView());
        resetDungeonButton.setOnAction(event -> resetDungeon());
        clearSelectionButton.setOnAction(event -> clearSelection());
        dfsReachabilityButton.setOnAction(event -> showDfsReachability());
        dfsPathButton.setOnAction(event -> showDfsPath());
        dijkstraPathButton.setOnAction(event -> showDijkstraPath());
        primMstButton.setOnAction(event -> showPrimMst());

        HBox actionBar = new HBox(10, resetButton, resetDungeonButton, clearSelectionButton,
                dfsReachabilityButton, dfsPathButton, dijkstraPathButton, primMstButton);
        actionBar.setPadding(new Insets(10));
        actionBar.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(modeBar, actionBar);

        VBox bottomBox = new VBox(5, outputArea);
        bottomBox.setPadding(new Insets(0, 10, 10, 10));

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(graphPane);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, GRAPH_WIDTH, GRAPH_HEIGHT + 260);
        stage.setTitle("Dungeon Path Planner - Visualization");
        stage.setScene(scene);
        stage.show();

        setInteractionMode(InteractionMode.SELECT);
        resetView();
    }

    private Map<String, double[]> createRoomPositions() {
        Map<String, double[]> positions = new HashMap<>();
        positions.put("Entrance", new double[]{80, 260});
        positions.put("Guard Room", new double[]{230, 180});
        positions.put("Library", new double[]{230, 360});
        positions.put("Watchtower", new double[]{380, 80});
        positions.put("Armory", new double[]{500, 160});
        positions.put("Prison Cell", new double[]{480, 300});
        positions.put("Alchemy Lab", new double[]{380, 470});
        positions.put("Hidden Shrine", new double[]{600, 500});
        positions.put("Treasure Room", new double[]{760, 420});
        positions.put("Crypt", new double[]{650, 300});
        positions.put("Boss Room", new double[]{820, 220});
        positions.put("Exit", new double[]{1000, 300});
        return positions;
    }

    private void setInteractionMode(InteractionMode mode) {
        interactionMode = mode;
        pendingFirstRoom = null;
        modeLabel.setText("Mode: " + mode.name());
        updateModeHelpText();
        redrawGraph();
    }

    private void updateModeHelpText() {
        String help = switch (interactionMode) {
            case SELECT -> "Click rooms to choose start and target for algorithms.\n"
                    + "First click = start, second click = target.";
            case ADD_ROOM -> "Click on an empty area in the graph to place a new room.";
            case ADD_EDGE -> "Click two rooms to connect them. You will be asked for the corridor cost.";
            case DELETE_ROOM -> "Click a room to delete it. Entrance and Exit cannot be deleted.";
            case DELETE_EDGE -> "Click two rooms to delete the corridor between them.";
            case EDIT_COST -> "Click a corridor cost label to change the weight.";
        };

        outputArea.setText(help + "\n\n" + formatSelectionLines());
    }

    public Room getSelectedStartRoom() {
        return selectedStartRoom;
    }

    public Room getSelectedTargetRoom() {
        return selectedTargetRoom;
    }

    private void clearSelection() {
        selectedStartRoom = null;
        selectedTargetRoom = null;
        algorithmHighlightedRooms.clear();
        algorithmHighlightedCorridors.clear();
        pendingFirstRoom = null;
        updateModeHelpText();
        redrawGraph();
    }

    private void clearSelectionIfRoomRemoved(Room removedRoom) {
        if (removedRoom == null) {
            return;
        }
        if (removedRoom.equals(selectedStartRoom)) {
            selectedStartRoom = null;
        }
        if (removedRoom.equals(selectedTargetRoom)) {
            selectedTargetRoom = null;
        }
        if (removedRoom.equals(pendingFirstRoom)) {
            pendingFirstRoom = null;
        }
    }

    private boolean requireStartAndTarget() {
        if (selectedStartRoom == null || selectedTargetRoom == null) {
            outputArea.setText(
                    "Please select a start room and a target room first.\n\n" + formatSelectionLines());
            return false;
        }
        return true;
    }

    private boolean requireStart() {
        if (selectedStartRoom == null) {
            outputArea.setText("Please select a start room first.\n\n" + formatSelectionLines());
            return false;
        }
        return true;
    }

    private String formatSelectionLines() {
        return "Selected start room: " + formatRoomName(selectedStartRoom) + "\n"
                + "Selected target room: " + formatRoomName(selectedTargetRoom);
    }

    private String formatRoomName(Room room) {
        if (room == null) {
            return "(none)";
        }
        return room.getName();
    }

    private void handlePaneClick(MouseEvent event) {
        if (interactionMode != InteractionMode.ADD_ROOM) {
            return;
        }

        double x = event.getX();
        double y = event.getY();

        if (isPositionOnExistingRoom(x, y)) {
            showMessage("Please click on an empty area, not on an existing room.");
            return;
        }

        Optional<String> nameResult = promptText("Add Room", "Enter room name:", "");
        if (nameResult.isEmpty() || nameResult.get().trim().isEmpty()) {
            return;
        }

        String roomName = nameResult.get().trim();

        try {
            graph.addRoom(roomName);
            roomPositions.put(roomName, new double[]{x, y});
            showMessage("Room added: " + roomName);
            redrawGraph();
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void handleRoomClick(Room clickedRoom) {
        switch (interactionMode) {
            case SELECT -> handleSelectModeClick(clickedRoom);
            case ADD_EDGE -> handleAddEdgeClick(clickedRoom);
            case DELETE_ROOM -> handleDeleteRoomClick(clickedRoom);
            case DELETE_EDGE -> handleDeleteEdgeClick(clickedRoom);
            case ADD_ROOM, EDIT_COST -> {
                // pane click or corridor click handles these modes
            }
        }
    }

    private void handleSelectModeClick(Room clickedRoom) {
        if (selectedStartRoom != null && selectedTargetRoom != null) {
            selectedStartRoom = clickedRoom;
            selectedTargetRoom = null;
        } else if (selectedStartRoom == null) {
            selectedStartRoom = clickedRoom;
        } else {
            selectedTargetRoom = clickedRoom;
        }

        updateModeHelpText();
        redrawGraph();
    }

    private void handleAddEdgeClick(Room clickedRoom) {
        if (pendingFirstRoom == null) {
            pendingFirstRoom = clickedRoom;
            showMessage("First room selected: " + clickedRoom.getName()
                    + "\nNow click the second room for the new corridor.");
            redrawGraph();
            return;
        }

        if (pendingFirstRoom.equals(clickedRoom)) {
            showMessage("Please choose a different second room.");
            return;
        }

        Optional<Integer> costResult = promptPositiveInt("Add Edge",
                "Corridor cost between " + pendingFirstRoom.getName() + " and " + clickedRoom.getName() + ":",
                "1");
        if (costResult.isEmpty()) {
            pendingFirstRoom = null;
            redrawGraph();
            return;
        }

        try {
            graph.addCorridor(pendingFirstRoom.getName(), clickedRoom.getName(), costResult.get());
            showMessage("Corridor added between " + pendingFirstRoom.getName()
                    + " and " + clickedRoom.getName() + " with cost " + costResult.get() + ".");
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }

        pendingFirstRoom = null;
        redrawGraph();
    }

    private void handleDeleteRoomClick(Room clickedRoom) {
        try {
            graph.removeRoom(clickedRoom.getName());
            roomPositions.remove(clickedRoom.getName());
            clearSelectionIfRoomRemoved(clickedRoom);
            showMessage("Room deleted: " + clickedRoom.getName());
            redrawGraph();
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void handleDeleteEdgeClick(Room clickedRoom) {
        if (pendingFirstRoom == null) {
            pendingFirstRoom = clickedRoom;
            showMessage("First room selected: " + clickedRoom.getName()
                    + "\nNow click the second room to delete the corridor between them.");
            redrawGraph();
            return;
        }

        if (pendingFirstRoom.equals(clickedRoom)) {
            showMessage("Please choose a different second room.");
            return;
        }

        try {
            graph.removeCorridor(pendingFirstRoom.getName(), clickedRoom.getName());
            showMessage("Corridor deleted between " + pendingFirstRoom.getName()
                    + " and " + clickedRoom.getName() + ".");
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }

        pendingFirstRoom = null;
        redrawGraph();
    }

    private void handleCorridorCostClick(Corridor corridor) {
        if (interactionMode != InteractionMode.EDIT_COST) {
            return;
        }

        Optional<Integer> costResult = promptPositiveInt("Edit Cost",
                "New cost for corridor " + corridor.getRoomA().getName()
                        + " <-> " + corridor.getRoomB().getName() + ":",
                String.valueOf(corridor.getCost()));
        if (costResult.isEmpty()) {
            return;
        }

        try {
            graph.setCorridorCost(corridor.getRoomA().getName(), corridor.getRoomB().getName(), costResult.get());
            showMessage("Corridor cost updated to " + costResult.get() + ".");
            redrawGraph();
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }
    }

    private void showMessage(String message) {
        outputArea.setText(message + "\n\n" + formatSelectionLines());
    }

    private Optional<String> promptText(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Value:");
        return dialog.showAndWait();
    }

    private Optional<Integer> promptPositiveInt(String title, String header, String defaultValue) {
        Optional<String> result = promptText(title, header, defaultValue);
        if (result.isEmpty()) {
            return Optional.empty();
        }

        try {
            int value = Integer.parseInt(result.get().trim());
            if (value <= 0) {
                showMessage("Corridor cost must be a positive integer.");
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (NumberFormatException exception) {
            showMessage("Please enter a valid positive integer.");
            return Optional.empty();
        }
    }

    private boolean isPositionOnExistingRoom(double x, double y) {
        for (Room room : graph.getRooms()) {
            double[] position = getPosition(room);
            double deltaX = x - position[0];
            double deltaY = y - position[1];
            if (deltaX * deltaX + deltaY * deltaY <= ROOM_RADIUS * ROOM_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private void resetView() {
        algorithmHighlightedRooms.clear();
        algorithmHighlightedCorridors.clear();
        redrawGraph();
        outputArea.setText(
                "Reset view.\n\n"
                        + "Algorithm highlights were cleared. The dungeon itself was not changed.\n"
                        + "Use Reset Dungeon to restore the original demo dungeon.\n\n"
                        + formatSelectionLines()
        );
    }

    private void resetDungeon() {
        graph = DemoDungeonFactory.createDemoDungeon();
        roomPositions = createRoomPositions();
        selectedStartRoom = null;
        selectedTargetRoom = null;
        pendingFirstRoom = null;
        algorithmHighlightedRooms.clear();
        algorithmHighlightedCorridors.clear();
        setInteractionMode(InteractionMode.SELECT);
        outputArea.setText(
                "Demo dungeon restored.\n\n"
                        + "Entrance and Exit are protected and cannot be deleted.\n"
                        + "You can add, delete, and edit other rooms and corridors.\n\n"
                        + formatSelectionLines()
        );
        redrawGraph();
    }

    private void showDfsReachability() {
        if (!requireStartAndTarget()) {
            return;
        }

        boolean reachable = GraphAlgorithms.depthFirstSearch(
                graph, selectedStartRoom, selectedTargetRoom);

        algorithmHighlightedRooms.clear();
        algorithmHighlightedRooms.add(selectedStartRoom);
        algorithmHighlightedRooms.add(selectedTargetRoom);
        algorithmHighlightedCorridors.clear();
        redrawGraph();

        StringBuilder text = new StringBuilder();
        text.append("DFS Reachability from ")
                .append(selectedStartRoom.getName())
                .append(" to ")
                .append(selectedTargetRoom.getName())
                .append("\n\n");
        text.append("This checks only whether the target room is reachable from the start room.\n");
        text.append("It does not return a path.\n\n");

        if (reachable) {
            text.append("Result: YES, the target room is reachable.\n\n");
        } else {
            text.append("Result: NO, the target room is not reachable.\n\n");
        }

        text.append(formatSelectionLines());
        outputArea.setText(text.toString());
    }

    private void showDfsPath() {
        if (!requireStartAndTarget()) {
            return;
        }

        List<Room> path = GraphAlgorithms.findPathWithDfs(
                graph, selectedStartRoom, selectedTargetRoom);

        if (path.isEmpty()) {
            algorithmHighlightedRooms.clear();
            algorithmHighlightedCorridors.clear();
            redrawGraph();
            outputArea.setText(
                    "DFS found no path from "
                            + selectedStartRoom.getName()
                            + " to "
                            + selectedTargetRoom.getName()
                            + ".\n\n"
                            + formatSelectionLines());
            return;
        }

        algorithmHighlightedRooms.clear();
        algorithmHighlightedRooms.addAll(path);
        algorithmHighlightedCorridors.clear();
        algorithmHighlightedCorridors.addAll(findCorridorsOnPath(path));
        redrawGraph();

        StringBuilder text = new StringBuilder();
        text.append("DFS Path from ")
                .append(selectedStartRoom.getName())
                .append(" to ")
                .append(selectedTargetRoom.getName())
                .append("\n\n");
        text.append("DFS explores one corridor as far as possible before backtracking.\n");
        text.append("It returns one valid path, but not necessarily the cheapest one.\n\n");
        text.append("DFS path: ").append(formatRoomPath(path)).append("\n\n");
        text.append(formatSelectionLines());
        outputArea.setText(text.toString());
    }

    private void showDijkstraPath() {
        if (!requireStartAndTarget()) {
            return;
        }

        PathResult result = GraphAlgorithms.dijkstra(
                graph, selectedStartRoom, selectedTargetRoom);

        if (!result.isPathFound()) {
            algorithmHighlightedRooms.clear();
            algorithmHighlightedCorridors.clear();
            redrawGraph();
            outputArea.setText(
                    "Dijkstra found no path from "
                            + selectedStartRoom.getName()
                            + " to "
                            + selectedTargetRoom.getName()
                            + ".\n\n"
                            + formatSelectionLines());
            return;
        }

        List<Room> path = result.getPath();
        algorithmHighlightedRooms.clear();
        algorithmHighlightedRooms.addAll(path);
        algorithmHighlightedCorridors.clear();
        algorithmHighlightedCorridors.addAll(findCorridorsOnPath(path));
        redrawGraph();

        StringBuilder text = new StringBuilder();
        text.append("Dijkstra Path from ")
                .append(selectedStartRoom.getName())
                .append(" to ")
                .append(selectedTargetRoom.getName())
                .append("\n\n");
        text.append("Dijkstra repeatedly picks the unvisited room with the smallest known total cost.\n");
        text.append("With positive corridor costs, it finds the cheapest path between two rooms.\n\n");
        text.append("Cheapest path: ").append(formatRoomPath(path)).append("\n");
        text.append("Total cost: ").append(result.getTotalCost()).append("\n\n");
        text.append(formatSelectionLines());
        outputArea.setText(text.toString());
    }

    private void showPrimMst() {
        if (!requireStart()) {
            return;
        }

        MinimumSpanningTreeResult result = GraphAlgorithms.prim(graph, selectedStartRoom);

        algorithmHighlightedCorridors.clear();
        algorithmHighlightedCorridors.addAll(result.getCorridors());
        algorithmHighlightedRooms.clear();
        algorithmHighlightedRooms.addAll(findRoomsOnCorridors(algorithmHighlightedCorridors));
        redrawGraph();

        StringBuilder text = new StringBuilder();
        text.append("Prim MST starting from ").append(selectedStartRoom.getName()).append("\n\n");
        text.append("Prim builds a minimum spanning tree (MST).\n");
        text.append("It connects the whole graph as cheaply as possible and is not a start-to-target path.\n\n");

        if (result.isComplete()) {
            text.append("All rooms are connected: yes\n\n");
        } else {
            text.append("All rooms are connected: no\n\n");
        }

        text.append("Selected MST corridors:\n");
        for (Corridor corridor : result.getCorridors()) {
            text.append("- ").append(corridor).append("\n");
        }
        text.append("\nTotal cost: ").append(result.getTotalCost()).append("\n\n");
        text.append(formatSelectionLines());
        outputArea.setText(text.toString());
    }

    private void redrawGraph() {
        drawGraph(algorithmHighlightedRooms, algorithmHighlightedCorridors);
    }

    private void drawGraph(Set<Room> highlightedRooms, Set<Corridor> highlightedCorridors) {
        graphPane.getChildren().clear();

        for (Corridor corridor : graph.getAllCorridors()) {
            drawCorridorLine(corridor, highlightedCorridors.contains(corridor));
        }

        for (Room room : graph.getRooms()) {
            drawRoomCircle(room, highlightedRooms.contains(room));
        }

        for (Corridor corridor : graph.getAllCorridors()) {
            drawCorridorCostLabel(corridor, highlightedCorridors.contains(corridor));
        }

        for (Room room : graph.getRooms()) {
            drawRoomLabel(room);
        }
    }

    private void drawCorridorLine(Corridor corridor, boolean highlighted) {
        double[] posA = getPosition(corridor.getRoomA());
        double[] posB = getPosition(corridor.getRoomB());

        Line line = new Line(posA[0], posA[1], posB[0], posB[1]);
        line.setStroke(highlighted ? HIGHLIGHT_CORRIDOR_COLOR : DEFAULT_CORRIDOR_COLOR);
        line.setStrokeWidth(highlighted ? 4 : 2);
        if (interactionMode == InteractionMode.EDIT_COST) {
            line.setCursor(Cursor.HAND);
            line.setOnMouseClicked(event -> handleCorridorCostClick(corridor));
        }
        graphPane.getChildren().add(line);
    }

    private void drawCorridorCostLabel(Corridor corridor, boolean highlighted) {
        double[] posA = getPosition(corridor.getRoomA());
        double[] posB = getPosition(corridor.getRoomB());

        double midX = (posA[0] + posB[0]) / 2;
        double midY = (posA[1] + posB[1]) / 2;

        Text costLabel = new Text(String.valueOf(corridor.getCost()));
        costLabel.setFill(Color.BLACK);
        costLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        costLabel.setX(midX - 6);
        costLabel.setY(midY - 6);
        if (interactionMode == InteractionMode.EDIT_COST) {
            costLabel.setCursor(Cursor.HAND);
            costLabel.setOnMouseClicked(event -> handleCorridorCostClick(corridor));
        }
        graphPane.getChildren().add(costLabel);
    }

    private void drawRoomCircle(Room room, boolean algorithmHighlighted) {
        double[] position = getPosition(room);
        boolean isStart = room.equals(selectedStartRoom);
        boolean isTarget = room.equals(selectedTargetRoom);
        boolean isPending = room.equals(pendingFirstRoom);

        if (isStart) {
            Circle startRing = new Circle(position[0], position[1], ROOM_RADIUS + 6);
            startRing.setFill(Color.TRANSPARENT);
            startRing.setStroke(START_ROOM_BORDER);
            startRing.setStrokeWidth(3);
            addRoomClickHandler(room, startRing);
            graphPane.getChildren().add(startRing);
        }

        if (isTarget) {
            Circle targetRing = new Circle(position[0], position[1], ROOM_RADIUS + 6);
            targetRing.setFill(Color.TRANSPARENT);
            targetRing.setStroke(TARGET_ROOM_BORDER);
            targetRing.setStrokeWidth(3);
            addRoomClickHandler(room, targetRing);
            graphPane.getChildren().add(targetRing);
        }

        Circle circle = new Circle(position[0], position[1], ROOM_RADIUS);
        circle.setFill(getRoomFillColor(algorithmHighlighted, isStart, isTarget, isPending));
        circle.setStroke(getRoomBorderColor(algorithmHighlighted, isStart, isTarget, isPending));
        circle.setStrokeWidth(getRoomBorderWidth(algorithmHighlighted, isStart, isTarget, isPending));
        addRoomClickHandler(room, circle);
        graphPane.getChildren().add(circle);
    }

    private Color getRoomFillColor(boolean algorithmHighlighted, boolean isStart, boolean isTarget,
                                   boolean isPending) {
        if (algorithmHighlighted) {
            return HIGHLIGHT_ROOM_FILL;
        }
        if (isPending) {
            return PENDING_ROOM_FILL;
        }
        if (isStart) {
            return START_ROOM_FILL;
        }
        if (isTarget) {
            return TARGET_ROOM_FILL;
        }
        return DEFAULT_ROOM_FILL;
    }

    private Color getRoomBorderColor(boolean algorithmHighlighted, boolean isStart, boolean isTarget,
                                     boolean isPending) {
        if (isStart) {
            return START_ROOM_BORDER;
        }
        if (isTarget) {
            return TARGET_ROOM_BORDER;
        }
        if (isPending) {
            return Color.GOLD;
        }
        if (algorithmHighlighted) {
            return HIGHLIGHT_CORRIDOR_COLOR;
        }
        return Color.DARKBLUE;
    }

    private double getRoomBorderWidth(boolean algorithmHighlighted, boolean isStart, boolean isTarget,
                                      boolean isPending) {
        if (isStart || isTarget || isPending) {
            return 3;
        }
        if (algorithmHighlighted) {
            return 3;
        }
        return 2;
    }

    private void drawRoomLabel(Room room) {
        double[] position = getPosition(room);

        Text label = new Text(room.getName());
        label.setFill(Color.BLACK);
        centerTextBelowRoom(label, position[0], position[1] + ROOM_RADIUS + 14);
        addRoomClickHandler(room, label);
        graphPane.getChildren().add(label);
    }

    private void addRoomClickHandler(Room room, Node node) {
        node.setOnMouseClicked(event -> {
            event.consume();
            handleRoomClick(room);
        });
        node.setCursor(Cursor.HAND);
    }

    private void centerTextBelowRoom(Text text, double centerX, double y) {
        text.applyCss();
        double textWidth = text.getLayoutBounds().getWidth();
        text.setX(centerX - textWidth / 2);
        text.setY(y);
    }

    private double[] getPosition(Room room) {
        double[] position = roomPositions.get(room.getName());
        if (position == null) {
            double[] defaultPosition = {GRAPH_WIDTH / 2.0, GRAPH_HEIGHT / 2.0};
            roomPositions.put(room.getName(), defaultPosition);
            return defaultPosition;
        }
        return position;
    }

    private Set<Corridor> findCorridorsOnPath(List<Room> path) {
        Set<Corridor> corridors = new HashSet<>();
        for (int i = 0; i < path.size() - 1; i++) {
            Corridor corridor = findCorridorBetween(path.get(i), path.get(i + 1));
            if (corridor != null) {
                corridors.add(corridor);
            }
        }
        return corridors;
    }

    private Corridor findCorridorBetween(Room roomA, Room roomB) {
        for (Corridor corridor : graph.getCorridorsFrom(roomA)) {
            if (corridor.getOtherRoom(roomA).equals(roomB)) {
                return corridor;
            }
        }
        return null;
    }

    private Set<Room> findRoomsOnCorridors(Set<Corridor> corridors) {
        Set<Room> rooms = new HashSet<>();
        for (Corridor corridor : corridors) {
            rooms.add(corridor.getRoomA());
            rooms.add(corridor.getRoomB());
        }
        return rooms;
    }

    private String formatRoomPath(List<Room> path) {
        List<String> names = new ArrayList<>();
        for (Room room : path) {
            names.add(room.getName());
        }
        return String.join(" -> ", names);
    }
}
