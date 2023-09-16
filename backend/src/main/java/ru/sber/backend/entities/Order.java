package ru.sber.backend.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sber.backend.entities.enums.EStatusOrder;
import ru.sber.backend.entities.enums.ETypeOrder;

import java.time.LocalDateTime;

@Data
@Table(name = "orders")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long numberOrder;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    private String street;

    private int apartmentNumber;

    private int entranceNumber;

    private int floor;

    private ETypeOrder type;

    private EStatusOrder status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    private int evaluation;
}
