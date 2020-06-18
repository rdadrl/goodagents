package Group2.Map;

import Interop.Geometry.Point;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Graph {

    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();

    private final double WALL_WEIGHT = 100;
    private final double DOOR_WINDOW_WEIGHT = 2;
    private final double DEFAULT_WEIGHT = 1;

    public Graph() {}


    public Graph(ObjectPerceptType[][] matrixMap) {

        for(int i=0; i<matrixMap.length; i++) {
            for(int j=0; j<matrixMap[0].length;j++) {
                Node newNode = new Node(matrixMap[i][j], new Point(j,i));
                this.addNode(newNode);
            }
        }

        //All nodes that are walls, or close to walls
        HashSet<Node> wallAdjacent = new HashSet<>();

        for(int i=0; i<this.nodes.size(); i++) {
            Node current = this.nodes.get(i);

            //Bottom node
            if(i > matrixMap[0].length) {
                Node neighbour = this.nodes.get(i - matrixMap[0].length);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

            //Top node
            if(i < (matrixMap.length -1)*matrixMap[0].length) {
                Node neighbour = this.nodes.get(i + matrixMap[0].length);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall  || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

            //Left node
            if(i % matrixMap[0].length != 0) {
                Node neighbour = this.nodes.get(i - 1);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

            //Right node
            if(i % matrixMap[0].length != matrixMap[0].length -1 ) {
                Node neighbour = this.nodes.get(i + 1);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

            //Bottom left node
            if(i > matrixMap[0].length && i % matrixMap[0].length != 0) {
                Node neighbour = this.nodes.get(i - matrixMap[0].length - 1);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

            //Bottom right node
            if(i > matrixMap[0].length && i % matrixMap[0].length != matrixMap[0].length -1) {
                Node neighbour = this.nodes.get(i - matrixMap[0].length + 1);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

            //Top left node
            if(i < (matrixMap.length -1)*matrixMap[0].length && i % matrixMap[0].length != 0) {
                Node neighbour = this.nodes.get(i + matrixMap[0].length - 1);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current,neighbour, weight);
            }

            //Top right node
            if(i < (matrixMap.length -1)*matrixMap[0].length && i % matrixMap[0].length != matrixMap[0].length -1) {
                Node neighbour = this.nodes.get(i + matrixMap[0].length + 1);
                double weight = DEFAULT_WEIGHT;
                if(neighbour.getObject() == ObjectPerceptType.Wall || current.getObject() == ObjectPerceptType.Wall) {
                    weight = WALL_WEIGHT;
                    wallAdjacent.addAll(current.getNeighbours());
                    wallAdjacent.addAll(neighbour.getNeighbours());
                }
                else if(neighbour.getObject()== ObjectPerceptType.Door || neighbour.getObject()==ObjectPerceptType.Window
                        ||current.getObject() == ObjectPerceptType.Door || current.getObject() == ObjectPerceptType.Window)
                    weight = DOOR_WINDOW_WEIGHT;
                this.addEdge(current, neighbour, weight);
            }

        }

        //Set the weights of nodes close to walls higher in order for the agent to avoid them
        for(Node node: wallAdjacent) {
            for(Node neighbour: node.getNeighbours()){
                this.getEdge(node, neighbour).setWeight(WALL_WEIGHT);
            }
        }
    }


    public void addEdge(Node x, Node y, double weight) {
        //Check if the edge does not already exist
        if(!x.getNeighbours().contains(y)) {
            x.addNeighbour(y);
            edges.add(new Edge(x, y, weight));
        }
    }

    public void addNode(Node x) {
        nodes.add(x);
    }

    public double getPathWeight(List<Node> path) {
        double value = 0;
        for(int i=0; i<path.size()-1; i++) {
            value += this.getEdge(path.get(i), path.get(i+1)).getWeight();
        }
        return value;
    }


    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public Edge getEdge(Node x, Node y) {
        for (int i=0; i<edges.size(); i++) {
            if(edges.get(i).getSource().equals(x) && edges.get(i).getTarget().equals(y) ||
                    edges.get(i).getSource().equals(y) && edges.get(i).getTarget().equals(x))
                return edges.get(i);
        }
        return null;
    }

    public Node getNode(Point point) {
        for(Node node: this.getNodes()) {
            if(node.getPos().getX() == Math.round(point.getX()) && node.getPos().getY() == Math.round(point.getY())) return node;
        }
        System.out.println("WARNING: Node not found");
        return null;
    }


}
