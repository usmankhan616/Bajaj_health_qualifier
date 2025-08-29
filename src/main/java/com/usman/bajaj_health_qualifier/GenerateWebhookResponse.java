package com.usman.bajaj_health_qualifier;

import lombok.Data;

@Data
public class GenerateWebhookResponse {
    private String webhook;
    private String accessToken;
}