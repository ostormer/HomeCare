package genetic_alg;

import java.util.ArrayList;
import java.util.Random;

public class Solution {
    private Problem problem;
    private ArrayList<ArrayList<Patient>> nursePlans; // The actual solution
    private static double DELAY_FACTOR = 100.;

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
                // Add some extra punishment?
            }
        }
        return travelTimeTotal + (double) delayTotal * DELAY_FACTOR;
    }
}
