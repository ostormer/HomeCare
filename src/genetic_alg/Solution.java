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
    private ArrayList<ArrayList<Patient>> nursePlans; // The actual solution TODO: Test performance of List vs ArrayList
    private List<Integer> activeNurses;
    private Random rand;
    final static double DELAY_FACTOR = 100.;
    final static double OVER_CAPACITY_PUNISHMENT = 1000.;

    public Solution(Problem problem) {
        this.problem = problem;
        this.rand = new Random();
        // Create empty ArrayList of plans.
        this.nursePlans = new ArrayList<ArrayList<Patient>>();
        for (int i = 0; i < this.problem.getNbrNurses(); i++) {
            this.nursePlans.add(new ArrayList<Patient>());
        }
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
    }
    
    public void generateRandomSorted() {
        // This generator does not care about travel times in any way,
        // but focuses on minimizing delays in the randomly generated plan.
        this.generateRandomUnfeasible();
        // Sort each of the nurse's plans after each patient's latest possible start time
        for (int i = 0; i < this.problem.getNbrNurses(); i++) {
            Collections.sort(this.nursePlans.get(i), Patient.COMPARE_BY_LATEST_CARE_START);
        }
    }
    
    public void generateRandomGreedy(int nbrActiveNurses) {
        List<Integer> range = new ArrayList<>();
        for (int i = 0; i <= this.problem.getNbrNurses(); i++) {
            range.add(i);
        }
        Collections.shuffle(range);
        this.activeNurses = range.subList(0, nbrActiveNurses);
        
        // test clustering to find distinct start points
        List<Patient> unassignedPatients = new LinkedList<Patient>(Arrays.asList(this.problem.getPatients()));
        // Assign a random patient to each nurse
        // TODO: Try clustering for better initial patient spread
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
    }
    /**
     * Insert patient into route where it increases travel time the least.
     * Does not take possible care time window into account
     * @param patient: Patient to insert. Assumes patient is not in any route
     */
    public void insertInBestRoute(Patient patient) {
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
    
    /** 
     * Insert point into best route selected by choosing route containing closest neighbor of patient.
     * Not necessarily best route, but slightly faster than insertInBestRoute()
     * @param patient
     */
    /*
    public void insertInBestRouteInferior(Patient patient) {
        // Assumes patient is not in any route and needs to be inserted
        // Does not necessarily insert into best route, as it only looks at distance to one patient at a time
        // First find closest neighbor of patient
        int closestNeighbor = -1;
        double minDist = Double.MAX_VALUE;
        int closestNeighborNurse = -1;
        int closestNeighborStop = -1;
        for (int nurseIndex=0; nurseIndex<nursePlans.size(); nurseIndex++) {
            for (int stopIndex=0; stopIndex<nursePlans.get(nurseIndex).size(); stopIndex++) {
                int neighborId = nursePlans.get(nurseIndex).get(stopIndex).getId();
                // Check distance, compare to minimum
                if (this.problem.getTravelTimes()[patient.getId()][neighborId] < minDist) {
                    minDist = this.problem.getTravelTimes()[patient.getId()][neighborId];
                    closestNeighbor = neighborId;
                    closestNeighborNurse = nurseIndex;
                    closestNeighborStop = stopIndex;
                }
            }
        }
        // Check if depot is closer
        if (this.problem.getTravelTimes()[patient.getId()][0] < minDist) {
            int nurseIndexInsert = -1;
            int stopIndexInsert = -1;
            double smallestDistIncrease = Double.MAX_VALUE;
            for (int nurseIndex=0; nurseIndex<nursePlans.size(); nurseIndex++) {
                if (nursePlans.get(nurseIndex).size() > 0) {
                    // Check if inserting at start of route is better
                    
                }
            }
        }
        // Decide whether to place before or after
        if (closestNeighborStop == 0) {
            // Depot is closest, place
        }
        double distBefore = 
                this.problem.getTravelTimes()[0][0]; // TODO: Fix
        
    }
    */
    @SuppressWarnings("unchecked")
    public Solution[] crossoverGreedyInsertion(Solution other) {
        Solution[] offspring = new Solution[2];
        // TODO: Crossover as described at end of Visma lecture notes
        int selfNurseIndex = this.activeNurses.get(rand.nextInt(this.activeNurses.size()));
        ArrayList<Patient> selfPatients = (ArrayList<Patient>) this.nursePlans.get(selfNurseIndex).clone();
        int otherNurseIndex = other.activeNurses.get(rand.nextInt(other.activeNurses.size()));
        ArrayList<Patient> otherPatients = (ArrayList<Patient>) other.nursePlans.get(otherNurseIndex).clone();
        // Remove patients in selfPatients from other's nursePlans
        thisPatientsLoop:
        for (Patient patient : selfPatients) {
            for (ArrayList<Patient> nursePlan : other.nursePlans ) {
                for (int i=0; i<nursePlan.size(); i++) {
                    if (nursePlan.get(i) == patient) {
                        // Found match! Remove it and continue
                        nursePlan.remove(i);
                        continue thisPatientsLoop;
                    }
                }
            }
        }
        // Remove patients in otherPatients from self's nursePlans
        otherPatientsLoop:
        for (Patient patient : otherPatients) {
            for (ArrayList<Patient> nursePlan : this.nursePlans ) {
                for (int i=0; i<nursePlan.size(); i++) {
                    if (nursePlan.get(i) == patient) {
                        // Found match! Remove it and continue
                        nursePlan.remove(i);
                        continue otherPatientsLoop;
                    }
                }
            }
        }
        // Add to offspring:
        this.clon
        return offspring;
    }
    
    public void mutateImproveOnePatient() {
        
        int improveId = rand.nextInt(this.problem.getPatients().length);
        // Find chosen patient and replace it
        for (ArrayList<Patient> plan : this.nursePlans) {
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
    }
    
    public void mutateSwapOnePatient() { // Dumb mutation
        // TODO: Write more mutations, ones that greedily improve solution in random ways
        int from = rand.nextInt(this.problem.getNbrNurses());
        while (this.nursePlans.get(from).size() == 0) { // Select again if plan of nurse is empty
            from = rand.nextInt(this.problem.getNbrNurses());
        }
        int to = rand.nextInt(this.problem.getNbrNurses());
        int patientFromIndex = rand.nextInt(this.nursePlans.get(from).size());
        int patientToIndex = rand.nextInt(this.nursePlans.get(to).size());
        Patient movePatient = this.nursePlans.get(from).remove(patientFromIndex);
        this.nursePlans.get(to).add(patientToIndex, movePatient);
        
    }
    
    public String toStringRepresentation() {
        String out = "[\n";
        for (int nurseIndex=0; nurseIndex<this.nursePlans.size(); nurseIndex++) {
            ArrayList<Patient> nursePlan = this.nursePlans.get(nurseIndex);
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
        for (ArrayList<Patient> nursePlan : this.nursePlans) {
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
    
    public double computeFeasibleUtility() {
        // Assumes solution is already confirmed as feasible
        double travelTimeTotal = 0;
        // Iterate through all nurses
        for (ArrayList<Patient> nursePlan : this.nursePlans) {
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
    
    public double computeUnfeasibleUtility() {
        double travelTimeTotal = 0;
        int delayTotal = 0;
        // Iterate through all nurses
        for (ArrayList<Patient> nursePlan : this.nursePlans) {
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
                System.out.println("Nurse over capacity");
                // TODO: Add some extra punishment? or remove this check
                delayTotal += OVER_CAPACITY_PUNISHMENT;
            }
            if (currentTime > this.problem.getReturnTime()) {
                delayTotal += currentTime - this.problem.getReturnTime();
            }
        }
        System.out.println("Total delay:"); // TODO: Remove print
        System.out.println(delayTotal); // TODO: Remove print
        return travelTimeTotal + (double) delayTotal * DELAY_FACTOR;
    }
    
    public Solution copy() {
        Solution child = new Solution(this.problem);
        
        child.activeNurses = new LinkedList<Integer>(this.activeNurses);
        child.nursePlans = new ArrayList<ArrayList<Patient>>();
    }

    public ArrayList<ArrayList<Patient>> getNursePlans() {
        return nursePlans;
    }
    
    public List<Integer> getActiveNurses() {
        return activeNurses;
    }
    
}
