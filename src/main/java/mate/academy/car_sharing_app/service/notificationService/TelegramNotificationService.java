package mate.academy.car_sharing_app.service.notificationService;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TelegramNotificationService implements NotificationService {

    private static final String TELEGRAM_API_URL =
            "https://api.telegram.org";

    private final String botToken;
    private final String chatId;
    private final RestClient restClient;

    public TelegramNotificationService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.chat.id}") String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.restClient = RestClient.builder()
                .baseUrl(TELEGRAM_API_URL)
                .build();
    }

    @Override
    public void sendMessage(String message) {
        restClient.post()
                .uri("/bot" + botToken + "/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "chat_id", chatId,
                        "text", message
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
