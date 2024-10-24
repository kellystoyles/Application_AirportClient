package com.keyin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;


public class ClientApplication {
    public static void main(String[] args) {
        AirportClientApp.main(args);
    }

    public static class AirportClientApp {
        private static final String BASE_URL = "http://localhost:8080/api";
        private static final HttpClient httpClient = HttpClient.newHttpClient();

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
                    scanner.next(); // Consume invalid input
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
            sendGetRequest("/Aircraft", "Aircraft");
        }

        private static void listAircraftCapacity() {
            sendGetRequest("/Aircraft/capacity", "Aircraft capacity");
        }

        private static void listAirportsByProvince() {
            sendGetRequest("/Airports/byProvince", "Airports by province");
        }

        private static void listProvincesWithMultipleAirports() {
            sendGetRequest("/Provinces/multipleAirports", "Provinces with multiple airports");
        }

        private static void listProvincesNeedingAirports() {
            sendGetRequest("/Provinces/needingAirports", "Provinces needing airports");
        }

        private static void sendGetRequest(String endpoint, String resourceName) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(AirportClientApp.BASE_URL + endpoint))
                        .GET()
                        .build();
                HttpResponse<String> response = AirportClientApp.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(resourceName + ": " + response.body());
            } catch (Exception e) {
                System.out.println("Error fetching " + resourceName + ": " + e.getMessage());
            }
        }
    }
}











