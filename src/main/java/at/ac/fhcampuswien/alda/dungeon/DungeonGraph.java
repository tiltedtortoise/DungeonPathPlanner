package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DungeonGraph {

    private final Map<String, Room> roomsByName;
    private final Map<Room, List<Corridor>> adjacencyList;
    private final List<Corridor> allCorridors;

    public DungeonGraph() {
        this.roomsByName = new LinkedHashMap<>();
        this.adjacencyList = new HashMap<>();
        this.allCorridors = new ArrayList<>();
    }

    public void addRoom(String name) {
        if (roomsByName.containsKey(name)) {
            throw new IllegalArgumentException("Room already exists: " + name);
        }
        Room room = new Room(name);
        roomsByName.put(name, room);
        adjacencyList.put(room, new ArrayList<>());
    }

    public void addCorridor(String roomAName, String roomBName, int cost) {
        if (cost <= 0) {
            throw new IllegalArgumentException("Corridor cost must be positive: " + cost);
        }

        Room roomA = getRoomByName(roomAName);
        Room roomB = getRoomByName(roomBName);

        if (roomA == null) {
            throw new IllegalArgumentException("Room not found: " + roomAName);
        }
        if (roomB == null) {
            throw new IllegalArgumentException("Room not found: " + roomBName);
        }
        if (roomA.equals(roomB)) {
            throw new IllegalArgumentException("A corridor cannot connect a room to itself.");
        }

        for (Corridor existing : allCorridors) {
            Room existingA = existing.getRoomA();
            Room existingB = existing.getRoomB();
            boolean samePair = (existingA.equals(roomA) && existingB.equals(roomB))
                    || (existingA.equals(roomB) && existingB.equals(roomA));
            if (samePair) {
                throw new IllegalArgumentException(
                        "Corridor already exists between " + roomAName + " and " + roomBName);
            }
        }

        Corridor corridor = new Corridor(roomA, roomB, cost);
        allCorridors.add(corridor);
        adjacencyList.get(roomA).add(corridor);
        adjacencyList.get(roomB).add(corridor);
    }

    public Room getRoomByName(String name) {
        return roomsByName.get(name);
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(new ArrayList<>(roomsByName.values()));
    }

    public List<Corridor> getCorridorsFrom(Room room) {
        List<Corridor> corridors = adjacencyList.get(room);
        if (corridors == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(corridors));
    }

    public List<Corridor> getAllCorridors() {
        return Collections.unmodifiableList(new ArrayList<>(allCorridors));
    }

    public void printDungeon() {
        System.out.println("=== Dungeon Rooms ===");
        for (Room room : getRooms()) {
            System.out.println("- " + room.getName());
        }

        System.out.println();
        System.out.println("=== Dungeon Corridors ===");
        for (Corridor corridor : getAllCorridors()) {
            System.out.println("- " + corridor);
        }
    }
}
