package at.ac.fhcampuswien.alda.dungeon;

import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final DungeonGraph graph;
    private final Scanner scanner;

    public ConsoleMenu(DungeonGraph graph) {
        this.graph = graph;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showDungeon();
                case "2" -> checkReachabilityWithDfs();
                case "3" -> showDfsPath();
                case "4" -> findCheapestPathWithDijkstra();
                case "5" -> buildMinimumSpanningTreeWithPrim();
                case "6" -> explainDataStructureAndAlgorithms();
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter a number from 0 to 6.");
            }

            if (running) {
                System.out.println();
            }
        }
    }

    private void printMenu() {
        System.out.println("=== Dungeon Path Planner ===");
        System.out.println("1. Show dungeon rooms and corridors");
        System.out.println("2. DFS Reachability (Check if a room is reachable");
        System.out.println("3. DFS Path (show one possible path)");
        System.out.println("4. Dijkstra Path (find cheapest path)" );
        System.out.println("5. Prim MST (build cheapest full network)");
        System.out.println("6. Explain data structure and algorithms");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void showDungeon() {
        graph.printDungeon();
    }

    private void checkReachabilityWithDfs() {
        Room start = readRoom("Enter start room name: ");
        if (start == null) {
            return;
        }

        Room target = readRoom("Enter target room name: ");
        if (target == null) {
            return;
        }

        boolean reachable = GraphAlgorithms.depthFirstSearch(graph, start, target);
        if (reachable) {
            System.out.println("Yes, " + target.getName() + " is reachable from " + start.getName() + " using DFS.");
        } else {
            System.out.println("No, " + target.getName() + " is not reachable from " + start.getName() + " using DFS.");
        }
    }

    private void showDfsPath() {
        Room start = readRoom("Enter start room name: ");
        if (start == null) {
            return;
        }

        Room target = readRoom("Enter target room name: ");
        if (target == null) {
            return;
        }

        List<Room> path = GraphAlgorithms.findPathWithDfs(graph, start, target);
        if (path.isEmpty()) {
            System.out.println("No path found from " + start.getName() + " to " + target.getName() + ".");
        } else {
            System.out.print("DFS path: ");
            for (int i = 0; i < path.size(); i++) {
                System.out.print(path.get(i).getName());
                if (i < path.size() - 1) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();
        }
    }

    private void findCheapestPathWithDijkstra() {
        Room start = readRoom("Enter start room name: ");
        if (start == null) {
            return;
        }

        Room target = readRoom("Enter target room name: ");
        if (target == null) {
            return;
        }

        PathResult result = GraphAlgorithms.dijkstra(graph, start, target);
        System.out.println(result);
    }

    private void buildMinimumSpanningTreeWithPrim() {
        Room start = readRoom("Enter starting room for Prim's algorithm: ");
        if (start == null) {
            return;
        }

        MinimumSpanningTreeResult result = GraphAlgorithms.prim(graph, start);
        System.out.println(result);
    }

    private void explainDataStructureAndAlgorithms() {
        System.out.println("""
                === Beginner-Friendly Explanation ===

                Console and JavaFX use the same DungeonGraph and GraphAlgorithms classes.
                Only the user interface is different (typing room names vs. clicking rooms).

                What is a graph in this application?
                The dungeon is modeled as a graph. Each room is a node, and each corridor
                is an edge with a positive cost (distance, danger, or construction cost).

                What is an adjacency list?
                DungeonGraph stores, for every room, a list of corridors connected to that room.
                This makes it easy to visit all neighbors of a room during graph algorithms.

                What does DFS do?
                Depth-First Search explores as far as possible along one corridor before backtracking.
                It uses recursion and a visited set. DFS can find whether a target room is reachable
                and can return one valid path, but it does not look at corridor costs, so the path is
                not guaranteed to be the cheapest one.

                What does Dijkstra do?
                Dijkstra repeatedly picks the unvisited room with the smallest known total cost from
                the start room, then updates neighbor costs. With positive edge costs, this finds the
                cheapest path between two selected rooms.

                What does Prim do?
                Prim builds a minimum spanning tree: the cheapest set of corridors that connects all
                rooms without cycles. Unlike Dijkstra, Prim does not find a path between two specific
                rooms. Instead, it finds the cheapest way to connect the whole dungeon network.
                """);
    }

    private Room readRoom(String prompt) {
        System.out.print(prompt);
        String name = scanner.nextLine().trim();
        Room room = graph.getRoomByName(name);

        if (room == null) {
            System.out.println("Room not found: " + name);
            System.out.println("Available rooms:");
            for (Room availableRoom : graph.getRooms()) {
                System.out.println("- " + availableRoom.getName());
            }
        }

        return room;
    }
}
