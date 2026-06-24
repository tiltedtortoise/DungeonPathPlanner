package at.ac.fhcampuswien.alda.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// statische hilfsklasse für unsere graphenalgorithmen (DFS, Dijkstra, Prim)
// wir instanziieren die klasse nicht, sondern rufen die methoden direkt auf
public class GraphAlgorithms {

    private GraphAlgorithms() {
    }

    // prüft nur ob das ziel vom start aus erreichbar ist (ja/nein)
    // liefert keinen pfad und schaut nicht auf korridor-kosten
    public static boolean depthFirstSearch(DungeonGraph graph, Room start, Room target) {
        // visited merkt sich welche räume wir schon besucht haben
        // sonst würden wir bei zyklen endlos im kreis laufen
        Set<Room> visited = new HashSet<>();
        return dfsRecursive(graph, start, target, visited);
    }

    // rekursive DFS-hilfsmethode
    private static boolean dfsRecursive(DungeonGraph graph, Room current, Room target, Set<Room> visited) {
        // ziel gefunden -> fertig
        if (current.equals(target)) {
            return true;
        }

        // aktuellen raum als besucht markieren
        visited.add(current);

        // alle nachbarn über die adjazenzliste anschauen
        for (Corridor corridor : graph.getCorridorsFrom(current)) {
            Room neighbor = corridor.getOtherRoom(current);

            // nur in unbesuchte räume weitergehen
            if (!visited.contains(neighbor)) {
                // rekursiv weitersuchen; wenn irgendein zweig das ziel findet, true zurückgeben
                if (dfsRecursive(graph, neighbor, target, visited)) {
                    return true;
                }
            }
        }

        // von diesem raum aus kein weg zum ziel gefunden
        return false;
    }

    // DFS die einen konkreten pfad als liste von räumen zurückgibt
    // auch hier werden korridor-kosten ignoriert
    public static List<Room> findPathWithDfs(DungeonGraph graph, Room start, Room target) {
        List<Room> path = new ArrayList<>();
        Set<Room> visited = new HashSet<>();

        if (dfsPathRecursive(graph, start, target, visited, path)) {
            return path;
        }

        // leere liste = kein pfad gefunden
        return new ArrayList<>();
    }

    // DFS mit backtracking für die pfad-liste
    private static boolean dfsPathRecursive(DungeonGraph graph, Room current, Room target,
                                            Set<Room> visited, List<Room> path) {
        // aktuellen raum zum pfad hinzufügen (wir probieren diesen weg gerade aus)
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

        // backtracking: dieser raum führt nicht zum ziel, also wieder aus dem pfad entfernen
        path.remove(path.size() - 1);
        return false;
    }

    // findet den günstigsten pfad von start zu target (positive korridor-kosten vorausgesetzt)
    // einfache version ohne PriorityQueue: nächsten raum per linearer suche auswählen
    public static PathResult dijkstra(DungeonGraph graph, Room start, Room target) {
        // distances = bisher bekannte gesamtkosten vom start zu jedem raum
        Map<Room, Integer> distances = new HashMap<>();
        // previous = von welchem raum wir gekommen sind (für pfad-rekonstruktion)
        Map<Room, Room> previous = new HashMap<>();
        // visited = räume deren kürzeste distanz schon feststeht
        Set<Room> visited = new HashSet<>();

        // am anfang ist jeder raum "unendlich weit" entfernt
        for (Room room : graph.getRooms()) {
            distances.put(room, Integer.MAX_VALUE);
        }
        // start hat kosten 0
        distances.put(start, 0);

        // hauptschleife: so lange es noch unbesuchte räume gibt
        while (visited.size() < graph.getRooms().size()) {
            // unbesuchten raum mit kleinster bekannter distanz wählen
            Room current = selectClosestUnvisitedRoom(graph, distances, visited);

            // kein erreichbarer raum mehr übrig -> abbrechen
            if (current == null || distances.get(current) == Integer.MAX_VALUE) {
                break;
            }

            visited.add(current);

            // sobald wir beim ziel sind können wir aufhören (pfad ist optimal)
            if (current.equals(target)) {
                break;
            }

            // nachbarn relaxieren (= ggf. günstigere distanz merken)
            for (Corridor corridor : graph.getCorridorsFrom(current)) {
                Room neighbor = corridor.getOtherRoom(current);

                // bereits fertig bearbeitete räume überspringen
                if (visited.contains(neighbor)) {
                    continue;
                }

                // neuer weg über current zum nachbarn
                int newDistance = distances.get(current) + corridor.getCost();

                // wenn der neue weg günstiger ist, distanz und vorgänger aktualisieren
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previous.put(neighbor, current);
                }
            }
        }

        // ziel nie erreicht (z.b. graph nicht zusammenhängend)
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

    // sucht den unbesuchten raum mit der kleinsten distanz (einfache schleife statt PriorityQueue)
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

    // baut den pfad rückwärts vom ziel zum start über die previous-map zusammen
    private static List<Room> reconstructPath(Map<Room, Room> previous, Room start, Room target) {
        List<Room> path = new ArrayList<>();
        Room current = target;

        while (current != null) {
            path.add(0, current); // vorne einfügen damit start -> target rauskommt
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }

        return path;
    }

    // Prim: baut einen minimum spanning tree (MST)
    // verbindet möglichst viele räume mit minimalen gesamtkosten, ohne kreise
    // startet bei einem beliebigen raum (hier: vom user gewählter start)
    public static MinimumSpanningTreeResult prim(DungeonGraph graph, Room start) {
        // visited = räume die schon im MST sind
        Set<Room> visited = new HashSet<>();
        List<Corridor> chosenCorridors = new ArrayList<>();
        int totalCost = 0;

        visited.add(start);

        // solange noch räume fehlen, günstigste kante nach außen suchen
        while (visited.size() < graph.getRooms().size()) {
            Corridor cheapestCorridor = null;
            Room roomToAdd = null;

            // alle korridore von bereits verbundenen räumen durchgehen
            for (Room visitedRoom : visited) {
                for (Corridor corridor : graph.getCorridorsFrom(visitedRoom)) {
                    Room otherRoom = corridor.getOtherRoom(visitedRoom);

                    // nur kanten zu räumen die noch nicht im MST sind
                    if (!visited.contains(otherRoom)) {
                        if (cheapestCorridor == null || corridor.getCost() < cheapestCorridor.getCost()) {
                            cheapestCorridor = corridor;
                            roomToAdd = otherRoom;
                        }
                    }
                }
            }

            // keine passende kante mehr -> graph ist an dieser stelle nicht vollständig verbunden
            if (cheapestCorridor == null) {
                break;
            }

            chosenCorridors.add(cheapestCorridor);
            totalCost += cheapestCorridor.getCost();
            visited.add(roomToAdd);
        }

        // complete = true wenn wirklich alle räume verbunden wurden
        boolean complete = visited.size() == graph.getRooms().size();
        return new MinimumSpanningTreeResult(chosenCorridors, totalCost, complete);
    }
}
