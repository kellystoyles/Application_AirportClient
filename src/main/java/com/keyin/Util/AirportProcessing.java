package com.keyin.Util;

import java.util.ArrayList;
import java.util.List;
/*
Call Reason Codes
1 - (Option 2.2) Which planes can fly into/out of xyz airport
99 - Testing Purposes
*/
public class AirportProcessing {
    public static void processAirportDataAll(String jsonResponse, Integer CallReason, String targetIATAcode, List<String> aircraftIdList){

        // Ensuring the Data Received is not Empty 2 characters being []
        if (jsonResponse.length() < 2) {
            System.out.println("No data received.");
            return;
        }

        // Removes the Surrounding Brackets "[" and "]"
        String trimmedResponse = jsonResponse.substring(1, jsonResponse.length() - 1);

        // Seperates the Entrys via seperating on },{ each element is now one "airport"
        String[] airportEntries = trimmedResponse.split("\\},\\{");

        // Splits entrys into the KeyValue pairs via the comma seperating each one
        for (String entry : airportEntries) {
            entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", "");
            String[] keyValuePairs = entry.split(",");

            Integer airportId = 0;
            String name = "";
            String iata_code = "";
            List<String> aircraft = new ArrayList<>();

            // Assigning Keys and Values based on the seperating ":" also removes surrounding double quotes
            for (String pair : keyValuePairs) {
                String[] keyAndValue = pair.split(":", 2);
                if (keyAndValue.length == 2) {
                    String key = keyAndValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyAndValue[1].trim().replaceAll("^\"|\"$", "");

                    if (key.equals("iata_code")) {
                        iata_code = value;
                    } else if (key.equals("name")) {
                        name = value;
                    } else if (key.equals("airportId")) {
                        try {
                            airportId = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid airportId value: " + value);
                            airportId = 0;
                        }
                    }else if (key.equals("aircraft")) {
                        // Removing brackets and splitting the list
                        String aircraftList = value.replaceAll("^\\[|\\]$", "");
                        String[] aircraftArray = aircraftList.split(",");
                        for (String aircrafts : aircraftArray) {
                            aircraft.add(aircrafts.trim().replaceAll("^\"|\"$", ""));
                        }
                    }
                }

            }
            if (CallReason == 1 && iata_code.equals(targetIATAcode)) {
                System.out.println(name+" "+iata_code+":");
                aircraftIdList.addAll(aircraft);
                break;
            }
            // Testing Call Reason
            if (CallReason == 99){
                System.out.println("ID: " + airportId);
                System.out.println("IATA: " + iata_code);
                System.out.println("Name: " + name);
                System.out.println("Aircrafts: " + aircraft);
            }
        }
    }
}
