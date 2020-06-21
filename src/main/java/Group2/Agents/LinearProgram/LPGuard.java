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
import Interop.Agent.Guard;
import Interop.Percept.GuardPercepts;

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

    @Override
    public GuardAction getAction(GuardPercepts percepts) {
        //always yell if intruder has been seen

        if (roundFinished) {
            sectorsAvailable.clear();

            double foVDegrees = percepts.getVision().getFieldOfView().getViewAngle().getDegrees();
            for (int i = 0; i < (int) (foVDegrees / SECTOR_ANGLE); i++) {
                Sector currentSector = new Sector(i * SECTOR_ANGLE);
                //TODO: Add sector entitites here:
                    //currentSector.addContent(someObject);
                //Done adding entities.
                sectorsAvailable.add(currentSector);
            }

            //Inspect each sector using LP:

            //Sector s <- highest scoring sector

            //rotationAngle <- relative angle from s + SECTOR_ANGLE / 2
            //movement <- always. LP makes sure to select a sector containing empty space to always allow for movements.

            //round[] <- {rotationAngle, movement}
        }

        //Execute roundActions[currentRound++]

        //check if roundFinished

        return null;
    }
}
