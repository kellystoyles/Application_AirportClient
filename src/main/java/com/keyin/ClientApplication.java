package com.keyin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class ClientApplication {
    public static void main(String[] args) {
        AirportClientApp.main(args);
    }

    public static class AirportClientApp {
        private static String BASE_URL;
        private static final HttpClient httpClient = HttpClient.newHttpClient();
        private static final Map<String, String> PROVINCE_ABBREVIATIONS = new HashMap<>();
        static {
            PROVINCE_ABBREVIATIONS.put("Newfoundland and Labrador", "NL");
            PROVINCE_ABBREVIATIONS.put("Prince Edward Island", "PE");
            PROVINCE_ABBREVIATIONS.put("Nova Scotia", "NS");
            PROVINCE_ABBREVIATIONS.put("New Brunswick", "NB");
            PROVINCE_ABBREVIATIONS.put("Quebec", "QC");
            PROVINCE_ABBREVIATIONS.put("Ontario", "ON");
            PROVINCE_ABBREVIATIONS.put("Manitoba", "MB");
            PROVINCE_ABBREVIATIONS.put("Saskatchewan", "SK");
            PROVINCE_ABBREVIATIONS.put("Alberta", "AB");
            PROVINCE_ABBREVIATIONS.put("British Columbia", "BC");
            PROVINCE_ABBREVIATIONS.put("Yukon", "YT");
            PROVINCE_ABBREVIATIONS.put("Northwest Territories", "NT");
            PROVINCE_ABBREVIATIONS.put("Nunavut", "NU");
        }


        static {
            try {
                String serviceType = "_airportserver._tcp.local.";
                JmDNS jmdns = JmDNS.create();

                AtomicReference<String> serverAddress = new AtomicReference<>();
                Object lock = new Object();

                ServiceListener listener = new ServiceListener() {
                    @Override
                    public void serviceAdded(ServiceEvent event) {
                        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent event) {
                    }

                    @Override
                    public void serviceResolved(ServiceEvent event) {
                        ServiceInfo info = event.getInfo();
                        String[] addresses = info.getHostAddresses();
                        if (addresses.length > 0) {
                            String hostAddress = addresses[0];
                            int port = info.getPort();
                            serverAddress.set("http://" + hostAddress + ":" + port + "/api");
                            synchronized (lock) {
                                lock.notifyAll();
                            }
                        }
                    }
                };

                jmdns.addServiceListener(serviceType, listener);

                synchronized (lock) {
                    lock.wait(5000);
                }

                if (serverAddress.get() != null) {
                    BASE_URL = serverAddress.get();
                    System.out.println("Discovered server at: " + BASE_URL);
                } else {
                    System.out.println("Could not discover server. Using default BASE_URL.");
                    BASE_URL = "http://localhost:8080/api";
                }

                jmdns.close();
            } catch (IOException | InterruptedException e) {
                System.out.println("Could not discover server. Using default BASE_URL.");
                BASE_URL = "http://localhost:8080/api";
            }
        }

        public static void main(String[] args) {
            System.out.println("\nWelcome to the Airport Management System!");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    displayMenu();
                    int choice = getUserChoice(scanner);
                    processUserChoice(choice);
                    if (choice == 6) {
                        System.out.println("Exiting the application. Goodbye!");
                        System.exit(0);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        private static void displayMenu() {
            System.out.println("\n1. List all the aircraft in each fleet");
            System.out.println("2. How many passengers can each aircraft hold");
            System.out.println("3. List airports in a province by IATA code");
            System.out.println("4. List all provinces that have more than 1 airport");
            System.out.println("5. List all provinces that have over 1 million people that require additional airports");
            System.out.println("6. Exit\n");
            System.out.print("Choose an option: ");
        }

        private static int getUserChoice(Scanner scanner) {
            while (true) {
                if (scanner.hasNextInt()) {
                    return scanner.nextInt();
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();
                }
            }
        }

        private static void processUserChoice(int choice) {
            switch (choice) {
                case 1: // Working
                    listAllAircraft();
                    break;
                case 2: // Working
                    listAircraftCapacity();
                    break;
                case 3: // Working
                    listAirportsByProvince();
                    break;
                case 4: // Not Working Yet
                    listProvincesWithMultipleAirports();
                    break;
                case 5: // Not Working Yet
                    listProvincesNeedingAirports();
                    break;
                case 6: // Working
                    System.out.println("Exiting the application. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        private static void listAllAircraft() {
            String responseBody = sendGetRequest("/aircraft", "Aircraft");
            if (responseBody != null) {
                processAircraftAllData(responseBody);
            }
        }


        private static void listAircraftCapacity() {
            String responseBody = sendGetRequest("/aircraft", "Aircraft capacity");
            if (responseBody != null) {
                processAircraftCapacityData(responseBody);
            }
        }

        private static void listAirportsByProvince() {
            String citiesResponse = sendGetRequest("/city", "Cities");

            if (citiesResponse != null) {
                processAirportsByProvince(citiesResponse);
            }
        }



        private static void listProvincesWithMultipleAirports() {
            sendGetRequest("/provinces/multipleAirports", "Provinces with multiple airports");
        }

        private static void listProvincesNeedingAirports() {
            sendGetRequest("/provinces/needingAirports", "Provinces needing airports");
        }

        private static String sendGetRequest(String endpoint, String resourceName) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    System.out.println("Error: Received status code " + response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error fetching " + resourceName + ": " + e.getMessage());
                return null;
            }
        }

        private static void processAircraftAllData(String jsonResponse) {
            if (jsonResponse.length() < 2) {
                System.out.println("No data received.");
                return;
            }
            String trimmedResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
            String[] aircraftEntries = trimmedResponse.split("\\},\\{");
            Map<String, List<String>> airlineModelsMap = new HashMap<>();
            for (String entry : aircraftEntries) {
                entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", "");
                String[] keyValuePairs = entry.split(",");
                String airline = "";
                String model = "";
                for (String pair : keyValuePairs) {
                    String[] keyAndValue = pair.split(":", 2);
                    if (keyAndValue.length == 2) {
                        String key = keyAndValue[0].trim().replaceAll("^\"|\"$", "");
                        String value = keyAndValue[1].trim().replaceAll("^\"|\"$", "");

                        if (key.equals("airline")) {
                            airline = value;
                        } else if (key.equals("model")) {
                            model = value;
                        }
                    }
                }
                if (!airline.isEmpty() && !model.isEmpty()) {
                    List<String> models = airlineModelsMap.getOrDefault(airline, new ArrayList<>());
                    if (!models.contains(model)) {
                        models.add(model);
                    }
                    airlineModelsMap.put(airline, models);
                }
            }
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

        private static void processAircraftCapacityData(String jsonResponse) {
            if (jsonResponse.length() < 2) {
                System.out.println("No data received.");
                return;
            }
            String trimmedResponse = jsonResponse.substring(1, jsonResponse.length() - 1);
            String[] aircraftEntries = trimmedResponse.split("\\},\\{");
            for (String entry : aircraftEntries) {
                entry = entry.replaceAll("^\\{", "").replaceAll("\\}$", "");
                String[] keyValuePairs = entry.split(",");
                String capacity = "";
                String model = "";
                for (String pair : keyValuePairs) {
                    String[] keyAndValue = pair.split(":", 2);
                    if (keyAndValue.length == 2) {
                        String key = keyAndValue[0].trim().replaceAll("^\"|\"$", "");
                        String value = keyAndValue[1].trim().replaceAll("^\"|\"$", "");
                        if (key.equals("model")) {
                            model = value;
                        } else if (key.equals("capacity")) {
                            capacity = value;
                        }
                    }
                }
                if (!model.isEmpty() && !capacity.isEmpty()) {
                    System.out.println(model + " - " + capacity);
                }
            }
        }

        private static void processAirportsByProvince(String citiesJson) {
            Scanner scanner = new Scanner(System.in);

            Map<String, List<String>> provinceToAirportsMap = new HashMap<>();

            parseCitiesData(citiesJson, provinceToAirportsMap);

            System.out.println("Available provinces:");
            for (String province : provinceToAirportsMap.keySet()) {
                String abbreviation = PROVINCE_ABBREVIATIONS.get(province);
                if (abbreviation != null) {
                    System.out.println(" - " + province + " (" + abbreviation + ")");
                } else {
                    System.out.println(" - " + province);
                }
            }

            System.out.print("Enter the province name or abbreviation (e.g., 'Ontario' or 'ON', or 'All' to display all provinces): ");
            String provinceInput = scanner.nextLine().trim();

            if (provinceInput.equalsIgnoreCase("All")) {
                displayAirportsByProvince(provinceToAirportsMap);
            } else {
                String provinceName = null;
                for (Map.Entry<String, String> entry : PROVINCE_ABBREVIATIONS.entrySet()) {
                    String fullName = entry.getKey();
                    String abbreviation = entry.getValue();

                    if (fullName.equalsIgnoreCase(provinceInput) || abbreviation.equalsIgnoreCase(provinceInput)) {
                        provinceName = fullName;
                        break;
                    }
                }

                if (provinceName == null) {
                    System.out.println("Province not found: " + provinceInput);
                } else {
                    Map<String, List<String>> filteredMap = new HashMap<>();
                    List<String> airports = provinceToAirportsMap.get(provinceName);
                    if (airports != null) {
                        filteredMap.put(provinceName, airports);
                        displayAirportsByProvince(filteredMap);
                    } else {
                        System.out.println("No airports found in " + provinceName + ".");
                    }
                }
            }
        }




        private static void parseCitiesData(String citiesJson, Map<String, List<String>> provinceToAirportsMap) {
            if (citiesJson.length() < 2) {
                System.out.println("No city data received.");
                return;
            }

            String trimmedResponse = citiesJson.substring(1, citiesJson.length() - 1);

            String[] cityEntries = trimmedResponse.split("\\},\\{");

            for (String entry : cityEntries) {
                if (!entry.startsWith("{")) {
                    entry = "{" + entry;
                }
                if (!entry.endsWith("}")) {
                    entry = entry + "}";
                }

                String province = extractValue(entry, "province");

                String airportsData = extractValue(entry, "airports");
                List<String> airportsList = new ArrayList<>();

                if (airportsData != null && !airportsData.equals("[]")) {
                    airportsData = airportsData.substring(1, airportsData.length() - 1);

                    String[] airportEntries = airportsData.split("\\},\\{");

                    for (String airportEntry : airportEntries) {
                        if (!airportEntry.startsWith("{")) {
                            airportEntry = "{" + airportEntry;
                        }
                        if (!airportEntry.endsWith("}")) {
                            airportEntry = airportEntry + "}";
                        }

                        String airportName = extractValue(airportEntry, "name");
                        String iataCode = extractValue(airportEntry, "iata_code");

                        if (airportName != null && iataCode != null) {
                            String airportInfo = airportName + " (" + iataCode + ")";
                            airportsList.add(airportInfo);
                        }
                    }
                }

                if (province != null && !airportsList.isEmpty()) {
                    List<String> existingAirports = provinceToAirportsMap.getOrDefault(province, new ArrayList<>());
                    existingAirports.addAll(airportsList);
                    provinceToAirportsMap.put(province, existingAirports);
                }
            }
        }

        private static String extractValue(String json, String key) {
            String keyPattern = "\"" + key + "\":";
            int keyIndex = json.indexOf(keyPattern);
            if (keyIndex == -1) {
                return null;
            }
            int valueStart = keyIndex + keyPattern.length();

            char firstChar = json.charAt(valueStart);
            if (firstChar == '\"') {
                int valueEnd = json.indexOf("\"", valueStart + 1);
                if (valueEnd == -1) {
                    return null;
                }
                return json.substring(valueStart + 1, valueEnd);
            } else if (firstChar == '[') {
                int brackets = 1;
                int i = valueStart + 1;
                while (i < json.length() && brackets > 0) {
                    if (json.charAt(i) == '[') brackets++;
                    else if (json.charAt(i) == ']') brackets--;
                    i++;
                }
                return json.substring(valueStart, i);
            } else if (firstChar == '{') {
                int braces = 1;
                int i = valueStart + 1;
                while (i < json.length() && braces > 0) {
                    if (json.charAt(i) == '{') braces++;
                    else if (json.charAt(i) == '}') braces--;
                    i++;
                }
                return json.substring(valueStart, i);
            } else {
                int valueEnd = json.indexOf(",", valueStart);
                if (valueEnd == -1) {
                    valueEnd = json.indexOf("}", valueStart);
                }
                if (valueEnd == -1) {
                    valueEnd = json.length();
                }
                return json.substring(valueStart, valueEnd).trim();
            }
        }


        private static void displayAirportsByProvince(Map<String, List<String>> provinceToAirportsMap) {
            for (Map.Entry<String, List<String>> entry : provinceToAirportsMap.entrySet()) {
                String province = entry.getKey();
                List<String> airports = entry.getValue();

                System.out.println(province + ":");
                for (String airport : airports) {
                    System.out.println("    - " + airport);
                }
                System.out.println();
            }
        }

    }
}
