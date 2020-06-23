package Group2.Agents.LinearProgram;

import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.ArrayList;

public class Sector {
    protected static int lastId = 0;
    private int id;
    private ArrayList<String> objectsInSector = new ArrayList<>();

    private double relativeStartAngle;

    public Sector(double relAngle) {
        relativeStartAngle = relAngle;
        id = ++lastId;
    }

    public void addContent(ObjectPercept obj) {
        objectsInSector.add(obj.getType().name());
    }
    public void addContent(String typeName) {
        objectsInSector.add(typeName);
    }
    public void addContent(ObjectPerceptType objType) {
        objectsInSector.add(objType.name());
    }

    public double getRelativeAngle() { return relativeStartAngle; }

    public int includesHowMany(String objectName) {
        int amount = 0;

        for (String sectorContent : objectsInSector) {
            if (objectName.equals(sectorContent)) {
                amount++;
            }
        }

        return amount;
    }

    public ArrayList getList () { return (ArrayList<ObjectPerceptType>) objectsInSector.clone(); }

    public int getID() {
        return id;
    }

    public String toString() {
        String res = "Sector " + id + "at angle " + relativeStartAngle + ": {";
        for (String type : objectsInSector) {
            res += ("\n\t" + type);
        }

        return res + "\n}";
    }
}
