package com.keyin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AirportClientAppTest {

    @Mock
    private HttpClient httpClient;

    private AirportClientApp clientApp;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create an instance of AirportClientApp with the mocked httpClient
        clientApp = new AirportClientApp(httpClient);
    }

    @Test
    public void testSendGetRequest() throws Exception {
        // Mock response
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"data\":\"sample response\"}");

        // Mock httpClient behavior
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // Call the method under test
        String response = clientApp.sendGetRequest("/some-endpoint", "SomeResource", "http://mock-url:8080/api");

        // Verify the result
        assertEquals("{\"data\":\"sample response\"}", response);
    }
}
