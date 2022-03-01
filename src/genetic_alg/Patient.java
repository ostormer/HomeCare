package genetic_alg;

import java.util.Comparator;

public class Patient {
	private int id;
	private int careTime;
	private int demand;
	private int endTime;
	private int startTime;
	private int xCoord;
	private int yCoord;
	
	public static Comparator<Patient> COMPARE_BY_LATEST_CARE_START = new Comparator<Patient>() {
        // Comparator used for sorting when generating solutions
        public int compare(Patient one, Patient other) {
            Integer timeOne = one.endTime - one.careTime;
            Integer timeOther = other.endTime - other.careTime;
            return timeOne.compareTo(timeOther);
        }
    };

	public Patient(int d, int careTime, int demand, int endTime, int startTime, int xCoord, int yCoord) {
		super();
		this.id = d;
		this.careTime = careTime;
		this.demand = demand;
		this.endTime = endTime;
		this.startTime = startTime;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}

	public int getId() {
		return id;
	}
	public int getCareTime() {
		return careTime;
	}
	public int getDemand() {
		return demand;
	}
	public int getEndTime() {
		return endTime;
	}
	public int getStartTime() {
		return startTime;
	}
	public int getxCoord() {
		return xCoord;
	}
	public int getyCoord() {
		return yCoord;
	}
}
