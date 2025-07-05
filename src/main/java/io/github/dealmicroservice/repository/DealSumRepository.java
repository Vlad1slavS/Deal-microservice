package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealSum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealSumRepository extends JpaRepository<DealSum, Long> {

    @Query("SELECT ds FROM DealSum ds WHERE ds.dealId = :dealId AND ds.isActive = true")
    List<DealSum> findAllActiveByDealId(UUID dealId);

    @Query("SELECT ds FROM DealSum ds WHERE ds.dealId = :dealId AND ds.isMain = true AND ds.isActive = true")
    Optional<DealSum> findMainSumByDealId(UUID dealId);

    @Query("SELECT ds FROM DealSum ds " +
            "LEFT JOIN FETCH ds.currency " +
            "WHERE ds.deal.id = :dealId")
    List<DealSum> findSumsByDealId(UUID dealId);

}
