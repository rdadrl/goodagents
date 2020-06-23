package Group2.Agents.LinearProgram;
/**
 * A fundamental mindset to translate a guard logic into a linear problem is to understand it having multiple states
 *
 * This follows the route the rule based agent follows, and that is that it has an exploration and a chase state.
 *
 * The exploration stage would be maximizing the chances of meeting an intruder for which
 * the map should be discovered to pinpoint positions of points of interests
 *  - doors
 *  - teleporters
 *  - windows
 *
 * Also for discovery we would like to travel to previously unknown tiles
 * and for that we would have a negative constraint for previously visitied tiles to have an affect on the linear program.
 *
 * For the movement, there would be constraints on the rotation and movement amounts
 *
 * take comments above with frustration. this costed me two days of work and a spagetti code.
 * now atleast I know what to avoid- presenting LPGuard v2.
 */

import Interop.Action.GuardAction;
import Interop.Action.Move;
import Interop.Action.Rotate;
import Interop.Action.Yell;
import Interop.Agent.Guard;
import Interop.Geometry.Angle;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Smell.SmellPercept;
import Interop.Percept.Sound.SoundPercept;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.ArrayList;
import java.util.Queue;

public class LPGuard implements Interop.Agent.Guard {
    private final int SECTOR_ANGLE = 9;
    private ArrayList<Sector> sectorsAvailable = new ArrayList<>();

    /**
     * Each round could be seen as a collection of 3 possible actions:
     *  -move forward
     *  -rotate by x angle
     *  -yell
     *
     *  and all these is to be determined through the linear program
     */
    private GuardAction[] roundActions = new GuardAction[2];
    private int currentRound = 0;
    private boolean roundFinished = true;
    private boolean yelledThisRound = false; //this could also be used as a chase toggle
    @Override
    public GuardAction getAction(GuardPercepts percepts) {
        //always yell if intruder has been seen
        if(!yelledThisRound) {
            for (ObjectPercept object : percepts.getVision().getObjects().getAll()) {
                if (object.getType().equals(ObjectPerceptType.Intruder)) {
                    yelledThisRound = true;
                    System.out.println("Agent yelled");
                    return new Yell();
                }
            }
        }

        if (roundFinished) {
            roundFinished = false;
            currentRound = 0;
            sectorsAvailable.clear();

            double foVDegrees = percepts.getVision().getFieldOfView().getViewAngle().getDegrees();
            int sectorCount = (int) (foVDegrees / SECTOR_ANGLE);
            for (int i = -sectorCount / 2; i <= sectorCount / 2; i++) {
                Sector currentSector = new Sector(i * SECTOR_ANGLE);
                //System.out.println("Sector (" + currentSector.toString() + ") created with relative angle " + currentSector.getRelativeAngle());
                sectorsAvailable.add(currentSector);
            }

            //Add visible objects>
            for (ObjectPercept object : percepts.getVision().getObjects().getAll()) {
                /*if(object.getType().equals(ObjectPerceptType.Wall))
                    System.out.println("Wall at" + object.getPoint().toString() + " and at degree " + object.getPoint().getClockDirection().getDegrees());//seenWalls++;
                if(object.getType().equals(ObjectPerceptType.Guard))
                    System.out.println("Guard at" + object.getPoint().toString() + " and at degree " + object.getPoint().getClockDirection().getDegrees());//seenGuards++;
                if(object.getType().equals(ObjectPerceptType.Door))
                    System.out.println("Door at" + object.getPoint().toString() + " and at degree " + object.getPoint().getClockDirection().getDegrees());//seenDoors++;
                if(object.getType().equals(ObjectPerceptType.Window))
                    System.out.println("Window at" + object.getPoint().toString() + " and at degree " + object.getPoint().getClockDirection().getDegrees());//seenWindows++;
                if(object.getType().equals(ObjectPerceptType.Teleport))
                    System.out.println("TP at" + object.getPoint().toString() + " and at degree " + object.getPoint().getClockDirection().getDegrees());//seenTeleports++;
                if(object.getType().equals(ObjectPerceptType.SentryTower))
                    System.out.println("ST at" + object.getPoint().toString() + " and at degree " + object.getPoint().getClockDirection().getDegrees());//seenSentryTowers++;
                */

                //normalising angles to [-foVDegrees/2,+foVDegrees/2]
                double objDegree = object.getPoint().getClockDirection().getDegrees();
                if (objDegree > foVDegrees / 2)
                    objDegree -= 360;

                //Add object into sector.
                boolean objectAdded = false;
                for (Sector sect : sectorsAvailable) {
                    if (objDegree < sect.getRelativeAngle()) {
                        sect.addContent(object);
                        objectAdded = true;
                    }
                }
                if (!objectAdded) {
                    sectorsAvailable.get(sectorsAvailable.size() - 1).addContent(object);
                }
            }

            //Smell doesn't have a direction- solving a linear problem dependent upon direction doesn't quite make sense.
            //instead, a sound has one!
            for (SoundPercept sound : percepts.getSounds().getAll()) {
                double soundDegree = sound.getDirection().getDegrees();
                if (soundDegree > foVDegrees / 2)
                    soundDegree -= 360;

                //Add object into sector.
                boolean soundAdded = false;
                for (Sector sect : sectorsAvailable) {
                    if (soundDegree < sect.getRelativeAngle()) {
                        sect.addContent(sound.getType().name());
                        soundAdded = true;
                    }
                }
                if (!soundAdded) {
                    sectorsAvailable.get(sectorsAvailable.size() - 1).addContent(sound.getType().name());
                }
            }

            //Inspect each sector using LP:
            double zBest = -10000;
            Sector sectorBest = null;
            for (Sector sector : sectorsAvailable) {
                //System.out.println(sector.toString());
                double Z = solveLinearProgram(sector);

                if (sector.getRelativeAngle() < 0) Z += 5;

                if (sector.getRelativeAngle() == 0) Z += 2.5; //go straight bias.
                else if (Math.abs(sector.getRelativeAngle()) == 9 ) Z-= 2.5;
                else Z -= 3.5;

                if (Z > zBest) {
                    zBest = Z;
                    sectorBest = sector;
                }
            }
            //Sector sectorBest <- highest scoring sector
            //rotationAngle <- relative angle from s + SECTOR_ANGLE / 2
            assert sectorBest != null;
            if (zBest < 0) {
                System.out.println("Shit moves left only.");
                roundFinished = true;
                return new Rotate(Angle.fromDegrees(180 * Math.random()));
            }
            else System.out.println(sectorBest.toString() + " with score " + zBest);

            double rotationAngle = sectorBest.getRelativeAngle() + (foVDegrees / 2) * Math.random();
            if (sectorBest.getRelativeAngle() < 0) rotationAngle = sectorBest.getRelativeAngle() - (foVDegrees / 2) * Math.random();
            rotationAngle = rotationAngle * (1+Math.random());
            //if (sectorBest.getRelativeAngle() < 0)
            //    rotationAngle = sectorBest.getRelativeAngle() - (SECTOR_ANGLE / 2d);
            System.out.println("Rotation angle: " + rotationAngle);
            //movement <- always. LP makes sure to select a sector containing empty space to always allow for movements.

            //round[] <- {rotationAngle, movement}
            roundActions[0] = new Rotate(Angle.fromDegrees(rotationAngle));
            roundActions[1] = new Move(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard());
            currentRound = 0;
            yelledThisRound = false;
        }

        //System.out.println(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue());
        currentRound++;
        if (currentRound == roundActions.length) roundFinished = true;
        return roundActions[currentRound - 1];
    }

    double w = -1.35;   //Wall
    double es = 0.9;  //EmptySpace
    double g = -3.5;  //Guard
    double y = 4;   //Yell
    double n = 1.25;   //Noise
    double i = 100;   //Intruder
    double wx = 1.25;  //Window
    double d = 1.25;   //Door
    double tp = 0.5;  //Teleport
    double st = 1.5;  //SentryTower
    double ta = 1.5;    //TargetArea

    private double solveLinearProgram(Sector sec) {
        double res = 0;

        res += sec.includesHowMany("Wall") * w;
        res += sec.includesHowMany("EmptySpace") * es;
        res += sec.includesHowMany("Guard") * g;
        //if (!yelledThisRound)
        res += sec.includesHowMany("Yell") * y;
        res += sec.includesHowMany("Noise") * n;
        res += sec.includesHowMany("Intruder") * i;
        res += sec.includesHowMany("Window") * wx;
        res += sec.includesHowMany("Door") * d;
        res += sec.includesHowMany("Teleport") * tp;
        res += sec.includesHowMany("SentryTower") * st;
        res += sec.includesHowMany("TargetArea") * ta;

        return res;
    }
}
