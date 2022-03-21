package genetic_alg;

import java.util.concurrent.TimeUnit;

public class SolutionVerifier {

	public static void main(String[] args) throws InterruptedException {

	    Problem problem = new Problem("src\\train\\train_0.json");
	    
	    GenAlg solver = new GenAlg(problem);
	    solver.run();
	    /*
	    Solution randomSolution = new Solution(problem);
	    randomSolution.generateRandomUnfeasible();
	    System.out.println(randomSolution.toStringRepresentation());
	    System.out.println(randomSolution.computeUnfeasibleFitness());
	    
        Solution greedySolution = new Solution(problem);
        greedySolution.generateRandomGreedy(problem.getNbrNurses());
        System.out.println(greedySolution.toStringRepresentation());
        System.out.println(greedySolution.computeUnfeasibleFitness());
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
        System.out.println(greedySolution.computeUnfeasibleFitness());
        
        */
	}

}
