package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealStatusRepository extends JpaRepository<DealStatus, String> {

    Optional<DealStatus> findByIdAndIsActiveTrue(String id);

    List<DealStatus> findAllByIsActiveTrue();

}
