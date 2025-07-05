package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.entity.DealContractor;
import io.github.dealmicroservice.model.entity.DealSum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID>, JpaSpecificationExecutor<Deal> {

    @Query("SELECT d FROM Deal d WHERE d.id = :id AND d.isActive = true")
    Optional<Deal> findActiveById(UUID id);

    @Query("SELECT d FROM Deal d " +
            "LEFT JOIN FETCH d.type " +
            "LEFT JOIN FETCH d.status " +
            "WHERE d.id = :id AND d.isActive = true")
    Optional<Deal> findByIdWithBasicDetails(UUID id);

    @Query("SELECT ds FROM DealSum ds " +
            "LEFT JOIN FETCH ds.currency " +
            "WHERE ds.deal.id = :dealId")
    List<DealSum> findSumsByDealId(@Param("dealId") UUID dealId);

    @Query("SELECT dc FROM DealContractor dc " +
            "LEFT JOIN FETCH dc.roles dcr " +
            "LEFT JOIN FETCH dcr.role " +
            "WHERE dc.deal.id = :dealId")
    List<DealContractor> findContractorsByDealId(@Param("dealId") UUID dealId);

    @Query("SELECT d FROM Deal d WHERE d.isActive = true")
    Page<Deal> findAllByIsActive(Pageable pageable);

}
