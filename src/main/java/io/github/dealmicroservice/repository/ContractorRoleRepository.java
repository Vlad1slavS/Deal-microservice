package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.ContractorRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractorRoleRepository extends JpaRepository<ContractorRole, String> {

    Optional<ContractorRole> findByIdAndIsActiveTrue(String id);

}
