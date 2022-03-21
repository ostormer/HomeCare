package genetic_alg;

public class Params {
    static final int maxGenerations = 20000;
    // popSize >= eliteSize + nbrParents
    static final int popSize = 100;
    static final int eliteSize = 4;
    static final int nbrParents = 60;
    static final double bestSelectionWeight = 0.5;
    static final double bestSurvivorProb = 0.7;
    static final int tournamentSize = 6;
    static final double delayPunishmentFactor = 500;
    static final double overCapacityPunishmentFactor = 500;
    // Mutations
    static final double mutationRate = 0.3;
    static final double mutationImproveWeight = 5;
    static final double mutationSwapWeight = 5;
    static final double mutationSortWeight = 4;
    static final double mutationSplitWeight = 2;
    
    //
}
