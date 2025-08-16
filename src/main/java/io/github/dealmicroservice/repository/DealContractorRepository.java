package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealContractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DealContractorRepository extends JpaRepository<DealContractor, UUID> {

    Optional<DealContractor> findByIdAndIsActiveTrue(UUID id);

    Optional<DealContractor> findByDealIdAndMainTrueAndIsActiveTrue(UUID dealId);

    @Modifying
    @Query("""
        UPDATE DealContractor
        SET name = :name,
            inn = :inn,
            modifyDate = CURRENT_TIMESTAMP
        WHERE contractorId = :contractorId
        """)
    int updateContractorInfo(String contractorId, String name, String inn);

}
