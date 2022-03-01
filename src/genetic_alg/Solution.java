package genetic_alg;

import java.util.ArrayList;
import java.util.Random;

public class Solution {
    private Problem problem;
    private ArrayList<ArrayList<Patient>> nursePlans; // The actual solution
    private static double DELAY_FACTOR = 100.;
    private static double OVER_CAPACITY_PUNISHMENT = 1000.;

    public Solution(Problem problem) {
        this.problem = problem;
    }
    
    public void generateRandomUnfeasible() {
        Random rand = new Random();
        this.nursePlans = new ArrayList<ArrayList<Patient>>();
        for (int i = 0; i < this.problem.getNbrNurses(); i++) { // Wow i miss python
            this.nursePlans.add(new ArrayList<Patient>());
        }
        for (Patient patient : this.problem.getPatients()) {
            // Assign patient to random Nurse completely at random
            this.nursePlans.get(rand.nextInt(this.nursePlans.size())).add(patient);
        }
    }
    
    public void generateRandomSorted() {
        this.generateRandomUnfeasible();
        // TODO Sort each of the nurse's plans after each patient's latest possible start time
    }
    
    public String toStringRepresentation() {
        String out = "";
        for (ArrayList<Patient> nursePlan : this.nursePlans) {
            out += "[ ";
            for (Patient patient : nursePlan) {
                out += String.valueOf(patient.getId()) + " ";
            }
            out += "]\n";
        }
        return out;
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
        return travelTimeTotal + (double) delayTotal * DELAY_FACTOR;
    }
}
