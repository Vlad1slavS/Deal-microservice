package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DealStatusRepository extends JpaRepository<DealStatus, String> {

    @Query("SELECT ds FROM DealStatus ds WHERE ds.id = :id AND ds.isActive = true")
    Optional<DealStatus> findByIdAndIsActive(String id);



}
