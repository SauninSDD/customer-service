package ru.sber.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.sber.backend.clients.orders.OrderService;
import ru.sber.backend.models.Message;
import ru.sber.backend.models.OrderResponse;
import ru.sber.backend.services.CartService;
import ru.sber.backend.services.EmailService;

import java.util.List;

/**
 * Контроллер для обработки запросов к заказу клиента
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final EmailService emailService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, EmailService emailService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.emailService = emailService;
    }

    /**
     * Получает список всех заказов клиента по ID
     *
     * @return получение списка заказов клиента
     */
    @GetMapping("/client")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<List<OrderResponse>> getOrdersByClientId() {
        log.info("Обращаемся к серверу заказов для получения истории пользователя");
        List<OrderResponse> listOrders = orderService.getOrdersByClientId();
        log.info("Выводим историю пользователя. История: {}", listOrders);
        return ResponseEntity.ok().body(listOrders);
    }

    /**
     * Создает заказ
     *
     * @param order заказ клиента
     * @return статус запроса
     */
    @PostMapping
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<Long> createOrder(@RequestBody OrderResponse order) {
        log.info("Создаем заказ клиента {}", order);
        Long idCreatedOrder = orderService.createOrder(order);
        log.info("Id созданного заказа: {}", idCreatedOrder);

        try {
            emailService.sendOrderConfirmation(order);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String clientId = order.getClientId();
        log.info("Очищаем корзину пользователся с Id: {}", clientId);
        cartService.deleteAllDish();
        log.info("id order {}", idCreatedOrder);
        return ResponseEntity.ok().body(idCreatedOrder);
    }

    /**
     * Оплачивает заказ по его ID
     *
     * @param orderId ID заказа
     * @return статус выполнения операции
     */
    @PutMapping("/{orderId}/payment")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<?> paymentOfOrderById(@PathVariable Long orderId) {
        log.info("Оплачиваем заказ с id {}", orderId);
        orderService.paymentOfOrderById(orderId);

        return ResponseEntity.accepted().build();
    }

    /**
     * Отменяет заказ по его ID
     *
     * @param orderId      ID заказа, который нужно отменить
     * @param cancelReason Причина отмены заказа
     * @return статус выполнения операции
     */
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('client_user')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, @RequestBody Message cancelReason) {
        log.info("Отменяет заказ с id: {}", orderId);

        orderService.cancelOrder(orderId, cancelReason);

        return ResponseEntity.accepted().build();
    }
}
