package genetic_alg;

import java.util.concurrent.TimeUnit;

public class SolutionVerifier {

	public static void main(String[] args) throws InterruptedException {

	    Problem problem = new Problem("src\\train\\train_0.json");
	    
	    Solution randomSolution = new Solution(problem);
	    randomSolution.generateRandomUnfeasible();
	    System.out.println(randomSolution.toStringRepresentation());
	    System.out.println(randomSolution.computeUnfeasibleUtility());
	    
	    Solution sortedSolution = new Solution(problem);
	    sortedSolution.generateRandomSorted();
        System.out.println(sortedSolution.toStringRepresentation());
        System.out.println(sortedSolution.computeUnfeasibleUtility());
        RouteDisplayComponent comp = sortedSolution.displaySolution();
        for (int i=0; i<10; i++) {
            TimeUnit.SECONDS.sleep(2);
            sortedSolution.mutateImproveOnePatient();
            sortedSolution.updateDisplay(comp);
        }
        
	}

}
