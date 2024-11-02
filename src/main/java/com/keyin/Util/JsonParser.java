package com.keyin.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void parseCities(String jsonResponse, Integer CallReason) {
        try {
            JsonNode citiesArray = objectMapper.readTree(jsonResponse);
            Map<String, ProvinceData> provinceDataMap = new HashMap<>();

            for (JsonNode cityNode : citiesArray) {
                String province = cityNode.path("province").asText();
                int cityPopulation = cityNode.path("cityPopulation").asInt();
                int provincePopulation = cityNode.path("provincePopulation").asInt();
                JsonNode airportsNode = cityNode.path("airports");

                ProvinceData provinceData = provinceDataMap.getOrDefault(province, new ProvinceData());
                provinceData.totalPopulation = provincePopulation;

                if (airportsNode.isArray() && airportsNode.size() > 0) {
                    provinceData.airportCount += airportsNode.size();
                }
                provinceDataMap.put(province, provinceData);
            }

            if (CallReason == 1) {
                System.out.println("Provinces with Population > 1,000,000 and exactly 1 airport:");
                for (Map.Entry<String, ProvinceData> entry : provinceDataMap.entrySet()) {
                    String provinceName = entry.getKey();
                    ProvinceData data = entry.getValue();

                    if (data.totalPopulation > 1000000 && data.airportCount == 1) {
                        System.out.println("Province: " + provinceName + ", Population: " + data.totalPopulation);
                    }
                }
            } else if (CallReason == 99) {
                for (Map.Entry<String, ProvinceData> entry : provinceDataMap.entrySet()) {
                    System.out.println("Province: " + entry.getKey() + ", Population: " + entry.getValue().totalPopulation + ", Airports: " + entry.getValue().airportCount);
                }
            }
        } catch (IOException e) {
            System.out.println("Error parsing cities JSON: " + e.getMessage());
        }
    }

    private static class ProvinceData {
        int totalPopulation = 0;
        int airportCount = 0;
    }

    public static void parseAirports(String jsonResponse, Integer CallReason, String targetIATAcode) {
        try {
            JsonNode airportsArray = objectMapper.readTree(jsonResponse);
            for (JsonNode airportNode : airportsArray) {
                String name = airportNode.path("name").asText();
                String iataCode = airportNode.path("iata_code").asText();

                if (CallReason == 1 && iataCode.equalsIgnoreCase(targetIATAcode)) {
                    System.out.println("Airport: " + name + ", IATA Code: " + iataCode);

                    JsonNode aircraftNode = airportNode.path("aircraft");
                    if (aircraftNode.isArray()) {
                        for (JsonNode aircraft : aircraftNode) {
                            int aircraftId = aircraft.path("aircraftId").asInt();
                            String model = aircraft.path("model").asText();
                            System.out.println("  - Aircraft: " + model + " (ID: " + aircraftId + ")");
                        }
                    }
                }
                if (CallReason == 4) {
                    System.out.println(jsonResponse);
                }
                if (CallReason == 99) {
                    System.out.println("Airport: " + name + ", IATA Code: " + iataCode);
                }
            }
        } catch (IOException e) {
            System.out.println("Error parsing airports JSON: " + e.getMessage());
        }
    }

    public static void parseAircraft(String jsonResponse, Integer CallReason) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            Map<String, List<AircraftData>> airlineAircraftMap = new HashMap<>();

            // Process nodes based on whether the root is an array or a single object
            if (rootNode.isArray()) {
                for (JsonNode aircraftNode : rootNode) {
                    processAircraftNode(aircraftNode, airlineAircraftMap, CallReason);
                }
            } else {
                processAircraftNode(rootNode, airlineAircraftMap, CallReason);
            }

            // Display results for CallReason 3 and 4
            if (CallReason == 3) {
                System.out.println("\nAirlines with aircraft having capacity greater than 180:");
                for (Map.Entry<String, List<AircraftData>> entry : airlineAircraftMap.entrySet()) {
                    String airlineName = entry.getKey();
                    List<AircraftData> aircraftList = entry.getValue();

                    System.out.println("\nAirline: " + airlineName + " (" + aircraftList.size() + ")");
                    for (AircraftData aircraft : aircraftList) {
                        System.out.println("    - " + aircraft.model + " (" + aircraft.capacity + ")");
                    }
                }
            }

            if (CallReason == 4) {
                for (Map.Entry<String, List<AircraftData>> entry : airlineAircraftMap.entrySet()) {
                    String airlineName = entry.getKey();
                    List<AircraftData> aircraftList = entry.getValue();
                    System.out.println("\nAirline: " + airlineName);

                    for (AircraftData aircraft : aircraftList) {
                        if (aircraft.lastServiceDate != null) {
                            LocalDate nextServiceDate = aircraft.lastServiceDate.plusDays(30);
                            System.out.println("Last Service: " + aircraft.model + " - " + aircraft.lastServiceDate);
                            System.out.println("Service By: " + aircraft.model + " - " + nextServiceDate);
                        } else {
                            System.out.println("Last Service: " + aircraft.model + " - Not Available");
                            System.out.println("Service By: " + aircraft.model + " - Not Available");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error parsing aircraft JSON: " + e.getMessage());
        }
    }



    private static void processAircraftNode(JsonNode aircraftNode, Map<String, List<AircraftData>> airlineAircraftMap, Integer CallReason) {
        String model = aircraftNode.path("model").asText();
        String airline = aircraftNode.path("airline").asText();
        int capacity = aircraftNode.path("capacity").asInt();
        LocalDate lastServiceDate = null;

        // Safely parse lastServiceDate
        String dateText = aircraftNode.path("lastServiceDate").asText();
        if (dateText != null && !dateText.equals("null") && !dateText.isEmpty()) {
            try {
                lastServiceDate = LocalDate.parse(dateText);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Unable to parse lastServiceDate. Invalid format: " + e.getMessage());
            }
        }

        // Only add to the map if CallReason matches and capacity > 180 (for CallReason 3)
        if ((CallReason == 3 && capacity > 180) || CallReason == 4) {
            List<AircraftData> aircraftList = airlineAircraftMap.getOrDefault(airline, new ArrayList<>());
            aircraftList.add(new AircraftData(model, capacity, lastServiceDate));
            airlineAircraftMap.put(airline, aircraftList);
        }
    }




    private static class AircraftData {
        String model;
        int capacity;
        LocalDate lastServiceDate;

        public AircraftData(String model, int capacity, LocalDate lastServiceDate) {
            this.model = model;
            this.capacity = capacity;
            this.lastServiceDate = lastServiceDate;
        }
    }

    public static void parsePassenger(String jsonResponse, Integer CallReason, String IATA) {
        try {
            JsonNode passengerArray = objectMapper.readTree(jsonResponse);
            int passengerCount = 0; // Counter for passengers who visited the specified IATA

            for (JsonNode passengerNode : passengerArray) {
                String firstName = passengerNode.path("firstName").asText();
                String lastName = passengerNode.path("lastName").asText();
                JsonNode airportsVisitedNode = passengerNode.path("airportsVisited");

                // Check if the airportsVisited field is an array and contains the specified IATA code
                if (airportsVisitedNode.isArray()) {
                    for (JsonNode airportNode : airportsVisitedNode) {
                        if (airportNode.asText().equalsIgnoreCase(IATA)) {
                            passengerCount++;
                            System.out.println("Passenger: " + firstName + " " + lastName);
                            break; // Stop checking once the IATA is found for this passenger
                        }
                    }
                }
            }

            // Print the total number of passengers who visited the specified IATA
            System.out.println("\nTotal number of passengers who visited airport " + IATA + ": " + passengerCount);

        } catch (IOException e) {
            System.out.println("Error parsing passenger JSON: " + e.getMessage());
        }
    }


}
