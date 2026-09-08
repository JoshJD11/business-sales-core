package io.github.joshua.notification;
import io.github.joshua.util.AppConfig;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import com.greenapi.pkg.api.GreenApi;
import com.greenapi.pkg.models.request.OutgoingMessage;


public class WhatsAppNotificationSender implements NotificationSender {

    private final GreenApi greenApi;

    public WhatsAppNotificationSender() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        String instanceId = AppConfig.get("GREENAPI_INSTANCE_ID");
        String token = AppConfig.get("GREENAPI_TOKEN");

        this.greenApi = new GreenApi(
            restTemplate,
            "https://media.green-api.com",
            "https://api.green-api.com",
            instanceId,
            token
        );
    }

    @Override
    public void sendNotification(String recipient,String message) {
        var response = greenApi.sending.sendMessage(
            OutgoingMessage.builder()
                .chatId(recipient)
                .message(message)
                .build()
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Mensaje enviado correctamente.");
        } else {
            System.out.println("Error al enviar mensaje. Código: " + response.getStatusCode());
        }
    }
}
