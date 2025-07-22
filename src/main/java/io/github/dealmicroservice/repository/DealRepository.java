package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DealRepository extends JpaRepository<Deal, UUID>, JpaSpecificationExecutor<Deal> {

    Optional<Deal> findByIdAndIsActiveTrue(UUID id);

    @EntityGraph(value = "Deal.withTypeAndStatus")
    Optional<Deal> findActiveByIdWithBasicDetails(UUID id);

    @EntityGraph(value = "Deal.withContractors")
    Optional<Deal> findByIdWithContractors(UUID id);

    @EntityGraph(value = "Deal.withSums")
    Optional<Deal> findByIdWithSums(UUID id);

    @EntityGraph(value = "Deal.withTypeAndStatus")
    Page<Deal> findAll(Specification<Deal> specification, Pageable pageable);

    @EntityGraph(value = "Deal.withSums")
    @Query("SELECT d FROM Deal d WHERE d.id IN :dealIds")
    List<Deal> findDealsWithSums(Set<UUID> dealIds);

    @EntityGraph(value = "Deal.withContractors")
    @Query("SELECT d FROM Deal d WHERE d.id IN :dealIds")
    List<Deal> findDealsWithContractors(Set<UUID> dealIds);

}
