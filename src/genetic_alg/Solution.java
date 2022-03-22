package genetic_alg;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JFrame;

public class Solution {
    private Problem problem;
    private List<List<Patient>> nursePlans;
    private List<Integer> activeNurses;
    private Random rand;
    private double fitness;
    private boolean fitnessChanged;

    private static ArrayList<Double> mutationWeights = computeMutationWeights();
    
    public Solution(Problem problem) {
        this.problem = problem;
        this.rand = new Random();
        // Create empty ArrayList of plans.
        this.nursePlans = new ArrayList<List<Patient>>();
        for (int i = 0; i < this.problem.getNbrNurses(); i++) {
            this.nursePlans.add(new ArrayList<Patient>());
        }
        fitnessChanged = true;
    }
    
    private static ArrayList<Double> computeMutationWeights(){
        ArrayList<Double> mw = new ArrayList<Double>();
        mw.add(Params.mutationImproveWeight);
        mw.add(mw.get(mw.size()-1) + Params.mutationSwapWeight);
        mw.add(mw.get(mw.size()-1) + Params.mutationSplitWeight);
        mw.add(mw.get(mw.size()-1) + Params.mutationSortWeight);
        return mw;
    }
    
    public RouteDisplayComponent displaySolution() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame f = new JFrame("Route Display");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(new Dimension((problem.getMaxXCoord() + 10) * RouteDisplayComponent.DRAW_FACTOR,
                (problem.getMaxYCoord() + 10) * RouteDisplayComponent.DRAW_FACTOR));
        final RouteDisplayComponent comp = new RouteDisplayComponent(this.problem);
        f.add(comp);
        comp.displaySolution(this);
        comp.setPreferredSize(new Dimension(problem.getMaxXCoord() * RouteDisplayComponent.DRAW_FACTOR,
                problem.getMaxYCoord() * RouteDisplayComponent.DRAW_FACTOR));
        f.setVisible(true);
        return comp;
    }
    
    public void updateDisplay(RouteDisplayComponent comp) {
        comp.clearLines();
        comp.displaySolution(this);
    }
    
    public void generateRandomUnfeasible() {
        
        for (Patient patient : this.problem.getPatients()) {
            // Assign patient to random Nurse completely at random
            this.nursePlans.get(rand.nextInt(this.nursePlans.size())).add(patient);
        }
        this.activeNurses = IntStream.rangeClosed(0, this.problem.getNbrNurses()-1)
                .boxed().collect(Collectors.toList());
        this.computeUnfeasibleFitness();
        this.fitnessChanged = false;
    }
    
    public void generateRandomSorted() {
        // This generator does not care about travel times in any way,
        // but focuses on minimizing delays in the randomly generated plan.
        this.generateRandomUnfeasible();
        // Sort each of the nurse's plans after each patient's latest possible start time
        for (int i = 0; i < this.problem.getNbrNurses(); i++) {
            Collections.sort(this.nursePlans.get(i), Patient.COMPARE_BY_LATEST_CARE_START);
        }
        this.computeUnfeasibleFitness();
        this.fitnessChanged = false;
    }
    
    public void generateRandomGreedy(int nbrActiveNurses) {
        List<Integer> range = new ArrayList<>();
        for (int i = 0; i < this.problem.getNbrNurses(); i++) {
            range.add(i);
        }
        Collections.shuffle(range);
        this.activeNurses = new ArrayList<Integer>(range.subList(0, nbrActiveNurses));
        
        List<Patient> unassignedPatients = new LinkedList<Patient>(Arrays.asList(this.problem.getPatients()));
        // Assign a random patient to each nurse
        for (Integer nurseIndex : this.activeNurses) {
            // Select random patient from unassigned patient list.
            int assignPatientIndex = rand.nextInt(unassignedPatients.size() - 1);
            // Remove it from list of unassigned patients and assign it to a nurse.
            Patient assignPatient = unassignedPatients.remove(assignPatientIndex);
            this.nursePlans.get(nurseIndex).add(assignPatient);
        }
        // assign rest of patients by greedily adding them to routes in a random order
        Collections.shuffle(unassignedPatients);
        while (unassignedPatients.size() > 0) {
            Patient assignPatient = unassignedPatients.remove(0);
            this.insertInBestRoute(assignPatient);
        }
        this.computeUnfeasibleFitness();
        this.fitnessChanged = false;
    }
    /**
     * Insert patient into route where it increases travel time the least.
     * Does not take possible care time window into account
     * @param patient: Patient to insert. Assumes patient is not in any route
     */
    private void insertInBestRoute(Patient patient) {
        double minIncrease = Double.MAX_VALUE;
        int nurseIndexInsert = -1;
        int stopIndexInsert = -1;
        // Search for smallest increase
        for (int nurseIndex : this.activeNurses) {
            // For each nurse's plan
            // If plan empty
            if (nursePlans.get(nurseIndex).size() == 0) {
                double increase = this.problem.getTravelTimes()[0][patient.getId()] + 
                        this.problem.getTravelTimes()[patient.getId()][0];
                
                if (increase < minIncrease) { // Compare increase
                    minIncrease = increase;
                    nurseIndexInsert = nurseIndex;
                    stopIndexInsert = 0;
                }
            }
            // Plan not empty
            else {
                // First check adding it at start of route
                double increase = this.problem.getTravelTimes()[0][patient.getId()]
                                + this.problem.getTravelTimes()[patient.getId()][nursePlans.get(nurseIndex).get(0).getId()]
                                - this.problem.getTravelTimes()[0][nursePlans.get(nurseIndex).get(0).getId()];
                
                if (increase < minIncrease) { // Compare increase
                    minIncrease = increase;
                    nurseIndexInsert = nurseIndex;
                    stopIndexInsert = 0;
                }
                for (int stopIndex=0; stopIndex<nursePlans.get(nurseIndex).size()-1; stopIndex++) {
                    // Calculate of travel time by adding patient after stopIndex
                    int idBefore = nursePlans.get(nurseIndex).get(stopIndex).getId();
                    int idAfter = nursePlans.get(nurseIndex).get(stopIndex + 1).getId();
                    increase = this.problem.getTravelTimes()[idBefore][patient.getId()]
                             + this.problem.getTravelTimes()[patient.getId()][idAfter]
                             - this.problem.getTravelTimes()[idBefore][idAfter];
                    
                    if (increase < minIncrease) { // Compare increase
                        minIncrease = increase;
                        nurseIndexInsert = nurseIndex;
                        stopIndexInsert = stopIndex + 1;
                    }
                }
                // Check adding it at end of route
                increase = this.problem.getTravelTimes()[nursePlans.get(nurseIndex).get(nursePlans.get(nurseIndex).size()-1).getId()][patient.getId()]
                         + this.problem.getTravelTimes()[patient.getId()][0]
                         - this.problem.getTravelTimes()[nursePlans.get(nurseIndex).get(nursePlans.get(nurseIndex).size()-1).getId()][0];
                
                if (increase < minIncrease) { // Compare increase
                    minIncrease = increase;
                    nurseIndexInsert = nurseIndex;
                    stopIndexInsert = nursePlans.get(nurseIndex).size();
                }
            } // End if plan not empty
        } // End for each nursePlan
        // Insert patient in best position of best route
        nursePlans.get(nurseIndexInsert).add(stopIndexInsert, patient);
    }
    
    public Solution[] crossoverGreedyInsertion(Solution other) {
        // Crossover as described at end of Visma lecture notes
        Solution[] offspring = new Solution[2];
        
        int selfNurseIndex = this.activeNurses.get(rand.nextInt(this.activeNurses.size()));
        List<Patient> selfPatients = new ArrayList<Patient>(this.nursePlans.get(selfNurseIndex));
        int otherNurseIndex = other.activeNurses.get(rand.nextInt(other.activeNurses.size()));
        List<Patient> otherPatients = new ArrayList<Patient>(other.nursePlans.get(otherNurseIndex));
        // Remove patients in selfPatients from other's nursePlans
        Solution thisChild = this.copy();
        Solution otherChild = other.copy();
        thisPatientsLoop:
        for (Patient patientToRemove : selfPatients) {
            for (List<Patient> nursePlan : otherChild.nursePlans ) {
                for (int i=0; i<nursePlan.size(); i++) {
                    if (nursePlan.get(i) == patientToRemove) {
                        // Found match! Remove it and continue
                        nursePlan.remove(i);
                        continue thisPatientsLoop;
                    }
                }
            }
        }
        // Remove patients in otherPatients from self's nursePlans
        otherPatientsLoop:
        for (Patient patientToRemove : otherPatients) {
            for (List<Patient> nursePlan : thisChild.nursePlans ) {
                for (int i=0; i<nursePlan.size(); i++) {
                    if (nursePlan.get(i) == patientToRemove) {
                        // Found match! Remove it and continue
                        nursePlan.remove(i);
                        continue otherPatientsLoop;
                    }
                }
            }
        }
        // Loop through patients removed from other and re-insert them
        for (Patient removedPatient : selfPatients) {
            otherChild.insertInBestRoute(removedPatient);
        }
        for (Patient removedPatient : otherPatients) {
            thisChild.insertInBestRoute(removedPatient);
        }
        // Mark that they need their fitness computed again.
        // Do not do it yet, as they may still mutate before fitness is needed
        thisChild.fitnessChanged = true;
        otherChild.fitnessChanged = true;
        // Add to offspring:
        offspring[0] = thisChild;
        offspring[1] = otherChild;
        return offspring;
    }
    
    public void mutateImproveOnePatient() {
        
        int improveId = rand.nextInt(this.problem.getPatients().length);
        // Find chosen patient and replace it
        for (List<Patient> plan : this.nursePlans) {
            for (int stop=0; stop<plan.size(); stop++) {
                Patient patient = plan.get(stop);
                if (patient.getId() - 1 == improveId) {
                    plan.remove(stop);
                    insertInBestRoute(patient);
                    return;
                }
            }
        }
        System.out.println("Did not find patient to improve in solution. Something is wrong.");
        this.fitnessChanged = true;
    }
    
    public void mutateSwapOnePatient() { // Dumb mutation
        int to = rand.nextInt(this.problem.getNbrNurses());
        int from = rand.nextInt(this.problem.getNbrNurses());
        while (this.nursePlans.get(from).size() == 0 || to == from) { // Select again if plan of nurse is empty
            from = rand.nextInt(this.problem.getNbrNurses());
        }
        int patientFromIndex = rand.nextInt(this.nursePlans.get(from).size());
        int patientToIndex = rand.nextInt(this.nursePlans.get(to).size()+1);
        Patient movePatient = this.nursePlans.get(from).remove(patientFromIndex);
        this.nursePlans.get(to).add(patientToIndex, movePatient);
        if (this.nursePlans.get(from).size() == 0) {
            this.activeNurses.remove(Integer.valueOf(from)); // Removes occurence of from, not index from
        }
        if (! this.activeNurses.contains(to)) {
            this.activeNurses.add(to);
        }
        this.fitnessChanged = true;
    }
    
    public void mutateSplitOneNursePlan() {
        // Mutation splitting one nursePlan into two shorter ones
        // Find random plan that is at least length 2
        int nurseIndex = this.rand.nextInt(this.activeNurses.size());
        while (this.nursePlans.get(nurseIndex).size() < 2) {
            nurseIndex = this.rand.nextInt(this.activeNurses.size());
        }
        LinkedList<Integer> emptyPlans = new LinkedList<Integer>();
        for (int i=0; i<problem.getNbrNurses(); i++) {
            if(nursePlans.get(i).size() == 0) {
                emptyPlans.add(i);
            }
        }
        if (emptyPlans.size() == 0) {
            // No empty plans, don't mutate
            return;
        }
        int targetIndex = emptyPlans.get(this.rand.nextInt(emptyPlans.size()));
        int splitIndex = this.rand.nextInt(this.nursePlans.get(nurseIndex).size() - 1) + 1;
        // split-index first patients are removed and added to target
        for (int i=0; i<splitIndex; i++) {
            this.nursePlans.get(targetIndex).add(this.nursePlans.get(nurseIndex).remove(0));
        }
        if (! this.activeNurses.contains(targetIndex)) {
            this.activeNurses.add(targetIndex);
        }
        this.fitnessChanged = true;
    }
    
    public void mutateSortOneNursePlan() {
        // Find one random nursePlan larger than 2
        int nurseIndex = this.rand.nextInt(this.activeNurses.size());
        while (this.nursePlans.get(nurseIndex).size() < 2) {
            nurseIndex = this.rand.nextInt(this.activeNurses.size());
        }
        List<Patient> sortPlan = this.nursePlans.get(nurseIndex);
        Collections.sort(sortPlan, Patient.COMPARE_BY_LATEST_CARE_START);
        
        this.fitnessChanged = true;
    }
    
    public void applyRandomMutation() {
        double mutationNumber = this.rand.nextDouble() * Solution.mutationWeights.get(Solution.mutationWeights.size()-1);
        int mutationId = -1;
        for (int i=0; i<Solution.mutationWeights.size(); i++) {
            if (mutationNumber < Solution.mutationWeights.get(i)) {
                mutationId = i;
                break;
            }
        }
        switch (mutationId) {
        case 0: // Improve
            this.mutateImproveOnePatient();
            break;
        case 1: // Swap
            this.mutateSwapOnePatient();
            break;
        case 2: // Split
            this.mutateSplitOneNursePlan();
            break;
        case 3: // Sort one by time
            this.mutateSortOneNursePlan();
            break;
        }

    }
    
    public String toStringRepresentation() {
        String out = "[\n";
        for (int nurseIndex=0; nurseIndex<this.nursePlans.size(); nurseIndex++) {
            List<Patient> nursePlan = this.nursePlans.get(nurseIndex);
            out += "[ ";
            for (int stopIndex=0; stopIndex<nursePlan.size(); stopIndex++) {
                out += String.valueOf(nursePlan.get(stopIndex).getId());
                if (stopIndex < nursePlan.size() - 1) {
                    out += ", ";
                }
            }
            out += "]";
            if (nurseIndex < this.nursePlans.size() - 1) {
                out += ",\n";
            }
        }
        
        return out + "]";
    }
    
    public boolean isFeasible() {
        for (List<Patient> nursePlan : this.nursePlans) {
            int currentTime = 0;
            int currentLocation = 0; // Start at depot
            int usedCapacity = 0;
            // Iterate through all patients on current nurse's plan
            for (Patient patient : nursePlan) {
                // Travel to patient
                currentTime += this.problem.getTravelTimes()[currentLocation][patient.getId()];
                currentLocation = patient.getId();
                // Possibly wait until patient care window starts or start immediately
                if (currentTime < patient.getStartTime()) {
                    // Wait until care window starts
                    currentTime = patient.getStartTime();
                }
                // Care for patient
                usedCapacity += patient.getDemand();
                currentTime += patient.getCareTime();
                // Punish if care ended too late
                if (currentTime > patient.getEndTime()) {
                    return false; // Not feasible
                }
            } // end for all patients on route
            if (usedCapacity > this.problem.getCapacityNurse()) {
                return false; // Not feasible
            }
            // Travel from last patient to depot
            currentTime += this.problem.getTravelTimes()[currentLocation][0];
            if (currentTime > this.problem.getReturnTime()) {
                return false;
            }
        } // end for all nursePlans
        // Got through all nurses' plans without detecting anything infeasible
        return true;
    }
    
    public double computeFeasibleFitness() {
        // Assumes solution is already confirmed as feasible
        double travelTimeTotal = 0;
        // Iterate through all nurses
        for (List<Patient> nursePlan : this.nursePlans) {
            int currentTime = 0;
            int currentLocation = 0; // Start at depot
            // Iterate through all patients on current nurse's plan
            for (Patient patient : nursePlan) {
                // Travel to patient
                travelTimeTotal += this.problem.getTravelTimes()[currentLocation][patient.getId()];
                currentTime += this.problem.getTravelTimes()[currentLocation][patient.getId()];
                currentLocation = patient.getId();
                // Possibly wait until patient care window starts or start immediately
                if (currentTime < patient.getStartTime()) {
                    // Wait until care window starts
                    currentTime = patient.getStartTime();
                }
                // Care for patient
                currentTime += patient.getCareTime();
            }
            // Travel from last patient to depot
            travelTimeTotal += this.problem.getTravelTimes()[currentLocation][0];
            currentTime += this.problem.getTravelTimes()[currentLocation][0];
        }
        return travelTimeTotal;
    }
    
    public double computeUnfeasibleFitness() {
        double travelTimeTotal = 0;
        int delayTotal = 0;
        int overCapacityTotal = 0;
        // Iterate through all nurses
        for (List<Patient> nursePlan : this.nursePlans) {
            int currentTime = 0;
            int currentLocation = 0; // Start at depot
            int usedCapacity = 0;
            // Iterate through all patients on current nurse's plan
            for (Patient patient : nursePlan) {
                // Travel to patient
                travelTimeTotal += this.problem.getTravelTimes()[currentLocation][patient.getId()];
                currentTime += this.problem.getTravelTimes()[currentLocation][patient.getId()];
                currentLocation = patient.getId();
                // Possibly wait until patient care window starts or start immediately
                if (currentTime < patient.getStartTime()) {
                    // Wait until care window starts
                    // No extra punishment is given for this. We don't care about nurses waiting
                    currentTime = patient.getStartTime();
                }
                // Care for patient
                usedCapacity += patient.getDemand();
                currentTime += patient.getCareTime();
                // Punish if care ended too late
                if (currentTime > patient.getEndTime()) {
                    delayTotal += currentTime - patient.getEndTime();
                }
            }
            // Travel from last patient to depot
            travelTimeTotal += this.problem.getTravelTimes()[currentLocation][0];
            currentTime += this.problem.getTravelTimes()[currentLocation][0];
            if (usedCapacity > this.problem.getCapacityNurse()) {
                overCapacityTotal += usedCapacity - this.problem.getCapacityNurse();
            }
            if (currentTime > this.problem.getReturnTime()) {
                delayTotal += currentTime - this.problem.getReturnTime();
            }
        }
        this.fitness = travelTimeTotal
                + ((double) delayTotal * Params.delayPunishmentFactor)
                + ((double) overCapacityTotal * Params.overCapacityPunishmentFactor);
        return this.fitness;
    }
    
    public Solution copy() {
        // Return copy of this, but with new lists containing pointers to the same patient objects
        // Could call it a semi-shallow copy.
        Solution child = new Solution(this.problem);
        child.fitness = this.fitness;
        child.fitnessChanged = this.fitnessChanged;
        child.activeNurses = new LinkedList<Integer>(this.activeNurses);
        child.nursePlans = new ArrayList<List<Patient>>();
        for (int i=0; i<this.nursePlans.size(); i++) {
            child.nursePlans.add(new ArrayList<Patient>(this.nursePlans.get(i)));
        }
        return child;
    }
    
    public boolean getFitnessChanged() {
        return fitnessChanged;
    }
    public double getFitness() {
        return fitness;
    }
    public List<List<Patient>> getNursePlans() {
        return nursePlans;
    }
    public List<Integer> getActiveNurses() {
        return activeNurses;
    }
    
}
