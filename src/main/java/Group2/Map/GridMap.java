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
import Interop.Percept.Smell.SmellPercept;
import Interop.Percept.Smell.SmellPerceptType;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GridMap {

    private Point currentPosition;
    private Point currentMapTopRight;
    private Angle currentAngle;
    private Point targetPosition;
    public ObjectPerceptType[][] newMap;


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



        //Add all the points in the range of view of the agent to the map
        for(ObjectPercept objectPercept: percepts.getVision().getObjects().getAll()) {

            //Object point in the agent's cartesian system (agent is at (0,0))
            Point objectPoint = new Point(objectPercept.getPoint().getX(), objectPercept.getPoint().getY());


            double distanceToObject = new Distance(objectPoint, new Point(0,0)).getValue();
            //Angle with respect to the agent
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


            //Set all points between the object percept point and the agent to empty spaces
            for(int i = 1; i < (int) distanceToObject; i++) {

                //Coordinates of the point in the agent's coordinate system (agent at (0,0))
                double x = Math.cos(angleInMap.getRadians()) * i;
                double y = Math.sin(angleInMap.getRadians()) * i;

                int xInMap = (int) Math.round(currentPosition.getX() + x);
                int yInMap = (int) Math.round(currentPosition.getY() + y);

                if(currentMap[yInMap][xInMap] == null) currentMap[yInMap][xInMap] = ObjectPerceptType.EmptySpace;
            }
        }
//        ObjectPerceptType[][] aMap = new ObjectPerceptType[currentMap.length][currentMap[0].length];
//        for(int i=0; i<aMap.length; i++) {
//            for (int j = 0; j < aMap[0].length; j++) {
//                if (currentMap[i][j] == ObjectPerceptType.Wall) {
//                    aMap[i][j] = ObjectPerceptType.Wall;
//                }
//                 if (i>0 && currentMap[i-1][j]== ObjectPerceptType.EmptySpace) {aMap[i-1][j] = ObjectPerceptType.EmptySpace;}else break;
//                if (j>0 && currentMap[i][j-1]==  ObjectPerceptType.EmptySpace) aMap[i][j-1] = ObjectPerceptType.EmptySpace;else break;
//                if (i+1<currentMap.length && currentMap[i+1][j]==ObjectPerceptType.EmptySpace) aMap[i+1][j] = ObjectPerceptType.EmptySpace;else break;
//                if (j+1<currentMap[i].length && currentMap[i][j+1]==ObjectPerceptType.EmptySpace) aMap[i][j+1] = ObjectPerceptType.EmptySpace;else break;
//                if (j+1<currentMap[i].length && i+1<currentMap.length && currentMap[i+1][j+1]==ObjectPerceptType.EmptySpace) aMap[i+1][j+1] = ObjectPerceptType.EmptySpace;else break;
//                if (j>0 && i+1<currentMap.length && currentMap[i+1][j-1]==ObjectPerceptType.EmptySpace) aMap[i+1][j-1] = ObjectPerceptType.EmptySpace;else break;
//                if (j+1<currentMap[i].length && i>0 && currentMap[i-1][j+1]==ObjectPerceptType.EmptySpace) aMap[i-1][j+1] = ObjectPerceptType.EmptySpace;else break;
//                if (j>0 && i>0 && currentMap[i-1][j-1]==ObjectPerceptType.EmptySpace) aMap[i-1][j-1] = ObjectPerceptType.EmptySpace;else break;
//            }
//        }



        //System.out.println("----------------------------------------NORMAL MAP----------------------------------------");
        System.out.println(printMap(currentMap));


        ObjectPerceptType[][] newMap1 =  dilate(currentMap);
        //  ObjectPerceptType[][] newMap2 =  erode(newMap1);
        //System.out.println("----------------------------------------DILATED MAP----------------------------------------");
        //System.out.println(printMap(newMap1));
        //currentMap = newMap1;





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

            double distToWall = distanceToWall(distance.getValue(), (IntruderPercepts) percepts);

            //The agent can only move from the distance between him and the wall
            if(distToWall < Double.MAX_VALUE) {
                if(distToWall > 0.5) distance = new Distance(distToWall);
                else distance = new Distance(0);
            }

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

    }


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
        targetPosition = new Point(targetPosition.getX(), targetPosition.getY()+shiftLength);
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
    public double distanceToWall(double moveDistance, IntruderPercepts percepts) {
        double minDist = Double.MAX_VALUE;
        for(ObjectPercept objectPercept: percepts.getVision().getObjects().getAll()) {
                if(objectPercept.getType().isSolid()) {
                    //Distance from agent to wall minus agent's radius
                    double dist = objectPercept.getPoint().getDistance(new Point(0,0)).getValue() -0.5;
                    if(dist <= moveDistance && dist < minDist) {
                        minDist = dist;
                    }
                }
        }
        return minDist;
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

    public String printMap(ObjectPerceptType[][] map) {
        String str = "";
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == null) str += " ";
                else {
                    switch (map[i][j]) {
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



    boolean delete = false;
    public ObjectPerceptType[][] dilate(ObjectPerceptType[][] currentMap){
        for (int i=0; i<currentMap.length; i++){
            for (int j=0; j<currentMap[i].length; j++){
                if(currentMap[i][j] == ObjectPerceptType.EmptySpace){
                    if (i>0 && currentMap[i-1][j]== ObjectPerceptType.EmptySpace) break;
                    if (j>0 && currentMap[i][j-1]==  ObjectPerceptType.EmptySpace) break;
                    if (i+1<currentMap.length && currentMap[i+1][j]==ObjectPerceptType.EmptySpace) break;
                    if (j+1<currentMap[i].length && currentMap[i][j+1]==ObjectPerceptType.EmptySpace) break;
                    if (j+1<currentMap[i].length && i+1<currentMap.length && currentMap[i+1][j+1]==ObjectPerceptType.EmptySpace) break;
                    if (j>0 && i+1<currentMap.length && currentMap[i+1][j-1]==ObjectPerceptType.EmptySpace) break;
                    if (j+1<currentMap[i].length && i>0 && currentMap[i-1][j+1]==ObjectPerceptType.EmptySpace) break;
                    if (j>0 && i>0 && currentMap[i-1][j-1]==ObjectPerceptType.EmptySpace) break;

                }else if (currentMap[i][j] == ObjectPerceptType.Wall){
                    if (i>0 && currentMap[i-1][j]== ObjectPerceptType.EmptySpace) {currentMap[i-1][j] = ObjectPerceptType.ShadedArea;}
                  //  if (j>0 && currentMap[i][j-1]==  ObjectPerceptType.EmptySpace) currentMap[i][j-1] = ObjectPerceptType.ShadedArea;
                    if (i+1<currentMap.length && currentMap[i+1][j]==ObjectPerceptType.EmptySpace) currentMap[i+1][j] = ObjectPerceptType.ShadedArea;
                 //   if (j+1<currentMap[i].length && currentMap[i][j+1]==ObjectPerceptType.EmptySpace) currentMap[i][j+1] = ObjectPerceptType.ShadedArea;
//                    if (j+1<currentMap[i].length && i+1<currentMap.length && currentMap[i+1][j+1]==ObjectPerceptType.EmptySpace) currentMap[i+1][j+1] = ObjectPerceptType.ShadedArea;
//                    if (j>0 && i+1<currentMap.length && currentMap[i+1][j-1]==ObjectPerceptType.EmptySpace) currentMap[i+1][j-1] = ObjectPerceptType.ShadedArea;
//                    if (j+1<currentMap[i].length && i>0 && currentMap[i-1][j+1]==ObjectPerceptType.EmptySpace) currentMap[i-1][j+1] = ObjectPerceptType.ShadedArea;
//                    if (j>0 && i>0 && currentMap[i-1][j-1]==ObjectPerceptType.EmptySpace) currentMap[i-1][j-1] = ObjectPerceptType.ShadedArea;

                }else break;

            }
        }
        for (int i=0; i<currentMap.length; i++){
            for (int j=0; j<currentMap[i].length; j++) {
                if (currentMap[i][j] == ObjectPerceptType.ShadedArea) {
                    currentMap[i][j] = ObjectPerceptType.Wall;
                    delete = true;
                }
            }
        }
        return currentMap;
    }
    public ObjectPerceptType[][] erode(ObjectPerceptType[][] currentMap){
        for (int i=0; i<currentMap.length; i++){
            for (int j=0; j<currentMap[i].length; j++){
                if (currentMap[i][j] == ObjectPerceptType.Wall && delete == true){
                    if (i>0 && currentMap[i-1][j]== ObjectPerceptType.Wall) currentMap[i-1][j] = ObjectPerceptType.Window;
                    if (j>0 && currentMap[i][j-1]==  ObjectPerceptType.Wall) currentMap[i][j-1] = ObjectPerceptType.Window;
                    if (i+1<currentMap.length && currentMap[i+1][j]==ObjectPerceptType.Wall) currentMap[i+1][j] = ObjectPerceptType.Window;
                    if (j+1<currentMap[i].length && currentMap[i][j+1]==ObjectPerceptType.Wall) currentMap[i][j+1] = ObjectPerceptType.Window;
                    if (j+1<currentMap[i].length && i+1<currentMap.length && currentMap[i+1][j+1]==ObjectPerceptType.Wall) currentMap[i+1][j+1] = ObjectPerceptType.Window;
                    if (j>0 && i+1<currentMap.length && currentMap[i+1][j-1]==ObjectPerceptType.Wall) currentMap[i+1][j-1] = ObjectPerceptType.Window;
                    if (j+1<currentMap[i].length && i>0 && currentMap[i-1][j+1]==ObjectPerceptType.Wall) currentMap[i-1][j+1] = ObjectPerceptType.Window;
                    if (j>0 && i>0 && currentMap[i-1][j-1]==ObjectPerceptType.Wall) currentMap[i-1][j-1] = ObjectPerceptType.Window;

                }
            }
        }
        for (int i=0; i<currentMap.length; i++){
            for (int j=0; j<currentMap[i].length; j++){
                if (currentMap[i][j] == ObjectPerceptType.Window && delete == true){
                    currentMap[i][j] = ObjectPerceptType.EmptySpace;
                }
            }
        }
        return currentMap;
    }
}
