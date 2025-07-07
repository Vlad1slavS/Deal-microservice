package io.github.dealmicroservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * Сущность суммы сделки
 */
@Entity
@Table(name = "deal_sum")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealSum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false)
    private UUID dealId;

    @Column(name = "sum", nullable = false, precision = 100, scale = 2)
    private BigDecimal sum;

    @Column(name = "currency_id", nullable = false, length = 3)
    private String currencyId;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "deal_id", insertable = false, updatable = false)
    private Deal deal;

    @ManyToOne
    @JoinColumn(name = "currency_id", insertable = false, updatable = false)
    private Currency currency;

}


