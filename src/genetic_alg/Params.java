package genetic_alg;

public class Params {
    static final int maxGenerations = 5000;
    // popSize >= eliteSize + nbrParents
    static final int popSize = 40;
    static final int eliteSize = 4;
    static final int nbrParents = 20;
    static final double bestSelectionWeight = 0.75;
    static final double bestSurvivorProb = 0.8;
    static final int tournamentSize = 4;
    // Mutations
    static final double mutationRate = 0.2;
    static final double mutationImproveWeight = 10;
    static final double mutationSwapWeight = 5;
    static final double mutationSplitWeight = 1;
    
    
    //
    static final int nbrOldSurvivors = popSize - eliteSize - nbrParents;
}
