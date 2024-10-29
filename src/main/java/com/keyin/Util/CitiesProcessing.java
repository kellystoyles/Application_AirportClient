package com.keyin.Util;

import java.util.*;

/*
Call Reason Codes
1 - 2.1 (List all provinces that have over 1 million people and only 1 major airport)
*/
public class CitiesProcessing {
    public static void processCityAllData(String jsonResponse, Integer CallReason) {

        // Ensuring the Data Received is not Empty 2 characters being []
        if (jsonResponse.length() < 2) {
        System.out.println("No data received.");
        return;
        }

        // Removes the Surrounding Brackets "[" and "]"
        String trimmedResponse = jsonResponse.substring(1, jsonResponse.length() - 1);

        // Seperates the Entrys via seperating on },{ each element is now one "City"
        String[] cityEntries = trimmedResponse.split("\\},\\{");

        // List to hold cities meeting the criteria for Reason Code 1
        Map<String, ProvinceData> provinceDataMap = new HashMap<>();

        for (String entry : cityEntries) {
            entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", "");
            String[] keyValuePairs = entry.split(",");

            Integer cityId = 0;
            String name = "";
            String province = "";
            Integer cityPopulation = 0;
            Integer provincePopulation = 0;
            List<String> airports = new ArrayList<>();

            // Assigning Keys and Values based on the seperating ":" also removes surrounding double quotes
            for (String pair : keyValuePairs) {
                String[] keyAndValue = pair.split(":", 2);
                if (keyAndValue.length == 2) {
                    String key = keyAndValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyAndValue[1].trim().replaceAll("^\"|\"$", "");

                    if (key.equals("province")) {
                        province = value;
                    } else if (key.equals("name")) {
                        name = value;
                    }else if (key.equals("cityId")) {
                        try {
                            cityId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid City ID value: " + value);
                            cityId = 0;
                        }
                    }else if (key.equals("cityPopulation")) {
                        try {
                            cityPopulation = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid City Population value: " + value);
                            cityPopulation = 0;
                        }
                    }else if (key.equals("provincePopulation")) {
                        try {
                            provincePopulation = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid Province Population value: " + value);
                            provincePopulation = 0;
                        }
                    } else if (key.equals("airports")) {
                        // Removing brackets and splitting the list
                        String airportsList = value.replaceAll("^\\[|\\]$", "");
                        String[] airportArray = airportsList.split(",");
                        for (String airport : airportArray) {
                            airports.add(airport.trim().replaceAll("^\"|\"$", ""));
                        }
                    }
                }
            }
            if (!province.isEmpty()) {
                ProvinceData provinceData = provinceDataMap.getOrDefault(province, new ProvinceData(provincePopulation));
                provinceData.addAirports(airports);
                provinceDataMap.put(province, provinceData);
            }
        }

        if (CallReason == 1) {
            System.out.println("Provinces with Population > 1,000,000 and exactly 1 airport:");
            System.out.println();
            int maxNameLength = provinceDataMap.keySet().stream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(15);
            for (Map.Entry<String, ProvinceData> entry : provinceDataMap.entrySet()) {
                String provinceName = entry.getKey();
                ProvinceData data = entry.getValue();
                if (data.getProvincePopulation() > 1000000 && data.getAirports().size() == 1) {
                    int population = entry.getValue().getProvincePopulation();

                    // Calculate the number of dashes needed
                    int dashCount = maxNameLength - provinceName.length() + 5;  // 5 is for spacing consistency

                    // Build the dash separator dynamically
                    String dashes = " ".repeat(1) + "-".repeat(dashCount) + " ";

                    System.out.println(provinceName + dashes + String.format("%,d", population));
                }
            }
            System.out.println();
        }

    }
    // Helper Function
    private static class ProvinceData {
        private int provincePopulation;
        private Set<String> airports;

        public ProvinceData(int provincePopulation) {
            this.provincePopulation = provincePopulation;
            this.airports = new HashSet<>();
        }

        public int getProvincePopulation() {
            return provincePopulation;
        }

        public Set<String> getAirports() {
            return airports;
        }

        public void addAirports(List<String> airportList) {
            airports.addAll(airportList);  // Add all airports from the city to the province set
        }
    }
}
