package com.keyin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
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
                case 1:
                    listAllAircraft();
                    break;
                case 2:
                    listAircraftCapacity();
                    break;
                case 3:
                    listAirportsByProvince();
                    break;
                case 4:
                    listProvincesWithMultipleAirports();
                    break;
                case 5:
                    listProvincesNeedingAirports();
                    break;
                case 6:
                    System.out.println("Exiting the application. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        private static void listAllAircraft() {
            sendGetRequest("/aircraft", "Aircraft");
        }

        private static void listAircraftCapacity() {
            sendGetRequest("/aircraft/capacity", "Aircraft capacity");
        }

        private static void listAirportsByProvince() {
            sendGetRequest("/airports/byProvince", "Airports by province");
        }

        private static void listProvincesWithMultipleAirports() {
            sendGetRequest("/provinces/multipleAirports", "Provinces with multiple airports");
        }

        private static void listProvincesNeedingAirports() {
            sendGetRequest("/provinces/needingAirports", "Provinces needing airports");
        }

        private static void sendGetRequest(String endpoint, String resourceName) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + endpoint))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(resourceName + ": " + response.body());
            } catch (Exception e) {
                System.out.println("Error fetching " + resourceName + ": " + e.getMessage());
            }
        }
    }
}