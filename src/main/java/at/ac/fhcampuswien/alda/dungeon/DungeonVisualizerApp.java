package at.ac.fhcampuswien.alda.dungeon;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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
import java.util.Set;

public class DungeonVisualizerApp extends Application {

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

    private DungeonGraph graph;
    private Pane graphPane;
    private TextArea outputArea;
    private Map<String, double[]> roomPositions;

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

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(8);

        Button resetButton = new Button("Reset");
        Button clearSelectionButton = new Button("Clear Selection");
        Button dfsReachabilityButton = new Button("DFS Reachability");
        Button dfsPathButton = new Button("DFS Path");
        Button dijkstraPathButton = new Button("Dijkstra Path");
        Button primMstButton = new Button("Prim MST");

        resetButton.setOnAction(event -> resetView());
        clearSelectionButton.setOnAction(event -> clearSelection());
        dfsReachabilityButton.setOnAction(event -> showDfsReachability());
        dfsPathButton.setOnAction(event -> showDfsPath());
        dijkstraPathButton.setOnAction(event -> showDijkstraPath());
        primMstButton.setOnAction(event -> showPrimMst());

        HBox buttonBar = new HBox(10, resetButton, clearSelectionButton, dfsReachabilityButton,
                dfsPathButton, dijkstraPathButton, primMstButton);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBox = new VBox(5, outputArea);
        bottomBox.setPadding(new Insets(0, 10, 10, 10));

        BorderPane root = new BorderPane();
        root.setTop(buttonBar);
        root.setCenter(graphPane);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, GRAPH_WIDTH, GRAPH_HEIGHT + 200);
        stage.setTitle("Dungeon Path Planner - Visualization");
        stage.setScene(scene);
        stage.show();

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
        updateSelectionText();
        redrawGraph();
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

    private void updateSelectionText() {
        outputArea.setText(
                "This app uses a demo dungeon with multiple rooms and corridors.\n"
                        + "Click on room circles to select start and target.\n"
                        + "First click = start, second click = target.\n"
                        + "If both are already selected, the next click starts a new selection.\n\n"
                        + formatSelectionLines()
        );
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

    private void handleRoomClick(Room clickedRoom) {
        if (selectedStartRoom != null && selectedTargetRoom != null) {
            selectedStartRoom = clickedRoom;
            selectedTargetRoom = null;
        } else if (selectedStartRoom == null) {
            selectedStartRoom = clickedRoom;
        } else {
            selectedTargetRoom = clickedRoom;
        }

        updateSelectionText();
        redrawGraph();
    }

    private void resetView() {
        algorithmHighlightedRooms.clear();
        algorithmHighlightedCorridors.clear();
        redrawGraph();
        outputArea.setText(
                "Reset view.\n\n"
                        + "This app uses a demo dungeon with multiple rooms and corridors.\n"
                        + "Algorithm highlights were cleared. Selected start and target rooms are kept.\n"
                        + "Click rooms to choose start and target, then run an algorithm.\n\n"
                        + formatSelectionLines()
        );
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
        graphPane.getChildren().add(costLabel);
    }

    private void drawRoomCircle(Room room, boolean algorithmHighlighted) {
        double[] position = getPosition(room);
        boolean isStart = room.equals(selectedStartRoom);
        boolean isTarget = room.equals(selectedTargetRoom);

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
        circle.setFill(getRoomFillColor(algorithmHighlighted, isStart, isTarget));
        circle.setStroke(getRoomBorderColor(algorithmHighlighted, isStart, isTarget));
        circle.setStrokeWidth(getRoomBorderWidth(algorithmHighlighted, isStart, isTarget));
        addRoomClickHandler(room, circle);
        graphPane.getChildren().add(circle);
    }

    private Color getRoomFillColor(boolean algorithmHighlighted, boolean isStart, boolean isTarget) {
        if (algorithmHighlighted) {
            return HIGHLIGHT_ROOM_FILL;
        }
        if (isStart) {
            return START_ROOM_FILL;
        }
        if (isTarget) {
            return TARGET_ROOM_FILL;
        }
        return DEFAULT_ROOM_FILL;
    }

    private Color getRoomBorderColor(boolean algorithmHighlighted, boolean isStart, boolean isTarget) {
        if (isStart) {
            return START_ROOM_BORDER;
        }
        if (isTarget) {
            return TARGET_ROOM_BORDER;
        }
        if (algorithmHighlighted) {
            return HIGHLIGHT_CORRIDOR_COLOR;
        }
        return Color.DARKBLUE;
    }

    private double getRoomBorderWidth(boolean algorithmHighlighted, boolean isStart, boolean isTarget) {
        if (isStart || isTarget) {
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
        node.setOnMouseClicked(event -> handleRoomClick(room));
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
            return new double[]{0, 0};
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
