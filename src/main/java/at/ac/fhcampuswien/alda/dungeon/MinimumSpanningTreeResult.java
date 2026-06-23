package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimumSpanningTreeResult {

    private final List<Corridor> corridors;
    private final int totalCost;

    public MinimumSpanningTreeResult(List<Corridor> corridors, int totalCost) {
        this.corridors = new ArrayList<>(corridors);
        this.totalCost = totalCost;
    }

    public List<Corridor> getCorridors() {
        return Collections.unmodifiableList(corridors);
    }

    public int getTotalCost() {
        return totalCost;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
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
