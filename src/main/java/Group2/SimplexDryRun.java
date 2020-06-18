package Group2;
public class SimplexDryRun {
    public static void main(String[] args){
        double[][] array = {{1,-3,-2,0,0,0,0},{0,2,1,1,0,0,18},{0,2,3,0,1,0,42},{0,3,1,0,0,1,24}};
        Simplex solver = new Simplex(array);
        double[][] solvedArray = solver.getMatrix();
    }
}
