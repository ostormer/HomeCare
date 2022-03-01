package genetic_alg;

public class SolutionVerifier {

	public static void main(String[] args) {

	    Problem problem = new Problem("src\\train\\train_0.json");
	    
	    Solution randomSolution = new Solution(problem);
	    randomSolution.generateRandomUnfeasible();
	    System.out.println(randomSolution.toStringRepresentation());
	    System.out.println(randomSolution.computeUnfeasibleUtility());
	    
	    Solution sortedSolution = new Solution(problem);
	    sortedSolution.generateRandomSorted();
        System.out.println(sortedSolution.toStringRepresentation());
        System.out.println(sortedSolution.computeUnfeasibleUtility());
	}

}
