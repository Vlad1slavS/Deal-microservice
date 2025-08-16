package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.ContractorToRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractorToRoleRepository extends JpaRepository<ContractorToRole, UUID> {

    Optional<ContractorToRole> findByContractorIdAndRoleIdAndIsActiveTrue(UUID contractorId, String roleId);

}
