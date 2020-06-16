package Group2.Agents;

import Group2.Map.Graph;
import Group2.Map.GridMap;
import Group2.Map.Node;
import Group2.Map.PathFinding;
import Interop.Action.IntruderAction;
import Interop.Action.Move;
import Interop.Action.Rotate;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Direction;
import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.IntruderPercepts;

import java.util.LinkedList;

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
        //System.out.println("View Angle in degrees: " + percepts.getTargetDirection().getDegrees());
        Angle maxRotationAngle = percepts.getScenarioIntruderPercepts().getScenarioPercepts().getMaxRotationAngle();



        if(goalCoord == null) {

            if (selfPoint1 == null && selfPoint2 == null) {
                //System.out.println("Seeking goal location...");
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
                System.out.println(currentMap);
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
                //System.out.println("Goal coordinates are: " + goalCoord);
                this.currentMap.setTargetPosition(goalCoord);
                if(goalCoord.getX() <= 0) this.currentMap.extendMapToLeft(50);
                if(goalCoord.getY() <= 0) this.currentMap.extendMapToBottom(50);
            }
        }


        Point sourcePos = this.currentMap.getCurrentPosition();
        Point targetPos = this.currentMap.getTargetPosition();
        Distance maxDistance = percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder();


        //Recompute the path every 'counter' turns in case the intruder discovered new obstacles
        if (counter == 0) {
            computeAStarPath(sourcePos, targetPos);
            counter = 2;
        }


        //Point of the path that the agent will aiming to
        Point subTarget;
        int subPathSize = 2;
        //System.out.println(this.path);

        //Follow the path by aiming at every subSizePath points
        if (path.size() < subPathSize) subTarget = new Point(targetPos.getX(), targetPos.getY());
        else subTarget = new Point(path.get(subPathSize-1).getPos().getX(), path.get(subPathSize-1).getPos().getY());

        //Compute the angle from which the agent has to rotate in order to aim to the target point
        double deltaX = subTarget.getX() - sourcePos.getX();
        double deltaY = subTarget.getY() - sourcePos.getY();
        //Direction dir = Direction.fromRadians(Math.atan(deltaY / deltaX));
        Angle dir = Angle.fromRadians(Math.atan2(deltaY, deltaX));
        //System.out.println("Dir: " +dir.getDegrees());
        Angle rotationAngle = dir.getDistance(this.currentMap.getCurrentAngle());
        if(deltaX > 0) rotationAngle = Angle.fromDegrees(-rotationAngle.getDegrees());

        //System.out.println("Rotation angle: " +rotationAngle.getDegrees());


        //Angle of rotation is very small, move forward
        if (Math.abs(rotationAngle.getDegrees()) < 3) {
            //System.out.println("Move forward");
            action = new Move(maxDistance);
        }
        //Angle of rotation is bigger than the maximum angle, rotate from the largest angle possible
        else if (rotationAngle.getDegrees() > maxRotationAngle.getDegrees()) {
            //System.out.println("Rotate from max angle");
            action = new Rotate(maxRotationAngle);
        }
        //Angle of rotation is smaller than -maximum angle, rotate from the smallest angle possible
        else if (rotationAngle.getDegrees() < -maxRotationAngle.getDegrees()) {
            //System.out.println("Rotate from -max angle");
            action = new Rotate(Angle.fromDegrees(-maxRotationAngle.getDegrees()));
        }
        //Rotate from the rotation angle
        else {
            //System.out.println("Rotate from dir");
            action = new Rotate(rotationAngle);
        }

        counter--;

        this.currentMap.updateMap(action, percepts);
        System.out.println(currentMap);
        return action;
    }



    public void computeDijkstraPath(Point sourcePos, Point targetPos) {
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


    public void computeAStarPath(Point sourcePos, Point targetPos) {
        Graph graph = new Graph(this.currentMap.getCurrentMap());
        PathFinding finder = new PathFinding(graph);
        Node source = graph.getNode(sourcePos);
        Node target = graph.getNode(targetPos);
        if (target != null) {
            path = (LinkedList) finder.aStar(source, target);
            //System.out.println("Path cost: " +graph.getPathWeight(path));
        }
        else {
            System.out.println("WARNING: Target not found");
            path = null;
        }
    }


}
