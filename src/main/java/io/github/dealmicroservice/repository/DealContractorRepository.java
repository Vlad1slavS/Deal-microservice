package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.dto.ContractorDTO;
import io.github.dealmicroservice.model.entity.DealContractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealContractorRepository extends JpaRepository<DealContractor, UUID> {

    @Query("SELECT dc FROM DealContractor dc " +
            "LEFT JOIN FETCH dc.roles r " +
            "LEFT JOIN FETCH r.role " +
            "WHERE dc.dealId = :dealId AND dc.isActive = true")
    List<DealContractor> findAllActiveByDealId(UUID dealId);

    @Query("SELECT dc FROM DealContractor dc WHERE dc.id = :id AND dc.isActive = true")
    Optional<DealContractor> findByIdActive(UUID id);

    @Query("SELECT dc FROM DealContractor dc WHERE dc.dealId = :dealId AND dc.main = true AND dc.isActive = true")
    Optional<DealContractor> findMainContractorByDealId(UUID dealId);

}
