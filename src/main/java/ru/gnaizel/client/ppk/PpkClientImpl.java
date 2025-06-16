package ru.gnaizel.client.ppk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.gnaizel.exception.ClientValidationException;

@Service
@RequiredArgsConstructor
public class PpkClientImpl implements PpkClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getHtmlScheduleForPpkSite() {
        String url = "https://ppk.sstu.ru/schedule/";
        String html = restTemplate.getForObject(url, String.class);

        if (html == null || html.isEmpty()) {
            throw new ClientValidationException("Html empty or not loaded");
        }
        return html;
    }
}
