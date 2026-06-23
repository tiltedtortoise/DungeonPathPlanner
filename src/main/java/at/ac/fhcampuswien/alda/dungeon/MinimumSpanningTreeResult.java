package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimumSpanningTreeResult {

    private final List<Corridor> corridors;
    private final int totalCost;
    private final boolean complete;

    public MinimumSpanningTreeResult(List<Corridor> corridors, int totalCost, boolean complete) {
        this.corridors = new ArrayList<>(corridors);
        this.totalCost = totalCost;
        this.complete = complete;
    }

    public List<Corridor> getCorridors() {
        return Collections.unmodifiableList(corridors);
    }

    public int getTotalCost() {
        return totalCost;
    }

    public boolean isComplete() {
        return complete;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (complete) {
            builder.append("All rooms are connected.").append(System.lineSeparator());
        } else {
            builder.append("Warning: The graph is not fully connected, so not all rooms could be reached.")
                    .append(System.lineSeparator());
        }

        builder.append("Minimum spanning tree corridors:").append(System.lineSeparator());

        if (corridors.isEmpty()) {
            builder.append("(none)");
        } else {
            for (Corridor corridor : corridors) {
                builder.append("- ").append(corridor).append(System.lineSeparator());
            }
        }

        builder.append("Total cost: ").append(totalCost);
        return builder.toString();
    }
}
