package at.ac.fhcampuswien.alda.dungeon;

public class Corridor {

    private final Room roomA;
    private final Room roomB;
    private int cost;

    public Corridor(Room roomA, Room roomB, int cost) {
        this.roomA = roomA;
        this.roomB = roomB;
        this.cost = cost;
    }

    public Room getRoomA() {
        return roomA;
    }

    public Room getRoomB() {
        return roomB;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public Room getOtherRoom(Room room) {
        if (room.equals(roomA)) {
            return roomB;
        }
        if (room.equals(roomB)) {
            return roomA;
        }
        throw new IllegalArgumentException("Room " + room + " is not connected by this corridor.");
    }

    @Override
    public String toString() {
        return roomA + " <-> " + roomB + " (cost " + cost + ")";
    }
}