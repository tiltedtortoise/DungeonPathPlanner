package at.ac.fhcampuswien.alda.dungeon;

public class Main {

    public static void main(String[] args) {
        DungeonGraph graph = DemoDungeonFactory.createDemoDungeon();
        ConsoleMenu menu = new ConsoleMenu(graph);
        menu.start();
    }
}
