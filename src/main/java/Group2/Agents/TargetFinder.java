package Group2.Agents;

import Group2.Map.Graph;
import Group2.Map.GridMap;
import Group2.Map.Node;
import Group2.Map.PathFinding;
import Interop.Action.IntruderAction;
import Interop.Action.Move;
import Interop.Action.NoAction;
import Interop.Action.Rotate;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Direction;
import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Percepts;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

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

    private int turnsBeforeTeleporting = 1000;

    int in=1;
    @Override
    public IntruderAction getAction(IntruderPercepts percepts) {
        in++;
        IntruderAction action = null;
        //System.out.println("View Angle in degrees: " + percepts.getTargetDirection().getDegrees());
        Angle maxRotationAngle = percepts.getScenarioIntruderPercepts().getScenarioPercepts().getMaxRotationAngle();

        //if(percepts.getAreaPercepts().isJustTeleported()) goalCoord = null;


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

                action = new Move(u);
                this.currentMap.updateMap(action, percepts);
                //System.out.println(currentMap);
                return action;
            }

            if(selfPoint1 != null && selfPoint2 == null) {
                selfPoint2 = this.currentMap.getCurrentPosition();
                if (toRight){
                    goalStepAngle = 180 - (360 - percepts.getTargetDirection().getDegrees());

                } else if (toLeft) {
                    goalStepAngle = 180 - percepts.getTargetDirection().getDegrees();
                }

                //calc point coords with respect to the origin
                double goalCornerAngle = 180 - goalInitAngle - goalStepAngle;
                double stepLength = u.getValue();
                double stepOverGoal = (Math.sin(goalStepAngle) / Math.sin(goalCornerAngle));
                double distOriginToGoal = stepLength * (Math.abs(Math.sin(Math.toRadians(goalStepAngle))) / Math.abs(Math.sin(Math.toRadians(goalCornerAngle))) );
                if (toRight) {
                    goalCoord = new Point((distOriginToGoal * (Math.cos(Math.toRadians(goalInitAngle)))), (-1)*(distOriginToGoal * (Math.sin(Math.toRadians(goalInitAngle)))));
                } else if (toLeft) {
                    goalCoord = new Point((distOriginToGoal * Math.cos(Math.toRadians(goalInitAngle))), (distOriginToGoal * Math.sin(Math.toRadians(goalInitAngle))) );
                }
                this.currentMap.setTargetPosition(goalCoord);
                if(goalCoord.getX() <= 0) {
                    while(this.currentMap.getTargetPosition().getX() < 0)
                        this.currentMap.extendMapToLeft(50);
                }
                if(goalCoord.getY() <= 0) {
                    while(this.currentMap.getTargetPosition().getY() < 0)
                        this.currentMap.extendMapToBottom(50);
                }
            }
        }


        Point sourcePos = this.currentMap.getCurrentPosition();
        Point targetPos = this.currentMap.getTargetPosition();
        Distance maxDistance = percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder();


        //Recompute the path every 'counter' turns in case the intruder discovered new obstacles
        if (counter == 0) {
            computeAStarPath(sourcePos, targetPos);
            counter = 5;
        }


        //Point of the path that the agent will aiming to
        Point subTarget;
        int subPathSize = 5;

        //Follow the path by aiming at every subSizePath points
        if (path.size() < subPathSize) subTarget = new Point(targetPos.getX(), targetPos.getY());
        else subTarget = new Point(path.get(subPathSize-1).getPos().getX(), path.get(subPathSize-1).getPos().getY());

        //System.out.println(path);

        //The intruder has been standing in the computed target area location for 10 turns
        //This means that the target area is on another level and we need a teleport in order to reach it
//        if(turnsBeforeTeleporting <= 0) {
//            return findTeleport(percepts);
//        }

        double deltaX = subTarget.getX() - sourcePos.getX();
        double deltaY = subTarget.getY() - sourcePos.getY();

        //Angle in the map from the agent to the subtarget
        double angle = Math.atan2(deltaY, deltaX);
        if(angle < 0) angle += 2 * Math.PI;
        Angle targetAngle = Angle.fromRadians(angle);

        //Agent's direction angle in the map
        Angle agentDirection = this.currentMap.getCurrentAngle();


        //Distance angle between subtarget and agent's direction (i.e. angle the agent needs to rotate from to reach the point)
        Angle rotationAngle = Angle.fromDegrees((targetAngle.getDegrees() - agentDirection.getDegrees())%360);
        while(rotationAngle.getDegrees() < -180) rotationAngle = Angle.fromDegrees(rotationAngle.getDegrees()+360);



        //Angle of rotation is very small, move forward
        if (Math.abs(rotationAngle.getDegrees()) < 3) {
            action = new Move(maxDistance);
        }
        //Angle of rotation is bigger than the maximum angle, rotate from the largest angle possible
        else if (rotationAngle.getDegrees() > maxRotationAngle.getDegrees()) {
            action = new Rotate(maxRotationAngle);
        }
        //Angle of rotation is smaller than -maximum angle, rotate from the smallest angle possible
        else if (rotationAngle.getDegrees() < -maxRotationAngle.getDegrees()) {
            action = new Rotate(Angle.fromDegrees(-maxRotationAngle.getDegrees()));
        }
        //Rotate from the rotation angle
        else {
            action = new Rotate(rotationAngle);
        }

        counter--;

        this.currentMap.updateMap(action, percepts);

        //System.out.println(currentMap);
        //System.out.println("number of INTRUDER actions"+" "+ in);

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
        }
        else {
            System.out.println("WARNING: Target not found");
            path = null;
        }
    }


    /**
     * Method that checks whether the agents sees a guard
     * @param percepts the intruder's percepts
     * @return true if there is a guard in the vision field, false otherwise
     */
    public boolean isGuard(Percepts percepts) {
        for(ObjectPercept objectPercept: percepts.getVision().getObjects().getAll()) {
            if(objectPercept.getType()== ObjectPerceptType.Guard) return true;
        }
        return false;
    }


    /**
     * Method that makes the intruder explore the map if it does not know where the teleport is sets it as the target
     * if it finds it. This is called if the target area needs a teleport in order to be reached
     * @param percepts, the intruder's percepts
     * @return No Action if the intruder knows where the teleport is, a random exploration action otherwise
     */
    public IntruderAction findTeleport(IntruderPercepts percepts) {

        //Check if the intruder knows where the teleport is
        ObjectPerceptType[][] gridMap = this.currentMap.getCurrentMap();
        for(int i=0; i<gridMap.length; i++) {
            for(int j=0; j<gridMap[0].length; j++) {
                if(gridMap[i][j] == ObjectPerceptType.Teleport) {

                    //Intruder knows location of teleport, set this as the new target
                    this.currentMap.setTargetPosition(new Point(j, i));
                    turnsBeforeTeleporting = 1000;
                    return new NoAction();
                }
            }
        }

        //The intruder does not know where the teleport is, explore the map randomly to find it
        Morontruder randomIntruder = new Morontruder();
        return randomIntruder.getAction(percepts);

    }


}
