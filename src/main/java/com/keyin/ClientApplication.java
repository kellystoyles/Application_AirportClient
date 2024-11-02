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
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import com.keyin.Util.JsonParser;

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
                    if (choice == 4 && ActiveMenu != 1 && ActiveMenu != 5) {
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
                    3. Check Aircraft Service Date
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
            if (ActiveMenu == 1) {
                switch (choice) {
                    case 1:
                        ActiveMenu = 2;
                        break;
                    case 2:
                        ActiveMenu = 3;
                        break;
                    case 3:
                        ActiveMenu = 4;
                        break;
                    case 4:
                        ActiveMenu = 5;
                        break;
                    case 5:
                        break;
                    default:
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            }
            else if  (ActiveMenu == 2) {
                switch (choice) {
                    case 1:
                        System.out.println("Enter the Aircraft ID for the aircraft you want to update");
                        Scanner scanner = new Scanner(System.in);
                        String endpointId = Integer.toString(getUserChoiceInt(scanner));
                        updateAircraftStatus(endpointId);
                        ActiveMenu = 1;
                        break;
                    case 2:
                        airportTraffic();
                        ActiveMenu = 1;
                        break;
                    case 3:
                        lockdown();
                        ActiveMenu = 1;
                        break;
                    case 4:
                        break;
                    default:
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            } else if (ActiveMenu == 3) {
                switch (choice) {
                    case 1:
                        airportsByPopulation();
                        ActiveMenu = 1;
                        break;
                    case 2:
                        aircraftAccess();
                        ActiveMenu = 1;
                        break;
                    case 3:
                        aircraftServicing();
                        ActiveMenu = 1;
                        break;
                    case 5:
                        break;
                    default:
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            } else if (ActiveMenu == 4) {
                switch (choice) {
                    case 1:
                        airlineNumOfLrgCapacity();
                        ActiveMenu = 1;
                        break;
                    case 2:
                        addNewAircraft();
                        ActiveMenu = 1;
                        break;
                    case 3:
                        System.out.println("Aircraft ID to delete");
                        Scanner scanner = new Scanner(System.in);
                        String idToDelete = Integer.toString(getUserChoiceInt(scanner));
                        deletingUnactiveAircrafts(idToDelete);
                        ActiveMenu = 1;
                        break;
                    case 5:
                        break;
                    default:
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            } else if (ActiveMenu == 5) {
                switch (choice) {
                    case 1:
                        addPassenger();
                        ActiveMenu = 1;
                        break;
                    case 2:
                        removePassenger();
                        ActiveMenu = 1;
                        break;
                    case 3:
                        checkIn();
                        break;
                    case 4:
                        ActiveMenu = 1;
                        break;
                    default:
                        System.out.println("Invalid input. Please enter a valid choice.");
                        break;
                }
            }
        }

        private static void checkIn() {
            System.out.println("Enter your passenger ID:");
            Scanner scanner = new Scanner(System.in);
            String passengerId = Integer.toString(getUserChoiceInt(scanner));

            System.out.println("Enter IATA code for the airport you're checking into:");
            String visitedIATA = getUserChoiceStr(scanner).toUpperCase();

            String requestBody = String.format(
                    "{ \"visited_airports\": [\"%s\"] }",
                    visitedIATA
            );

            String responseBody = sendPatchRequest("/passenger/" + passengerId, requestBody);

            if (responseBody != null) {
                System.out.println("Successfully checked in. Updated visited airports for passenger ID " + passengerId + ".");
            } else {
                System.out.println("Failed to check in or update visited airports.");
            }
        }

        private static void addPassenger(){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Add New Passenger");
            System.out.println("================");

            System.out.print("Enter First Name: ");
            String firstName = scanner.nextLine();

            System.out.print("Enter Last Name: ");
            String lastName = scanner.nextLine();

            System.out.print("Enter Email: ");
            String email = scanner.nextLine();

            String requestBody = String.format(
                    "{ \"first_name\": \"%s\", \"last_name\": \"%s\", \"email\": \"%s\" }",
                    firstName, lastName, email
            );

            String response = sendPostRequest("/passenger", requestBody, "Passenger");

            if (response != null) {
                System.out.println("New passenger added successfully.");
            } else {
                System.out.println("Failed to add new passenger.");
            }
        }

        private static void removePassenger(){
            System.out.println("Enter the Passenger ID to delete:");
            Scanner scanner = new Scanner(System.in);
            String passengerId = Integer.toString(getUserChoiceInt(scanner));

            String response = sendDeleteRequest("/passenger/" + passengerId, "Passenger");

            if (response != null) {
                System.out.println("Passenger with ID " + passengerId + " deleted successfully.");
            } else {
                System.out.println("Failed to delete passenger with ID " + passengerId + ".");
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
                System.out.print(">> ");
                input = getUserChoiceStr(scanner).trim().toUpperCase();

                if (input.equals("EXIT")) {
                    System.out.println("Exiting lockdown. Returning to main menu. \n");
                    break;
                } else {
                    System.out.println("Invalid input. Please type EXIT to leave lockdown.");
                }
            }
        }



        private static void airportsByPopulation() {
            String responseBody = sendGetRequest("/city", "City");
            if (responseBody != null) {
                JsonParser.parseCities(responseBody, 1);
            }
        }

        private static void airlineNumOfLrgCapacity() {
            String responseBody = sendGetRequest("/aircraft", "Aircraft");
            if (responseBody != null) {
                JsonParser.parseAircraft(responseBody, 3);
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

            String status = "Active";

            LocalDate maintenanceDate = LocalDate.now().plusDays(30);

            String requestBody = String.format(
                    "{ \"airline\": \"%s\", \"model\": \"%s\", \"capacity\": %d, \"status\": \"%s\", \"maintenanceDate\": \"%s\" }",
                    airline, model, capacity, status, maintenanceDate
            );

            String response = sendPostRequest("/aircraft", requestBody, "Aircraft");

            if (response != null) {
                System.out.println("New aircraft added successfully.");
            } else {
                System.out.println("Failed to add new aircraft.");
            }
        }

        private static void aircraftServicing(){
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter ID for the Aircraft");
            String aircraftId = getUserChoiceStr(scanner);
            String responseBody = sendGetRequest("/aircraft/" + aircraftId, "Aircraft");
            if (responseBody != null){
                JsonParser.parseAircraft(responseBody, 4);
            }
        }

        private static void airportTraffic(){
            System.out.println("IATA Code for the airport");
            Scanner scanner = new Scanner(System.in);
            String iataToFind = getUserChoiceStr(scanner).toUpperCase();
            String responseBody = sendGetRequest("/passenger/count/" + iataToFind, "Airports");
            if (responseBody != null) {
                JsonParser.parseAirports(responseBody, 4, iataToFind);
            }
        }

        private static void aircraftAccess(){
            String responseBodyAirport = sendGetRequest("/airports", "Airports");
            System.out.println("IATA Code for the airport");
            Scanner scanner = new Scanner(System.in);
            String iataToFind = getUserChoiceStr(scanner).toUpperCase();
            if (responseBodyAirport != null) {
                JsonParser.parseAirports(responseBodyAirport, 1, iataToFind);
            }
        }

        private static void updateAircraftStatus(String id) {
            String responseBody = sendGetRequest("/aircraft/" + id, "Aircraft/" + id);
            String requestBody = "Undefined";
            if (responseBody != null) {
                System.out.println("Enter A, D, or S for (Active, Decommissioned, Sold) respectively ");
                Scanner scanner = new Scanner(System.in);
                String newStatus = getUserChoiceStr(scanner);
                if (newStatus.toUpperCase() == "A") {
                    String newStatusLong = "Active";
                    requestBody = "{\"status\":\"" + newStatusLong + "\"}";
                }else if (newStatus.toUpperCase() == "S") {
                    String newStatusLong = "Sold";
                    requestBody = "{\"status\":\"" + newStatusLong + "\"}";
                }else if (newStatus.toUpperCase() == "D") {
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
                        .DELETE()
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
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 204) {
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
