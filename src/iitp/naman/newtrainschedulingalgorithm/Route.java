package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.TrainTime;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Class containing route info for schedule.
 */
public class Route {
    private final Map<String, Station> mapStation;
    private final List<String> stationOrder;
    private final Map<String, Track> mapTrack;
    private final List<String> trackOrder;

    public Route() {
        this.mapStation = new HashMap<>();
        this.stationOrder = new ArrayList<>();
        this.mapTrack = new HashMap<>();
        this.trackOrder = new ArrayList<>();
    }

    public double minimumTimeToCoverTheRoute() {
        double minTime = 0;
        for(String trackKey : this.trackOrder) {
            minTime += (double)(mapTrack.get(trackKey).getLength())/mapTrack.get(trackKey).getMaxSpeedLimit();
        }
        return minTime;
    }

    public double getLengthOfRoute(){
        return Math.abs(mapStation.get(this.stationOrder.get(stationOrder.size()-1)).getDistance() - mapStation.get(this.stationOrder.get(0)).getDistance());
    }

    public double maximumAverageSpeedOfTheRoute(){
        return getLengthOfRoute()/minimumTimeToCoverTheRoute();
    }
    /**
     * Add stations in route.
     *
     * @param stoppageNo            stoppage number.
     * @param id                    station id.
     * @param name                  station name.
     * @param distance              distance between two consecutive station.
     * @param isDirectLineAvailable is direct line available for station.
     * @param noOfUpPlatform        number of up platform for station.
     * @param noOfDownPlatform      number of down platform for station.
     * @param noOfDualPlatform      number of dual platform for station.
     * @param noOfUpTrack           number of up track for station.
     * @param noOfDownTrack         number of down track for station.
     * @param noOfDualTrack         number of dual track for station.
     * @return true if successful.
     */
    public boolean addStation(int stoppageNo, String id, String name, double distance, boolean isDirectLineAvailable,
                              int noOfUpPlatform, int noOfDownPlatform, int noOfDualPlatform, int noOfUpTrack,
                              int noOfDownTrack, int noOfDualTrack) {
        requireNonNull(id, "Station id is null.");
        requireNonNull(name, "Station name is null.");
        this.mapStation.put(id + ":" + stoppageNo, new Station(id, name, distance, isDirectLineAvailable, noOfUpPlatform,
                noOfDownPlatform, noOfDualPlatform, noOfUpTrack, noOfDownTrack, noOfDualTrack));
        return stationOrder.add(id + ":" + stoppageNo);
    }

    public boolean addTrack(int trackNo, Station source, Station destination, String pathMaxSpeedLimit) {
        requireNonNull(source, "source station required for track");
        requireNonNull(destination, "destination station required for track");
        this.mapTrack.put(source.getName()+":"+destination.getName()+":"+trackNo, new Track(source, destination, pathMaxSpeedLimit));
        return this.trackOrder.add(source.getName()+":"+destination.getName()+":"+trackNo);
    }

    /**
     * @return the list of station in route.
     */
    public List<String> getStationList() {
        return new ArrayList<>(this.stationOrder);
    }

    public List<String> getTrackList() {return  new ArrayList<>(this.trackOrder);}
    /**
     * @return number of station in route.
     */
    public int getNumberOfStation() {
        return this.stationOrder.size();
    }
    public int getNumberOfTrack() {return this.trackOrder.size();}

    /**
     * @param id station id.
     * @return station details.
     */
    public Station getStation(String id) {
        requireNonNull(id, "Station id is null.");
        return this.mapStation.getOrDefault(id, null);
    }

    public Track getTrack(int trackNo) {
        return this.mapTrack.get(this.trackOrder.get(trackNo-1));
    }
    /**
     * @param id station id.
     * @return return first matched station in route.
     */
    public Station getFirstMatchedStation(String id) {
        requireNonNull(id, "Station id is null.");
        for (String stId : this.stationOrder) {
            if (stId.split(":")[0].equalsIgnoreCase(id)) {
                return this.mapStation.getOrDefault(stId, null);
            }
        }
        return null;
    }

    /**
     * @param start start time.
     * @param end   end time.
     * @return list of list of free nodes for every station.
     */
    public List<List<Node>> getFreeSlots(TrainTime start, TrainTime end) {
        List<List<Node>> nodes = new ArrayList<>(this.stationOrder.size());
        for (String stationId : this.stationOrder) {
            Station station = this.mapStation.get(stationId);
            if (station == null) {
                throw new RuntimeException("Unable to load station");
            }
            nodes.add(station.getNodesFreeList(start, end));
        }
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        for (Station station : this.mapStation.values()) {
            stringBuilder.append(station.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
