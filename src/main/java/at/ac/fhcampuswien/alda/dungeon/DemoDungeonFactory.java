package at.ac.fhcampuswien.alda.dungeon;

public class DemoDungeonFactory {

    private DemoDungeonFactory() {
    }

    public static DungeonGraph createDemoDungeon() {
        DungeonGraph graph = new DungeonGraph();

        graph.addRoom("Entrance");
        graph.addRoom("Guard Room");
        graph.addRoom("Armory");
        graph.addRoom("Library");
        graph.addRoom("Prison Cell");
        graph.addRoom("Treasure Room");
        graph.addRoom("Boss Room");
        graph.addRoom("Exit");

        graph.addCorridor("Entrance", "Guard Room", 4);
        graph.addCorridor("Entrance", "Library", 6);
        graph.addCorridor("Guard Room", "Armory", 2);
        graph.addCorridor("Guard Room", "Prison Cell", 5);
        graph.addCorridor("Library", "Treasure Room", 3);
        graph.addCorridor("Library", "Prison Cell", 4);
        graph.addCorridor("Armory", "Boss Room", 8);
        graph.addCorridor("Prison Cell", "Boss Room", 4);
        graph.addCorridor("Treasure Room", "Exit", 5);
        graph.addCorridor("Boss Room", "Exit", 2);
        graph.addCorridor("Prison Cell", "Exit", 9);

        return graph;
    }
}
