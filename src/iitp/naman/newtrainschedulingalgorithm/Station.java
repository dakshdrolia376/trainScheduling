package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Class to store station details.
 */
public class Station {
    private final String name;
    private final String id;
    private final double distance;
    private final boolean isDirectLineAvailable;
    private final int noOfUpPlatform;
    private final int noOfDownPlatform;
    private final int noOfDualPlatform;
    private final int noOfUpTrack;
    private final int noOfDownTrack;
    private final int noOfDualTrack;
    private final List<TrainAtStation> arrDeptSchedule;

    public Station(String id, String name, double distance, boolean isDirectLineAvailable, int noOfUpPlatform,
                   int noOfDownPlatform, int noOfDualPlatform, int noOfUpTrack, int noOfDownTrack, int noOfDualTrack) {
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        this.arrDeptSchedule = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.isDirectLineAvailable = isDirectLineAvailable;
        this.noOfUpPlatform = noOfUpPlatform;
        this.noOfDownPlatform = noOfDownPlatform;
        this.noOfDualPlatform = noOfDualPlatform;
        this.noOfUpTrack = noOfUpTrack;
        this.noOfDownTrack = noOfDownTrack;
        this.noOfDualTrack = noOfDualTrack;
    }

    /**
     * @return station id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return station name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return distance between this & next station.
     */
    public double getDistance() {
        return this.distance;
    }

    /**
     * @return true if direct line available.
     */
    public boolean isDirectLineAvailable() {
        return this.isDirectLineAvailable;
    }

    /**
     * @return number of up platforms.
     */
    public int getNoOfUpPlatform() {
        return this.noOfUpPlatform;
    }

    /**
     * @return number of down platforms.
     */
    public int getNoOfDownPlatform() {
        return this.noOfDownPlatform;
    }

    /**
     * @return number of dual platforms.
     */
    public int getNoOfDualPlatform() {
        return this.noOfDualPlatform;
    }

    /**
     * @return number of up tracks.
     */
    public int getNoOfUpTrack() {
        return this.noOfUpTrack;
    }

    /**
     * @return number of down tracks.
     */
    public int getNoOfDownTrack() {
        return this.noOfDownTrack;
    }

    /**
     * @return number of dual tracks.
     */
    public int getNoOfDualTrack() {
        return this.noOfDualTrack;
    }

    /**
     * Adds a train stoppage.
     *
     * @param TrainAtStation train stopage details.
     * @return true if successful.
     */
    public boolean addTrain(TrainAtStation TrainAtStation) {
        return this.arrDeptSchedule.add(TrainAtStation);
    }

    /**
     * Sorts current station stoppage schedule according to arrival time of trains.
     */
    public void sortArr() {
        this.arrDeptSchedule.sort((o1, o2) -> {
            if (o1.getArr().equals(o2.getArr())) {
                return o1.getDept().compareTo(o2.getDept());
            }
            return o1.getArr().compareTo(o2.getArr());
        });
    }

    /**
     * @param startTime start time.
     * @param endTime   end time.
     * @return list of nodes containing 1 node for every minutes.
     */
    public List<Node> getNodesFreeList(TrainTime startTime, TrainTime endTime) {
        List<Node> nextWeekNodes = new ArrayList<>();
        if (endTime.compareTo(startTime) < 0) {
            nextWeekNodes = getNodesFreeList(new TrainTime(0, 0, 0), endTime);
            endTime = new TrainTime(6, 23, 59);
        }
        List<Node> stationNodes = new ArrayList<>();
        TrainTime slotDept = new TrainTime(startTime);
        while (slotDept.compareTo(endTime) < 0) {
            stationNodes.add(new Node(slotDept, this.id));
            slotDept.addMinutes(1);
        }
        stationNodes.add(new Node(slotDept, this.id));
        stationNodes.addAll(nextWeekNodes);
        return stationNodes;
    }

    @Override
    public String toString() {
        this.sortArr();
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("Station id: ");
        stringBuilder.append(this.id);
        stringBuilder.append(" Name: ");
        stringBuilder.append(this.name);
        stringBuilder.append(" Distance: ");
        stringBuilder.append(this.distance);
        stringBuilder.append(" No of trains passing: ");
        stringBuilder.append(this.arrDeptSchedule.size());
        stringBuilder.append('\n');
        stringBuilder.append("DirectLine: ");
        stringBuilder.append(this.isDirectLineAvailable);
        stringBuilder.append(" Platform Up No: ");
        stringBuilder.append(this.noOfUpPlatform);
        stringBuilder.append(" Platform Down No: ");
        stringBuilder.append(this.noOfDownPlatform);
        stringBuilder.append(" Platform Dual No: ");
        stringBuilder.append(this.noOfDownPlatform);
        stringBuilder.append('\n');
        stringBuilder.append("Track Up No: ");
        stringBuilder.append(this.noOfUpTrack);
        stringBuilder.append(" Track Down No: ");
        stringBuilder.append(this.noOfDownTrack);
        stringBuilder.append(" Track Dual No: ");
        stringBuilder.append(this.noOfDualTrack);
        stringBuilder.append('\n');
        stringBuilder.append("Train\tArrival\tDeparture");
        stringBuilder.append('\n');
        for (TrainAtStation trainAtStation : this.arrDeptSchedule) {
            stringBuilder.append(trainAtStation.getTrainNo());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getArr());
            stringBuilder.append('\t');
            stringBuilder.append(trainAtStation.getDept());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
