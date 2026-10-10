package com.wildguard.conservation.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.wildguard.conservation.util.ErrorHandler.AppException;

// REST calls from my service to the other microservices.
// Incidents and patrols belong to operations-service, animals belong to wildlife-service.
// I never read their tables, I ask them through their API.
//
// Those two services are not finished yet, so the check is "soft":
//   service is not running            -> I keep the id as it was typed
//   service says 404 and strict=false -> same, keep the id (the url may not exist yet)
//   service says 404 and strict=true  -> error, the record does not exist
@Component
public class ServiceGateway {

    @Value("${wildguard.services.wildlife-url}")
    private String wildlifeUrl;

    @Value("${wildguard.services.operations-url}")
    private String operationsUrl;

    @Value("${wildguard.strict-links}")
    private boolean strict;

    private RestTemplate rest;

    public ServiceGateway() {
        // wait only 2 seconds for the other service, otherwise my page would hang
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(4));
        rest = new RestTemplate(factory);
    }

    // these three return the code to show (INC-109, AN-1045, PAT-205)
    public String checkIncident(String id, String token) {
        Map<?, ?> incident = get(operationsUrl + "/api/incidents/" + id, "Incident " + id, token);
        return codeOf(incident, id, "incidentCode", "code", "incidentId");
    }

    public String checkAnimal(String id, String token) {
        Map<?, ?> animal = get(wildlifeUrl + "/api/animals/" + id, "Animal " + id, token);
        return codeOf(animal, id, "animalCode", "code", "animalId");
    }

    public String checkPatrol(String id, String token) {
        Map<?, ?> patrol = get(operationsUrl + "/api/patrols/" + id, "Patrol " + id, token);
        String code = codeOf(patrol, id, "patrolCode", "code", "patrolId");
        if (patrol != null && patrol.get("status") != null) {
            String status = patrol.get("status").toString().toUpperCase();
            if (status.equals("COMPLETED") || status.equals("CANCELLED")) {
                throw new AppException(409, code + " is " + status.toLowerCase()
                        + "; resources can only be allocated to planned or running patrols.");
            }
        }
        return code;
    }

    // tell operations-service that the incident now has a rescue running.
    // if this fails the rescue case is still saved, so only a message is printed
    public void markIncidentRescueInProgress(String incidentId, String rescueCode, String token) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("status", "RESCUE_IN_PROGRESS");
            body.put("notes", "Rescue case " + rescueCode + " opened.");
            rest.exchange(operationsUrl + "/api/incidents/" + incidentId + "/status", HttpMethod.PATCH,
                    new HttpEntity<>(body, headers(token)), Void.class);
        } catch (Exception ex) {
            System.out.println("Could not update incident " + incidentId + " in operations-service: " + ex.getMessage());
        }
    }

    // GET call. the user's token is passed on so the other service knows who is asking
    private Map<?, ?> get(String url, String what, String token) {
        try {
            return rest.exchange(url, HttpMethod.GET, new HttpEntity<>(headers(token)), Map.class).getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            if (strict) {
                throw new AppException(400, what + " does not exist.");
            }
            return null;
        } catch (Exception ex) {
            System.out.println("Could not check " + what + " (" + ex.getMessage() + "). The id is saved as typed.");
            return null;
        }
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    // first field that is present in the answer, otherwise the id itself
    private String codeOf(Map<?, ?> answer, String id, String... fields) {
        if (answer != null) {
            for (String field : fields) {
                Object value = answer.get(field);
                if (value != null && !value.toString().isBlank()) {
                    return value.toString();
                }
            }
        }
        return id;
    }
}
