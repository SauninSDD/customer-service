import React, {FC, useState} from 'react';
import {Button, InputNumber, Modal, Tooltip} from "antd";
import {Link} from "react-router-dom";
import CartService from "../../services/cartService";
import {user} from "../../constants/constants";
import { IDish, IDishFromCart} from "../../types/types";
import {useAppDispatch, useAppSelector} from "../../hooks";

interface ModalCardDishProps {
    dish: IDish | IDishFromCart;
    isModalOpen: boolean;
    onClose: () => void;
}

/**
 * Модальное окно с подробной информацией о блюде
 * @constructor
 */
const ModalCardDish: FC<ModalCardDishProps> =
    ({
         dish, isModalOpen, onClose
     }) => {

        const [isLogged] = useState<boolean>(user !== null);
        const dispatch = useAppDispatch();
        const dishFromCartById: IDishFromCart | undefined = useAppSelector((state) => state.cart.cartItems.find(item => item.id === dish.id))
        const handleAddClick = () => {
            isLogged && (
                CartService.addToCart(dish.id, dispatch)
            )
        }

        const handleUpdateAmount = (dishId: number, quantity: number) => {
            const newQuantity = {
                quantity: quantity,
            };
            CartService.updateQuantity(dishId, newQuantity, dispatch)
            if (quantity === 0) {
                CartService.deleteFromCart(dishId, dispatch)
            }
        }
        return (
            <Modal
                title={dish.name}
                open={isModalOpen}
                onCancel={onClose}
                width={1000}
                footer={null}
            >
                <div style={{display: "flex"}}>
                    <div style={{flex: 1}}>
                        <img
                            src={dish.urlImage}
                            alt={'Фото ' + dish.name}
                            style={{
                                width: "100%",
                                height: "400px",
                                maxHeight: "400px",
                                maxWidth: "500px",
                            }}
                        />
                    </div>
                    <div style={{flex: 1, marginLeft: "20px"}}>
                        <p>{dish.weight} гр.</p>
                        <Tooltip
                            placement={"bottom"}
                            title={dish.description}
                        >
                            <Button type={"link"}>
                                <span style={{color: "black", textDecoration: "underline", textUnderlineOffset: "3px"}}>
                                    Состав
                                </span>
                            </Button>
                        </Tooltip>
                        <div>
                            <p>{dish.price} ₽</p>
                            {dishFromCartById ? (
                                <InputNumber
                                    value={dishFromCartById?.quantity ?? 0}
                                    min={0}
                                    onChange={(quantity: number | null) => handleUpdateAmount(dish.id, quantity ?? 0)}
                                />

                            ) : (
                                <Button
                                    onClick={handleAddClick}
                                    type="primary"
                                    danger
                                    shape="round"
                                    size="large"
                                >
                                    {isLogged ? (
                                        <div>Добавить в корзину</div>
                                    ) : (
                                        <Link to="/api/auth/signin">Войти</Link>
                                    )}
                                </Button>
                            )}
                        </div>

                    </div>
                </div>
            </Modal>
        );
    };

export default ModalCardDish;