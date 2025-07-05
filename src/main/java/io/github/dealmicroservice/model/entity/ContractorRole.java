package io.github.dealmicroservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contractor_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorRole {

    @Id
    @Column(name = "id", length = 30)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
