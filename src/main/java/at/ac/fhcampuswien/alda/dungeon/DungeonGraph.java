package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// unser graph als adjazenzliste: jeder raum kennt seine korridore zu nachbarn
// der dungeon ist ungerichtet und gewichtet (jeder korridor hat positive kosten)
public class DungeonGraph {

    // schnelles finden von räumen per name (z.b. "Entrance")
    private final Map<String, Room> roomsByName;
    // adjazenzliste: für jeden raum eine liste aller angrenzenden korridore
    private final Map<Room, List<Corridor>> adjacencyList;
    // alle korridore einmal gespeichert (praktisch zum ausgeben und für prim/dfs schleifen)
    private final List<Corridor> allCorridors;

    public DungeonGraph() {
        this.roomsByName = new LinkedHashMap<>();
        this.adjacencyList = new HashMap<>();
        this.allCorridors = new ArrayList<>();
    }

    public void addRoom(String name) {
        // kein doppelter raumname erlaubt
        if (roomsByName.containsKey(name)) {
            throw new IllegalArgumentException("Room already exists: " + name);
        }
        Room room = new Room(name);
        roomsByName.put(name, room);
        // neuer raum startet mit leerer nachbarliste
        adjacencyList.put(room, new ArrayList<>());
    }

    public void addCorridor(String roomAName, String roomBName, int cost) {
        // dijkstra/prim brauchen positive gewichte
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

        // graph ist ungerichtet -> gleicher korridor darf nicht zweimal existieren (auch nicht A-B vs B-A)
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
        // bei ungerichtetem graph: korridor in beide adjazenzlisten eintragen
        adjacencyList.get(roomA).add(corridor);
        adjacencyList.get(roomB).add(corridor);
    }

    public Room getRoomByName(String name) {
        return roomsByName.get(name);
    }

    public List<Room> getRooms() {
        // kopie zurückgeben damit niemand von außen unsere interne liste verändert
        return Collections.unmodifiableList(new ArrayList<>(roomsByName.values()));
    }

    // wichtig für DFS/Dijkstra/Prim: alle nachbarn eines raums über seine korridore finden
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

    public static boolean isProtectedRoom(String name) {
        return "Entrance".equals(name) || "Exit".equals(name);
    }

    public void removeRoom(String name) {
        if (isProtectedRoom(name)) {
            throw new IllegalArgumentException("Cannot delete protected room: " + name);
        }

        Room room = getRoomByName(name);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + name);
        }

        List<Corridor> corridorsToRemove = new ArrayList<>(adjacencyList.get(room));
        for (Corridor corridor : corridorsToRemove) {
            removeCorridorInternal(corridor);
        }

        roomsByName.remove(name);
        adjacencyList.remove(room);
    }

    public void removeCorridor(String roomAName, String roomBName) {
        Room roomA = getRoomByName(roomAName);
        Room roomB = getRoomByName(roomBName);

        if (roomA == null) {
            throw new IllegalArgumentException("Room not found: " + roomAName);
        }
        if (roomB == null) {
            throw new IllegalArgumentException("Room not found: " + roomBName);
        }

        Corridor corridor = findCorridorBetween(roomA, roomB);
        if (corridor == null) {
            throw new IllegalArgumentException(
                    "Corridor not found between " + roomAName + " and " + roomBName);
        }

        removeCorridorInternal(corridor);
    }

    public void setCorridorCost(String roomAName, String roomBName, int cost) {
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

        Corridor corridor = findCorridorBetween(roomA, roomB);
        if (corridor == null) {
            throw new IllegalArgumentException(
                    "Corridor not found between " + roomAName + " and " + roomBName);
        }

        corridor.setCost(cost);
    }

    private Corridor findCorridorBetween(Room roomA, Room roomB) {
        for (Corridor corridor : getCorridorsFrom(roomA)) {
            if (corridor.getOtherRoom(roomA).equals(roomB)) {
                return corridor;
            }
        }
        return null;
    }

    private void removeCorridorInternal(Corridor corridor) {
        allCorridors.remove(corridor);
        adjacencyList.get(corridor.getRoomA()).remove(corridor);
        adjacencyList.get(corridor.getRoomB()).remove(corridor);
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
