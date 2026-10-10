package com.wildguard.conservation.client;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.wildguard.conservation.error.ApiException;
import com.wildguard.conservation.security.Caller;

// This class does the REST calls from my service to the other services.
// My service does NOT read their tables. An incident or a patrol belongs to operations-service
// and an animal belongs to wildlife-service, so I ask them through their REST API.
//
// The other services are still being built by my team mates, so I made the checks "soft":
//   - the service answers 200  -> the record exists, I take its code from the answer
//   - the service is not running -> I accept the id as it was typed
//   - the service answers 404 -> while wildguard.strict-links is false (now) I also accept it,
//     because the url may simply not be built yet. When the other services are ready we set
//     wildguard.strict-links=true and then a 404 means "does not exist" and I refuse the request.
// Like this my part already works alone and becomes strict when the others are ready.
@Component
public class ServiceGateway {

    private String wildlifeUrl;
    private String operationsUrl;
    private boolean strictLinks;
    private RestClient restClient;

    // the urls come from application.properties
    public ServiceGateway(@Value("${wildguard.services.wildlife-url}") String wildlifeUrl,
            @Value("${wildguard.services.operations-url}") String operationsUrl,
            @Value("${wildguard.strict-links}") boolean strictLinks) {
        this.strictLinks = strictLinks;
        this.wildlifeUrl = wildlifeUrl;
        this.operationsUrl = operationsUrl;

        // dont wait long if the other service is not running
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(4));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    // returns the code to show for an incident (like INC-109)
    public String checkIncident(String incidentId) {
        Map<String, Object> incident = fetch(operationsUrl + "/api/incidents/" + incidentId, "Incident " + incidentId);
        return pick(incident, incidentId, "incidentCode", "code", "incidentId");
    }

    // returns the code to show for an animal (like AN-1045)
    public String checkAnimal(String animalId) {
        Map<String, Object> animal = fetch(wildlifeUrl + "/api/animals/" + animalId, "Animal " + animalId);
        return pick(animal, animalId, "animalCode", "code", "animalId");
    }

    // returns the code to show for a patrol (like PAT-205).
    // a finished or cancelled patrol cannot get a vehicle
    public String checkPatrol(String patrolId) {
        Map<String, Object> patrol = fetch(operationsUrl + "/api/patrols/" + patrolId, "Patrol " + patrolId);
        String code = pick(patrol, patrolId, "patrolCode", "code", "patrolId");
        if (patrol != null && patrol.get("status") != null) {
            String status = patrol.get("status").toString().toUpperCase();
            if (status.equals("COMPLETED") || status.equals("CANCELLED")) {
                throw ApiException.conflict(code + " is " + status.toLowerCase()
                        + "; resources can only be allocated to planned or running patrols.");
            }
        }
        return code;
    }

    // When a rescue case is opened for an incident we tell operations-service to change the
    // incident status to RESCUE_IN_PROGRESS. If that call fails we only print a message,
    // because the rescue case should still be saved.
    public void markIncidentRescueInProgress(String incidentId, String rescueCode) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("status", "RESCUE_IN_PROGRESS");
            body.put("notes", "Rescue case " + rescueCode + " opened.");
            restClient.patch().uri(operationsUrl + "/api/incidents/" + incidentId + "/status")
                    .header("Authorization", "Bearer " + Caller.current().getToken())
                    .body(body).retrieve().toBodilessEntity();
        } catch (Exception ex) {
            System.out.println("Could not update incident " + incidentId + " in operations-service: " + ex.getMessage());
        }
    }

    // ---------- helper methods ----------

    // GET call to another service. We pass on the same JWT token the user sent to us.
    // returns the JSON as a Map, or null when the service could not be asked.
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetch(String url, String what) {
        try {
            return restClient.get().uri(url)
                    .header("Authorization", "Bearer " + Caller.current().getToken())
                    .retrieve().body(Map.class);
        } catch (HttpClientErrorException.NotFound ex) {
            // the service is running and says: not found
            if (strictLinks) {
                throw ApiException.badRequest(what + " does not exist.");
            }
            return null;
        } catch (Exception ex) {
            // service not running or it gave another error -> soft check, we accept the id
            System.out.println("Could not check " + what + " (" + ex.getMessage() + "). The id is saved as typed.");
            return null;
        }
    }

    // takes the first of the given fields that is present in the answer, otherwise the id itself
    private String pick(Map<String, Object> answer, String id, String... fields) {
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
