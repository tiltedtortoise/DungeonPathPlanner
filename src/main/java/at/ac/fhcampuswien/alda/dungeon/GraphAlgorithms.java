package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphAlgorithms {

    private GraphAlgorithms() {
    }

    public static boolean depthFirstSearch(DungeonGraph graph, Room start, Room target) {
        Set<Room> visited = new HashSet<>();
        return dfsRecursive(graph, start, target, visited);
    }

    private static boolean dfsRecursive(DungeonGraph graph, Room current, Room target, Set<Room> visited) {
        if (current.equals(target)) {
            return true;
        }

        visited.add(current);

        for (Corridor corridor : graph.getCorridorsFrom(current)) {
            Room neighbor = corridor.getOtherRoom(current);
            if (!visited.contains(neighbor)) {
                if (dfsRecursive(graph, neighbor, target, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static List<Room> findPathWithDfs(DungeonGraph graph, Room start, Room target) {
        List<Room> path = new ArrayList<>();
        Set<Room> visited = new HashSet<>();
        if (dfsPathRecursive(graph, start, target, visited, path)) {
            return path;
        }
        return new ArrayList<>();
    }

    private static boolean dfsPathRecursive(DungeonGraph graph, Room current, Room target,
                                            Set<Room> visited, List<Room> path) {
        path.add(current);

        if (current.equals(target)) {
            return true;
        }

        visited.add(current);

        for (Corridor corridor : graph.getCorridorsFrom(current)) {
            Room neighbor = corridor.getOtherRoom(current);
            if (!visited.contains(neighbor)) {
                if (dfsPathRecursive(graph, neighbor, target, visited, path)) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1);
        return false;
    }

    public static PathResult dijkstra(DungeonGraph graph, Room start, Room target) {
        Map<Room, Integer> distances = new HashMap<>();
        Map<Room, Room> previous = new HashMap<>();
        Set<Room> visited = new HashSet<>();

        for (Room room : graph.getRooms()) {
            distances.put(room, Integer.MAX_VALUE);
        }
        distances.put(start, 0);

        while (visited.size() < graph.getRooms().size()) {
            Room current = selectClosestUnvisitedRoom(graph, distances, visited);

            if (current == null || distances.get(current) == Integer.MAX_VALUE) {
                break;
            }

            visited.add(current);

            if (current.equals(target)) {
                break;
            }

            for (Corridor corridor : graph.getCorridorsFrom(current)) {
                Room neighbor = corridor.getOtherRoom(current);
                if (visited.contains(neighbor)) {
                    continue;
                }

                int newDistance = distances.get(current) + corridor.getCost();
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                }
            }
        }

        if (!previous.containsKey(target) && !start.equals(target)) {
            return new PathResult(new ArrayList<>(), 0, false);
        }

        List<Room> path = reconstructPath(previous, start, target);
        int totalCost = distances.get(target);

        if (totalCost == Integer.MAX_VALUE) {
            return new PathResult(new ArrayList<>(), 0, false);
        }

        return new PathResult(path, totalCost, true);
    }

    private static Room selectClosestUnvisitedRoom(DungeonGraph graph, Map<Room, Integer> distances,
                                                   Set<Room> visited) {
        Room closest = null;
        int smallestDistance = Integer.MAX_VALUE;

        for (Room room : graph.getRooms()) {
            if (!visited.contains(room) && distances.get(room) < smallestDistance) {
                smallestDistance = distances.get(room);
                closest = room;
            }
        }

        return closest;
    }

    private static List<Room> reconstructPath(Map<Room, Room> previous, Room start, Room target) {
        List<Room> path = new ArrayList<>();
        Room current = target;

        while (current != null) {
            path.add(0, current);
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }

        return path;
    }

    public static MinimumSpanningTreeResult prim(DungeonGraph graph, Room start) {
        Set<Room> visited = new HashSet<>();
        List<Corridor> chosenCorridors = new ArrayList<>();
        int totalCost = 0;

        visited.add(start);

        while (visited.size() < graph.getRooms().size()) {
            Corridor cheapestCorridor = null;
            Room roomToAdd = null;

            for (Room visitedRoom : visited) {
                for (Corridor corridor : graph.getCorridorsFrom(visitedRoom)) {
                    Room otherRoom = corridor.getOtherRoom(visitedRoom);
                    if (!visited.contains(otherRoom)) {
                        if (cheapestCorridor == null || corridor.getCost() < cheapestCorridor.getCost()) {
                            cheapestCorridor = corridor;
                            roomToAdd = otherRoom;
                        }
                    }
                }
            }

            if (cheapestCorridor == null) {
                break;
            }

            chosenCorridors.add(cheapestCorridor);
            totalCost += cheapestCorridor.getCost();
            visited.add(roomToAdd);
        }

        boolean complete = visited.size() == graph.getRooms().size();
        return new MinimumSpanningTreeResult(chosenCorridors, totalCost, complete);
    }
}
