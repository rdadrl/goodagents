package Group2.Map;

import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.ArrayList;

public class Node implements Comparable{

    private final ObjectPerceptType object;
    private ArrayList<Node> neighbours = new ArrayList<>();
    private Point pos;
    private double fScore;

    public Node(ObjectPerceptType object, Point pos){
        this.pos = pos;
        this.object = object;
        this.fScore = Double.MAX_VALUE;
    }

    public ObjectPerceptType getObject() {
        return object;
    }

    public ArrayList<Node> getNeighbours() {
        return neighbours;
    }


    public void addNeighbour(Node neighbour) {
        this.neighbours.add(neighbour);
        neighbour.neighbours.add(this);
    }

    public Point getPos() {
        return pos;
    }

    public void setfScore(double score) {
        this.fScore = score;
    }

    public double getfScore() {
        return fScore;
    }

    @Override
    public String toString() {
        return "Node{" +
                "object=" + object +
                "point=" +pos +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        if(this.fScore < ((Node) o).getfScore()) return -1;
        else if(this.fScore > ((Node) o).getfScore()) return 1;
        return 0;
    }
}
