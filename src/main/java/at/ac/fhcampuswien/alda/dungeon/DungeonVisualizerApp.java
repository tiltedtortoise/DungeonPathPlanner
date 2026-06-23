package at.ac.fhcampuswien.alda.dungeon;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    private static final double GRAPH_WIDTH = 860;
    private static final double GRAPH_HEIGHT = 480;

    private static final Color DEFAULT_CORRIDOR_COLOR = Color.GRAY;
    private static final Color HIGHLIGHT_CORRIDOR_COLOR = Color.ORANGERED;
    private static final Color DEFAULT_ROOM_FILL = Color.LIGHTBLUE;
    private static final Color HIGHLIGHT_ROOM_FILL = Color.LIGHTGREEN;

    private DungeonGraph graph;
    private Pane graphPane;
    private TextArea outputArea;
    private Map<String, double[]> roomPositions;

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
        outputArea.setText("Click Reset to show the full dungeon graph.");

        Button resetButton = new Button("Reset");
        Button dfsButton = new Button("Show DFS path Entrance to Exit");
        Button dijkstraButton = new Button("Show Dijkstra path Entrance to Exit");
        Button primButton = new Button("Show Prim MST from Entrance");

        resetButton.setOnAction(event -> resetView());
        dfsButton.setOnAction(event -> showDfsPath());
        dijkstraButton.setOnAction(event -> showDijkstraPath());
        primButton.setOnAction(event -> showPrimMst());

        HBox buttonBar = new HBox(10, resetButton, dfsButton, dijkstraButton, primButton);
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
        positions.put("Entrance", new double[]{80, 220});
        positions.put("Guard Room", new double[]{230, 150});
        positions.put("Armory", new double[]{390, 80});
        positions.put("Library", new double[]{230, 320});
        positions.put("Prison Cell", new double[]{420, 240});
        positions.put("Treasure Room", new double[]{420, 390});
        positions.put("Boss Room", new double[]{620, 180});
        positions.put("Exit", new double[]{760, 260});
        return positions;
    }

    private void resetView() {
        drawGraph(new HashSet<>(), new HashSet<>());
        outputArea.setText(
                "Reset view.\n\n"
                        + "All rooms and corridors are shown in the default style.\n"
                        + "Use the buttons above to run DFS, Dijkstra, or Prim on the demo dungeon."
        );
    }

    private void showDfsPath() {
        Room entrance = graph.getRoomByName("Entrance");
        Room exit = graph.getRoomByName("Exit");

        List<Room> path = GraphAlgorithms.findPathWithDfs(graph, entrance, exit);

        if (path.isEmpty()) {
            drawGraph(new HashSet<>(), new HashSet<>());
            outputArea.setText("DFS found no path from Entrance to Exit.");
            return;
        }

        Set<Room> highlightedRooms = new HashSet<>(path);
        Set<Corridor> highlightedCorridors = findCorridorsOnPath(path);

        drawGraph(highlightedRooms, highlightedCorridors);

        StringBuilder text = new StringBuilder();
        text.append("DFS (Depth-First Search) from Entrance to Exit\n\n");
        text.append("DFS explores one corridor as far as possible before backtracking.\n");
        text.append("It finds any valid path and does NOT look at corridor costs.\n");
        text.append("The highlighted path is therefore not guaranteed to be the cheapest one.\n\n");
        text.append("DFS path: ").append(formatRoomPath(path));
        outputArea.setText(text.toString());
    }

    private void showDijkstraPath() {
        Room entrance = graph.getRoomByName("Entrance");
        Room exit = graph.getRoomByName("Exit");

        PathResult result = GraphAlgorithms.dijkstra(graph, entrance, exit);

        if (!result.isPathFound()) {
            drawGraph(new HashSet<>(), new HashSet<>());
            outputArea.setText("Dijkstra found no path from Entrance to Exit.");
            return;
        }

        List<Room> path = result.getPath();
        Set<Room> highlightedRooms = new HashSet<>(path);
        Set<Corridor> highlightedCorridors = findCorridorsOnPath(path);

        drawGraph(highlightedRooms, highlightedCorridors);

        StringBuilder text = new StringBuilder();
        text.append("Dijkstra from Entrance to Exit\n\n");
        text.append("Dijkstra repeatedly picks the unvisited room with the smallest known total cost.\n");
        text.append("With positive corridor costs, it finds the cheapest path between two rooms.\n\n");
        text.append("Cheapest path: ").append(formatRoomPath(path)).append("\n");
        text.append("Total cost: ").append(result.getTotalCost());
        outputArea.setText(text.toString());
    }

    private void showPrimMst() {
        Room entrance = graph.getRoomByName("Entrance");

        MinimumSpanningTreeResult result = GraphAlgorithms.prim(graph, entrance);

        Set<Corridor> highlightedCorridors = new HashSet<>(result.getCorridors());
        Set<Room> highlightedRooms = findRoomsOnCorridors(highlightedCorridors);

        drawGraph(highlightedRooms, highlightedCorridors);

        StringBuilder text = new StringBuilder();
        text.append("Prim's algorithm starting from Entrance\n\n");
        text.append("Prim builds a minimum spanning tree (MST).\n");
        text.append("It connects all rooms with the cheapest total corridor cost, without cycles.\n");
        text.append("Unlike Dijkstra, Prim does not find one path from start to target.\n\n");

        if (result.isComplete()) {
            text.append("All rooms are connected.\n\n");
        } else {
            text.append("Warning: The graph is not fully connected.\n\n");
        }

        text.append("Selected MST corridors:\n");
        for (Corridor corridor : result.getCorridors()) {
            text.append("- ").append(corridor).append("\n");
        }
        text.append("\nTotal cost: ").append(result.getTotalCost());
        outputArea.setText(text.toString());
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
        costLabel.setFill(highlighted ? HIGHLIGHT_CORRIDOR_COLOR : Color.DARKGRAY);
        costLabel.setX(midX - 6);
        costLabel.setY(midY - 6);
        graphPane.getChildren().add(costLabel);
    }

    private void drawRoomCircle(Room room, boolean highlighted) {
        double[] position = getPosition(room);

        Circle circle = new Circle(position[0], position[1], ROOM_RADIUS);
        circle.setFill(highlighted ? HIGHLIGHT_ROOM_FILL : DEFAULT_ROOM_FILL);
        circle.setStroke(highlighted ? HIGHLIGHT_CORRIDOR_COLOR : Color.DARKBLUE);
        circle.setStrokeWidth(highlighted ? 3 : 2);
        graphPane.getChildren().add(circle);
    }

    private void drawRoomLabel(Room room) {
        double[] position = getPosition(room);

        Text label = new Text(room.getName());
        label.setFill(Color.BLACK);
        centerTextBelowRoom(label, position[0], position[1] + ROOM_RADIUS + 14);
        graphPane.getChildren().add(label);
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
