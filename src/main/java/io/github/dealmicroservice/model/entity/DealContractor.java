package io.github.dealmicroservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deal_contractor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealContractor {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "deal_id", nullable = false)
    private UUID dealId;

    @Column(name = "contractor_id", nullable = false, length = 12)
    private String contractorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "inn")
    private String inn;

    @Column(name = "main", nullable = false)
    private Boolean main = false;

    @CreationTimestamp
    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "create_user_id")
    private String createUserId;

    @Column(name = "modify_user_id")
    private String modifyUserId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "deal_id", insertable = false, updatable = false)
    private Deal deal;

    @OneToMany(mappedBy = "contractor", cascade = CascadeType.ALL)
    private List<ContractorToRole> roles;

}
