package ru.sber.backend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.sber.backend.clients.restaurants.RestaurantService;
import ru.sber.backend.entities.CartItem;
import ru.sber.backend.models.DishFromCart;
import ru.sber.backend.services.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для обработки запросов к корзине клиента
 */
@Slf4j
@RestController
@RequestMapping("cart")
public class CartController {

    private final CartService cartService;

    private final RestaurantService restaurantService;

    @Autowired
    public CartController(CartService cartService, RestaurantService restaurantService) {
        this.cartService = cartService;
        this.restaurantService = restaurantService;
    }

    /**
     * Получает список блюд по id корзины
     *
     * @return получение списка блюд
     */
    @PreAuthorize("hasRole('client_user')")
    @GetMapping()
    public ResponseEntity<List<DishFromCart>> getDishesCart() {
        List<CartItem> cartItems = cartService.getCartItemsByCartId();

        log.info("Получаем список id блюд в корзине");
        List<Long> listDishesIds = cartService.getListOfDishIdsInCart();
        String stringWithDishesIds = listDishesIds.toString()
                .substring(1, listDishesIds.toString().length() - 1).replaceAll("\\s", "");

        log.info("Получаем список блюд в корзине по строке с id блюд: {}", stringWithDishesIds);

        if(stringWithDishesIds.isEmpty()){
            return ResponseEntity.ok().body(new ArrayList<>());
        }

        List<DishFromCart> dishes = restaurantService.getListDishesById(stringWithDishesIds).stream()
                .peek(dish -> {
                    CartItem cartItem = cartItems.stream()
                            .filter(item -> item.getDishId() == dish.getId())
                            .findFirst()
                            .orElse(null);

                    if (cartItem != null) {
                        dish.setQuantity(cartItem.getQuantity());
                    } else {
                        dish.setQuantity(0);
                    }
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(dishes);
    }


    /**
     * Добавляет блюдо в корзину
     *
     * @param dishId id блюда
     * @return корзину с добавленными блюдами
     */
    @PreAuthorize("hasRole('client_user')")
    @PostMapping("/dish/{dishId}")
    public ResponseEntity<String> addProductToCart(@PathVariable Long dishId) {
        log.info("Добавление в корзину блюда с id: {}", dishId);
        boolean recordInserted = cartService.addToCart(dishId);
        if (recordInserted) {
            return ResponseEntity.ok("Блюдо успешно добавлено в корзину");
        } else {
            return ResponseEntity.badRequest().body("Не удалось добавить блюдо в корзину");
        }
    }

    /**
     * Обновляет количество блюда в корзине
     *
     * @param dishId id блюда
     * @param dish   блюда, у которого изменяется количество
     * @return корзину с измененным количеством блюда
     */
    @PreAuthorize("hasRole('client_user')")
    @PutMapping("/dish/{dishId}")
    public ResponseEntity<String> updateCartItemQuantity(@PathVariable long dishId, @RequestBody CartItem dish) {
        log.info("Изменяется количество товара на {} в корзине", dish.getQuantity());
        boolean recordUpdated = cartService.updateDishAmount(dishId, dish.getQuantity());

        if (recordUpdated) {
            return ResponseEntity.ok("Количество блюд изменено");
        } else {
            return ResponseEntity.badRequest().body("Не удалось изменить блюдо");
        }
    }

    /**
     * Удаляет блюдо из корзины
     *
     * @param dishId id блюда
     * @return корзина с внесенными изменениями
     */
    @PreAuthorize("hasRole('client_user')")
    @DeleteMapping("/dish/{dishId}")
    public ResponseEntity<String> deleteDish(@PathVariable long dishId) {

        log.info("Удаление из корзины блюда с id: {}", dishId);

        boolean isDeleted = cartService.deleteDish(dishId);

        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().body("Не удалось удалить блюдо");
        }
    }
}
