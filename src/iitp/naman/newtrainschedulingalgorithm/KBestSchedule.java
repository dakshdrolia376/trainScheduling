package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.datahelper.FetchStationDetails;
import iitp.naman.newtrainschedulingalgorithm.datahelper.RouteHelper;
import iitp.naman.newtrainschedulingalgorithm.datahelper.TrainHelper;
import iitp.naman.newtrainschedulingalgorithm.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class KBestSchedule {
    private Route route;
    private final Map<String, List<Train>> trainMap;
    private List<String> stationList;
    private List<List<Node>> nodes;
    private GraphKBestPath graphKBestPath;
    private long edgeCount;
    private FetchStationDetails fetchStationDetails;

    public KBestSchedule(String pathStationDatabase) {
        this.trainMap = new HashMap<>();
        this.fetchStationDetails = new FetchStationDetails(pathStationDatabase);
    }

    private boolean addRoute(String pathMaxSpeedLimit, List<String> stationIdList, List<String> stationNameList,
                             List<Double> stationDistanceList, List<Boolean> isDirectLineAvailableList,
                             List<Integer> noOfUpPlatformList, List<Integer> noOfDownPlatformList,
                             List<Integer> noOfDualPlatformList, List<Integer> noOfUpTrackList,
                             List<Integer> noOfDownTrackList, List<Integer> noOfDualTrackList) {
        requireNonNull(stationIdList, "Station id list is null.");
        requireNonNull(stationNameList, "Station name list is null.");
        requireNonNull(stationDistanceList, "Station distance list is null.");
        requireNonNull(isDirectLineAvailableList, "Station direct line list is null.");
        requireNonNull(noOfUpPlatformList, "Station no of up platform list is null.");
        requireNonNull(noOfDownPlatformList, "Station no of down platform list is null.");
        requireNonNull(noOfDualPlatformList, "Station no of dual platform list is null.");
        this.route = new Route();

        int sizeStation = stationIdList.size();
        if (stationNameList.size() != sizeStation || stationDistanceList.size() != sizeStation ||
                isDirectLineAvailableList.size() != sizeStation || noOfUpPlatformList.size() != sizeStation ||
                noOfDownPlatformList.size() != sizeStation || noOfDualPlatformList.size() != sizeStation) {
            throw new IllegalArgumentException("Invalid arguments for route");
        }

        // adding station to route
        for (int i = 0; i < sizeStation; i++) {
            if (!this.route.addStation((i + 1), stationIdList.get(i), stationNameList.get(i), stationDistanceList.get(i),
                    isDirectLineAvailableList.get(i), noOfUpPlatformList.get(i), noOfDownPlatformList.get(i),
                    noOfDualPlatformList.get(i), noOfUpTrackList.get(i), noOfDownTrackList.get(i),
                    noOfDualTrackList.get(i))) {
                throw new RuntimeException("Unable to add station to route");
            }
        }

        // new part adding starts here
//        // adding tracks to route
        for(int i = 0; i< sizeStation-1; i++) {
            Station source = new Station(stationIdList.get(i), stationNameList.get(i), stationDistanceList.get(i), isDirectLineAvailableList.get(i),
                    noOfUpPlatformList.get(i), noOfDownPlatformList.get(i), noOfDualPlatformList.get(i), noOfUpTrackList.get(i),
                    noOfDownTrackList.get(i), noOfDualTrackList.get(i));
            Station destination = new Station(stationIdList.get(i+1), stationNameList.get(i+1), stationDistanceList.get(i+1), isDirectLineAvailableList.get(i+1),
                    noOfUpPlatformList.get(i+1), noOfDownPlatformList.get(i+1), noOfDualPlatformList.get(i+1), noOfUpTrackList.get(i+1),
                    noOfDownTrackList.get(i+1), noOfDualTrackList.get(i+1));

            if(!this.route.addTrack(i+1, source, destination, pathMaxSpeedLimit)) {
                throw new RuntimeException("Unable to add tracks to route");
            }
        }
        System.out.println("Route maximum average speed: "+Double.toString(this.route.maximumAverageSpeedOfTheRoute()) + " km/hr");
//            ends here
        return true;
    }

    private boolean addTrainFromFile(int trainNo, String trainName, String pathTrainSchedule, int trainDay) {
        int stoppageDay = trainDay;
        try {
            this.trainMap.putIfAbsent(trainNo + "", new ArrayList<>());
            FileReader fReader = new FileReader(pathTrainSchedule);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            TrainTime arrival, departure = null;
            String stationId;
            String prevStationId = "";
            String data[];
            String data1[];
            Train train = new Train(trainNo, trainName);
            Set<String> stationIdsInTrain = new HashSet<>();
            boolean atLeastOneStationInRoute = false;
            int stoppageNo = 0;
            while ((line = bReader.readLine()) != null) {
                data = line.split("\\s+");
                stationId = StationIdHelper.getStationIdFromName(data[0]);

                int numOfPlatform = fetchStationDetails.getNumberOfPlatform(stationId);
                if (numOfPlatform <= 0) {
                    continue;
                }
                //need to increment if station has platforms
                stoppageNo++;

                Station station = this.route.getFirstMatchedStation(stationId);
                if (station == null || prevStationId.equalsIgnoreCase(stationId)) {
                    prevStationId = stationId;
                    continue;
                }

                if (!stationIdsInTrain.add(stationId)) {
                    this.trainMap.get(trainNo + "").add(train);
                    train = new Train(trainNo, trainName);
                    stationIdsInTrain = new HashSet<>();
                    stationIdsInTrain.add(stationId);
                    Station station1 = this.route.getFirstMatchedStation(prevStationId);
                    if (station1 != null) {
                        stationIdsInTrain.add(prevStationId);
                        if (!train.addStoppage(station1, departure, departure, (stoppageNo - 1))) {
                            System.out.println("Some error occurred");
                            bReader.close();
                            fReader.close();
                            return false;
                        }
                    }
                }

                data1 = data[1].split(":");
                arrival = new TrainTime(stoppageDay, Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if (departure != null && arrival.compareTo(departure) < 0) {
                    arrival.addDay(1);
                    stoppageDay = arrival.getDay();
                }
                data1 = data[2].split(":");
                departure = new TrainTime(stoppageDay, Integer.parseInt(data1[0]), Integer.parseInt(data1[1]));
                if (departure.compareTo(arrival) < 0) {
                    departure.addDay(1);
                    stoppageDay = departure.getDay();
                }
                if (!train.addStoppage(station, arrival, departure, stoppageNo)) {
                    System.out.println("Some error occurred");
                    bReader.close();
                    fReader.close();
                    return false;
                }
                atLeastOneStationInRoute = true;
                prevStationId = stationId;
            }
            this.trainMap.get(trainNo + "").add(train);
            if (!atLeastOneStationInRoute) {
                this.trainMap.remove(trainNo + "");
            }
            bReader.close();
            fReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean addTrainFromFolderSingleDay(String pathOldTrainScheduleFolder, int trainDay) {
        File[] listOfFiles = new File(pathOldTrainScheduleFolder).listFiles();
        if (listOfFiles == null) {
            System.out.println("No old trains found : " + pathOldTrainScheduleFolder);
            return true;
        }

        for (File file : listOfFiles) {
            if (file.isFile()) {
                int trainNo;
                try {
                    trainNo = Integer.parseInt(file.getName().split("\\.")[0]);
                } catch (Exception e) {
                    System.out.print("File name should be train Number.");
                    System.out.println("Skipping file : " + file.getPath());
                    e.printStackTrace();
                    continue;
                }
                if (!addTrainFromFile(trainNo, file.getName(), file.getPath(), trainDay)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean addTrainFromFolder(String pathOldTrainScheduleFolder) {

        return addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                "day0", 0) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day1", 1) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day2", 2) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day3", 3) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day4", 4) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day5", 5) &&
                addTrainFromFolderSingleDay(pathOldTrainScheduleFolder + File.separator +
                        "day6", 6);
    }

    private void getNodesFreeSlot() {

        this.nodes = this.route.getFreeSlots(new TrainTime(0, 0, 0),
                new TrainTime(6, 23, 59));
        List<Node> nodeSrcList = new ArrayList<>();
        nodeSrcList.add(new Node(null, "source"));
        this.nodes.add(0, nodeSrcList);
        List<Node> nodeDestList = new ArrayList<>();
        nodeDestList.add(new Node(null, "dest"));
        this.nodes.add(nodeDestList);
    }

    private void getStationList() {
        this.stationList = this.route.getStationList();
        this.stationList.add(0, "source");
        this.stationList.add("dest");
    }

    private int isValidEdge(int delayBwStation, int waitTimeStationEnd, Node nodeStart, Node nodeEnd,
                            int maxDelayBwStations, int minDelayBwTrains,
                            int totalUpPlatform, int totalDownPlatform, int totalDualPlatform, int totalUpTrack,
                            int totalDownTrack, int totalDualTrack, boolean isDirectLineAvailable) {
        // System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS").format(new Date())+" Is valid edge start "+
        //         nodeStart.toString()+" > " +nodeEnd.toString());
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");
        totalUpPlatform--;
        totalUpTrack--;

        if (nodeStart.getTime() == null || nodeEnd.getTime() == null) {
            return -1;
        }

        boolean addedNodeEnd = false;
        int timeNodeStart = nodeStart.getTime().getValue();
        int timeNodeEnd = nodeEnd.getTime().getValue();
        if (timeNodeEnd < timeNodeStart) {
            timeNodeEnd += 10080;
            addedNodeEnd = true;
        }
        int timeEarliestToReach = timeNodeStart + delayBwStation;
        int timeEarliestToDepart = timeEarliestToReach + waitTimeStationEnd;
        int timeMaxToDepart = timeEarliestToDepart + maxDelayBwStations;

        if (timeEarliestToDepart <= timeNodeEnd && timeNodeEnd <= timeMaxToDepart) {

            if (!nodeStart.getStationId().equalsIgnoreCase("source") &&
                    !nodeEnd.getStationId().equalsIgnoreCase("dest")) {
                for (List<Train> trains : this.trainMap.values()) {
                    for (Train train : trains) {
                        TrainAtStation s1 = train.getStationInfo(nodeStart.getStationId());
                        TrainAtStation s2 = train.getStationInfo(nodeEnd.getStationId());
                        if (s2 == null) {
                            continue;
                        }
                        //check platform requirement
                        boolean addedOldTrain = false;
                        TrainTime oldTrainArrStation2 = s2.getArr();
                        TrainTime oldTrainDeptStation2 = s2.getDept();
                        if (oldTrainArrStation2 == null || oldTrainDeptStation2 == null) {
                            System.out.println("Some error occurred in fetching timings for train: " + train.getTrainNo());
                            continue;
                        }

                        int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                        int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                        if (oldTrainDeptStation2.getDay() == 0 && oldTrainArrStation2.getDay() == 6) {
                            addedOldTrain = true;
                        }

                        if (!(isDirectLineAvailable && timeOldTrainArrStation2 == timeOldTrainDeptStation2)) {
                            //need to check availability of platform
                            if (addedNodeEnd && addedOldTrain) {
                                totalUpPlatform--;
                            } else if (addedOldTrain) {
                                if ((timeNodeEnd >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2)) {
                                    totalUpPlatform--;
                                }
                            } else if (addedNodeEnd) {
                                if (timeEarliestToReach >= 10080) {
                                    if (!((timeNodeEnd - 10080) < timeOldTrainArrStation2 || (timeEarliestToReach - 10080) > timeOldTrainDeptStation2)) {
                                        totalUpPlatform--;
                                    }
                                } else {
                                    if (((timeNodeEnd - 10080) >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2)) {
                                        totalUpPlatform--;
                                    }
                                }
                            } else if (!(timeNodeEnd < timeOldTrainArrStation2 || timeEarliestToReach > timeOldTrainDeptStation2)) {
                                totalUpPlatform--;
                            }
                        }

                        if (s1 != null) {

                            if (s1.getStoppageNo() == (s2.getStoppageNo() - 1)) {
                                //same direction train
                                TrainTime oldTrainDeptStation1 = s1.getDept();
                                oldTrainArrStation2 = s2.getArr();

                                int timeOldTrainDeptStation1 = oldTrainDeptStation1.getValue();
                                timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                                addedOldTrain = false;
                                if (timeOldTrainArrStation2 < timeOldTrainDeptStation1) {
                                    timeOldTrainArrStation2 += 10080;
                                    addedOldTrain = true;
                                }
                                if ((timeOldTrainDeptStation1 + minDelayBwTrains) >= timeNodeStart &&
                                        (timeOldTrainDeptStation1 - minDelayBwTrains) <= timeNodeStart) {
                                    totalUpTrack--;
                                    continue;
                                }

                                if ((timeOldTrainArrStation2 + minDelayBwTrains) >= timeEarliestToReach &&
                                        (timeOldTrainArrStation2 - minDelayBwTrains) <= timeEarliestToReach) {
                                    totalUpTrack--;
                                    continue;
                                }

                                if (timeEarliestToReach >= 10080 && addedOldTrain) {
                                    if ((timeNodeStart < timeOldTrainDeptStation1) && (timeEarliestToReach > timeOldTrainArrStation2) ||
                                            ((timeNodeStart > timeOldTrainDeptStation1) && (timeEarliestToReach < timeOldTrainArrStation2))) {
                                        totalUpTrack--;
                                    }
                                } else if (addedOldTrain) {
                                    timeOldTrainArrStation2 -= 10080;
                                    if ((timeEarliestToReach < timeOldTrainArrStation2) || (timeNodeStart > timeOldTrainDeptStation1)) {
                                        totalUpTrack--;
                                    }
                                } else if (timeEarliestToReach >= 10080) {
                                    if ((timeOldTrainArrStation2 < (timeEarliestToReach - 10080)) || (timeOldTrainDeptStation1 > timeNodeStart)) {
                                        totalUpTrack--;
                                    }
                                } else {
                                    if ((timeNodeStart < timeOldTrainDeptStation1) && (timeEarliestToReach > timeOldTrainArrStation2) ||
                                            ((timeNodeStart > timeOldTrainDeptStation1) && (timeEarliestToReach < timeOldTrainArrStation2))) {
                                        totalUpTrack--;
                                    }
                                }
                            } else if (s2.getStoppageNo() == (s1.getStoppageNo() - 1)) {
                                //opposite direction train
                                TrainTime oldTrainArrStation1 = s1.getArr();
                                oldTrainDeptStation2 = s2.getDept();

                                int timeOldTrainArrStation1 = oldTrainArrStation1.getValue();
                                timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                                addedOldTrain = false;
                                if (timeOldTrainArrStation1 < timeOldTrainDeptStation2) {
                                    addedOldTrain = true;
                                    timeOldTrainArrStation1 += 10080;
                                }

                                if ((timeOldTrainArrStation1 + minDelayBwTrains) >= timeNodeStart &&
                                        (timeOldTrainArrStation1 - minDelayBwTrains) <= timeNodeStart) {
                                    totalUpTrack--;
                                    continue;
                                }

                                if ((timeOldTrainDeptStation2 + minDelayBwTrains) >= timeEarliestToReach &&
                                        (timeOldTrainDeptStation2 - minDelayBwTrains) <= timeEarliestToReach) {
                                    totalUpTrack--;
                                    continue;
                                }

                                if (timeEarliestToReach >= 10080 && addedOldTrain) {
                                    if ((timeNodeStart < timeOldTrainArrStation1) && (timeEarliestToReach > timeOldTrainDeptStation2)) {
                                        totalUpTrack--;
                                    }
                                } else if (addedOldTrain) {
                                    timeOldTrainArrStation1 -= 10080;
                                    if ((timeEarliestToReach > timeOldTrainDeptStation2) || (timeNodeStart < timeOldTrainArrStation1)) {
                                        totalUpTrack--;
                                    }
                                } else if (timeEarliestToReach >= 10080) {
                                    if (((timeEarliestToReach - 10080) > timeOldTrainDeptStation2) || (timeNodeStart < timeOldTrainArrStation1)) {
                                        totalUpTrack--;
                                    }
                                } else {
                                    if ((timeNodeStart < timeOldTrainArrStation1) && (timeEarliestToReach > timeOldTrainDeptStation2)) {
                                        totalUpTrack--;
                                    }
                                }
                            }
                            // else{
                            //     //goes via another direction
                            //     System.out.println(train.toString());
                            // }
                        }
                    }
                }
            }

            if ((totalUpTrack + totalDualTrack + totalDownTrack) < 0) {
                return -2;
            } else if ((totalUpPlatform + totalDualPlatform + totalDownPlatform) < 0) {
                return -3;
            } else {
                return 2;
            }
        } else if (timeEarliestToDepart > timeNodeEnd) {
            return -4;
        } else {
            return -5;
        }
    }

    private boolean isValidStartEdge(int waitTimeStationEnd, Node nodeStart, Node nodeEnd,
                                     int totalUpPlatform, int totalDownPlatform, int totalDualPlatform, boolean isDirectLineAvailable) {
        requireNonNull(nodeStart, "start node is null");
        requireNonNull(nodeEnd, "end node is null");
        totalUpPlatform--;
        TrainTime nodeStartTime;
        if (nodeStart.getStationId().equalsIgnoreCase("source")) {
            if (nodeStart.getTime() != null) {
                return false;
            } else if (nodeEnd.getTime() == null) {
                return false;
            } else {
                nodeStartTime = new TrainTime(nodeEnd.getTime());
                nodeStartTime.subMinutes(waitTimeStationEnd);
            }
        } else {
            return nodeEnd.getStationId().equalsIgnoreCase("dest");
        }
        int timeEarliestToReach = nodeStartTime.getValue();
        int timeNodeEnd = timeEarliestToReach + waitTimeStationEnd;
        boolean addedNodeEnd = false;
        if (timeNodeEnd >= 10080) {
            addedNodeEnd = true;
        }

        for (List<Train> trains : this.trainMap.values()) {
            for (Train train : trains) {
                TrainAtStation s2 = train.getStationInfo(nodeEnd.getStationId());
                if (s2 == null) {
                    continue;
                }
                //check platform requirement
                boolean addedOldTrain = false;
                TrainTime oldTrainArrStation2 = s2.getArr();
                TrainTime oldTrainDeptStation2 = s2.getDept();
                if (oldTrainArrStation2 == null || oldTrainDeptStation2 == null) {
                    System.out.println("Some error occurred in fetching timings for train: " + train.getTrainNo());
                    continue;
                }

                int timeOldTrainArrStation2 = oldTrainArrStation2.getValue();
                int timeOldTrainDeptStation2 = oldTrainDeptStation2.getValue();

                if (oldTrainDeptStation2.getDay() == 0 && oldTrainArrStation2.getDay() == 6) {
                    addedOldTrain = true;
                }

                if (!(isDirectLineAvailable && timeOldTrainArrStation2 == timeOldTrainDeptStation2)) {
                    //need to check availability of platform
                    if (addedNodeEnd && addedOldTrain) {
                        totalUpPlatform--;
                    } else if (addedOldTrain) {
                        if ((timeNodeEnd >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2)) {
                            totalUpPlatform--;
                        }
                    } else if (addedNodeEnd) {
                        if (timeEarliestToReach >= 10080) {
                            if (!((timeNodeEnd - 10080) < timeOldTrainArrStation2 || (timeEarliestToReach - 10080) > timeOldTrainDeptStation2)) {
                                totalUpPlatform--;
                            }
                        } else {
                            if (((timeNodeEnd - 10080) >= timeOldTrainArrStation2) || (timeEarliestToReach <= timeOldTrainDeptStation2)) {
                                totalUpPlatform--;
                            }
                        }
                    } else if (!(timeNodeEnd < timeOldTrainArrStation2 || timeEarliestToReach > timeOldTrainDeptStation2)) {
                        totalUpPlatform--;
                    }
                }
            }
        }
        return ((totalUpPlatform + totalDualPlatform + totalDownPlatform) >= 0);
    }

    private boolean addEdgeBwStations(int i, int delayBwStation, int waitTimeStationEnd,
                                      boolean isSingleDay, int minDelayBwTrains, int trainDay,
                                      List<Integer> maxDelayList, TrainTime sourceTime, boolean onSourceTime) {

        Node nodeStart;
        Node nodeEnd;
        if (this.nodes.get(i).isEmpty()) {
            System.out.println("No path found as no available slot for station " + this.stationList.get(i));
            return false;
        }
        if (this.nodes.get(i + 1).isEmpty()) {
            System.out.println("No path found as no available slot for station " + this.stationList.get(i + 1));
            return false;
        }

        if (!this.nodes.get(i).get(0).getStationId().equalsIgnoreCase(this.stationList.get(i).split(":")[0]) ||
                !this.nodes.get(i + 1).get(0).getStationId().equalsIgnoreCase(this.stationList.get(i + 1).split(":")[0])) {
            System.out.println("Invalid path Info.");
            return false;
        }

        if (i == 0 && !this.graphKBestPath.addMultipleNode(this.nodes.get(i))) {
            System.out.println("Some error occurred in adding source nodes");
            return false;
        }

        Station station2 = this.route.getStation(this.stationList.get(i + 1));

        if ((!this.stationList.get(i + 1).equalsIgnoreCase("dest") && station2 == null)) {
            System.out.println("Some error occurred...");
            return false;
        }
        int totalUpPlatform, totalDownPlatform, totalDualPlatform, totalUpTrack, totalDownTrack, totalDualTrack;
        boolean isDirectLineAvailable;

        if (station2 != null) {
            totalUpPlatform = station2.getNoOfUpPlatform();
            totalDownPlatform = station2.getNoOfDownPlatform();
            totalDualPlatform = station2.getNoOfDualPlatform();
            totalUpTrack = station2.getNoOfUpTrack();
            totalDownTrack = station2.getNoOfDownTrack();
            totalDualTrack = station2.getNoOfDualTrack();
            isDirectLineAvailable = station2.isDirectLineAvailable();
        } else {
            totalUpPlatform = 1000;
            totalDownPlatform = 1000;
            totalDualPlatform = 1000;
            totalUpTrack = 1000;
            totalDownTrack = 1000;
            totalDualTrack = 1000;
            isDirectLineAvailable = true;
        }

        if ((totalUpPlatform + totalDownPlatform + totalDualPlatform) <= 0 || (totalUpTrack + totalDownTrack + totalDualTrack) <= 0) {
            System.out.println("No platforms/tracks is available to schedule " + station2.getId());
            return false;
        }

        if (!this.graphKBestPath.addMultipleNode(this.nodes.get(i + 1))) {
            System.out.println("Some error occurred in adding nodes " + this.nodes.get(i + 1).get(0).toString());
            return false;
        }

        System.out.print(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + "\t");
        System.out.print("Adding edge bw stations " + this.stationList.get(i) + "\t->\t" + this.stationList.get(i + 1) + "\t");
        System.out.println("MinDelay: " + delayBwStation + ", StopTime: " + waitTimeStationEnd + ", TotalTime: " + (delayBwStation + waitTimeStationEnd));
        RuntimeMemoryHelper.getRuntimeMemory();

        for (int j = 0; j < this.nodes.get(i).size(); j++) {
            nodeStart = this.nodes.get(i).get(j);
            if (nodeStart.getInEdgeCount() <= 0 && !nodeStart.getStationId().equalsIgnoreCase("source")) {
                continue;
            }

            int countLoopMax = maxDelayList.get(i) + 1;
            if (countLoopMax > this.nodes.get(i + 1).size() || nodeStart.getStationId().equalsIgnoreCase("source")) {
                countLoopMax = this.nodes.get(i + 1).size();
            }

            for (int countLoop = 0; countLoop < countLoopMax; countLoop++) {
                int k = Math.floorMod(j + countLoop, this.nodes.get(i + 1).size());
                nodeEnd = this.nodes.get(i + 1).get(k);
                if (nodeStart.getTime() == null || nodeEnd.getTime() == null) {
                    if (isSingleDay && nodeEnd.getTime() != null && nodeEnd.getTime().getDay() != trainDay) {
                        continue;
                    }
                    double edgeCost = 0;
                    if (onSourceTime && sourceTime != null && nodeStart.getStationId().equalsIgnoreCase("source")) {
                        edgeCost = nodeEnd.getTime().compareTo(sourceTime);
                        if (edgeCost < 0) {
                            edgeCost += 10080;
                        }
                    }

                    if (isValidStartEdge(waitTimeStationEnd, nodeStart, nodeEnd, totalUpPlatform, totalDownPlatform,
                            totalDualPlatform, isDirectLineAvailable)) {
                        if (this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd, edgeCost, (edgeCost != 0)))) {
                            this.edgeCount++;
                            nodeStart.incrementOutEdgeCount();
                            nodeEnd.incrementInEdgeCount();
                        } else {
                            System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                    " and " + nodeEnd.toString() + " cost 0");
                        }
                    }
                } else {
                    int codeValidEdge = isValidEdge(delayBwStation, waitTimeStationEnd, nodeStart,
                            nodeEnd, maxDelayList.get(i), minDelayBwTrains, totalUpPlatform, totalDownPlatform,
                            totalDualPlatform, totalUpTrack, totalDownTrack, totalDualTrack, isDirectLineAvailable);
                    if (codeValidEdge == -5) {
                        break;
                    } else if (codeValidEdge > 0) {
                        int edgeCost = nodeEnd.getTime().compareTo(nodeStart.getTime());
                        if (edgeCost < 0) {
                            edgeCost += 10080;
                        }
                        if (this.graphKBestPath.addEdge(new Edge(nodeStart, nodeEnd, edgeCost, (edgeCost > (delayBwStation + waitTimeStationEnd))))) {
                            this.edgeCount++;
                            nodeStart.incrementOutEdgeCount();
                            nodeEnd.incrementInEdgeCount();
                        } else {
                            System.err.println("Some error occurred in adding edge bw " + nodeStart.toString() +
                                    " and " + nodeEnd.toString() + " cost " + edgeCost);
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<Path>
    scheduleKBestPathOptimized(String pathTemp, int noOfPaths, TrainTime sourceTime,
                               int minDelayBwTrains, List<Integer> stopTime,
                               List<Double> avgTimeNewTrain, boolean isSingleDay,
                               boolean usePreviousComputation, List<Double> maxCostList, int trainDay,
                               List<Integer> maxDelayList, boolean onSourceTime) {
        try {
            this.graphKBestPath = new GraphKBestPath(usePreviousComputation, pathTemp);

            if (!usePreviousComputation) {
                getStationList();
                getNodesFreeSlot();
                if (this.nodes == null || this.stationList == null || this.nodes.isEmpty() || this.stationList.isEmpty()) {
                    System.out.println("Error in loading data");
                    return Collections.emptyList();
                }
                if (this.nodes.size() != this.stationList.size()) {
                    System.out.println("Invalid nodes in graph... exiting");
                    return Collections.emptyList();
                }

                this.edgeCount = 0;
                System.out.println("Station size: " + (this.nodes.size() - 2));
                System.out.println("Initializing graph");
                int waitTimeStationEnd;
                int delayBwStation;
                double delaySecondsAdded = 0;
                double delayBwStationActual;

                for (int i = 0; i < this.stationList.size() - 1; i++) {
                    if (i < this.stationList.size() - 2) {
                        waitTimeStationEnd = stopTime.get(i);
                    } else {
                        waitTimeStationEnd = 0;
                    }
                    delayBwStationActual = avgTimeNewTrain.get(i + 1);
                    delayBwStation = (int) Math.ceil(delayBwStationActual - delaySecondsAdded);
                    if (waitTimeStationEnd == 0) {
                        delaySecondsAdded = delayBwStation - (delayBwStationActual - delaySecondsAdded);
                    } else {
                        delaySecondsAdded = 0;
                    }
                    if (!addEdgeBwStations(i, delayBwStation, waitTimeStationEnd,
                            isSingleDay, minDelayBwTrains, trainDay, maxDelayList, sourceTime, onSourceTime)) {
                        System.out.println("Some error occurred in adding edges.");
                        return Collections.emptyList();
                    }
                    // System.out.println("Edge size: " + this.edgeCount);
                }
                if (!this.graphKBestPath.flushData()) {
                    System.out.println("Some error occurred in graph");
                    return Collections.emptyList();
                }
                System.out.println("Edge size: " + this.edgeCount);
            }

            // System.out.println(this.graphKBestPath.toString());
            List<Path> paths;
            KShortestPathFinder kShortestPathFinder = new KShortestPathFinder();
            paths = kShortestPathFinder.findShortestPaths(this.nodes.get(0).get(0),
                    this.nodes.get(this.nodes.size() - 1).get(0), this.graphKBestPath, noOfPaths, maxCostList, this.stationList, onSourceTime);

            if (!this.graphKBestPath.disconnect()) {
                System.out.println("Some error occurred with graph.");
            }
            this.graphKBestPath = null;
            return paths;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public boolean routeMaximumSpeedLessThanAverageSpeedOfTrain(String newTrainType, String pathTrainTypeAverageSpeed) {
        double maxAverageSpeedOfRoute = this.route.maximumAverageSpeedOfTheRoute();
        double averageSpeedOfNewTrainType = -1;
        double threshold = 10;
        try {
            FileReader fileReader = new FileReader(pathTrainTypeAverageSpeed);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine())!= null) {
                if(line.split("\\s+")[0].equals(newTrainType)) {
                    averageSpeedOfNewTrainType = Double.parseDouble(line.split("\\s+")[1]);
                    break;
                }
            }
            if(averageSpeedOfNewTrainType == -1) {
                throw  new Exception("Train type not found");
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);

        }
        System.out.println("Average speed of new train type: "+averageSpeedOfNewTrainType+" km/hr");
        if(averageSpeedOfNewTrainType - maxAverageSpeedOfRoute < threshold)
            return false;
        else
            return true;
    }

    public List<Double> updateAvgTimeNewTrain(String newTrainType, List<Double> avgTimeNewTrain, String pathTrainTypeAvgSpeed) {
        double averageSpeedOfNewTypeTrain = 0;
        try {
            FileReader fileReader = new FileReader(pathTrainTypeAvgSpeed);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                if (newTrainType.equals(line.split("\\s+")[0])){
                    averageSpeedOfNewTypeTrain = Double.parseDouble(line.split("\\s+")[1]);
                    break;
                }
            }
            if(averageSpeedOfNewTypeTrain == 0) {
                Exception e = new Exception();
                throw e;
            }

        } catch (Exception e) {
            System.err.println("Error in computing average speed of the new train type.");
            e.printStackTrace();
            System.exit(-1);
        }
        for(int i = 1; i<avgTimeNewTrain.size();i++) {
            if(avgTimeNewTrain.get(i) == 0) {
                double newAvgTime = Math.max((this.route.getTrack(i).getLength()/averageSpeedOfNewTypeTrain), (this.route.getTrack(i).getLength()/this.route.getTrack(i).getMaxSpeedLimit()));
                avgTimeNewTrain.set(i, newAvgTime*60);
            }
        }
        return avgTimeNewTrain;
    }
    public List<Path> getScheduleNewTrain(String pathMaxSpeedLimit, String pathTemp, List<String> stationIdList, List<String> stationNameList,
                                          List<Double> stationDistanceList,
                                          List<Boolean> isDirectLineAvailableList,
                                          List<Integer> noOfUpPlatformList, List<Integer> noOfDownPlatformList,
                                          List<Integer> noOfDualPlatformList, List<Integer> noOfUpTrackList,
                                          List<Integer> noOfDownTrackList, List<Integer> noOfDualTrackList,
                                          int noOfPaths, TrainTime sourceTime,
                                          int minDelayBwTrains, String pathRouteTimeFile, String newTrainType, List<Integer> stopTime,
                                          String pathOldTrainSchedule, int trainDay, boolean isSingleDay,
                                          boolean usePreviousComputation, double ratio,
                                          boolean onSourceTime, int trainNotToLoad, String pathTrainTypeAverageSpeedList) {
        List<Path> paths = new ArrayList<>();
        if (ratio < 1) {
            System.out.println("Ratio must be greater than 1.0");
            return Collections.emptyList();
        }
        if (onSourceTime && sourceTime == null) {
            System.out.println("Invalid source Time");
            return Collections.emptyList();
        }
        long milli = new Date().getTime();
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.println(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        System.out.println("Train Type: " + newTrainType);
        System.out.println("Mode: " + (isSingleDay ? "Single Day" : "Week wise"));
        System.out.println("Train Day: " + trainDay);
        System.out.println("Max ratio: " + ratio);
        if (!addRoute(pathMaxSpeedLimit, stationIdList, stationNameList, stationDistanceList, isDirectLineAvailableList,
                noOfUpPlatformList, noOfDownPlatformList, noOfDualPlatformList, noOfUpTrackList, noOfDownTrackList,
                noOfDualTrackList)) {
            System.out.println("Some error occurred in adding route info");
            return Collections.emptyList();
        }

        if (!addTrainFromFolder(pathOldTrainSchedule)) {
            System.out.println("Some error occurred in adding old train info");
            return Collections.emptyList();
        }

        if (trainNotToLoad != -1) {
            this.trainMap.remove(trainNotToLoad + "");
        }
        if(!routeMaximumSpeedLessThanAverageSpeedOfTrain(newTrainType, pathTrainTypeAverageSpeedList)) {
            List<Double> avgTimeNewTrain = TrainHelper.loadNewTrainTimeData(pathRouteTimeFile, newTrainType);

            if (avgTimeNewTrain.size() != stationDistanceList.size()) {
                System.out.println("Some error occurred in fetching new train best possible time");
                return Collections.emptyList();
            }
            avgTimeNewTrain = updateAvgTimeNewTrain(newTrainType, avgTimeNewTrain, pathTrainTypeAverageSpeedList);
            avgTimeNewTrain.add(0, 0.0);
            avgTimeNewTrain.add(0.0);
            System.out.println("New train delay Time: " + avgTimeNewTrain.toString());

            // System.out.println(this.route.toString());
            // System.out.println("***********************************************************************************");
            // System.out.println(this.trainMap.values().toString());
            System.out.println("***********************************************************************************");
            if (stopTime.size() != this.route.getNumberOfStation()) {
                System.out.println("Please give stop time for every station in route. if it does not stop at " +
                        "any particular station, give stop time as 0.");
                return Collections.emptyList();
            }

            List<Double> maxCostList = new ArrayList<>();
            List<Integer> maxDelayList = new ArrayList<>();
            if (stationDistanceList.size() != stopTime.size()) {
                System.out.println("Station distance and stop time size does not match");
                return Collections.emptyList();
            }
            maxCostList.add(60.0);
            maxDelayList.add(60);

            double previousCost = maxCostList.get(0);
            for (int i = 0; i < stationDistanceList.size(); i++) {
                int temp1 = (int) Math.ceil(avgTimeNewTrain.get(i + 1));
                if (temp1 < 5) {
                    temp1 = 5;
                }
                if (i > 0) {
                    maxDelayList.add(temp1 * 5 + stopTime.get(i));
                }
                previousCost += temp1 * ratio + stopTime.get(i);
                maxCostList.add(previousCost);
            }
            maxCostList.add(maxCostList.get(maxCostList.size() - 1));
            maxDelayList.add(0);
            System.out.println("Max Cost List: " + maxCostList.toString());
            System.out.println("Max Delay List: " + maxDelayList.toString());

            paths = scheduleKBestPathOptimized(pathTemp, noOfPaths, sourceTime,
                    minDelayBwTrains, stopTime, avgTimeNewTrain, isSingleDay, usePreviousComputation, maxCostList, trainDay,
                    maxDelayList, onSourceTime);
            milli = new Date().getTime() - milli;
            System.out.println("Duration: " + milli + " ms");
            System.out.println("---------------------------------------------------------------------------------------------------------");
        } else {
            System.out.println("This train type can't run on this route due to the high average speed of train type.");
        }
        return paths;
    }
}
