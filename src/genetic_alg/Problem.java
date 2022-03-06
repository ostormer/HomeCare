package genetic_alg;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

public class Problem {
	    private String instanceName;
	private int nbrNurses;
	private int capacityNurse;
	private double benchmark;
	private int xCoordDepot;
	private int yCoordDepot;
	private int maxXCoord = Integer.MIN_VALUE;
	private int maxYCoord = Integer.MIN_VALUE;
	private int returnTime;
	private Patient[] patients;
	// private ArrayList<ArrayList<Double>> travelTimes;
	private Double[][] travelTimes;
	
	@SuppressWarnings("unchecked")
	public Problem(String path) {
	    System.out.println("Print this");
		try {
		    // create Gson instance
			Gson gson = new Gson();
		    // create a reader
		    Reader reader = Files.newBufferedReader(Paths.get(path));
		    // convert JSON file to map
		    Map<?, ?> map = gson.fromJson(reader, Map.class);
		    // Read problem values
		    this.instanceName = (String) map.get("instance_name");
		    Double nbrNursesRead = (Double) map.get("nbr_nurses");
		    this.nbrNurses = nbrNursesRead.intValue();
		    Double capacityNurseRead = (Double) map.get("capacity_nurse");
		    this.capacityNurse = capacityNurseRead.intValue();
		    this.benchmark = (double) map.get("benchmark");
		    // Read depot data
		    Map<String, Double> depotMap = (Map<String, Double>) map.get("depot");
		    Double xCoordDepot = (Double) depotMap.get("x_coord");
		    this.xCoordDepot = xCoordDepot.intValue();
		    Double yCoordDepot = (Double) depotMap.get("y_coord");
            this.yCoordDepot = yCoordDepot.intValue();
            Double returnTime = (Double) depotMap.get("return_time");
            this.returnTime = returnTime.intValue();
            
		    // Read patient info
		    Map<String, Map<String, Double>> patientMaps = (Map<String, Map<String, Double>>) map.get("patients");
		    this.patients = new Patient[patientMaps.size()];
		    for (Entry<String, Map<String, Double>> patientEntry : patientMaps.entrySet()) {
		        Map<String, Double> patientMap = (Map<String, Double>) patientEntry.getValue();
		        Double careTime = patientMap.get("care_time");
		        Double demand = patientMap.get("demand");
		        Double endTime = patientMap.get("end_time");
		        Double startTime = patientMap.get("start_time");
		        Double xCoord = patientMap.get("x_coord");
		        if (xCoord > this.maxXCoord) {
		            this.maxXCoord = xCoord.intValue();
		        }
		        Double yCoord = patientMap.get("y_coord");
		        if (yCoord > this.maxYCoord) {
		            this.maxYCoord = yCoord.intValue();
		        }
		        Patient patient = new Patient(
		                Integer.parseInt(patientEntry.getKey()),
		                careTime.intValue(),
		                demand.intValue(),
		                endTime.intValue(),
		                startTime.intValue(),
		                xCoord.intValue(),
		                yCoord.intValue());
		        // Index in patient array is id - 1.
		        // Index in travel time matrix is id because the travel time matrix has the depot
		        this.patients[Integer.parseInt(patientEntry.getKey()) - 1] = patient;
		    }
		    // Read travel times matrix
            ArrayList<ArrayList<Double>> travelTimesArrayList = (ArrayList<ArrayList<Double>>) map.get("travel_times");
		    // Convert from ArrayList to Array for faster lookup
		    this.travelTimes = new Double[travelTimesArrayList.size()][];
		    for (int i = 0; i < travelTimesArrayList.size(); i++) {
		        ArrayList<Double> row = travelTimesArrayList.get(i);
		        this.travelTimes[i] = row.toArray(new Double[row.size()]);
		    }
		    // close reader
		    reader.close();

		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

	public String getInstanceName() {
        return instanceName;
    }
    public int getNbrNurses() {
        return nbrNurses;
    }
    public int getCapacityNurse() {
        return capacityNurse;
    }
    public double getBenchmark() {
        return benchmark;
    }
    public Patient[] getPatients() {
        return patients;
    }
    public int getxCoordDepot() {
        return xCoordDepot;
    }
    public int getyCoordDepot() {
        return yCoordDepot;
    }
    public int getMaxXCoord() {
        return maxXCoord;
    }
    public int getMaxYCoord() {
        return maxYCoord;
    }
    public int getReturnTime() {
        return returnTime;
    }
    public Double[][] getTravelTimes() {
        return travelTimes;
    }
}
