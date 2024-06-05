package ch.zhaw.rpa.arztpraxisuwebhookhandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Hauptklasse für die RestService-Template-SpringBoot-Applikation
 * 
 * @author scep
 */
@SpringBootApplication
@EnableAsync
public class ArztPraxisWebhookHandlerApplication {
    public static void main(String[] args){
        SpringApplication.run(ArztPraxisWebhookHandlerApplication.class, args);
    }
}
