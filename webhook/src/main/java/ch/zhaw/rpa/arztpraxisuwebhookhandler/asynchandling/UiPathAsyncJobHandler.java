package ch.zhaw.rpa.arztpraxisuwebhookhandler.asynchandling;

import org.json.JSONObject;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import ch.zhaw.rpa.arztpraxisuwebhookhandler.restclients.UiPathOrchestratorRestClient;

@Component
public class UiPathAsyncJobHandler {

    @Autowired
    private UiPathOrchestratorRestClient client;

    @Async
    public void asyncRunUiPathCreatePatientConnector(DialogFlowSessionState sessionState, String vorname, String nachname, String ahvNumber, String email, String handynummer) {
        System.out.println("!!!!!!!!! Release Key angefordert von UiPath");
        String releaseKey = client.getReleaseKeyByProcessKey("rpa-arztpraxis");

        JSONObject inputArguments = new JSONObject();
        inputArguments.put("vorname", vorname);
        inputArguments.put("nachname", nachname);
        inputArguments.put("ahvNumber", ahvNumber);
        inputArguments.put("email", email);
        inputArguments.put("handynummer", handynummer);
        inputArguments.put("sessionId", sessionState.getDialogFlowSessionId());

        System.out.println("!!!!!!!!! Auftrag für Job starten erteilt");
        Integer id = client.startJobAndGetId(releaseKey, inputArguments);

        if (id == 0) {
            System.out.println("!!!!!!!!! Auftrag für Job starten fehlgeschlagen");
            sessionState.setUiPathJobState("failed");
        } else {
            System.out.println("!!!!!!!!! Auftrag für Job starten erfolgreich");
            sessionState.setUiPathJobState("created");
            JSONObject outputArguments = client.getJobById(id, 1000, 60);

            if (outputArguments == null || 
                (outputArguments.has("out_exceptionDescription") && 
                 !outputArguments.isNull("out_exceptionDescription") && 
                 !outputArguments.getString("out_exceptionDescription").isEmpty())) {
                System.out.println("!!!!!!!!! Job fehlgeschlagen");
                sessionState.setUiPathJobState("failed");
                sessionState.setUiPathExceptionMessage(outputArguments == null ? 
                    "Die Registrierung des Patienten ist fehlgeschlagen." : 
                    outputArguments.getString("out_exceptionDescription"));
            } else {
                System.out.println("!!!!!!!!! Job erfolgreich durchgeführt");
                sessionState.setUiPathJobState("successfull");
                sessionState.setOutputArguments(outputArguments);
            }
        }
    }

    @Async
    public void asyncRunUiPathGetAvailableAppointments(DialogFlowSessionState sessionState, String ahvNumber) {
        System.out.println("!!!!!!!!! Release Key angefordert von UiPath");
        String releaseKey = client.getReleaseKeyByProcessKey("rpa-arztpraxis");

        JSONObject inputArguments = new JSONObject();
        inputArguments.put("ahvNumber", ahvNumber);
        inputArguments.put("sessionId", sessionState.getDialogFlowSessionId());

        System.out.println("!!!!!!!!! Auftrag für Job starten erteilt");
        Integer id = client.startJobAndGetId(releaseKey, inputArguments);

        if (id == 0) {
            System.out.println("!!!!!!!!! Auftrag für Job starten fehlgeschlagen");
            sessionState.setUiPathJobState("failed");
        } else {
            System.out.println("!!!!!!!!! Auftrag für Job starten erfolgreich");
            sessionState.setUiPathJobState("created");
            JSONObject outputArguments = client.getJobById(id, 1000, 60);

            if (outputArguments == null || 
                (outputArguments.has("out_exceptionDescription") && 
                 !outputArguments.isNull("out_exceptionDescription") && 
                 !outputArguments.getString("out_exceptionDescription").isEmpty())) {
                System.out.println("!!!!!!!!! Job fehlgeschlagen");
                sessionState.setUiPathJobState("failed");
                sessionState.setUiPathExceptionMessage(outputArguments == null ? 
                    "Die Abrufung der verfügbaren Termine ist fehlgeschlagen." : 
                    outputArguments.getString("out_exceptionDescription"));
            } else {
                System.out.println("!!!!!!!!! Job erfolgreich durchgeführt");
                sessionState.setUiPathJobState("successfull");
                sessionState.setAvailableAppointments(outputArguments);
                // Hier müssen wir die verfügbaren Termine dem Benutzer anzeigen
                System.out.println("Verfügbare Termine: " + outputArguments.toString());
            }
        }
    }

    @Async
    public void asyncRunUiPathBookAppointment(DialogFlowSessionState sessionState, String termin) {
        System.out.println("!!!!!!!!! Release Key angefordert von UiPath");
        String releaseKey = client.getReleaseKeyByProcessKey("rpa-arztpraxis");

        JSONObject inputArguments = new JSONObject();
        inputArguments.put("termin", termin);
        inputArguments.put("sessionId", sessionState.getDialogFlowSessionId());

        System.out.println("!!!!!!!!! Auftrag für Job starten erteilt");
        Integer id = client.startJobAndGetId(releaseKey, inputArguments);

        if (id == 0) {
            System.out.println("!!!!!!!!! Auftrag für Job starten fehlgeschlagen");
            sessionState.setUiPathJobState("failed");
        } else {
            System.out.println("!!!!!!!!! Auftrag für Job starten erfolgreich");
            sessionState.setUiPathJobState("created");
            JSONObject outputArguments = client.getJobById(id, 1000, 60);

            if (outputArguments == null || 
                (outputArguments.has("out_exceptionDescription") && 
                 !outputArguments.isNull("out_exceptionDescription") && 
                 !outputArguments.getString("out_exceptionDescription").isEmpty())) {
                System.out.println("!!!!!!!!! Job fehlgeschlagen");
                sessionState.setUiPathJobState("failed");
                sessionState.setUiPathExceptionMessage(outputArguments == null ? 
                    "Die Buchung des Termins ist fehlgeschlagen." : 
                    outputArguments.getString("out_exceptionDescription"));
            } else {
                System.out.println("!!!!!!!!! Job erfolgreich durchgeführt");
                sessionState.setUiPathJobState("successfull");
                sessionState.setOutputArguments(outputArguments);
            }
        }
    }
}
