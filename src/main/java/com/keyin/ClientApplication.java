package com.keyin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

// File Imports
import com.keyin.Util.CitiesProcessing;
import com.keyin.Util.AirportProcessing;
import com.keyin.Util.AircraftProccessing;

public class ClientApplication {
    public static void main(String[] args) {
        AirportClientApp.main(args);
    }

    public static class AirportClientApp {
        private static Integer ActiveMenu;
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
            ActiveMenu = 1;
            while (true) {
                try {
                    MenuDisplay();
                    int choice = getUserChoiceInt(scanner);
                    processUserChoice(choice);
                    if (choice == 4 && ActiveMenu != 1) {
                        ActiveMenu = 1;
                    } else if (choice == 5 && ActiveMenu == 1) {
                        System.out.println("Exiting the application. Goodbye!");
                        System.exit(0);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        private static void MenuDisplay() {
            if(ActiveMenu == 1){
                System.out.println("""
                =============================
                          Main Menu
                =============================
                1. Daily Airport Management
                2. Future Airport Management
                3. Airline Management
                4. Passenger Management
                5. Exit
                =============================
                """);
            }
            else if (ActiveMenu == 2) {
                System.out.println("""
                    =============================
                       Daily Airport Management
                    =============================
                    1. Update an aircraft out of service
                    2. List the number of passengers will use airport YYZ daily
                    3. Lockdown
                    4. Back to main menu
                    =============================
                    """);
            }
            else if (ActiveMenu == 3) {
                System.out.println("""
                    =============================
                      Future Airport Management
                    =============================
                    1. List all provinces that have over 1 million people and only 1 major airport
                    2. Which planes can fly into/out of xyz airport
                    3. Aircraft Service Dates
                    4. Back to main menu
                    =============================
                    """);
            } else if (ActiveMenu == 4) {
                System.out.println("""
                    =============================
                      Airline Management
                    =============================
                    1. How many planes have a capacity greater than 180 passengers in each fleet
                    2. Add new aircraft
                    3. Delete any plane that is decommissioned or sold
                    4. Back to main menu
                    =============================
                    """);
            } else if (ActiveMenu == 5) {
                System.out.println("""
                    =============================
                      Passenger Management
                    =============================
                    1. Add a passenger to a flight
                    2. Delete a passenger from a flight
                    3. Checking in
                    4. Back to main menu
                    =============================
                    """);
            }
        }

        private static int getUserChoiceInt(Scanner scanner) {
            while (true) {
                System.out.print("Enter your choice: ");
                if (scanner.hasNextInt()) {
                    System.out.println();
                    return scanner.nextInt();
                } else {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();
                }
            }
        }
        private static String getUserChoiceStr(Scanner scanner) {
            while (true) {
                System.out.print("Enter your choice: ");
                if (scanner.hasNext()) {
                    System.out.println();
                    return scanner.next();
                } else {
                    System.out.println("Invalid input.");
                    scanner.next();
                }
            }
        }

        private static void processUserChoice(int choice) {
            if (ActiveMenu == 1) { // Main Menu Open
                switch (choice) {
                    case 1: // Open Daily Airport Menu
                        ActiveMenu = 2;
                        break;
                    case 2: // Open Future Airport Menu
                        ActiveMenu = 3;
                        break;
                    case 3: // Open Airline Management
                        ActiveMenu = 4;
                        break;
                    case 4: // Open Passenger Management
                        ActiveMenu = 5;
                        break;
                    case 5: // Exit
                        break;
                    default: // Invalid Choice
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            }
            else if  (ActiveMenu == 2) { // Daily Airport Menu
                switch (choice) {
                    case 1: // Update all aircraft out of service #Implemented but untested#
                        System.out.println("Enter the Aircraft ID for the aircraft you want to update");
                        Scanner scanner = new Scanner(System.in);
                        String endpointId = Integer.toString(getUserChoiceInt(scanner));
                        updateAircraftStatus(endpointId);
                        break;
                    case 2: // List the number of passengers will use airport YHZ daily
                       PassengerCountForAirport();
                        break;
                    case 3: // Lockdown
                        lockdown();
                    case 4: // Back To Main
                        break;
                    default: // Invalid Input
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            } else if (ActiveMenu == 3) { // Future Airport Menu
                switch (choice) {
                    case 1: // List all provinces that have over 1 million people and only 1 major airport
                        airportsByPopulation();
                        ActiveMenu = 1;
                        break;
                    case 2: // Which planes cannot fly into/out of xyz airport
                        aircraftAccess();
                        ActiveMenu = 1;
                        break;
                    case 3: // Aircraft Service Dates
                        break;
                    case 5: // Back To Main
                        break;
                    default: // Invalid Input
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            } else if (ActiveMenu == 4) { // Airline Management Menu
                switch (choice) {
                    case 1: // How many planes have a capacity greater than 180 passengers in each fleet
                        airlineNumOfLrgCapacity();
                        break;
                    case 2: // Add new aircraft
                        addNewAircraft(); // Not Complete needs re looking after the data is final
                        break;
                    case 3: // Delete any plane that is decommissioned or sold
                        // If Based on id the following works
                        System.out.println("Aircraft ID to delete");
                        Scanner scanner = new Scanner(System.in);
                        String idToDelete = Integer.toString(getUserChoiceInt(scanner));
                        deletingUnactiveAircrafts(idToDelete);
                        ActiveMenu = 1;
                        break;
                    case 5: // Back To Main
                        break;
                    default: // Invalid Input
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            } else if (ActiveMenu == 5) { // Passenger Management Menu
                switch (choice) {
                    case 1: // List the number of passengers will use airport YHZ daily
                        getPassengerCountForAirport(String airportCode)
                        break;
                    case 2: // Delete a passenger from a flight
                        deletePassenger(Long id)
                        break;
                    case 3: // Add a passenger to a flight
                        addPassenger(Passenger passenger);
                        break;
                    case 5: // Back To Main
                        break;
                    default: // Invalid Input
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            }
        }

        private static void lockdown(){
            System.out.println("\n" + "#".repeat(51));
            System.out.println("#".repeat(51));
            System.out.println("#".repeat(20) + " ".repeat(11) + "#".repeat(20));
            System.out.println("#".repeat(20) + " LOCK DOWN " + "#".repeat(20));
            System.out.println("#".repeat(20) + " TO  LEAVE " + "#".repeat(20));
            System.out.println("#".repeat(20) + " TYPE EXIT " + "#".repeat(20));
            System.out.println("#".repeat(20) + " ".repeat(11) + "#".repeat(20));
            System.out.println("#".repeat(51));
            System.out.println("#".repeat(51) + "\n");
            Scanner scanner = new Scanner(System.in);
            String input;

            while (true) {
                System.out.print(">> "); // Prompt symbol for user input
                input = getUserChoiceStr(scanner).trim().toUpperCase(); // Read and process input

                if (input.equals("EXIT")) {
                    System.out.println("Exiting lockdown. Returning to main menu. \n");
                    break; // Exit the loop and return to the main menu
                } else {
                    System.out.println("Invalid input. Please type EXIT to leave lockdown.");
                }
            }
        }

        private static void airportsByPopulation() {
            String responseBody = sendGetRequest("/city", "City");
            if (responseBody != null) {
                CitiesProcessing.processCityAllData(responseBody, 1);
            }
        }

        private static void airlineNumOfLrgCapacity() {
            String responseBody = sendGetRequest("/aircraft", "Aircraft");
            if (responseBody != null) {
                AircraftProccessing.processAircraftAllData(responseBody, 3);
            }
        }

        private static void addNewAircraft() {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Add New Aircraft");
            System.out.println("================");

            System.out.print("Enter Airline Name: ");
            String airline = scanner.nextLine();

            System.out.print("Enter Aircraft Model: ");
            String model = scanner.nextLine();

            System.out.print("Enter Aircraft Capacity: ");
            int capacity = getUserChoiceInt(scanner);

            // Needs Status Value Might be best to auto assign as "Active"

            // Need to add Airport Access list

            System.out.print("Enter Last Maintenance Date (e.g., 2024-12-01): ");
            String maintenanceSchedule = getUserChoiceStr(scanner);

            // Construct JSON request body
            String requestBody = String.format(
                    "{ \"airline\": \"%s\", \"model\": \"%s\", \"capacity\": %d, \"maintenanceSchedule\": \"%s\" }",
                    airline, model, capacity, maintenanceSchedule
            );

            // Send POST request to create new aircraft
            String response = sendPostRequest("/aircraft", requestBody, "Aircraft");

            if (response != null) {
                // Optionally process the response, e.g., parse the created aircraft details
                System.out.println("New aircraft added successfully.");
            } else {
                System.out.println("Failed to add new aircraft.");
            }
        }


        private static void aircraftAccess(){
            String responseBody = sendGetRequest("/airports", "Airports");
            System.out.println("IATA Code for the airport");
            Scanner scanner = new Scanner(System.in);
            String iataToFind = getUserChoiceStr(scanner).toUpperCase();
            List<String> aircraftList = new ArrayList<>();
            if (responseBody != null) {
                AirportProcessing.processAirportDataAll(responseBody, 1, iataToFind, aircraftList);
            }
            if (!aircraftList.isEmpty()) {
                System.out.println("Aircraft for IATA code " + iataToFind + ": " + aircraftList+ "\n");
            }else {
                System.out.println("No Aircraft found for IATA code " + iataToFind+ "\n");
            }
        }

        private static void updateAircraftStatus(String id) {
            String responseBody = sendGetRequest("/aircraft/" + id, "Aircraft/" + id);
            String requestBody = "Undefined";
            if (responseBody != null) {
                System.out.println("Enter A, D, or S for (Active, Decommissioned, Sold) respectively ");
                Scanner scanner = new Scanner(System.in);
                String newStatus = Integer.toString(getUserChoiceInt(scanner));
                if (newStatus == "A") {
                    String newStatusLong = "Active";
                    requestBody = "{\"status\":\"" + newStatusLong + "\"}";
                }else if (newStatus == "S") {
                    String newStatusLong = "Sold";
                    requestBody = "{\"status\":\"" + newStatusLong + "\"}";
                }else if (newStatus == "D") {
                    String newStatusLong = "Decommissioned";
                    requestBody = "{\"status\":\"" + newStatusLong + "\"}";
                }
                sendPatchRequest("/aircraft/" + id, requestBody);
            } else {
                System.out.println("Aircraft not found");
            }
        }

        private static void deletingUnactiveAircrafts(String aircraftId) {
            String endpoint = "/aircraft/" + aircraftId;
            String responseBody = sendDeleteRequest(endpoint, "Aircraft Id");
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

        private static String sendDeleteRequest(String endpoint, String resourceName) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .DELETE()  // Use DELETE() method here
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 204) {
                    System.out.println("Successfully deleted " + resourceName + ": " + endpoint.substring(endpoint.lastIndexOf('/') + 1));
                    return response.body();
                } else {
                    System.out.println("Error: Received status code " + response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error deleting " + resourceName + ": " + e.getMessage());
                return null;
            }
        }

        private static String sendPatchRequest(String endpoint, String requestBody) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))  // Custom PATCH method
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 204) {  // 204 No Content often indicates success for PATCH
                    return response.body();
                } else {
                    System.out.println("Error: Received status code " + response.statusCode());
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error updating resource: " + e.getMessage());
                return null;
            }
        }

        private static String sendPostRequest(String endpoint, String requestBody, String resourceName) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 201) {
                    System.out.println("Successfully created " + resourceName + ": " + response.body());
                    return response.body();
                } else {
                    System.out.println("Error: Received status code " + response.statusCode() + " while creating " + resourceName);
                    System.out.println("Response Body: " + response.body());
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error creating " + resourceName + ": " + e.getMessage());
                return null;
            }
        }


    }
}
