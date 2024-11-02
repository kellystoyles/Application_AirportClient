package com.keyin.Util;

import java.util.ArrayList;
import java.util.List;

public class AirportProcessing {
    public static void processAirportDataAll(String jsonResponse, Integer CallReason, String targetIATAcode) {

        // Ensure the data received is not empty
        if (jsonResponse.length() < 2) {
            System.out.println("No data received.");
            return;
        }

        // Remove the surrounding brackets "[" and "]"
        String trimmedResponse = jsonResponse.substring(1, jsonResponse.length() - 1);

        // Split each airport entry by "},{", treating each airport JSON object separately
        String[] airportEntries = trimmedResponse.split("\\},\\{");

        for (String entry : airportEntries) {
            entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", "");
            String[] keyValuePairs = entry.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by commas outside of quotes

            Integer airportId = 0;
            String name = "";
            String iata_code = "";
            List<String> aircraft = new ArrayList<>();

            for (String pair : keyValuePairs) {
                String[] keyAndValue = pair.split(":", 2);
                if (keyAndValue.length == 2) {
                    String key = keyAndValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyAndValue[1].trim().replaceAll("^\"|\"$", "");

                    if (key.equals("iata_code")) {
                        iata_code = value.toUpperCase(); // Normalize IATA code for comparison
                    } else if (key.equals("name")) {
                        name = value;
                    } else if (key.equals("airportId")) {
                        try {
                            airportId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid airportId value: " + value);
                            airportId = 0;
                        }
                    } else if (key.equals("aircraft")) {
                        System.out.println("Parsing aircraft list for airport: " + name); // Debug print

                        // Strip the outer brackets of the aircraft array and split by "},{" to separate aircraft objects
                        if (value.startsWith("[") && value.endsWith("]")) {
                            String aircraftList = value.substring(1, value.length() - 1).trim();
                            String[] aircraftObjects = aircraftList.split("\\},\\s*\\{");

                            for (String aircraftObj : aircraftObjects) {
                                aircraftObj = aircraftObj.replaceAll("^\\{|\\}$", "").trim();

                                Integer aircraftId = null;
                                String model = "";

                                // Split properties within each aircraft object
                                String[] aircraftProperties = aircraftObj.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                                for (String property : aircraftProperties) {
                                    String[] propKeyValue = property.split(":", 2);
                                    if (propKeyValue.length == 2) {
                                        String propKey = propKeyValue[0].trim().replaceAll("^\"|\"$", "");
                                        String propValue = propKeyValue[1].trim().replaceAll("^\"|\"$", "");

                                        if (propKey.equals("aircraftId")) {
                                            try {
                                                aircraftId = Integer.parseInt(propValue);
                                            } catch (NumberFormatException e) {
                                                System.out.println("Invalid aircraftId value: " + propValue);
                                            }
                                        } else if (propKey.equals("model")) {
                                            model = propValue;
                                        }
                                    }
                                }

                                // Add each parsed aircraft to the list
                                if (aircraftId != null && !model.isEmpty()) {
                                    System.out.println("Adding aircraft: " + model + " (" + aircraftId + ")"); // Debug print
                                    aircraft.add(model + " (" + aircraftId + ")");
                                }
                            }
                        }
                    }
                }
            }

            // Display output when CallReason matches and IATA code is correct
            if (CallReason == 1 && iata_code.equals(targetIATAcode.toUpperCase())) {
                System.out.println(name + " " + iata_code + ":");

                if (!aircraft.isEmpty()) {
                    for (String aircraftDetail : aircraft) {
                        System.out.println("    - " + aircraftDetail);
                    }
                } else {
                    System.out.println("No aircraft data available for " + name + " " + iata_code);
                }
                break; // Stop after finding the target airport
            }

            // Testing output for each airport if CallReason is 99
            if (CallReason == 99) {
                System.out.println("Aircraft list for " + name + ": " + aircraft); // Debug print
            }
        }

        // Final debug print of the entire JSON response (for troubleshooting)
        if (CallReason == 99) {
            System.out.println("Full JSON response: " + jsonResponse);
        }
    }
}
