package Group2.Agents.LinearProgram;

public abstract class Constraint {
    private double constrainedValue;
    private char inequalityType;
    private String notes = null;

    public Constraint(double initialValue, char inequalityType) {
        constrainedValue = initialValue;

        switch (inequalityType) { //need this to determine what to add (artificial/surplus vars.)
            case('l')://lesser
                this.inequalityType = inequalityType;
                break;
            case('e')://equal
                this.inequalityType = inequalityType;
                break;
            case('g')://greater
                this.inequalityType = inequalityType;
                break;
            default:
                System.out.println("Warning! Unknown inequality type detected on constraint. Setting as 'g' default.");
                this.inequalityType = 'g';
        }
    }
    public Constraint(double initialValue, char inequalityType, String notes) {
        constrainedValue = initialValue;
        this.notes = notes;

        switch (inequalityType) { //need this to determine what to add (artificial/surplus vars.)
            case('l')://lesser
                this.inequalityType = inequalityType;
                break;
            case('e')://equal
                this.inequalityType = inequalityType;
                break;
            case('g')://greater
                this.inequalityType = inequalityType;
                break;
            default:
                System.out.println("Warning! Unknown inequality type detected on constraint. Setting as 'g' default.");
                this.inequalityType = 'g';
        }
    }

    protected abstract boolean clause(double a, double b);

    public boolean checkValueFitness(double a) {
        return clause(a, constrainedValue);
    }

    public void updateConstraintValue (double newConstraint) {
        constrainedValue = newConstraint;
    }
    public double getConstrainedValue() { return constrainedValue; }

    public char getInequalityType() { return inequalityType; }

    @Override
    public String toString() {
        String res = "Constraint " + this + ": subject to " + inequalityType + " of " + constrainedValue + ".";
        if (notes != null) res += "\n\t" + notes;
        return res;
    }
}
