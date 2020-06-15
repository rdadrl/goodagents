package Group2.Map;

import Group9.math.Vector2;
import Group9.tree.PointContainer;
import Interop.Action.Action;
import Interop.Action.Move;
import Interop.Action.Rotate;
import Interop.Action.Sprint;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Percepts;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.Arrays;
import java.util.Set;

public class GridMap {

    private Point currentPosition;
    private Point currentMapTopRight;
    private Angle currentAngle;
    private Point targetPosition;

    private ObjectPerceptType[][] currentMap; //Cell will be null if it hasn't been discovered

    public GridMap() {
        this.currentMap = new ObjectPerceptType[100][100];
        this.currentPosition = new Point(0,0);
        this.currentMapTopRight = new Point(100,100);
        this.currentAngle = Angle.fromDegrees(0);
        this.targetPosition = new Point(0,0);
    }

    /**
     * Method that keeps track of the current map discovered by the agent
     * @param action, the action the agent just took
     * @param percepts, the set of percepts of the agent
     */
    public void updateMap(Action action, Percepts percepts) {

        //Length from which we extend the map in case the agent goes outside
        int shiftLength = 50;

        //Reset the map if the agent just got teleported
        if(percepts.getAreaPercepts().isJustTeleported()) resetParameters();

        //Update the direction angle
        if(action instanceof Rotate) {
            //System.out.println("Current angle: " +currentAngle.getDegrees());
            currentAngle = Angle.fromDegrees((currentAngle.getDegrees() + ((Rotate) action).getAngle().getDegrees())%360);
            while(currentAngle.getDegrees() < 0) currentAngle = Angle.fromDegrees(currentAngle.getDegrees() + 360);
            //System.out.println("New angle: " +currentAngle.getDegrees());
        }

        //Update the agent's position on the map
        if(action instanceof Move || action instanceof Sprint) {
            Distance distance;
            if (action instanceof Move) distance = ((Move) action).getDistance();
            else distance = ((Sprint) action).getDistance();

            if(isValidMove(distance.getValue(), (IntruderPercepts) percepts)) {


                //Change the sign of y to keep the y-axis pointing downwards
                Point changeInPosition = new Point(Math.cos(currentAngle.getRadians()) * distance.getValue(), Math.sin(currentAngle.getRadians()) * distance.getValue());
                Point newPosition = new Point(currentPosition.getX() + changeInPosition.getX(), currentPosition.getY() + changeInPosition.getY());

                currentPosition = newPosition;

                //Increase the size of the current map if the agent is outside
                if (newPosition.getX() <= 0) extendMapToLeft(shiftLength);
                else if (newPosition.getX() >= currentMapTopRight.getX()) extendMapToRight(shiftLength);

                if (newPosition.getY() <= 0) extendMapToBottom(shiftLength);
                else if (newPosition.getY() >= currentMapTopRight.getY()) extendMapToTop(shiftLength);

            }

            else {
                System.out.println("MOVE NOT VALID");
            }


        }


        //Add all the points in the range of view of the agent to the map
        for(ObjectPercept objectPercept: percepts.getVision().getObjects().getAll()) {

            //Object point in the agent's cartesian system (agent is at (0,0))
            Point objectPoint = new Point(objectPercept.getPoint().getX(), objectPercept.getPoint().getY());


            double distanceToObject = new Distance(objectPoint, new Point(0,0)).getValue();
            //double deltaX = Math.abs(objectPoint.getX());
            //Angle between object point and agent's direction
            //Angle objectAngle = Angle.fromRadians(Math.asin(deltaX/distanceToObject));
            //if(objectPoint.getX() > 0) objectAngle = Angle.fromDegrees(-objectAngle.getDegrees());
            Angle objectAngle = Angle.fromRadians(Math.atan2(objectPoint.getY(), objectPoint.getX()) - Math.PI/2);


            //Angle of the object point in the map's coordinate system
            Angle angleInMap = Angle.fromDegrees(currentAngle.getDegrees() + objectAngle.getDegrees());

            //Coordinates of the object point in the map's coordinate system
            int objectXInMap = (int) Math.round(currentPosition.getX() + Math.cos(angleInMap.getRadians())*distanceToObject);
            int objectYInMap = (int) Math.round(currentPosition.getY() + Math.sin(angleInMap.getRadians())*distanceToObject);


            //Extend the map if the observed point is outside
            if(objectXInMap <= 0) {
                extendMapToLeft(shiftLength);
                objectXInMap += shiftLength;
            }
            else if(objectXInMap >= currentMapTopRight.getX()) extendMapToRight(shiftLength);
            if(objectYInMap <= 0) {
               extendMapToBottom(shiftLength);
               objectYInMap += shiftLength;
            }
            else if(objectYInMap >= currentMapTopRight.getY()) extendMapToTop(shiftLength);


            //Add the observed object to the map (except if it is a guard as they are not static)
            //Only add object to the map if it hasn't been added before
            if(objectPercept.getType() != ObjectPerceptType.Guard && currentMap[objectYInMap][objectXInMap] == null) {
                currentMap[objectYInMap][objectXInMap] = objectPercept.getType();
            }
            //System.out.println("Add in Map at [" + objectXInMap +", " + objectYInMap +"]: " +objectPercept.getType());


            //Set all points between the object percept point and the agent to empty spaces
            for(int i = 1; i < (int) distanceToObject; i++) {

                //Coordinates of the point in the agent's coordinate system (agent at (0,0))
                double x = Math.cos(angleInMap.getRadians()) * i;
                double y = Math.sin(angleInMap.getRadians()) * i;

                int xInMap = (int) Math.round(currentPosition.getX() + x);
                int yInMap = (int) Math.round(currentPosition.getY() + y);
                //System.out.println("Add in Map at [" + xInMap +", " + yInMap +"] EmptySpace");

                if(currentMap[yInMap][xInMap] == null) currentMap[yInMap][xInMap] = ObjectPerceptType.EmptySpace;
            }

        }

    }


    /**
     * Method that increases the size of the current map that the agent is keeping track of and updates the agent's position accordingly
     * @param x = -1 if the map needs to be extended on the left, = 1 if it is on the right
     * @param y = -1 if the map needs to be extended on the top, = -1 if it is on the bottom
     * @param newPosition, the position the agent needs to be updated to if it moved (set to current position if the agent isn't moving)
     */
    /*
    public void extendMap(int x, int y, Point newPosition, int shiftLength) {
        //Variables keeping track of change in size of the map

        //Point is on the left of the current known map, extend the map to that area and shift all the points to the left
        if(x == -1) {
            //System.out.println("Extend map to left");
            ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length][currentMap[0].length +shiftLength];
            for(int i=0; i<currentMap.length; i++) {
                for(int j=0; j<currentMap[0].length ;j++) {
                    newMap[i][j+shiftLength] = currentMap[i][j];
                }
            }
            currentMap = newMap;
        }


        //Point is on the right of the current known map, extend the map to that area
        if(x == 1) {
            //System.out.println("Extend map to right");
            ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length][currentMap[0].length +shiftLength];
            for(int i=0; i<currentMap.length; i++) {
                for(int j=0; j<currentMap[0].length ;j++) {
                    newMap[i][j] = currentMap[i][j];
                }
            }
            currentMap = newMap;
        }

        //Point is above the current known map, extend the map to that area and shift all points to the top
        if(y == -1) {
            //System.out.println("Extend map to top");
            ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length +shiftLength][currentMap[0].length];
            for(int i=0; i<currentMap.length; i++) {
                for(int j=0; j<currentMap[0].length ;j++) {
                    newMap[i][j] = currentMap[i][j];
                }
            }
            currentMap = newMap;
        }

        //Point is under the current known map, extend the map to that area
        if(y == 1) {
            //System.out.println("Extend map to bottom");
            ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length +shiftLength][currentMap[0].length];
            for(int i=0; i<currentMap.length; i++) {
                for(int j=0; j<currentMap[0].length ;j++) {
                    newMap[i+shiftLength][j] = currentMap[i][j];
                }
            }
            currentMap = newMap;
        }

        currentPosition = new Point(newPosition.getX(), newPosition.getY());
        if(x == -1) {
            currentPosition = new Point(currentPosition.getX() + shiftLength, currentPosition.getY());
            currentMapTopLeft = new Point(currentPosition.getX() + shiftLength, currentPosition.getY());
        }
        if(y == 1) {
            currentPosition = new Point(currentPosition.getX(), currentPosition.getY() + shiftLength);
            currentMapTopLeft = new Point(currentPosition.getX(), currentPosition.getY() + shiftLength);
        }
    } */


    /**
     * Method that increases the size of the current map that the agent is keeping track of and updates the agent's position accordingly
     * @param shiftLength, the size from which the map is extended
     */
    public void extendMapToLeft(int shiftLength) {
        //Point is on the left of the current known map, extend the map to that area and shift all the points to the left
        System.out.println("Extend map to left");
        ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length][currentMap[0].length + shiftLength];
        for (int i = 0; i < currentMap.length; i++) {
            for (int j = 0; j < currentMap[0].length; j++) {
                newMap[i][j + shiftLength] = currentMap[i][j];
            }
        }
        currentMap = newMap;
        currentMapTopRight = new Point(currentMapTopRight.getX() +shiftLength, currentMapTopRight.getY());
        currentPosition = new Point(currentPosition.getX() + shiftLength, currentPosition.getY());
        targetPosition = new Point(targetPosition.getX() + shiftLength, targetPosition.getY());
    }

    /**
     * Method that increases the size of the current map that the agent is keeping track of and updates the agent's position accordingly
     * @param shiftLength, the size from which the map is extended
     */
    public void extendMapToRight(int shiftLength) {
        System.out.println("Extend map to right");
        ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length][currentMap[0].length +shiftLength];
        for(int i=0; i<currentMap.length; i++) {
            for(int j=0; j<currentMap[0].length ;j++) {
                newMap[i][j] = currentMap[i][j];
            }
        }
        currentMap = newMap;
        currentMapTopRight = new Point(currentMapTopRight.getX() +shiftLength, currentMapTopRight.getY());
    }

    /**
     * Method that increases the size of the current map that the agent is keeping track of and updates the agent's position accordingly
     * @param shiftLength, the size from which the map is extended
     */
    public void extendMapToTop(int shiftLength) {
        System.out.println("Extend map to top");
        ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length +shiftLength][currentMap[0].length];
        for(int i=0; i<currentMap.length; i++) {
            for(int j=0; j<currentMap[0].length ;j++) {
                newMap[i][j] = currentMap[i][j];
            }
        }
        currentMap = newMap;
        currentMapTopRight = new Point(currentMapTopRight.getX(), currentMapTopRight.getY() +shiftLength);
    }

    /**
     * Method that increases the size of the current map that the agent is keeping track of and updates the agent's position accordingly
     * @param shiftLength, the size from which the map is extended
     */
    public void extendMapToBottom(int shiftLength) {
        System.out.println("Extend map to bottom");
        ObjectPerceptType[][] newMap = new ObjectPerceptType[currentMap.length +shiftLength][currentMap[0].length];
        for(int i=0; i<currentMap.length; i++) {
            for(int j=0; j<currentMap[0].length ;j++) {
                newMap[i+shiftLength][j] = currentMap[i][j];
            }
        }
        currentMap = newMap;
        currentPosition = new Point(currentPosition.getX(), currentPosition.getY() + shiftLength);
        currentMapTopRight = new Point(currentPosition.getX(), currentPosition.getY() + shiftLength);
        targetPosition = new Point(targetPosition.getX(), targetPosition.getY()+ shiftLength);
    }


    public void resetParameters() {
        this.currentMap = new ObjectPerceptType[100][100];
        this.currentPosition = new Point(0,0);
        this.currentMapTopRight = new Point(100,100);
        this.currentAngle = Angle.fromDegrees(0);
    }

    public void setCurrentPosition(Point currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setCurrentAngle(Angle currentAngle) {
        this.currentAngle = currentAngle;
    }

    public ObjectPerceptType[][] getCurrentMap() {
        return currentMap;
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public Angle getCurrentAngle() {
        return currentAngle;
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    //Add to the current target position to take the map shifts into account
    public void setTargetPosition(Point targetPosition) {
        this.targetPosition = new Point(targetPosition.getX() +this.targetPosition.getX(),
                targetPosition.getY() + this.targetPosition.getY());
    }

    //Collision checker
    public boolean isValidMove(double distance, IntruderPercepts percepts) {
        for(ObjectPercept objectPercept: percepts.getVision().getObjects().getAll()) {
            if(Math.abs(objectPercept.getPoint().getX()) <= 0.1) {
                if(objectPercept.getType().isSolid()) {
                    if(objectPercept.getPoint().getDistance(new Point(0,0)).getValue() >= distance) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for(int i=0; i<currentMap.length; i++) {
            for(int j=0; j<currentMap[0].length; j++) {
                if (currentMap[i][j] == null) str += " ";
                else {
                    switch (currentMap[i][j]) {
                        case EmptySpace:
                            str += ".";
                            break;
                        case ShadedArea:
                            str += "s";
                            break;
                        case Door:
                            str += "d";
                            break;
                        case Wall:
                            str += "w";
                            break;
                        case Window:
                            str += "i";
                            break;
                        case SentryTower:
                            str += "t";
                            break;
                        case TargetArea:
                            str += "T";
                            break;
                        case Teleport:
                            str += "t";
                            break;
                        default:
                            str += " ";
                            break;
                    }
                }
            }
            str += "\n";
        }
        return str;
    }
}
