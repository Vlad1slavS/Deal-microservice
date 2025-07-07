package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.ContractorRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContractorRoleRepository extends JpaRepository<ContractorRole, String> {

    @Query("SELECT cr FROM ContractorRole cr WHERE cr.id = :s AND cr.isActive = true")
    Optional<ContractorRole> findByIdIsActive(String s);

}
