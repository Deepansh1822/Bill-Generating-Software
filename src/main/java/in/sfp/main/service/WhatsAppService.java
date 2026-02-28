package in.sfp.main.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${whatsapp.access.token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends a document (PDF) to a WhatsApp number using Meta Cloud API.
     * 1. Uploads the PDF to Meta's servers to get a media_id.
     * 2. Sends a document message using that media_id.
     */
    public void sendInvoicePdf(String to, byte[] pdfBytes, String fileName) throws Exception {
        if ("PLACEHOLDER_PHONE_ID".equals(phoneNumberId)) {
            throw new RuntimeException(
                    "WhatsApp API not configured. Please add your Phone ID and Access Token in application.properties.");
        }

        // 1. Upload Media
        String mediaId = uploadMedia(pdfBytes, fileName);

        // 2. Send Document Message
        sendMessage(to, mediaId, fileName);
    }

    private String uploadMedia(byte[] pdfBytes, String fileName) {
        String url = String.format("%s/%s/media", apiUrl, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(accessToken);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        body.add("type", "application/pdf");
        body.add("messaging_product", "whatsapp");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("id");
        } else {
            throw new RuntimeException("Failed to upload media to WhatsApp: " + response.getStatusCode());
        }
    }

    private void sendMessage(String to, String mediaId, String fileName) {
        String url = String.format("%s/%s/messages", apiUrl, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("recipient_type", "individual");
        body.put("to", to);
        body.put("type", "document");

        Map<String, String> document = new HashMap<>();
        document.put("id", mediaId);
        document.put("filename", fileName);
        body.put("document", document);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to send WhatsApp message: " + response.getStatusCode());
        }
    }
}
