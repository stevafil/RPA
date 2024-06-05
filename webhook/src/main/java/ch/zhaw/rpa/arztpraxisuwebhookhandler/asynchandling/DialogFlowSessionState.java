package ch.zhaw.rpa.arztpraxisuwebhookhandler.asynchandling;

import java.util.Date;

import org.json.JSONObject;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DialogFlowSessionState {
    private String DialogFlowSessionId;
    private Date DialogFlowFirstRequestReceived;
    private String uiPathJobState;
    private JSONObject outputArguments;
    private String uiPathExceptionMessage;
    private String ahvNumber; // hinzufügen

    private JSONObject availableAppointments;

    public void setAvailableAppointments(JSONObject availableAppointments) {
        this.availableAppointments = availableAppointments;
    }

    public JSONObject getAvailableAppointments() {
        return this.availableAppointments;
    }
}
