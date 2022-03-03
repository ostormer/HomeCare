package genetic_alg;

import java.util.List;

public class GenAlg {
    private int popSize;
    private Problem problem;
    private List<Solution> pop; // TODO: divide into feasible and infeasible pops

    public void generatePop() {
        // Generates population.
        // Right now all of them as random sorted by time
        for (int i=0; i<popSize; i++) {
            Solution s = new Solution(problem);
            s.generateRandomSorted();
            pop.add(s);
        }
    }
    
    
}
