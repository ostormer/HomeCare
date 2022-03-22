package genetic_alg;

public class Params {
    static final String problemPath = "src\\train\\test_2.json";
    
//    static final int maxGenerations = 20000;
    static final int firstRoundGenerations = 32000;
    static final int GATournamentLayers = 4;
    // Runs maxGenerations * 2 ^ GATournamentLayers first
    // then merges best halves and goes again until one final match is held
    // In total 2^(layers+1)-1 runs of maxGenerations generations
    // popSize >= eliteSize + nbrParents
    static final int popSize = 60;
    static final int eliteSize = 4;
    static final int nbrParents = 40;
    static final double bestSelectionWeight = 0.7;
    static final double bestSurvivorProb = 0.6;
    static final int tournamentSize = 6;
    static final double delayPunishmentFactor = 500;
    static final double overCapacityPunishmentFactor = 500;
    // Mutations
    static final double mutationRate = 0.3;
    static final double mutationImproveWeight = 5;
    static final double mutationSwapWeight = 2;
    static final double mutationSortWeight = 2;
    static final double mutationSplitWeight = 2;
    
    //
}
