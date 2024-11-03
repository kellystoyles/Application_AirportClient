package com.keyin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.lang.reflect.Field;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ClientApplicationTest {

    @Mock
    private static HttpClient httpClient = HttpClient.newHttpClient();

    @InjectMocks
    public ClientApplication.AirportClientApp clientApp;

    @BeforeEach
    public void setUp() throws Exception {
        // Use reflection to set the static HttpClient field
        Field httpClientField = ClientApplication.AirportClientApp.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(null, httpClient); // Set the static field to the mocked HttpClient
    }

    @Test
    public void testSendGetRequest() throws Exception {
        // Mock response
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        lenient().when(mockResponse.statusCode()).thenReturn(200);
        lenient().when(mockResponse.body()).thenReturn("{\"data\":\"sample response\"}");

        // Mock httpClient behavior
        lenient().when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Call the method under test
        String response = clientApp.sendGetRequest("/some-endpoint", "SomeResource", "http://mock-url:8080/api");

        // Verify the result
        assertEquals("{\"data\":\"sample response\"}", response);
    }

    @Test
    public void testSendGetRequestWithError() throws Exception {
        // Mock response with an error status code
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        lenient().when(mockResponse.statusCode()).thenReturn(500);
        lenient().when(mockResponse.body()).thenReturn("Internal Server Error");

        // Mock httpClient behavior
        lenient().when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Call the method under test
        String response = clientApp.sendGetRequest("/error-endpoint", "ErrorResource", "http://mock-url:8080/api");

        // Verify the result
        assertNull(response);
    }
}
