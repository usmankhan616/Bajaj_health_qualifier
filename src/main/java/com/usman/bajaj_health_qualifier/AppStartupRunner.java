package com.usman.bajaj_health_qualifier;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppStartupRunner implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Application is running. Beginning the process...");

        GenerateWebhookResponse webhookResponse = generateWebhook();

        if (webhookResponse == null || webhookResponse.getAccessToken() == null) {
            System.err.println("unable to get the webhook and token. Stopping the process.");
            return;
        }
        System.out.println("Successfully received webhook URL and access token.");

        String yourRegNo = "REG22BCE9398";
        String finalSqlQuery = getFinalSqlQuery(yourRegNo);
        System.out.println("Determined the final SQL query.");

        submitSolution(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), finalSqlQuery);
    }

    private GenerateWebhookResponse generateWebhook() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        GenerateWebhookRequest requestBody = new GenerateWebhookRequest(
                "Tajuddin Usman Khan",
                "REG22BCE9398",
                "usman.22bce9398@vitapstudent.ac.in"
        );

        try {
            ResponseEntity<GenerateWebhookResponse> response = restTemplate.postForEntity(
                    url,
                    requestBody,
                    GenerateWebhookResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                System.err.println("Error calling generateWebhook API: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("An exception occurred during generateWebhook call: " + e.getMessage());
            return null;
        }
    }

    private String getFinalSqlQuery(String registrationNumber) {
        int lastTwoDigits = Integer.parseInt(registrationNumber.substring(registrationNumber.length() - 2));

        String sqlQuery;

        if (lastTwoDigits % 2 != 0) {
            System.out.println("Registration number ends in ODD digits. Solving Question 1.");
            sqlQuery = "SELECT patient.id, patient.name, patient.email, patient.phone_number, patient.gender, patient.date_of_birth, patient.address, patient.pincode, doctor.id AS doctor_id, doctor.name AS doctor_name, doctor.specialization, appointment.appointment_date_time, appointment.status FROM patient JOIN appointment ON patient.id = appointment.patient_id JOIN doctor ON doctor.id = appointment.doctor_id WHERE appointment.status = 'SCHEDULED' ORDER BY appointment.appointment_date_time ASC;";
        } else {
            System.out.println("Registration number ends in EVEN digits. Solving Question 2.");
            sqlQuery = "WITH RankedDoctors AS ( SELECT d.id AS doctor_id, d.name AS doctor_name, d.specialization, COUNT(a.id) AS appointment_count, ROW_NUMBER() OVER(PARTITION BY d.specialization ORDER BY COUNT(a.id) DESC) as rn FROM doctor d JOIN appointment a ON d.id = a.doctor_id WHERE a.status = 'COMPLETED' GROUP BY d.id, d.name, d.specialization ) SELECT doctor_id, doctor_name, specialization, appointment_count FROM RankedDoctors WHERE rn = 1 ORDER BY appointment_count DESC, doctor_name ASC;";
        }

        System.out.println("Final SQL Query: \n" + sqlQuery);
        return sqlQuery;
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        String url = webhookUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        SubmitSolutionRequest requestBody = new SubmitSolutionRequest(finalQuery);

        HttpEntity<SubmitSolutionRequest> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Successfully submitted the solution!");
                System.out.println("Response: " + response.getBody());
            } else {
                System.err.println("Error submitting solution: " + response.getStatusCode());
                System.err.println("Response Body: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("An exception occurred during solution submission: " + e.getMessage());
        }
    }
}
