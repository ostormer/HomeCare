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
    }
    
    private List<Solution> generatePop(int populationSize) {
        // Generates population.
        List<Solution> pop = new ArrayList<Solution>();
        for (int i=0; i<populationSize; i++) {
            Solution s = new Solution(problem);
            if (i % 3 == 0) {
                s.generateRandomSorted();                
            } else if (i % 3 == 1) {
                s.generateRandomUnfeasible();
            } else {
                s.generateRandomGreedy(rand.nextInt(problem.getNbrNurses()-1) + 2); // Between 2 and nbrNurses active nurses
            }
            pop.add(s);
        }
        pop.sort(Comparator.comparingDouble(Solution::getFitness));
        return pop;
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
    private List<Solution> mutate(List<Solution> solutions) {
        for (Solution solution : solutions) {
            if (this.rand.nextDouble() < Params.mutationRate) {
                solution.mutate();
            }
        }
        return solutions;
    }
    
    private void updateFitness(List<Solution> solutions) {
        for (Solution s : solutions) {
            if (s.getFitnessChanged()) {
                s.computeUnfeasibleFitness();
            }
        }
    }
    
    /**
     * Select survivors from population and offspring.
     * Implement elitism here.
     * @param offspring
     * @return survivors
     */
    private List<Solution> mutateAndSelectSurvivors(List<Solution> offspring) {
        List<Solution> survivors = new ArrayList<Solution>();
        
        List<Solution> oldGeneration = new ArrayList<Solution>(this.population); // Copy of old pop (copy may be unnecessary)
        // Old pop is already sorted
        List<Solution> elite = new ArrayList<Solution>(oldGeneration.subList(0, Params.eliteSize));
        survivors.addAll(elite);
        
        mutate(offspring);
        survivors.addAll(offspring);
        updateFitness(offspring);
        
        List<Solution> notElite = new ArrayList<Solution>(oldGeneration.subList(Params.eliteSize, oldGeneration.size()));
        mutate(notElite);
        updateFitness(notElite);
        // Possible to mutate elite and add copy
        for (Solution s : elite) {
            if (this.rand.nextDouble() < Params.mutationRate) {
                Solution mutatedCopy = s.copy();
                mutatedCopy.mutate();
                mutatedCopy.computeUnfeasibleFitness();
                notElite.add(mutatedCopy);
            }
        }
        notElite.sort(Comparator.comparingDouble(Solution::getFitness)); // Sorting again, only so the possibly mutated elite is correct
        selectOldSurvivors:
        while (survivors.size() < Params.popSize) {
            for (int c=0; c<notElite.size(); c++) {
                if (this.rand.nextDouble() < Params.bestSurvivorProb) {
                    survivors.add(notElite.remove(c));
                    continue selectOldSurvivors;
                }
            }
            // Iterated through all possible survivors, random was never under bestSurvivorProb
            // Just add and remove the best then.
            survivors.add(notElite.remove(0));
            System.out.println("Seeing this is possible but should be very very rare.");
        }
        survivors.sort(Comparator.comparingDouble(Solution::getFitness));
        return survivors;
    }
    
    public void run() {
        // Prep
        this.population = generatePop(Params.popSize);
        Solution bestSolution = this.population.get(0);
        int lastImprovedGen = -1;
        double bestFitness = bestSolution.getFitness();
        
        RouteDisplayComponent comp = bestSolution.displaySolution();
        // Loop
        while (generation < Params.maxGenerations) {
            updateFitness(this.population);
            List<Solution> parents = parentSelection();
            List<Solution> offspring = crossover(parents);
            // Mutate all survivors, not just offspring.
            this.population = mutateAndSelectSurvivors(offspring);
            
            bestSolution = population.get(0);
            if (bestFitness - bestSolution.getFitness() > 1e-6) {
                // New best is better
                bestFitness = bestSolution.getFitness();
                lastImprovedGen = generation;
            }
            
            // Check if converging, add freshly generated bad solutions to diversify pop
            if (generation - lastImprovedGen > 1000) {
                List<Solution> freshPopulation = new ArrayList<Solution>(this.population.subList(0, Params.popSize / 2));
                freshPopulation.addAll(generatePop(Params.popSize - Params.popSize / 2));
                this.population = freshPopulation;
                lastImprovedGen = generation;
                System.out.println("Generating new random and greedy solutions to diversify population");
            }
            
            if (generation % 100 == 0) {
                System.out.println(String.format("Generation %d\tBest fitness: %.2f\t", generation, bestSolution.getFitness()));
            }
            if (generation % 1000 == 0) {
                bestSolution.updateDisplay(comp);
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
            this.generation++;
            
        }
        
    }
}
