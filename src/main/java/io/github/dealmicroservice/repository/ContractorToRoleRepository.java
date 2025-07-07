package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.ContractorToRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractorToRoleRepository extends JpaRepository<ContractorToRole, UUID> {

    @Query("SELECT ctr FROM ContractorToRole ctr " +
            "WHERE ctr.contractorId = :contractorId AND ctr.roleId = :roleId AND ctr.isActive = true")
    Optional<ContractorToRole> findByContractorIdAndRoleIdAndIsActiveTrue(UUID contractorId, String roleId);

}
