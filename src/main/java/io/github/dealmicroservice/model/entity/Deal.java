package io.github.dealmicroservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedEntityGraphs;
import jakarta.persistence.NamedAttributeNode;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


/**
 * Сущность сделки
 */
@Entity
@Table(name = "deal")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "Deal.withTypeAndStatus",
        attributeNodes = {
            @NamedAttributeNode("type"),
            @NamedAttributeNode("status")
        }
    ),
    @NamedEntityGraph(
        name = "Deal.withSums",
        attributeNodes = {
            @NamedAttributeNode("type"),
            @NamedAttributeNode("status"),
            @NamedAttributeNode("sums")
        }
    ),
    @NamedEntityGraph(
        name = "Deal.withContractors",
        attributeNodes = {
            @NamedAttributeNode("type"),
            @NamedAttributeNode("status"),
            @NamedAttributeNode("contractors")
        }
    )
})
public class Deal {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "description")
    private String description;

    @Column(name = "agreement_number")
    private String agreementNumber;

    @Column(name = "agreement_date")
    private LocalDate agreementDate;

    @Column(name = "agreement_start_date")
    private LocalDateTime agreementStartDate;

    @Column(name = "availability_date")
    private LocalDate availabilityDate;

    @Column(name = "close_dt")
    private LocalDateTime closeDt;

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

    @Column(name = "type_id")
    private String typeId;

    @Column(name = "status_id")
    private String statusId;

    @ManyToOne
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private DealType type;

    @ManyToOne
    @JoinColumn(name = "status_id", insertable = false, updatable = false)
    private DealStatus status;

    @OneToMany(mappedBy = "dealId", cascade = CascadeType.ALL)
    private List<DealSum> sums;

    @OneToMany(mappedBy = "dealId", cascade = CascadeType.ALL)
    private List<DealContractor> contractors;

}
