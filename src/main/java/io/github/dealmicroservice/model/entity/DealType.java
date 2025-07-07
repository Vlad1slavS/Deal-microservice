package io.github.dealmicroservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность типа сделки
 */
@Entity
@Table(name = "deal_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealType {

    @Id
    @Column(name = "id", length = 30)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

}
