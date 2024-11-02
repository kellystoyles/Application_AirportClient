package com.keyin.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
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
            JsonNode aircraftArray = objectMapper.readTree(jsonResponse);
            Map<String, List<AircraftData>> airlineAircraftMap = new HashMap<>();

            for (JsonNode aircraftNode : aircraftArray) {
                String model = aircraftNode.path("model").asText();
                String airline = aircraftNode.path("airline").asText();
                int capacity = aircraftNode.path("capacity").asInt();
                LocalDate lastServiceDate = LocalDate.parse(aircraftNode.path("lastServiceDate").asText());

                if (CallReason == 3 && capacity > 180) {
                    List<AircraftData> aircraftList = airlineAircraftMap.getOrDefault(airline, new ArrayList<>());
                    aircraftList.add(new AircraftData(model, capacity, lastServiceDate));
                    airlineAircraftMap.put(airline, aircraftList);
                }

                if (CallReason == 4) {
                    List<AircraftData> aircraftList = airlineAircraftMap.getOrDefault(airline, new ArrayList<>());
                    aircraftList.add(new AircraftData(model, capacity, lastServiceDate));
                    airlineAircraftMap.put(airline, aircraftList);
                }
            }

            if (CallReason == 3) {
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
                        LocalDate nextServiceDate = aircraft.lastServiceDate.plusDays(30);
                        System.out.println("Last Service: " + aircraft.model + " - " + aircraft.lastServiceDate);
                        System.out.println("Service By: " + aircraft.model + " - " + nextServiceDate);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error parsing aircraft JSON: " + e.getMessage());
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

            for (JsonNode passengerNode : passengerArray) {
                List<String> airportsVisited = passengerNode.findValuesAsText("airportsVisited");
            }

        } catch (IOException e) {
            System.out.println("Error parsing aircraft JSON: " + e.getMessage());
        }
    }
}
