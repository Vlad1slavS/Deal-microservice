package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DealTypeRepository extends JpaRepository<DealType, String> {

    @Query("SELECT dt FROM DealType dt WHERE dt.id = :id AND dt.isActive = true")
    Optional<DealType> findByIdIsActive(String id);

}
