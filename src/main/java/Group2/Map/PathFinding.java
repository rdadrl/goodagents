package Group2.Map;

import Interop.Geometry.Distance;

import java.util.*;

public class PathFinding {

    private Graph graph;
    private List<List<Node>> allPaths = new ArrayList<>();
    private HashSet<Node> nodeSet = new HashSet<>();

    public PathFinding(Graph graph) {
        this.graph = graph;
    }


    public List<Node> shortestPathDijkstra(Node source, Node target) {

        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        HashSet<Node> nodes = new HashSet<>(graph.getNodes());

        for (Node node : graph.getNodes()) {
            dist.put(node, Double.MAX_VALUE);
            previous.put(node, null);
        }

        dist.put(source, 0.0);

        while (!nodes.isEmpty()) {
            //Node current = (Node) nodes.stream().min(Comparator.comparingDouble(dist).getKey());
            double min = Double.MAX_VALUE;
            Node current = null;
            for(Node n: nodes){
                if(dist.containsKey(n)) {
                    if(dist.get(n)<min) {
                        min = dist.get(n);
                        current = n;
                    }
                }
            }
            nodes.remove(current);

            for (Node neighbour : current.getNeighbours()) {
                Edge edge = graph.getEdge(current, neighbour);
                double alt = dist.get(current) + edge.getWeight();
                if (alt < dist.get(neighbour)) {
                    dist.put(neighbour, alt);
                    previous.put(neighbour, current);
                }
            }
        }

        LinkedList<Node> path = new LinkedList<>();
        if (previous.get(target) != null || target.equals(source)) {
            while (target != null) {
                path.addFirst(target);
                target = previous.get(target);
            }
        }
        return path;
    }


    //Find all possible paths from source to target
    public void createPaths(Node source, Node target, List<Node> currentPath) {

        this.nodeSet.add(source);

        if(source.equals(target)) {

            List<Node> newPath = new ArrayList<>();
            newPath.addAll(currentPath);
            allPaths.add(newPath);
            System.out.println(currentPath);

            //Backtrack
            this.nodeSet.remove(source);
            return;
        }

        for(Object o: source.getNeighbours()) {
            Node x = (Node) o;

            if(!this.nodeSet.contains(x)) {
                currentPath.add(x);
                createPaths(x, target, currentPath);

                //Backtrack
                currentPath.remove(x);
            }
        }
        //Backtrack
        this.nodeSet.remove(source);
    }


    public List<Node> aStar(Node source, Node target) {

        // The set of discovered nodes that may need to be (re-)expanded.
        // Initially, only the start node is known.
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        openSet.add(source);

        // For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start
        // to n currently known
        HashMap<Node, Node> cameFrom = new HashMap<>();

        // For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
        HashMap<Node, Double> gScore = new HashMap<>();
        gScore.put(source, 0.0);

        // For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
        // how short a path from start to finish can be if it goes through n.
        HashMap<Node, Double> fScore = new HashMap<>();
        double score = new Distance(source.getPos(), target.getPos()).getValue();
        source.setfScore(score);
        fScore.put(source, score);



        while(!openSet.isEmpty()) {
            //The node in openSet having the lowest fScore[] value
            Node current = openSet.peek();

            if(current.equals(target)) {
                return constructPath(cameFrom, current);
            }

            openSet.remove(current);
            for(Node neighbour: current.getNeighbours()) {
                //Distance from start to the neighbor through current
                double tentative_gScore = gScore.getOrDefault(current, Double.MAX_VALUE) + this.graph.getEdge(current, neighbour).getWeight();
                if (tentative_gScore < gScore.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    // This path to neighbor is better than any previous one
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentative_gScore);
                    score = gScore.get(neighbour) + new Distance(neighbour.getPos(), target.getPos()).getValue();
                    neighbour.setfScore(score);
                    fScore.put(neighbour, score);
                    if(!openSet.contains(neighbour))
                        openSet.add(neighbour);
                }
            }
        }


        // Open set is empty but goal was never reached
        System.out.println("GOAL IS NOT REACHED");
        return null;
    }

    public List<Node> constructPath(HashMap<Node, Node> cameFrom, Node current) {
        List<Node> path = new LinkedList<>();
        path.add(current);
        while(cameFrom.keySet().contains(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        return path;
    }

    public List<List<Node>> getAllPaths() {
        return allPaths;
    }
}
