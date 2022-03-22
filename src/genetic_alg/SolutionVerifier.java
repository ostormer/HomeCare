package genetic_alg;


public class SolutionVerifier {

	public static void main(String[] args) throws InterruptedException {

	    Problem problem = new Problem(Params.problemPath);
	    
	    GenAlg solver = new GenAlg(problem);
	    Solution winner = solver.runGATournament();
	    winner.saveToFile("out\\winner.json");
	}

}
