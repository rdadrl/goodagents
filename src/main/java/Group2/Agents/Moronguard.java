package Group2.Agents;

import Group2.GuardAgent;
import Interop.Action.*;
import Interop.Agent.Guard;
import Interop.Percept.GuardPercepts;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Smell.SmellPerceptType;
import Interop.Percept.Vision.ObjectPercept;

import java.util.Set;

public class Moronguard implements Guard {

    private int sprintCooldown = 0;
    private boolean lastTurnSawWall = false;
    private boolean negative = false;
    private int counter = 0;

    public GuardAction getAction(GuardPercepts percepts) {
        //Return whatever action to take, depending on the perception we have under here.
        Set<ObjectPercept> objects = percepts.getVision().getObjects().getAll();
        if (counter % 10 == 0) {
            counter++;
            return new Yell();
        }
        counter++;
        return new Move(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard());
    }
}
