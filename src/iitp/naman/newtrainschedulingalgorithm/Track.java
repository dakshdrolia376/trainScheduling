package iitp.naman.newtrainschedulingalgorithm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iitp.naman.newtrainschedulingalgorithm.datahelper.DatabaseHelper;
import iitp.naman.newtrainschedulingalgorithm.datahelper.TrainHelper;
import iitp.naman.newtrainschedulingalgorithm.util.StationIdHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class Track {
    private final Station source, destination;
    private double maxSpeedLimit;
    private double length;

    Track (Station source, Station destination, String pathMaxSpeedLimit) {
        this.source = source;
        this.destination = destination;
        this.length = Math.abs(source.getDistance() - destination.getDistance());
        this.maxSpeedLimit = computeMaxSpeedLimit(pathMaxSpeedLimit);
    }

    public double getLength(){
        return this.length;
    }

    public  double getMaxSpeedLimit() {
        return this.maxSpeedLimit;
    }

    public double computeMaxSpeedLimit(String pathMaxSpeedLimit) {
        double maxSpeedLimit = -1;
        try {
            FileReader fileReader = new FileReader(pathMaxSpeedLimit);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                String sourceDestinationPair = line.split("\\s+")[0];
                if(sourceDestinationPair.equals(this.source.getName()+":"+this.destination.getName()) || sourceDestinationPair.equals(this.destination.getName()+":"+this.source.getName())) {
                    maxSpeedLimit =  Double.parseDouble(line.split("\\s+")[1]);
                    break;
                }
            }
            if(maxSpeedLimit == -1) {
                Exception e = new Exception();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Can't find the maximum speed limit");
            e.printStackTrace();
            System.exit(-1);
        }
        return maxSpeedLimit;
    }
//    // t1 and t2 follow the format -> hh:mm
//    public  double timeDifference(String t1, String t2) {
//        int t1InMinutes = Integer.parseInt(t1.split(":")[0])*60 + Integer.parseInt(t1.split(":")[1]);
//        int t2InMinutes = Integer.parseInt(t2.split(":")[0])*60 + Integer.parseInt(t2.split(":")[1]);
//        double timeDifference = t1InMinutes - t2InMinutes;
//        if(timeDifference < 0)
//            timeDifference += 1440;
//        return  timeDifference/60;
//    }
//
//    public List<Double> computeAvgSpeedOfTrainsOnTrack(String source, String destination, List<Integer> trainIndexes, String pathDatabaseTrain ){
//        List<Double> avgSpeedOfTrainsOnTrack = new ArrayList<>();
//        for(int trainIndex : trainIndexes) {
//            //System.out.println(Integer.toString(trainIndex));
//            List<List<String>> RouteDetails = new ArrayList<>();
//            List<Integer> indexOfSourceAndDestination = new ArrayList<>();
//            try{
//                FileReader fileReader = new FileReader(pathDatabaseTrain + File.separator + Integer.toString(trainIndex) + ".txt");
//                BufferedReader bufferedReader = new BufferedReader(fileReader);
//                String line;
//                while((line = bufferedReader.readLine()) != null) {
//                    RouteDetails.add(Arrays.asList(line.split("\\s+")));
//                }
//                for (int i=0;i<RouteDetails.size();i++) {
//                    List <String> stationOnRoute = RouteDetails.get(i);
//                    if( stationOnRoute.get(0).equals(source) || stationOnRoute.get(0).equals(destination)) {
//                        indexOfSourceAndDestination.add(i);
//                    }
//                }
//                if(indexOfSourceAndDestination.get(1) - indexOfSourceAndDestination.get(0) > 1) {
//                    continue; // case in which an old train doesn't take the direct route between the stations
//                } else {
//                    List <String> station01OnRoute = RouteDetails.get(indexOfSourceAndDestination.get(0));
//                    List <String> station02OnRoute = RouteDetails.get(indexOfSourceAndDestination.get(1));
//                    double distance = Double.parseDouble(station02OnRoute.get(3)) - Double.parseDouble(station01OnRoute.get(3));
//                    String arrivalTimeAtStation02 = station02OnRoute.get(1);
//                    String departureTimeAtStation01 = station01OnRoute.get(2);
//
//                    double timeTaken = timeDifference(arrivalTimeAtStation02, departureTimeAtStation01);
//                    /*if(trainIndex == 1114) {
//                        System.out.println(station02OnRoute.get(3) + " " + station01OnRoute.get(3));
//                        System.out.println(Double.toString(distance) + " " + arrivalTimeAtStation02 + " " + departureTimeAtStation01 + " " + Double.toString(timeTaken));
//                    }*/
//                    avgSpeedOfTrainsOnTrack.add(distance/timeTaken);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        return avgSpeedOfTrainsOnTrack;
//    }
//
//    public double computeMaxSpeedLimit(String pathDatabaseTrain) {
//
//        List<String> stationIds = new ArrayList<>();
//        DatabaseHelper databaseHelper = new DatabaseHelper();
//
//        stationIds.add(source.getId());
//        List<Integer> trainsNosAtSource = databaseHelper.getTrainNosForStation(stationIds);
//
//        stationIds.clear();
//
//        stationIds.add(destination.getId());
//        List<Integer> trainsNosAtDestination = databaseHelper.getTrainNosForStation(stationIds);
//
//        trainsNosAtSource.retainAll(trainsNosAtDestination);
//        List<Integer> oldTrainNosOnTrack = trainsNosAtSource;
//
//        List<Integer> oldTrainIndexOnTrack = TrainHelper.getTrainIndexNoFromTrainNo(oldTrainNosOnTrack, pathDatabaseTrain);
//        List<Double> oldTrainAvgSpeedOnTrack = computeAvgSpeedOfTrainsOnTrack(this.source.getName(), this.destination.getName(), oldTrainIndexOnTrack
//        , pathDatabaseTrain);
//        for (int i=0;i<oldTrainAvgSpeedOnTrack.size();i++) {
//            System.out.println(Integer.toString(oldTrainNosOnTrack.get(i))+" "+Double.toString(oldTrainAvgSpeedOnTrack.get(i)));
//        }
//        try{
//            this.maxSpeedLimit = Collections.max(oldTrainAvgSpeedOnTrack);
//        } catch (Exception e) {
//            System.err.println("No direct track found between "+this.source.getName()+" & "+this.destination.getName());
//            e.printStackTrace();
//            System.exit(-1);
//        }


}
