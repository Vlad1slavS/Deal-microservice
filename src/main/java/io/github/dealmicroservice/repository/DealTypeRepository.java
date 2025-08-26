package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealTypeRepository extends JpaRepository<DealType, String> {

    Optional<DealType> findByIdAndIsActiveTrue(String id);

    List<DealType> findAllByIsActiveTrue();

}
