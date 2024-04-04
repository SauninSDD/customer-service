package ru.sber.backend.clients.orders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.sber.backend.entities.OrderToken;
import ru.sber.backend.models.Message;
import ru.sber.backend.models.OrderResponse;
import ru.sber.backend.services.JwtService;
import ru.sber.backend.services.OrderTokenService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderServiceClient orderServiceClient;

    private final JwtService jwtService;
    private final OrderTokenService orderTokenService;


    @Autowired
    public OrderServiceImpl(OrderServiceClient orderServiceClient, JwtService jwtService
            , OrderTokenService orderTokenService) {
        this.orderServiceClient = orderServiceClient;
        this.jwtService = jwtService;
        this.orderTokenService = orderTokenService;
    }

    @Override
    public List<OrderResponse> getOrdersByClientId() {
        checkAndUpdateOrderTokens();
        List<OrderToken> orderToken = orderTokenService.findAll();
        Jwt jwt = jwtService.getJwtSecurityContext();
        return orderServiceClient.getOrdersByClientId("Bearer " + orderToken.get(0).getAccessToken()
                , jwtService.getSubClaim(jwt));
    }

    @Override
    public Long createOrder(OrderResponse orderResponse) {
        log.info("order: {}", orderResponse);
        checkAndUpdateOrderTokens();
        List<OrderToken> orderToken = orderTokenService.findAll();
        orderResponse.setClientId(jwtService.getSubClaim(jwtService.getJwtSecurityContext()));
        orderResponse.setClientName(jwtService.getPreferredUsernameClaim(jwtService.getJwtSecurityContext()));
        orderResponse.setClientPhoneNumber(jwtService.getPhoneNumberClaim(jwtService.getJwtSecurityContext()));
        log.info("token {}", orderToken.get(0).getAccessToken());

        return orderServiceClient.createOrder("Bearer " + orderToken.get(0).getAccessToken(), orderResponse);
    }

    @Override
    public void paymentOfOrderById(Long orderId) {
        checkAndUpdateOrderTokens();
        List<OrderToken> orderToken = orderTokenService.findAll();
        orderServiceClient.paymentOfOrderById("Bearer " + orderToken.get(0).getAccessToken(), orderId);
    }

    @Override
    public void cancelOrder(Long orderId, Message cancelReason) {
        checkAndUpdateOrderTokens();
        List<OrderToken> orderToken = orderTokenService.findAll();
        orderServiceClient.cancelOrder("Bearer " + orderToken.get(0).getAccessToken(),
                orderId, cancelReason);
    }

    private void checkAndUpdateOrderTokens() {
        List<OrderToken> orderToken = orderTokenService.findAll();

        if (orderToken.isEmpty()) {
            log.info("Токен заказа отсутствует, отправка запроса на получение токена");
            updateOrderTokens();
        } else {
            LocalDateTime currentDateTime = LocalDateTime.now();

            if (orderToken.stream().allMatch(token -> currentDateTime.isBefore(token.getTokenExpiration()))) {
                log.info("Список токенов не пуст и токен действителен. Нет необходимости обновлять токен заказа");
            } else {
                log.info("Список токенов содержит токен с истекшим сроком действия. Отправка запроса на получение токена");
                updateOrderTokens();
            }
        }
    }

    @Transactional
    public void updateOrderTokens() {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
        tokenBody.add("grant_type", "password");
        tokenBody.add("client_id", "login-app");
        tokenBody.add("username", "user");
        tokenBody.add("password", "password");

        HttpEntity<MultiValueMap<String, String>> tokenEntity = new HttpEntity<>(tokenBody, tokenHeaders);

        try {
            ResponseEntity<String> tokenResponseEntity = new RestTemplate().exchange(
                    "http://localhost:8080/realms/order-realm/protocol/openid-connect/token",
                    HttpMethod.POST, tokenEntity, String.class);

            String jsonResponse = tokenResponseEntity.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            String accessToken = jsonNode.get("access_token").asText();

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(14);

            OrderToken newToken = new OrderToken(1, accessToken, expirationTime);

            orderTokenService.save(newToken);
            log.info("Токен заказа был обновлён успешно");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
