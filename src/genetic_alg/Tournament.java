package genetic_alg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Tournament {

    private List<Solution> population;
    private static ArrayList<Double> probs = computeProbs();
    private Random rand;
    
    public Tournament(List<Solution> population) {
        this.population = population;
        this.rand = new Random();
    }
    /**
     * Computes cumulative selection probabilities based on rank from a sorted list of solutions
     * @return ArrayList<Double> of probabilities
     */
    private static ArrayList<Double> computeProbs() {
        double p = Params.bestSelectionWeight;
        double cumulativeProb = 0.;
        ArrayList<Double> probs = new ArrayList<Double>();
        for (int i=0; i<Params.tournamentSize; i++) {
            cumulativeProb += p*Math.pow(1-p, i);
            probs.add(cumulativeProb);
        }
        return probs;
    }
    
    /**
     * Select a single Solution from population
     */
    public Solution run() {
        // Select tournamentSize solutions from population to compete for selection
        ArrayList<Solution> competitors = new ArrayList<Solution>();
        while (competitors.size() < Params.tournamentSize) {
            // Random selection WITH REPLACEMENT
            // Without replacement is more expensive due to required shuffling, and doesn't help much
            competitors.add(this.population.get(this.rand.nextInt(population.size())));
        }
        
        // Sort by fitness
        // The following sorting is the most expensive operation here
        competitors.sort(Comparator.comparingDouble(Solution::getFitness));
        
        double r = rand.nextDouble();
        if (r > probs.get(probs.size()-1)){
            // Technically breaks the intended distribution,
            // but return best as skewing towards best is not that bad
            return competitors.get(0);
        }
        for (int i=0; i<Params.tournamentSize; i++){
            if (r < probs.get(i)){
                return competitors.get(i);
            }
        }
        // Should never run
        System.out.println("No solution selected in tournament, something went wrong");
        return competitors.get(0);
    }

}
