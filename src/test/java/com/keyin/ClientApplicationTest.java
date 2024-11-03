package com.keyin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
   @Mock
   private HttpResponse<String> httpResponse;
   @InjectMocks
   private ClientApplication.AirportClientApp clientApp;
   @BeforeEach
   public void setUp() throws Exception {
       // Initialize the mocks
       MockitoAnnotations.openMocks(this);
       // Use reflection to set the static HttpClient field in the tested class
       Field httpClientField = ClientApplication.AirportClientApp.class.getDeclaredField("httpClient");
       httpClientField.setAccessible(true);
       httpClientField.set(null, httpClient); // Set the static field to the mocked HttpClient
   }
   @Test
   public void testSendGetRequest_success() throws Exception {
       // Arrange
       String mockResponseBody = "{\"data\":\"sample response\"}";
       when(httpResponse.statusCode()).thenReturn(200);
       when(httpResponse.body()).thenReturn(mockResponseBody);
       when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
               .thenReturn(httpResponse);
       // Act
       String response = clientApp.sendGetRequest("/test-endpoint", "TestResource", "http://mock-url");
       // Assert
       assertNotNull(response);
       assertEquals(mockResponseBody, response);
       verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
   }
   @Test
   public void testSendGetRequest_serverError() throws Exception {
       // Arrange
       when(httpResponse.statusCode()).thenReturn(500);
       when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
               .thenReturn(httpResponse);
       // Act
       String response = clientApp.sendGetRequest("/test-endpoint", "TestResource", "http://mock-url");
       // Assert
       assertNull(response);
       verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
   }
   @Test
   public void testSendGetRequest_exceptionHandling() throws Exception {
       // Arrange
       when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
               .thenThrow(new RuntimeException("Simulated exception"));
       // Act
       String response = clientApp.sendGetRequest("/test-endpoint", "TestResource", "http://mock-url");
       // Assert
       assertNull(response);
       verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
   }
}
