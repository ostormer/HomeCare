package genetic_alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
        oldGeneration.sort(Comparator.comparingDouble(Solution::getFitness)); // Sort it
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
            System.out.println("Seeing this is possible but should be very rare.");
        }
        
        return survivors;
    }
    
    public void run() {
        // Prep
        generatePop();
        this.population.sort(Comparator.comparingDouble(Solution::getFitness));
        Solution bestSolution = this.population.get(0);
        RouteDisplayComponent comp = bestSolution.displaySolution();
        // Loop
        while (generation < Params.maxGenerations) {
            updateFitness(this.population);
            List<Solution> parents = parentSelection();
            List<Solution> offspring = crossover(parents);
            // Mutate all survivors, not just offspring.
            List<Solution> newPopulation = mutateAndSelectSurvivors(offspring);
            this.population = newPopulation;
            bestSolution = population.get(0);
            if (generation % 10 == 0) {
                System.out.println(String.format("Generation %d\tBest fitness: %.2f\t", generation, bestSolution.getFitness()));
            }
            if (generation % 100 == 0) {
                bestSolution.updateDisplay(comp);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            this.generation++;
            
        }
        
    }
}
