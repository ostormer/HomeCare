package genetic_alg;

import java.util.concurrent.TimeUnit;

public class SolutionVerifier {

	public static void main(String[] args) throws InterruptedException {

	    Problem problem = new Problem("src\\train\\train_0.json");
	    
	    Solution randomSolution = new Solution(problem);
	    randomSolution.generateRandomUnfeasible();
	    System.out.println(randomSolution.toStringRepresentation());
	    System.out.println(randomSolution.computeUnfeasibleUtility());
	    
	    /*
	    Solution sortedSolution = new Solution(problem);
	    sortedSolution.generateRandomSorted();
        System.out.println(sortedSolution.toStringRepresentation());
        System.out.println(sortedSolution.computeUnfeasibleUtility());
        RouteDisplayComponent comp = sortedSolution.displaySolution();
        sortedSolution.updateDisplay(comp);
        for (int i=0; i<1000; i++) {
            sortedSolution.mutateImproveOnePatient();
            if(i%100 == 0) {
                sortedSolution.updateDisplay(comp);
                TimeUnit.SECONDS.sleep(1);
            }
        }
        sortedSolution.updateDisplay(comp);
        System.out.println(sortedSolution.toStringRepresentation());
        System.out.println(sortedSolution.computeUnfeasibleUtility());
        
        TimeUnit.SECONDS.sleep(5);
        */
	    
        Solution greedySolution = new Solution(problem);
        greedySolution.generateRandomGreedy(problem.getNbrNurses());
        System.out.println(greedySolution.toStringRepresentation());
        System.out.println(greedySolution.computeUnfeasibleUtility());
        RouteDisplayComponent comp = greedySolution.displaySolution();
        greedySolution.updateDisplay(comp);
        for (int i=0; i<1000; i++) {
            greedySolution.mutateImproveOnePatient();
            if(i%100 == 0) {
                greedySolution.updateDisplay(comp);
                TimeUnit.SECONDS.sleep(1);
                greedySolution.mutateSplitOneNursePlan();
                greedySolution.updateDisplay(comp);
                TimeUnit.SECONDS.sleep(1);
            }
        }
        greedySolution.updateDisplay(comp);
        System.out.println(greedySolution.toStringRepresentation());
        System.out.println(greedySolution.computeUnfeasibleUtility());
	}

}
