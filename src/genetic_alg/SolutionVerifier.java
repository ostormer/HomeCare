package genetic_alg;


public class SolutionVerifier {

	public static void main(String[] args) throws InterruptedException {

	    Problem problem = new Problem("src\\train\\train_9.json");
	    
	    GenAlg solver = new GenAlg(problem);
	    solver.run();

	}

}
