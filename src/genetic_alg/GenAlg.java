package genetic_alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenAlg {
    private int popSize;
    private int generation = 0;
    private Problem problem;
    private List<Solution> population; // TODO: divide into feasible and infeasible pops
    private Random rand;
    
    public GenAlg(Problem problem) {
        this.problem = problem;
        this.rand = new Random();
        population = new ArrayList<Solution>();
    }
    
    public void generatePop() {
        // Generates population.
        // Right now all of them as random sorted by time
        for (int i=0; i<popSize; i++) {
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
    
    public ArrayList<Solution> parentSelection() {
        // TODO: Implement elitism
        ArrayList<Solution> parents = new ArrayList<Solution>();
        while (parents.size() < Params.nbrParents) {
            Tournament t = new Tournament(population);
            parents.add(t.run());
        }
        return parents;
    }
    
    public ArrayList<Solution> crossover(ArrayList<Solution> parents) {
        ArrayList<Solution> offspring = new ArrayList<Solution>();
        Collections.shuffle(parents);
        for (int i=0; i<Params.nbrParents; i+=2) {
            Solution[] children = parents.get(i).crossoverGreedyInsertion(parents.get(i+1));
            offspring.add(children[0]);
            offspring.add(children[1]);
        }
        return offspring;
    }
    
    public void run() {
        // Prep
        
        // Loop
        while (generation < Params.maxGenerations) {
            ArrayList<Solution> parents = parentSelection();
            ArrayList<Solution> offspring = crossover(parents);
            
            this.generation++;
        }
    }
}
