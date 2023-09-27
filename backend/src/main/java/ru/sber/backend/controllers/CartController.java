package ru.sber.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sber.backend.entities.Cart;
import ru.sber.backend.services.CartService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Добавляет блюдо в корзину
     *
     * @param cartId id корзины
     * @param dishId id блюда
     * @return корзину с добавленными блюдами
     */
    @PostMapping("/{cartId}/dish/{dishId}")
    public ResponseEntity<?> addProductToCart(@PathVariable long cartId, @PathVariable Long dishId) {

        log.info("Добавление в корзину блюда с id: {}", dishId);

        boolean recordInserted = cartService.addToCart(dishId, cartId);

        if (recordInserted) {
            return ResponseEntity.ok("Товар успешно добавлен в корзину");
        } else {
            return ResponseEntity.badRequest().body("Не удалось добавить товар в корзину");
        }
    }

    /**
     * Получение списка блюд по id корзины
     *
     * @param cartId id корзины
     * @return получение списка блюд
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<?> getDishes(@PathVariable long cartId) {

        log.info("Получение корзины пользователя с id {}", cartId);

        List<Long> dishIdsInCart = cartService.getListOfDishIdsInCart(cartId);

        return ResponseEntity.ok().body(dishIdsInCart);
    }

    /**
     * Удаляет блюдо из корзины
     *
     * @param cartId id корзины
     * @param dishId id блюда
     * @return корзина с внесенными изменениями
     */
    @DeleteMapping("/{cartId}/dish/{dishId}")
    public ResponseEntity<?> deleteDish(@PathVariable long cartId, @PathVariable long dishId) {

        log.info("Удаление из корзины блюда с id: {}", dishId);

        boolean isDeleted = cartService.deleteDish(cartId, dishId);

        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
