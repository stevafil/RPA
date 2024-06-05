package ch.zhaw.rpa.arztpraxisuwebhookhandler.restclients;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.ArrayList;

@Component
public class UiPathOrchestratorRestClient {

    private RestTemplate restTemplate;

    @Autowired
    private RestTemplateBuilder builder;

    @Value("${uipath.tenant-name}")
    private String tenantName;

    @Value("${uipath.root-uri}")
    private String rootUri;

    @Value("${uipath.auth-uri}")
    private String authUri;

    @Value("${uipath.client-id}")
    private String clientId;

    @Value("${uipath.user-key}")
    private String userKey;

    @Value("${uipath.folder-id}")
    private String folderId;

    private HttpHeaders httpHeaders;

    @PostConstruct
    public void postConstruct() {
        this.restTemplate = this.builder
            .rootUri(rootUri)
            .build();

        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("X-UIPATH-TenantName", tenantName);
        httpHeaders.set("X-UIPATH-OrganizationUnitId", folderId);
    }

    private void authenticate() {
        // Generate Body
        JSONObject body = new JSONObject();
        body.put("grant_type", "refresh_token");
        body.put("client_id", clientId);
        body.put("refresh_token", userKey);

        RequestEntity<String> requestEntity = RequestEntity
            .post(authUri)
            .headers(httpHeaders)
            .body(body.toString());

        try {
            ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

            JSONObject responseBody = new JSONObject(response.getBody());
            String accessToken = responseBody.getString("access_token");

            httpHeaders.setBearerAuth(accessToken);
            System.out.println("!!!!!!!!! UiPath REST Authentication successful");
        } catch (Exception e) {
            System.out.println("!!!!!!!!! UiPath REST Authentication failed");
            e.printStackTrace();
        }
    }

    public String getReleaseKeyByProcessKey(String processKey) {
        this.authenticate();

        String uri = "/odata/Releases?$filter=ProcessKey eq '" + processKey + "'";

        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
            JSONObject responseBody = new JSONObject(response.getBody());
            Integer resultCount = responseBody.getInt("@odata.count");
            if (resultCount != 1) {
                return "";
            } else {
                String releaseKey = responseBody.getJSONArray("value").getJSONObject(0).getString("Key");
                return releaseKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public Integer startJobAndGetId(String releaseKey, JSONObject inputArguments) {
        this.authenticate();

        String uri = "/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs";

        // Generate Startinfo
        JSONObject startInfo = new JSONObject();
        startInfo.put("ReleaseKey", releaseKey);
        startInfo.put("JobsCount", 1);
        startInfo.put("Source", "Manual");
        startInfo.put("Strategy", "JobsCount");
        startInfo.put("InputArguments", inputArguments.toString());

        // Generate Body
        JSONObject body = new JSONObject();
        body.put("startInfo", startInfo);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            JSONObject responseBody = new JSONObject(response.getBody());

            Integer id = responseBody.getJSONArray("value").getJSONObject(0).getInt("Id");
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<String> getAvailableAppointments(String ahvNumber) {
        this.authenticate();

        String releaseKey = getReleaseKeyByProcessKey("rpa-arztpraxis");

        String uri = rootUri + "/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs";
        
        JSONObject inputArguments = new JSONObject();
        inputArguments.put("ahvNumber", ahvNumber);

        JSONObject startInfo = new JSONObject();
        startInfo.put("ReleaseKey", releaseKey);
        startInfo.put("Strategy", "JobsCount");
        startInfo.put("JobsCount", 1);
        startInfo.put("InputArguments", inputArguments.toString());

        JSONObject body = new JSONObject();
        body.put("startInfo", startInfo);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            JSONObject responseBody = new JSONObject(response.getBody());

            Integer jobId = responseBody.getJSONArray("value").getJSONObject(0).getInt("Id");

            // Polling to get the job result
            JSONObject jobResult = getJobById(jobId, 1000, 60);

            List<String> freieTermine = new ArrayList<>();
            JSONArray termineArray = jobResult.getJSONArray("availableAppointments");

            for (int i = 0; i < termineArray.length(); i++) {
                freieTermine.add(termineArray.getString(i));
            }

            return freieTermine;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public boolean bookAppointment(String termin) {
        this.authenticate();

        String releaseKey = getReleaseKeyByProcessKey("rpa-arztpraxis");

        String uri = rootUri + "/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs";
        
        JSONObject inputArguments = new JSONObject();
        inputArguments.put("termin", termin);

        JSONObject startInfo = new JSONObject();
        startInfo.put("ReleaseKey", releaseKey);
        startInfo.put("Strategy", "JobsCount");
        startInfo.put("JobsCount", 1);
        startInfo.put("InputArguments", inputArguments.toString());

        JSONObject body = new JSONObject();
        body.put("startInfo", startInfo);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            JSONObject responseBody = new JSONObject(response.getBody());

            Integer jobId = responseBody.getJSONArray("value").getJSONObject(0).getInt("Id");

            // Polling to get the job result
            JSONObject jobResult = getJobById(jobId, 1000, 60);

            return jobResult.getBoolean("success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerPatient(String vorname, String nachname, String ahvNumber, String email, String handynummer, String sessionId) {
        this.authenticate();

        String releaseKey = getReleaseKeyByProcessKey("rpa-arztpraxis");

        String uri = rootUri + "/odata/Jobs/UiPath.Server.Configuration.OData.StartJobs";
        
        JSONObject inputArguments = new JSONObject();
        inputArguments.put("vorname", vorname);
        inputArguments.put("nachname", nachname);
        inputArguments.put("ahvNumber", ahvNumber);
        inputArguments.put("email", email);
        inputArguments.put("handynummer", handynummer);
        inputArguments.put("sessionId", sessionId);

        JSONObject startInfo = new JSONObject();
        startInfo.put("ReleaseKey", releaseKey);
        startInfo.put("Strategy", "JobsCount");
        startInfo.put("JobsCount", 1);
        startInfo.put("InputArguments", inputArguments.toString());

        JSONObject body = new JSONObject();
        body.put("startInfo", startInfo);

        HttpEntity<String> request = new HttpEntity<>(body.toString(), httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
            JSONObject responseBody = new JSONObject(response.getBody());

            Integer jobId = responseBody.getJSONArray("value").getJSONObject(0).getInt("Id");

            // Polling to get the job result
            JSONObject jobResult = getJobById(jobId, 1000, 60);

            return jobResult.getBoolean("success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONObject getJobById(Integer id, Integer pollingCycleInMilliseconds, Integer pollingMaxRetries) {
        this.authenticate();

        String uri = rootUri + "/odata/Jobs(" + id + ")";

        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        String jobState = "";
        Integer pollingCounter = 0;
        ResponseEntity<String> response;

        do {
            try {
                Thread.sleep(pollingCycleInMilliseconds);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return null;
            }

            try {
                response = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, String.class);
                JSONObject responseBody = new JSONObject(response.getBody());
                jobState = responseBody.getString("State");
                if (jobState.equals("Successful")) {
                    JSONObject outputArguments = new JSONObject(responseBody.getString("OutputArguments"));
                    return outputArguments;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        } while (!jobState.equals("Successful") && pollingCounter <= pollingMaxRetries);

        return null;
    }
}
