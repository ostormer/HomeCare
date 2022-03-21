package genetic_alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GenAlg {
    private int generation = 0;
    private Problem problem;
    private List<Solution> population; // TODO: divide into feasible and infeasible pops
    private Random rand;
    
    public GenAlg(Problem problem) {
        this.problem = problem;
        this.rand = new Random();
        population = new ArrayList<Solution>();
    }
    
    private void generatePop() {
        // Generates population.
        // Right now all of them as random sorted by time
        for (int i=0; i<Params.popSize; i++) {
            Solution s = new Solution(problem);
            if (i % 3 == 0) {
                s.generateRandomSorted();                
            } else if (i % 3 == 1) {
                s.generateRandomUnfeasible();
            } else {
                s.generateRandomGreedy(rand.nextInt(problem.getNbrNurses()-1) + 2); // Between 2 and nbrNurses active nurses
            }
            population.add(s);
        }
    }
    
    private List<Solution> parentSelection() {
        // TODO: Implement elitism
        List<Solution> parents = new ArrayList<Solution>();
        while (parents.size() < Params.nbrParents) {
            Tournament t = new Tournament(population);
            parents.add(t.run());
        }
        return parents;
    }
    
    private List<Solution> crossover(List<Solution> parents) {
        List<Solution> offspring = new ArrayList<Solution>();
        Collections.shuffle(parents);
        for (int i=0; i<Params.nbrParents; i+=2) {
            Solution[] children = parents.get(i).crossoverGreedyInsertion(parents.get(i+1));
            offspring.add(children[0]);
            offspring.add(children[1]);
        }
        return offspring;
    }
    /**
     * Mutate list of solutions in-place, also returns the list
     * @param offspring
     * @return
     */
    private List<Solution> mutate(List<Solution> offspring) {
        for (Solution solution : offspring) {
            if (this.rand.nextDouble() < Params.mutationRate) {
                solution.mutate();
            }
        }
        return offspring;
    }
    
    private void updateFitness(List<Solution> solutions) {
        for (Solution s : solutions) {
            if (s.getFitnessChanged()) {
                s.computeUnfeasibleUtility();
            }
        }
    }
    
    /**
     * Select survivors from population and offspring.
     * Implement elitism here.
     * @param offspring
     * @return
     */
    private List<Solution> survivorSelection(List<Solution> offspring) {
        List<Solution> oldGeneration = new ArrayList<Solution>(this.population);
        Collections.sort(oldGeneration, Comparator.comparingDouble(Solution::getFitness));
        List<Solution> survivors = new ArrayList<Solution>();
        updateFitness(offspring);
        // Se
        
        return survivors;
    }
    
    public void run() {
        // Prep
        generatePop();
        // Loop
        while (generation < Params.maxGenerations) {
            updateFitness(this.population);
            List<Solution> parents = parentSelection();
            List<Solution> offspring = crossover(parents);
            // Mutate all survivors, not just offspring.
            
            List<Solution> newPopulation = survivorSelection(offspring);
            
            offspring = mutate(newPopulation);
            
            
            this.generation++;
        }
    }
}
