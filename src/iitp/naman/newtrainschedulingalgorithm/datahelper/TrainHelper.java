package iitp.naman.newtrainschedulingalgorithm.datahelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import iitp.naman.newtrainschedulingalgorithm.util.StationIdHelper;
import iitp.naman.newtrainschedulingalgorithm.util.WriteToFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class TrainHelper {


    /**
     * Updates the type of trains & the train number at given path.
     *
     * @param pathTrainTypeFile file path where train type details has to be stored.
     */
    public static void updateTrainTypeFile(String pathTrainTypeFile) {
        List<List<String>> trainNames = new DatabaseHelper().getAllTrainNames();
        Map<String, List<String>> mapTrainTypes = new HashMap<>();
        for (List<String> trainType : trainNames) {
            String temp = trainType.get(2).replaceAll("\\s+", "-");
            temp = temp.replaceAll("/", "-");
            mapTrainTypes.putIfAbsent(temp, new ArrayList<>());
            mapTrainTypes.get(temp).add(trainType.get(0));
        }

        StringBuilder stringBuilder = new StringBuilder("");

        for (String type : mapTrainTypes.keySet()) {
            stringBuilder.append(type);
            List<String> traiNos = mapTrainTypes.get(type);
            for (String trainNo : traiNos) {
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathTrainTypeFile, stringBuilder.toString(), false);
    }
    // t1 and t2 follow the format -> hh:mm
    public  static double timeDifference(String arrivalTime, String departureTime) {
        int arrivalTimeInMinutes = Integer.parseInt(arrivalTime.split(":")[0])*60 + Integer.parseInt(arrivalTime.split(":")[1]);
        int departureTimeInMinutes = Integer.parseInt(departureTime.split(":")[0])*60 + Integer.parseInt(departureTime.split(":")[1]);
        double timeDifference = arrivalTimeInMinutes - departureTimeInMinutes;
        if(timeDifference < 0)
            timeDifference += 1440;
        return  timeDifference/60;
    }

    public static void createAverageSpeedListOfTrainType(String pathAverageSpeedList, String pathTrainTypeFile, String pathDatabaseTrain) {
        System.out.println("Creating Average Speed List File");
        Map<String, List<Integer>> mapTypeTrainIndex = new HashMap<>();
        Map<String, List<Double>> mapTrainTypeAverageSpeeds = new HashMap<>();
        try {
            FileReader fileReader = new FileReader(pathTrainTypeFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                String trainType = line.split("\\s+")[0];
                int noOfTrainsUsedToComputeAverageSpeed = (line.split("\\s+").length-1 > 20)? 20 : (line.split("\\s+").length-1);
                List<Integer> trainNo = new ArrayList<>();
                for(int i = 0;i <= noOfTrainsUsedToComputeAverageSpeed;i++) {
                    if(i==0)
                        continue;
                    trainNo.add(Integer.parseInt(line.split("\\s+")[i]));
                }
                List<Integer> trainIndex = getTrainIndexNoFromTrainNo(trainNo, pathDatabaseTrain);
                mapTypeTrainIndex.put(trainType,trainIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Index of the train collected");
        for (String trainType : mapTypeTrainIndex.keySet()) {
            List<Integer> trainIndexes = mapTypeTrainIndex.get(trainType);
            List<Double> averageSpeeds = new ArrayList<>();
            for(int trainIndex : trainIndexes){
                try {
                    String pathOfTrainRouteFile = pathDatabaseTrain + File.separator + Integer.toString(trainIndex) + ".txt";
                    FileReader fileReader = new FileReader(pathOfTrainRouteFile);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    double distanceOfSourceStation = 0;
                    double distanceOfLastStation = 0;
                    double timeTaken = 0;
                    int stoppageNumber = 1;
                    String departureTimeFromLastStation = "";
                    String arrivalTimeAtCurrentStation = "";
                    while((line = bufferedReader.readLine())!= null) {
                        String [] trainStopDetails = line.split("\\s+");
                        if(trainStopDetails.length != 4) {
                            System.err.println("Train route details not correct");
                        }
                        if(stoppageNumber == 1) {
                            distanceOfSourceStation = Double.parseDouble(trainStopDetails[3]);
                            departureTimeFromLastStation = trainStopDetails[2];
                            stoppageNumber++;
                        } else {
                            distanceOfLastStation = Double.parseDouble(trainStopDetails[3]);
                            arrivalTimeAtCurrentStation = trainStopDetails[1];
                            timeTaken += timeDifference(arrivalTimeAtCurrentStation, departureTimeFromLastStation);
                            departureTimeFromLastStation = arrivalTimeAtCurrentStation;
                            stoppageNumber++;
                        }
                    }
                    double averageSpeed;
                    if(timeTaken != 0)
                        averageSpeed = (distanceOfLastStation - distanceOfSourceStation)/timeTaken;
                    else
                        averageSpeed = 0;
                    averageSpeeds.add(averageSpeed);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            mapTrainTypeAverageSpeeds.put(trainType, averageSpeeds);
        }
        System.out.println("Average Speed Collected");
        StringBuilder fileContent = new StringBuilder();
        for(String trainType: mapTrainTypeAverageSpeeds.keySet()) {
            fileContent.append(trainType+"\t");
            double sum = 0;
            int trainsWithZeroSpeed = 0;
            if(!mapTrainTypeAverageSpeeds.get(trainType).isEmpty()) {
                for (double speed : mapTrainTypeAverageSpeeds.get(trainType)) {
                    sum += speed;
                    if(speed == 0)
                        trainsWithZeroSpeed++;
                }
                fileContent.append(Double.toString(sum / (mapTrainTypeAverageSpeeds.get(trainType).size() - trainsWithZeroSpeed)) + "\n");
            } else {
                fileContent.append("0\n");
            }
        }
        new WriteToFile().write(pathAverageSpeedList, fileContent.toString(), false);
    }

    /**
     * @param pathRouteTimeFile path for train avg timing details for the route.
     * @param newTrainType      type of new train.
     * @return List of time taken by train between stations in route.
     */

    public static List<Double> loadNewTrainTimeData(String pathRouteTimeFile, String newTrainType) {
        List<Double> timeNewTrain = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(pathRouteTimeFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            int count = -1;
            String data[] = line.split("\\s+");
            for (int i = 0; i < data.length; i++) {
                if (data[i].equalsIgnoreCase(newTrainType)) {
                    count = i;
                    break;
                }
            }
            while (count > 0 && (line = bufferedReader.readLine()) != null) {
                String avgTime = line.split("\\s+")[count];
                double avgTimeDouble = 0;
                try {
                    if (!avgTime.equalsIgnoreCase("NA")) {
                        avgTimeDouble = Double.parseDouble(avgTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                timeNewTrain.add(avgTimeDouble);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return timeNewTrain;
    }

    /**
     * Creates the list of train in the given route.
     *
     * @param pathRoute     path for file containing route info.
     * @param pathTrainList path for file containing train info.
     */
    public static void createTrainList(String pathRoute, String pathTrainList) {
        FileReader fReader;
        BufferedReader bReader;
        String line;
        String stationId;
        DatabaseHelper databaseHelper = new DatabaseHelper();
        List<String> stationIds = new ArrayList<>();
        try {
            fReader = new FileReader(pathRoute);
            bReader = new BufferedReader(fReader);
            while ((line = bReader.readLine()) != null) {
                stationId = StationIdHelper.getStationIdFromName(line.split("\\s+")[0]);
                stationIds.add(stationId);
            }
            bReader.close();
            fReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Integer> trainNos = databaseHelper.getTrainNosForStation(stationIds);
        List<List<Integer>> Trains = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Trains.add(databaseHelper.getTrainNosForDay(i));
            // remove all the train no. from the list of train numbers for each day not present in trainNos
            Trains.get(i).retainAll(trainNos);
        }

        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < 7; i++) {
            stringBuilder.append(i);
            for (int trainNo : Trains.get(i)) {
                stringBuilder.append('\t');
                stringBuilder.append(trainNo);
            }
            stringBuilder.append('\n');
        }
        new WriteToFile().write(pathTrainList, stringBuilder.toString(), false);
    }


    public static List<Integer> getTrainIndexNoFromTrainNo(List<Integer> trainNos, String pathDatabaseTrain) {
        List<Integer> trainIndexes = new ArrayList<>();
        HashMap<Integer, Integer> myMap;
        requireNonNull(pathDatabaseTrain, "path of database cant be null.");

        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<Map<Integer, Integer>>() {
            }.getType();
            File file = new File(pathDatabaseTrain + File.separator + "indexTrains.db");
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                myMap = gson.fromJson(fileReader, listType);
                fileReader.close();
            } else {
                myMap = new HashMap<>();
            }
        } catch (Exception e) {
            myMap = new HashMap<>();
        }
        if(myMap.size() == 0) {
            System.out.println("Train database file:\"indexTrains.db\" missing");
        } else {
            for (int trainNo : trainNos) {
                trainIndexes.add(myMap.get(trainNo));
            }
        }
        return trainIndexes;
    }
}
