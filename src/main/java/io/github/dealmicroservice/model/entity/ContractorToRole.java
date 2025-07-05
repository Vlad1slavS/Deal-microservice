package io.github.dealmicroservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "contractor_to_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ContractorToRoleId.class)
public class ContractorToRole {

    @Id
    @Column(name = "contractor_id")
    private UUID contractorId;

    @Id
    @Column(name = "role_id", length = 30)
    private String roleId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "contractor_id", insertable = false, updatable = false)
    private DealContractor contractor;

    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private ContractorRole role;

}

