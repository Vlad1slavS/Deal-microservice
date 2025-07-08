package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealContractor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DealContractorRepository extends JpaRepository<DealContractor, UUID> {

    Optional<DealContractor> findByIdAndIsActiveTrue(UUID id);

    Optional<DealContractor> findByDealIdAndMainTrueAndIsActiveTrue(UUID dealId);

}
