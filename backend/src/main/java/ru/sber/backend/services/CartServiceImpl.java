package ru.sber.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.backend.entities.Cart;
import ru.sber.backend.entities.CartItem;
import ru.sber.backend.entities.User;
import ru.sber.backend.repositories.CartItemRepository;
import ru.sber.backend.repositories.CartRepository;
import ru.sber.backend.repositories.ClientRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ClientRepository clientRepository;
    private final CartItemRepository cartItemRepository;
    private final JwtService jwtService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ClientRepository clientRepository, CartItemRepository cartItemRepository, JwtService jwtService) {
        this.cartRepository = cartRepository;
        this.clientRepository = clientRepository;
        this.cartItemRepository = cartItemRepository;
        this.jwtService = jwtService;
    }


    @Override
    public boolean addToCart(long dishId) {
        String clientId = getIdClient();

        Optional<Cart> cart = cartRepository.findCartByClient_Id(clientId);

        Cart shoppingCart = cart.orElseGet(() -> {
            Optional<User> user = clientRepository.findById(clientId);
            if (user.isPresent()) {
                Cart newCart = new Cart();
                newCart.setClient(user.get());
                log.info("Создание корзины пользователя");
                return cartRepository.save(newCart);
            }

            return null;
        });

        if (shoppingCart != null) {
            Optional<CartItem> cartItem = shoppingCart.getCartItems().stream()
                    .filter(item -> item.getDishId() == dishId)
                    .findFirst();

            if (cartItem.isPresent()) {
                CartItem existingCartItem = cartItem.get();
                existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
            } else {
                CartItem newCartItem = new CartItem();
                newCartItem.setCart(shoppingCart);
                newCartItem.setDishId(dishId);
                newCartItem.setQuantity(1);
                shoppingCart.getCartItems().add(newCartItem);
            }

            cartRepository.save(shoppingCart);

            return true;
        }

        return false;
    }


    @Override
    @Transactional
    public boolean deleteDish(long dishId) {
        Optional<Cart> cart = cartRepository.findCartByClient_Id(getIdClient());
        if (cart.isPresent()) {
            cartItemRepository.deleteCartItemByCartIdAndDishId(cart.get().getId(), dishId);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteAllDish() {
        Optional<Cart> cart = cartRepository.findCartByClient_Id(getIdClient());
        if (cart.isPresent()) {
            cartItemRepository.deleteAllByCart_Id(cart.get().getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateDishAmount(long dishId, int quantity) {
        Optional<Cart> cart = cartRepository.findCartByClient_Id(getIdClient());

        if (cart.isPresent()) {
            Cart shoppingCart = cart.get();
            List<CartItem> cartItems = shoppingCart.getCartItems();

            Optional<CartItem> cartItemToUpdate = cartItems.stream()
                    .filter(item -> item.getDishId() == dishId)
                    .findFirst();

            cartItemToUpdate.ifPresent(item -> {
                item.setQuantity(quantity);
                cartRepository.save(shoppingCart);
            });

            return cartItemToUpdate.isPresent();
        }

        return false;
    }

    @Override
    public void deleteCart() {
        Optional<Cart> cart = cartRepository.findCartByClient_Id(getIdClient());

        if (cart.isPresent()) {
            long cartId = cart.get().getId();
            cartItemRepository.deleteAll();
            cartRepository.deleteById(cartId);
        }
    }

    @Override
    public List<Long> getListOfDishIdsInCart() {
        Optional<Cart> cart = cartRepository.findCartByClient_Id(getIdClient());

        if (cart.isPresent()) {
            Cart shoppingCart = cart.get();

            return shoppingCart.getCartItems()
                    .stream()
                    .map(CartItem::getDishId)
                    .toList();
        }
        return Collections.emptyList();

    }

    @Override
    public List<CartItem> getCartItemsByCartId() {
        Optional<Cart> cart = cartRepository.findCartByClient_Id(getIdClient());

        if (cart.isPresent()) {
            return cartItemRepository.findByCartId(cart.get().getId());
        }
        return Collections.emptyList();
    }

    private String getIdClient() {
        Jwt jwt = jwtService.getJwtSecurityContext();
        return jwtService.getSubClaim(jwt);
    }

}
