package Group2.Agents;

import Group2.Map.Graph;
import Group2.Map.GridMap;
import Group2.Map.Node;
import Group2.Map.PathFinding;
import Group2.MapBumf;
import Interop.Action.IntruderAction;
import Interop.Action.Move;
import Interop.Action.Rotate;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Direction;
import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TargetFinder implements Intruder {
    Direction a = null;
    Direction b = null;
    Direction c = null;

    Distance u = null;


    private GridMap currentMap;
    private Point targetLocation;
    private int ID;
    private LinkedList<Node> path;

    public TargetFinder(int id) {
        this.currentMap = new GridMap();
        this.ID = id;
    }

    private Point goalCoord = null;
    private Point selfPoint1 = null;
    private Point selfPoint2 = null;
    private double goalInitAngle = 0;
    private double goalStepAngle = 0;
    private boolean toLeft = false;
    private boolean toRight = false;
    private boolean inFront = false;
    private boolean inBack = false;
    private int counter;


    @Override
    public IntruderAction getAction(IntruderPercepts percepts) {
        IntruderAction action = null;
        System.out.println("View Angle in degrees: " + percepts.getTargetDirection().getDegrees());
        Angle maxRotationAngle = percepts.getScenarioIntruderPercepts().getScenarioPercepts().getMaxRotationAngle();




        if(goalCoord == null) {

            if (selfPoint1 == null && selfPoint2 == null) {
                System.out.println("Seeking goal location...");
                u = percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder();
                selfPoint1 = this.currentMap.getCurrentPosition();
                goalInitAngle = percepts.getTargetDirection().getDegrees();
                if (goalInitAngle < 90){
                    toLeft = true;
                    inFront = true;
                } else if (goalInitAngle >= 90 && goalInitAngle < 180){
                    toLeft = true;
                    inBack = true;
                } else if (goalInitAngle >= 180 && goalInitAngle < 270){
                    toRight = true;
                    inBack = true;
                    goalInitAngle = 360 - goalInitAngle;
                } else if (goalInitAngle >= 270 && goalInitAngle < 360) {
                    toRight = true;
                    inFront = true;
                    goalInitAngle = 360 - goalInitAngle;
                }
                //System.out.println("u = " + u.getValue());
                //System.out.println("selfPoint1 = " + selfPoint1);
                //System.out.println("goalInitAngle = " + goalInitAngle);
                //System.out.println("toLeft = " + toLeft);
                //take step u
                action = new Move(u);
                this.currentMap.updateMap(action, percepts);
                return action;
            }

            if(selfPoint1 != null && selfPoint2 == null) {
                selfPoint2 = this.currentMap.getCurrentPosition();
                //System.out.println("selfPoint2 = " + selfPoint2);
                if (toRight){
                    goalStepAngle = 180 - (360 - percepts.getTargetDirection().getDegrees());
                    //System.out.println("StepAngle = " + goalStepAngle);

                } else if (toLeft) {
                    goalStepAngle = 180 - percepts.getTargetDirection().getDegrees();
                    //System.out.println("StepAngle = " + goalStepAngle);
                }

                //calc point coords with respect to the origin
                double goalCornerAngle = 180 - goalInitAngle - goalStepAngle;
                //System.out.println("goalCornerAngle = " + goalCornerAngle);
                double stepLength = u.getValue();
                //System.out.println("Steplength aka u: " + stepLength);
                double stepOverGoal = (Math.sin(goalStepAngle) / Math.sin(goalCornerAngle));
                //System.out.println("StepOverGoal: " + stepOverGoal);
                //System.out.println("times steplength: " +  1.4 * stepOverGoal );
                double distOriginToGoal = stepLength * (Math.abs(Math.sin(Math.toRadians(goalStepAngle))) / Math.abs(Math.sin(Math.toRadians(goalCornerAngle))) );
                //System.out.println("disttoGoal: " + distOriginToGoal);
                if (toRight && inFront) {
                    goalCoord = new Point( (distOriginToGoal * (Math.cos(Math.toRadians(goalInitAngle)))), Math.abs((distOriginToGoal * (Math.sin(Math.toRadians(goalInitAngle))))) );
                } else if (toRight && inBack) {
                    goalCoord = new Point( (distOriginToGoal * (Math.cos(Math.toRadians(goalInitAngle)))), (-1)*(distOriginToGoal * (Math.sin(Math.toRadians(goalInitAngle)))) );
                } else if (toLeft && inBack) {
                    goalCoord = new Point( (-1)*(distOriginToGoal * Math.cos(Math.toRadians(goalInitAngle))), (-1)*(distOriginToGoal * Math.sin(Math.toRadians(goalInitAngle))) );
                } else if (toLeft && inFront) {
                    goalCoord = new Point( (-1)*(distOriginToGoal * Math.cos(Math.toRadians(goalInitAngle))), Math.abs((distOriginToGoal * Math.sin(Math.toRadians(goalInitAngle)))) );
                }
                System.out.println("Goal coordinates are: " + goalCoord);
                this.currentMap.setTargetPosition(goalCoord);
                if(goalCoord.getX() <= 0) this.currentMap.extendMapToLeft(50);
                if(goalCoord.getY() <= 0) this.currentMap.extendMapToBottom(50);
            }
        }


        Point sourcePos = this.currentMap.getCurrentPosition();
        Point targetPos = this.currentMap.getTargetPosition();
        Distance maxDistance = percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder();


        //Recompute the path every 10 turns in case the intruder discovered new obstacles
        if (counter == 0) {
            computePath(sourcePos, targetPos);
            counter = 10;
        }


        Point subTarget;
        int subPathSize = 1;
        System.out.println(this.path);

        //Follow the path by aiming at every 10 points
        if (path.size() < subPathSize) subTarget = new Point(targetPos.getX(), targetPos.getY());
        else subTarget = new Point(path.get(subPathSize).getPos().getX(), path.get(subPathSize).getPos().getY());
        double deltaX = Math.abs(sourcePos.getX() - subTarget.getX());
        double deltaY = Math.abs(sourcePos.getY() - subTarget.getY());
        Direction dir = Direction.fromRadians(Math.atan(deltaY / deltaX));
        System.out.println("Dir: " +dir.getDegrees());
        Angle rotationAngle = dir.getDistance(this.currentMap.getCurrentAngle());
        System.out.println("Rotation angle: " +rotationAngle.getDegrees());
        if(sourcePos.getX() > subTarget.getX()) rotationAngle = Angle.fromDegrees(-rotationAngle.getDegrees());


        if (rotationAngle.getDegrees() < 5) {
            System.out.println("Move forward");
            action = new Move(maxDistance);
        }
        else if (rotationAngle.getDegrees() > maxRotationAngle.getDegrees()) {
            System.out.println("Rotate from max angle");
            System.out.println("Rot angle: " +rotationAngle.getDegrees());
            action = new Rotate(maxRotationAngle);
        }
        else if (rotationAngle.getDegrees() < -maxRotationAngle.getDegrees()) {
            System.out.println("Rotate from max angle");
            System.out.println("Rot angle: " +rotationAngle.getDegrees());
            action = new Rotate(Angle.fromDegrees(-maxRotationAngle.getDegrees()));
        }
        else {
            System.out.println("Rotate from dir");
            System.out.println("Rot angle: " +rotationAngle.getDegrees());
            action = new Rotate(rotationAngle);
        }

        counter--;

        this.currentMap.updateMap(action, percepts);
        return action;
    }



    public void computePath(Point sourcePos, Point targetPos) {
        Graph graph = new Graph(this.currentMap.getCurrentMap());
        PathFinding finder = new PathFinding(graph);
        Node source = graph.getNode(sourcePos);
        Node target = graph.getNode(targetPos);
        if (target != null)
            path = (LinkedList) finder.shortestPathDijkstra(source, target);
        else {
            System.out.println("WARNING: Target not found");
            path = null;
        }
    }


}
