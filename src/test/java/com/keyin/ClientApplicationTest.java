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
    private HttpClient httpClient;

    @InjectMocks
    private ClientApplication.AirportClientApp clientApp;

    // No need for reflection anymore
    @BeforeEach
    public void setUp() {
        clientApp = new ClientApplication.AirportClientApp(httpClient);
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