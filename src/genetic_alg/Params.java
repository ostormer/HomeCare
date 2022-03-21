package genetic_alg;

public class Params {
    static final int popSize = 40;
    static final int maxGenerations = 200;
    static final int eliteSize = 4;
    static final int nbrParents = 20;
    static final double bestSelectionWeight = 0.75;
    static final int tournamentSize = 4;
    // Mutations
    static final double mutationRate = 0.2;
    static final double mutationSplitWeight = 1;
    static final double mutationImproveWeight = 2;
    static final double mutationMoveWeight = 1;
    static final double mutationReorderWeight = 2;
}
