package Group2.Agents.LinearProgram;

import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.ArrayList;

public class Sector {
    protected static int lastId = 0;
    private int id;
    private ArrayList<ObjectPerceptType> objectsInSector = new ArrayList<>();

    private int relativeStartAngle;

    public Sector(int relAngle) {
        relativeStartAngle = relAngle;
        id = ++lastId;
    }

    public void addContent(ObjectPercept obj) {
        objectsInSector.add(obj.getType());
    }
    public void addContent(ObjectPerceptType objType) {
        objectsInSector.add(objType);
    }

    public int includesHowMany(ObjectPercept object) {
        int amount = 0;

        for (ObjectPerceptType sectorContent : objectsInSector) {
            if (object.getType().equals(sectorContent)) {
                amount++;
            }
        }

        return amount;
    }

    public ArrayList getList () { return (ArrayList<ObjectPerceptType>) objectsInSector.clone(); }

    public int getID() {
        return id;
    }
}
