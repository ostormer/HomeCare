package genetic_alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GenAlg {
    private int generation = 0;
    private Problem problem;
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
                s.generateRandomGreedy(rand.nextInt(problem.getNbrNurses()-2) + 2); // Between 2 and nbrNurses active nurses
            }
            pop.add(s);
        }
        updateFitness(pop);
        pop.sort(Comparator.comparingDouble(Solution::getFitness));
        return pop;
    }
    
    private List<Solution> parentSelection(List<Solution> population) {
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
                solution.applyRandomMutation();
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
    private List<Solution> mutateAndSelectSurvivors(List<Solution> offspring, List<Solution> population) {
        List<Solution> survivors = new ArrayList<Solution>();
        
        List<Solution> oldGeneration = new ArrayList<Solution>(population); // Copy of old pop (copy may be unnecessary)
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
                mutatedCopy.applyRandomMutation();
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
    
    
    public Solution runGATournament() {
        int round = 0;
        List<List<Solution>> populations = new ArrayList<List<Solution>>();
        // Generate first populations
        for (int i=0; i<Math.pow(2, Params.GATournamentLayers); i++) {
            populations.add(generatePop(Params.popSize));
        }
        int matchNumber = 0;
        while (round < Params.GATournamentLayers) {
            
            List<List<Solution>> nextRoundPopulations = new ArrayList<List<Solution>>();
            int roundGenSize = (int) (Params.firstRoundGenerations * Math.pow(2, round));
            while (populations.size() > 0) {
                List<Solution> p1 = populations.remove(0);
                List<Solution> p2 = populations.remove(0);
                p1 = runGenerationsOnPop(roundGenSize, p1, matchNumber);
                p2 = runGenerationsOnPop(roundGenSize, p2, matchNumber+1);
                matchNumber += 2;
                List<Solution> best = new ArrayList<>();
                best.addAll(p1.subList(0, Params.popSize / 2));
                best.addAll(p2.subList(0, Params.popSize - best.size()));
                nextRoundPopulations.add(best);
            }
            populations = nextRoundPopulations;
            round++;
        }
        System.out.println(populations.size());
        List<Solution> finalPop = populations.get(0);
        finalPop = runGenerationsOnPop((int) (Params.firstRoundGenerations * Math.pow(2, round)), finalPop, matchNumber);
        Solution winner = getBestFeasibleSolution(finalPop);
        winner.displaySolution();
        return winner;
    }
    
    private Solution getBestFeasibleSolution(List<Solution> pop) {
        // Assumes pop is sorted
        for (Solution s : pop) {
            if (s.isFeasible()) {
                return s;
            }
        }
        return null;
    }
    
    public List<Solution> runGenerationsOnPop(int generations, List<Solution> population, int runId) {
        population.sort(Comparator.comparingDouble(Solution::getFitness));
        Solution bestSolution = population.get(0);
        double bestFitness = bestSolution.getFitness();
        Solution bestFeasibleSolution = null;
        double bestFeasibleFitness = Double.MAX_VALUE;
        int lastImprovedGen = -1;
        int lastFreshInjection = 0;
        this.generation = 0;
        
        
        // Loop
        while (generation < generations) {
            updateFitness(population);
            List<Solution> parents = parentSelection(population);
            List<Solution> offspring = crossover(parents);
            // Mutate all survivors, not just offspring.
            population = mutateAndSelectSurvivors(offspring, population);
            // Simple genetic alg complete, rest is checking results or extra stuff
            
            bestSolution = population.get(0);
            if (bestFitness - bestSolution.getFitness() > 1e-6) {
                // New best is better
                bestFitness = bestSolution.getFitness();
                lastImprovedGen = generation;
            }
            
            // Check if converging, add freshly generated bad solutions to diversify pop
            if (generation - lastImprovedGen > 1000 && generation - lastFreshInjection > 1000) {
                List<Solution> freshPopulation = population.subList(0, Params.popSize / 2);
                List<Solution> generatedPopulation = generatePop(Params.popSize - Params.popSize / 2);
                freshPopulation.addAll(generatedPopulation);
                updateFitness(freshPopulation);
                freshPopulation.sort(Comparator.comparingDouble(Solution::getFitness));
                population = freshPopulation;
                lastFreshInjection = generation;
            }
            if (generation - lastImprovedGen > 10000) {
                break;
            }
            
            if (generation % 100 == 0) {
                population.sort(Comparator.comparingDouble(Solution::getFitness));
                for (Solution s : population) {
                    if(s.isFeasible()) {
                        if (bestFeasibleFitness - s.computeFeasibleFitness() > 1e-6) {
                            bestFeasibleFitness = s.computeFeasibleFitness();
                            bestFeasibleSolution = s;
                        }
                        break;
                    }
                }
            }
            if (generation % 1000 == 0) {
                System.out.println(String.format("Generation %d  \t Best fitness: %.2f \t Best feasible fitness: %.2f",
                        generation, bestSolution.getFitness(), bestFeasibleFitness));
            }
            this.generation++;
        } // End loop
        updateFitness(population);
        population.sort(Comparator.comparingDouble(Solution::getFitness));
        // One last check for best feasible solution
        for (Solution s : population) {
            if(s.isFeasible()) {
                if (bestFeasibleFitness - s.computeFeasibleFitness() > 1e-6) {
                    bestFeasibleFitness = s.computeFeasibleFitness();
                    bestFeasibleSolution = s;
                }
                break;
            }
        }
        System.out.println("GenAlg complete");
        bestFeasibleSolution.saveToFile(String.format("out\\best%03d.json", runId));
        return population;
    }
}
