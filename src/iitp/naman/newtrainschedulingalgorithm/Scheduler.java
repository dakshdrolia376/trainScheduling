package iitp.naman.newtrainschedulingalgorithm;

import iitp.naman.newtrainschedulingalgorithm.util.*;
import iitp.naman.newtrainschedulingalgorithm.datahelper.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import org.jfree.ui.RefineryUtilities;

import static java.util.Objects.requireNonNull;
import java.lang.Thread;

class MyThread implements Runnable {
    String name; // name of thread
    List<List<Path>> scheduleOftrainOnEachDay;
    String pathMaxSpeedLimit;
    String pathTemp;
    String pathRoute;
    String pathBestRoute;
    String pathOldTrainSchedule;
    boolean isSingleDay;
    int trainDay;
    boolean usePreviousComputation;
    double ratio;
    String pathRouteTimeFile;
    String newTrainType;
    String pathLog;
    TrainTime sourceTime;
    String pathRouteStopTime;
    int trainNotToLoad;
    String pathStationDatabase;
    String pathTrainTypeAverageSpeedList;
    Thread t;
    int index;
    MyThread(int index, List<List<Path>> scheduleOftrainOnEachDay, String pathMaxSpeedLimit, String pathTemp, String pathRoute, String pathBestRoute, String pathOldTrainSchedule, boolean isSingleDay,
             int trainDay, boolean usePreviousComputation, double ratio, String pathRouteTimeFile, String newTrainType,
             String pathLog, TrainTime sourceTime, String pathRouteStopTime, int trainNotToLoad, String pathStationDatabase,String pathTrainTypeAverageSpeedList) {
        this.index = index;
        this.scheduleOftrainOnEachDay = scheduleOftrainOnEachDay;
        this.pathBestRoute = pathBestRoute;
        this.pathMaxSpeedLimit = pathMaxSpeedLimit;
        this.pathTemp = pathTemp;
        this.pathRoute = pathRoute;
        this.pathOldTrainSchedule = pathOldTrainSchedule;
        this.isSingleDay = isSingleDay;
        this.trainDay = trainDay;
        this.usePreviousComputation = usePreviousComputation;
        this.ratio = ratio;
        this.pathRouteTimeFile = pathRouteTimeFile;
        this.newTrainType = newTrainType;
        this.pathLog = pathLog;
        this.sourceTime = sourceTime;
        this.pathRouteStopTime = pathRouteStopTime;
        this.trainNotToLoad = trainNotToLoad;
        this.pathStationDatabase = pathStationDatabase;
        this.pathTrainTypeAverageSpeedList = pathTrainTypeAverageSpeedList;
        t = new Thread(this);
        t.start();
    }

    public void run() {
//        try {
        System.out.println("Thread " +Integer.toString(this.index) + "starting" );
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Scheduler scheduler = new Scheduler();
        List<Path> paths ;
        System.out.println("Check "+ Integer.toString(this.index));
        paths = scheduler.test(pathMaxSpeedLimit, pathTemp, pathRoute, pathBestRoute, pathOldTrainSchedule, isSingleDay,
         trainDay, usePreviousComputation, ratio, pathRouteTimeFile, newTrainType,
                pathLog, sourceTime, pathRouteStopTime, trainNotToLoad, pathStationDatabase, pathTrainTypeAverageSpeedList);
        scheduleOftrainOnEachDay.set(index, paths);

        System.out.println("Thread " +Integer.toString(this.index) + "exiting" );
//        } catch (InterruptedException e) {
//            System.out.println(name + " interrupted.");
//        }

    }
}

public class Scheduler {

    private List<String> stationId;
    private List<String> stationName;
    private List<Double> stationDistance;
    private List<Boolean> stationDirectLine;
    private List<Integer> stationNoOfUpPlatformList;
    private List<Integer> stationNoOfDownPlatformList;
    private List<Integer> stationNoOfDualPlatformList;
    private List<Integer> stationNoOfUpTrackList;
    private List<Integer> stationNoOfDownTrackList;
    private List<Integer> stationNoOfDualTrackList;
    private static List<List<Path>> scheduleOftrainOnEachDay;

    public List<String> getStationIdList() {
        return this.stationId;
    }

    public List<String> getStationNameList() {
        return this.stationName;
    }

    public List<Double> getStationDistanceList() {
        return this.stationDistance;
    }

    public List<Boolean> getStationDirectLineList() {
        return this.stationDirectLine;
    }

    public List<Integer> getStationNoOfUpPlatformList() {
        return this.stationNoOfUpPlatformList;
    }

    public List<Integer> getStationNoOfDownPlatformList() {
        return this.stationNoOfDownPlatformList;
    }

    public List<Integer> getStationNoOfDualPlatformList() {
        return this.stationNoOfDualPlatformList;
    }

    public List<Integer> getStationNoOfUpTrackList() {
        return this.stationNoOfUpTrackList;
    }

    public List<Integer> getStationNoOfDownTrackList() {
        return this.stationNoOfDownTrackList;
    }

    public List<Integer> getStationNoOfDualTrackList() {
        return this.stationNoOfDualTrackList;
    }

    /**
     * Add route info from file.
     */
    public boolean addRouteFromFile(String pathRouteFile) {
        stationId = new ArrayList<>();
        stationName = new ArrayList<>();
        stationDistance = new ArrayList<>();
        stationDirectLine = new ArrayList<>();
        stationNoOfUpPlatformList = new ArrayList<>();
        stationNoOfDownPlatformList = new ArrayList<>();
        stationNoOfDualPlatformList = new ArrayList<>();
        stationNoOfUpTrackList = new ArrayList<>();
        stationNoOfDownTrackList = new ArrayList<>();
        stationNoOfDualTrackList = new ArrayList<>();

        try {
            FileReader fReader = new FileReader(pathRouteFile);
            BufferedReader bReader = new BufferedReader(fReader);
            String line;
            while ((line = bReader.readLine()) != null) {
                String data[] = line.split("\\s+");
                if (data.length < 9) {
                    System.out.println("Invalid station info. : " + line);
                    continue;
                }
                String id = StationIdHelper.getStationIdFromName(data[0]);
                stationId.add(id);
                stationName.add(data[0]);
                stationDistance.add(Double.parseDouble(data[1]));
                stationDirectLine.add(Integer.parseInt(data[2]) == 1);
                stationNoOfUpPlatformList.add(Integer.parseInt(data[3]));
                stationNoOfDownPlatformList.add(Integer.parseInt(data[4]));
                stationNoOfDualPlatformList.add(Integer.parseInt(data[5]));
                stationNoOfUpTrackList.add(Integer.parseInt(data[6]));
                stationNoOfDownTrackList.add(Integer.parseInt(data[7]));
                stationNoOfDualTrackList.add(Integer.parseInt(data[8]));
            }
            bReader.close();
            fReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Add route info from params.
     */
    public boolean addRoute(List<String> stationIdList, List<String> stationNameList, List<Double> stationDistanceList,
                            List<Boolean> isDirectLineAvailableList,
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
        int sizeStation = stationIdList.size();
        if (stationNameList.size() != sizeStation || stationDistanceList.size() != sizeStation ||
                isDirectLineAvailableList.size() != sizeStation || noOfUpPlatformList.size() != sizeStation ||
                noOfDownPlatformList.size() != sizeStation || noOfDualPlatformList.size() != sizeStation ||
                noOfUpTrackList.size() != sizeStation || noOfDownTrackList.size() != sizeStation ||
                noOfDualTrackList.size() != sizeStation) {
            throw new IllegalArgumentException("Invalid arguments for route");
        }
        this.stationId = stationIdList;
        this.stationName = stationNameList;
        this.stationDistance = stationDistanceList;
        this.stationDirectLine = isDirectLineAvailableList;
        this.stationNoOfUpPlatformList = noOfUpPlatformList;
        this.stationNoOfDownPlatformList = noOfDownPlatformList;
        this.stationNoOfDualPlatformList = noOfDualPlatformList;
        this.stationNoOfUpTrackList = noOfUpTrackList;
        this.stationNoOfDownTrackList = noOfDownTrackList;
        this.stationNoOfDualTrackList = noOfDualTrackList;
        return true;
    }

    /**
     * Write generated train schedule to file.
     */
    public void writePathsToFile(Path path, int countPath, String pathBestRouteFile, List<Integer> stopTime,
                                 String pathRouteTimeFile, String newTrainType, List<String> stationName, List<Double> stationDistance) {
        try {
            List<Double> avgTimeNewTrain = TrainHelper.loadNewTrainTimeData(pathRouteTimeFile, newTrainType);
            avgTimeNewTrain.add(0, 0.0);
            avgTimeNewTrain.add(0.0);
            BufferedWriter bWriter;
            FileWriter fWriter;
            List<Node> nodePathBestRoute = path.getNodeList();
            String arrivalTimeStation;
            int delayBwStation;
            double delaySecondsAdded = 0;
            double delayBwStationActual;

            TrainTime timePrevStation = null;
            fWriter = new FileWriter(pathBestRouteFile + " path " + countPath +
                    " cost " + (path.pathCost() - stopTime.get(stopTime.size() - 1)) + " .txt");
            bWriter = new BufferedWriter(fWriter);

            for (int i = 1; i < nodePathBestRoute.size() - 1; i++) {
                String stopType;
                Node bestRouteNode = nodePathBestRoute.get(i);
                double nodeDistance = stationDistance.get(i - 1);
                if (timePrevStation != null) {
                    delayBwStationActual = avgTimeNewTrain.get(i);
                    delayBwStation = (int) Math.ceil(delayBwStationActual - delaySecondsAdded);
                    if (stopTime.get(i - 1) == 0) {
                        delaySecondsAdded = delayBwStation - (delayBwStationActual - delaySecondsAdded);
                    } else {
                        delaySecondsAdded = 0;
                    }
                    timePrevStation.addMinutes(delayBwStation);
                    arrivalTimeStation = timePrevStation.getTimeString();
                } else {
                    timePrevStation = new TrainTime(bestRouteNode.getTime());
                    timePrevStation.subMinutes(stopTime.get(i - 1));
                    arrivalTimeStation = timePrevStation.getTimeString();
                }
                TrainTime timeStation = new TrainTime(bestRouteNode.getTime());
                if (timePrevStation.compareTo(timeStation) == 0) {
                    stopType = "N";
                } else {
                    if (stopTime.get(i - 1) == 0) {
                        stopType = "U";
                    } else {
                        timePrevStation.addMinutes(stopTime.get(i - 1));
                        if (timePrevStation.compareTo(timeStation) == 0) {
                            stopType = "S";
                        } else {
                            stopType = "E";
                        }
                    }
                }
                bWriter.append(stationName.get(i - 1));
                for (int lengthString = stationName.get(i - 1).length(); lengthString < 40; lengthString++) {
                    bWriter.append(' ');
                }
                bWriter.append(arrivalTimeStation);
                bWriter.append('\t');
                bWriter.append(bestRouteNode.getTime().getTimeString());
                bWriter.append('\t');
                bWriter.append(new DecimalFormat("#0.00").format(nodeDistance));
                bWriter.append('\t');
                bWriter.append(stopType);
                bWriter.write("\n");
                timePrevStation = timeStation;
            }
            bWriter.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPlot(String pathNewTrainFile, int newTrainNo, String pathPlotFile, String pathRoute,
                         String pathOldTrainSchedule, int trainDay, String pathStationDatabase,
                         String pathName) {
        String titlePlot = "Train Schedule";
        int windowHeight = 600;
        int windowWidth = 1000;
        LinePlotTrains demo = new LinePlotTrains(titlePlot, windowHeight, windowWidth, newTrainNo, pathPlotFile, pathRoute,
                pathOldTrainSchedule, pathNewTrainFile, trainDay, pathStationDatabase, pathName);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
    public Map<String, List<Integer>> getCongestionDetails(List<String> stationId) {
        Map<String, List<Integer>> congestionDetails = new HashMap<>();
        List<List<Map<String, String>>>scheduleForStation = new ArrayList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper();
        for(String id: stationId) {
            scheduleForStation.add(databaseHelper.getScheduleForStation(id));
        }
        String arrTime = new String();
        int arrHour;
        int arrMin;
        for(int i=0;i<stationId.size();i++) {
            List<Integer> congestion =  new ArrayList<Integer>(Collections.nCopies(24, 0));
            for(Map.Entry<String, String> oldTrainArrivingDetails : scheduleForStation.get(i).get(0).entrySet()) {
                arrTime = oldTrainArrivingDetails.getValue();
                arrHour = Integer.parseInt(arrTime.split(":")[0]);
                arrMin = Integer.parseInt(arrTime.split(":")[1]);
                if (arrMin > 30)
                    arrHour = (arrHour + 1) % 24;
                congestion.set(arrHour, congestion.get(arrHour)+1);
            }
            congestionDetails.put(stationId.get(i), congestion);
        }
        return  congestionDetails;
    }
//    public List<Path> sortPathsWithCongestionRestraint(List<Path> oldPaths, List<String> stationId) {
//        Map <String, List<Integer>> congestionDetailsOnRoute = getCongestionDetails(stationId);
//        Collections.sort(oldPaths, new Comparator<Path>() {
//            @Override
//            public int compare(Path o1, Path o2) {
//
//                return 0;
//            }
//        });
//    }
    public void scheduleTrain(String pathMaxSpeedLimit, String pathTemp, String pathRoute, String pathBestRoute, String pathOldTrainSchedule, boolean isSingleDay, boolean usePreviousComputation, double ratio, String pathRouteTimeFile, String newTrainType,
                              String pathLog, TrainTime sourceTime, String pathRouteStopTime, int trainNotToLoad, String pathStationDatabase,String pathTrainTypeAverageSpeedList, List<Boolean> runningStatusOnEachDay) {

        List<MyThread> myThreads = new ArrayList<>();
        List<List<Path>> scheduleOftrainOnEachDay = new ArrayList<>(7);
        for(int i=0;i<7;i++)
            scheduleOftrainOnEachDay.add(new ArrayList<>());
        for(int i=0;i<runningStatusOnEachDay.size();i++) {
            List<Path> paths = new ArrayList<>();
            if(runningStatusOnEachDay.get(i)) {
                myThreads.add(new MyThread(i, scheduleOftrainOnEachDay, pathMaxSpeedLimit, pathTemp, pathRoute, pathBestRoute, pathOldTrainSchedule, isSingleDay,
                        i, usePreviousComputation, ratio, pathRouteTimeFile, newTrainType,
                        pathLog, sourceTime, pathRouteStopTime, trainNotToLoad, pathStationDatabase,pathTrainTypeAverageSpeedList));

            }
            else
                scheduleOftrainOnEachDay.set(i, paths);
        }
        for(MyThread thread: myThreads) {
            try {
                thread.t.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator + "output.log"));
            System.setOut(o1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i=0;i<myThreads.size();i++) {
            System.out.println("Thread "+Integer.toString(i)+" alive: " + myThreads.get(i).t.isAlive());
        }
        int temp = 0;
        for(int i=0;i<7;i++) {
            if(scheduleOftrainOnEachDay.get(i).size()!=0)
                temp++;
        }
        System.out.println("Size of schedule list: "+ Integer.toString(temp));
        int maximumAllowableTimeDifference = 30;
        int firstDayOfTrain = runningStatusOnEachDay.indexOf(Boolean.TRUE);
        int numberOfPathsForEachDay = 10;
        int countOfPaths = 0;
        if(firstDayOfTrain == -1) {
            System.err.println("No running day for the train is specified");
        } else {
            for(int i=0; i < numberOfPathsForEachDay; i++) {
                for(int j=firstDayOfTrain+1;j<scheduleOftrainOnEachDay.size();j++) {
                    boolean pathAvailable = false;
                    if(!runningStatusOnEachDay.get(j))
                        pathAvailable = true;
                    else {
                        for (int k = 0; k < scheduleOftrainOnEachDay.get(j).size(); k++) {
                            if (Math.abs(scheduleOftrainOnEachDay.get(j).get(k).getSourceTime().compareTo(scheduleOftrainOnEachDay.get(firstDayOfTrain).get(i).getSourceTime()) - (1440 * (j  - firstDayOfTrain)))<maximumAllowableTimeDifference){
                                pathAvailable = true;
                                break;
                            }
                        }
                    }
                    if(!pathAvailable) {
                        break;
                    } else if(j == scheduleOftrainOnEachDay.size()-1)
                        System.out.println(scheduleOftrainOnEachDay.get(firstDayOfTrain).get(i).toString());
                        countOfPaths ++;
                }
            }
        }
        System.out.println(countOfPaths);

    }
    public List<Path> test(String pathMaxSpeedLimit, String pathTemp, String pathRoute, String pathBestRoute, String pathOldTrainSchedule, boolean isSingleDay,
                     int trainDay, boolean usePreviousComputation, double ratio, String pathRouteTimeFile, String newTrainType,
                     String pathLog, TrainTime sourceTime, String pathRouteStopTime, int trainNotToLoad, String pathStationDatabase,String pathTrainTypeAverageSpeedList) {
        if (sourceTime != null) {
            sourceTime = new TrainTime(sourceTime);
        }
        Scheduler scheduler = new Scheduler();
        if (!scheduler.addRouteFromFile(pathRoute)) {
            System.out.println("Unable to load route file");
            return Collections.emptyList();
        }
        ArrayList<Integer> stopTime = RouteHelper.getStopTime(pathRouteStopTime);
        int minDelayBwTrains = 3;
        int noOfPaths = 10;
        List<Path> paths = new ArrayList<Path>();
        int count;
        try {
            PrintStream o1 = new PrintStream(new File(pathLog + File.separator + "Output Type Full Day " + trainDay + " TrainType " + newTrainType +
                    " maxRatio " + ratio + ((sourceTime == null) ? " unconditional.log" : " conditional.log")));
            PrintStream console = System.out;
            System.setOut(o1);
            String pathBestRouteFile = pathBestRoute + File.separator + "Type Full Day " + trainDay + " TrainType " + newTrainType +
                    " maxRatio " + ratio + ((sourceTime == null) ? " unconditional " : " conditional ");
            paths = new KBestSchedule(pathStationDatabase).getScheduleNewTrain(pathMaxSpeedLimit, pathTemp, scheduler.getStationIdList(), scheduler.getStationNameList(),
                    scheduler.getStationDistanceList(), scheduler.getStationDirectLineList(),
                    scheduler.getStationNoOfUpPlatformList(), scheduler.getStationNoOfDownPlatformList(),
                    scheduler.getStationNoOfDualPlatformList(), scheduler.getStationNoOfUpTrackList(),
                    scheduler.getStationNoOfDownTrackList(), scheduler.getStationNoOfDualTrackList(), noOfPaths, sourceTime,
                    minDelayBwTrains, pathRouteTimeFile, newTrainType, stopTime, pathOldTrainSchedule,
                    trainDay, isSingleDay,
                    usePreviousComputation, ratio, (sourceTime != null), trainNotToLoad, pathTrainTypeAverageSpeedList);
            count = 0;
            System.out.print(paths.size());
            for (Path path : paths) {
                System.out.print("\t" + (path.pathCost() - stopTime.get(stopTime.size() - 1)));
            }
            System.out.println();

            for (Path path : paths) {
                System.out.println("Cost: " + (path.pathCost() - stopTime.get(stopTime.size() - 1)) + " Unscheduled Stop: " + path.getUnScheduledStop() + " " + path.toString());
                writePathsToFile(path, ++count, pathBestRouteFile, stopTime, pathRouteTimeFile, newTrainType, scheduler.getStationNameList(),
                        scheduler.getStationDistanceList());
            }
            System.setOut(console);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }
}
