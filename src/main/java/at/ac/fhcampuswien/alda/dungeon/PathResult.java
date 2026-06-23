package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathResult {

    private final List<Room> path;
    private final int totalCost;
    private final boolean pathFound;

    public PathResult(List<Room> path, int totalCost, boolean pathFound) {
        this.path = new ArrayList<>(path);
        this.totalCost = totalCost;
        this.pathFound = pathFound;
    }

    public List<Room> getPath() {
        return Collections.unmodifiableList(path);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public boolean isPathFound() {
        return pathFound;
    }

    @Override
    public String toString() {
        if (!pathFound) {
            return "No path found.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Path: ");
        for (int i = 0; i < path.size(); i++) {
            builder.append(path.get(i).getName());
            if (i < path.size() - 1) {
                builder.append(" -> ");
            }
        }
        builder.append(System.lineSeparator());
        builder.append("Total cost: ").append(totalCost);
        return builder.toString();
    }
}
