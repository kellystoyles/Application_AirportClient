package com.keyin.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Call Reason Codes
1 - Models in Airlines
2 - Airport Access
3 - Planes in Fleet with capacity > 180
*/

public class AircraftProccessing {
    public static void processAircraftAllData(String jsonResponse, Integer CallReason, List<String> aircraftList) {

        // Ensuring the Data Received is not Empty 2 characters being []
        if (jsonResponse.length() < 2) {
            System.out.println("No data received.");
            return;
        }

        // Removes the Surrounding Brackets "[" and "]"
        String trimmedResponse = jsonResponse.substring(1, jsonResponse.length() - 1);

        // Seperates the Entrys via seperating on },{ each element is now one "aircraft"
        String[] aircraftEntries = trimmedResponse.split("\\},\\{");

        Map<String, List<String>> airlineModelsMap = new HashMap<>();
        Map<String, Integer> airlineCapacityCount = new HashMap<>();

        // Splits entrys into the KeyValue pairs via the comma seperating each one
        for (String entry : aircraftEntries) {
            entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", "");
            String[] keyValuePairs = entry.split(",");

            Integer aircraftId = 0;
            String airline = "";
            String model = "";
            String status = "";
            Integer capacity = 0;

            // Assigning Keys and Values based on the seperating ":" also removes surrounding double quotes
            for (String pair : keyValuePairs) {
                String[] keyAndValue = pair.split(":", 2);
                if (keyAndValue.length == 2) {
                    String key = keyAndValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyAndValue[1].trim().replaceAll("^\"|\"$", "");

                    if (key.equals("airline")) {
                        airline = value;
                    } else if (key.equals("model")) {
                        model = value;
                    } else if (key.equals("status")) {
                        status = value;
                    }else if (key.equals("capacity")) {
                        try {
                            capacity = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid capacity value: " + value);
                            capacity = 0;
                        }
                    }else if (key.equals("aircraftId")) {
                        try {
                            aircraftId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid aircraftId value: " + value);
                            aircraftId = 0;
                        }
                    }
                }
            }

            // Adds Models to Airline
            if (!airline.isEmpty() && !model.isEmpty()) {
                List<String> models = airlineModelsMap.getOrDefault(airline, new ArrayList<>());
                if (!models.contains(model)) {
                    models.add(model);
                }
                airlineModelsMap.put(airline, models);
            }

            if (CallReason == 2) {
                for (String item : aircraftList) {
                    if (item.equals(Integer.toString(aircraftId))) {
                        System.out.println("    - " + model + " (" + aircraftId + ")");
                    }
                }
            }

            if (CallReason == 3 && capacity > 180 && !airline.isEmpty()) {
                airlineCapacityCount.put(airline, airlineCapacityCount.getOrDefault(airline, 0) + 1);
            }
        }
        if (CallReason == 1){
            for (Map.Entry<String, List<String>> entry : airlineModelsMap.entrySet()) {
                String airlineName = entry.getKey();
                List<String> models = entry.getValue();
                System.out.println("Airline: " + airlineName);
                System.out.println("Models:");
                for (String modelName : models) {
                    System.out.println("  - " + modelName);
                }
                System.out.println();
            }
        }
        if (CallReason == 3){
            System.out.println("Planes with capacity > 180 passengers by airline: \n");
            for (Map.Entry<String, Integer> entry : airlineCapacityCount.entrySet()) {
                System.out.println("Airline: " + entry.getKey() + " - Planes with capacity > 180: " + entry.getValue());
            }
            System.out.println();
        }
    }
}