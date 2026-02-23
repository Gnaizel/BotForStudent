package org.example.scraper.client;

import org.example.scraper.exception.ResponseValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PpkClient {

    private final String ppkUrl;
    private final RestTemplate restTemplate;

    public PpkClient(String ppkUrl) {
        this.ppkUrl = ppkUrl;
        this.restTemplate = new RestTemplate();
    }

    public String getHtmlPpk() throws ResponseValidationException {
        ResponseEntity<String> response = restTemplate.getForEntity(ppkUrl, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseValidationException("Can't get PPK HTML, status: " + response.getStatusCode());
        }

        String htmlPpk = response.getBody();

        if (htmlPpk == null || htmlPpk.isEmpty()) {
            throw new ResponseValidationException("HTML from response is empty");
        }

        return htmlPpk;
    }
}
