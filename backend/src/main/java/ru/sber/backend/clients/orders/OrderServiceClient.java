package ru.sber.backend.clients.orders;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.sber.backend.models.Message;
import ru.sber.backend.models.OrderResponse;

import java.util.List;

/**
 * Интерфейс для взаимодействия с сервисом заказов
 */
@FeignClient(value = "feignOrders", url = "http://localhost:8083/orders")
public interface OrderServiceClient {
    /**
     * Получает заказы клиента по ID клиента у сервиса заказов
     *
     * @param clientId ID клиента
     * @return список заказов клиента
     */
    @GetMapping(value = "/client/{clientId}", produces = "application/json")
    List<OrderResponse> getOrdersByClientId(@RequestHeader("Authorization") String bearerToken, @PathVariable String clientId);

    /**
     * Создает заказ клиента и передает его сервису заказов
     *
     * @param orderResponse объект заказа
     * @return объект созданного заказа
     */
    @PostMapping
    Long createOrder(@RequestHeader("Authorization") String bearerToken, @RequestBody OrderResponse orderResponse);

    /**
     * Оплачивает заказ
     *
     * @param orderId ID оплачеваемого заказа
     */
    @PutMapping(value = "/{orderId}/payment", consumes = "application/json")
    void paymentOfOrderById(@RequestHeader("Authorization") String bearerToken, @PathVariable Long orderId);

    /**
     * Отменяет заказ, обращаясь к сервису заказов
     *
     * @param orderId      ID заказа
     * @param cancelReason причина отмены заказа
     */
    @PutMapping(value = "/{orderId}/cancel", consumes = "application/json")
    void cancelOrder(@RequestHeader("Authorization") String bearerToken, @PathVariable Long orderId, @RequestBody Message cancelReason);
}


