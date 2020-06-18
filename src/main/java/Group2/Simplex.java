package Group2;
import java.util.Arrays;

public class Simplex {
    private double[][] matrix;
    private double lowestNegativeCoefficient;
    private int lowestNegativeCoefficientIndex;
    private int highestRatioIndex;

    public Simplex(double[][] matrix){
        this.matrix = matrix;
        this.solve();
    }

    private void solve(){
        printMatrix();
        while(findLowestNegativeCoefficient()!=-1){
            findHighestRatio();
            scalePivotRow();
            doTheRest();
            printMatrix();
        }
    }

    private void findHighestRatio(){
        double highestRatio = 0;
        double tempRatio;
        for(int i=1; i<matrix.length; i++){
            tempRatio = matrix[i][lowestNegativeCoefficientIndex]/matrix[i][matrix[i].length-1];
            if(tempRatio>highestRatio) {
                highestRatio = tempRatio;
                this.highestRatioIndex = i;
            }
        }
    }

    private int findLowestNegativeCoefficient(){
        this.lowestNegativeCoefficientIndex = -1;
        this.lowestNegativeCoefficient = 0;
        for(int i=0;i<matrix[0].length-1;i++){
            if(matrix[0][i]<this.lowestNegativeCoefficient){
                lowestNegativeCoefficientIndex = i;
                lowestNegativeCoefficient = matrix[0][i];
            }
        }
        System.out.println(this.lowestNegativeCoefficient);
        return this.lowestNegativeCoefficientIndex;
    }

    private void scalePivotRow(){
        double ratio = matrix[highestRatioIndex][lowestNegativeCoefficientIndex];
        for(int i=0;i<matrix[highestRatioIndex].length;i++){
            matrix[highestRatioIndex][i] /= ratio;
        }
    }

    private void doTheRest(){
        for(int i=0; i<matrix.length;i++){
            if(matrix[i][lowestNegativeCoefficientIndex]!=0 && i != highestRatioIndex){
                subtractPivot(i);
            }
        }
    }


    private void subtractPivot(int index){
        double scale = matrix[index][lowestNegativeCoefficientIndex];
        for(int i=0;i<matrix[index].length; i++){
            matrix[index][i]-=scale*matrix[highestRatioIndex][i];
        }
    }

    private void printMatrix(){
        System.out.println(Arrays.deepToString(matrix));
    }

    public double[][] getMatrix() {
        return matrix;
    }
}
