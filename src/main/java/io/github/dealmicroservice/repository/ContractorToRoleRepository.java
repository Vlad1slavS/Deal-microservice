package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.ContractorToRole;
import io.github.dealmicroservice.model.entity.ContractorToRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractorToRoleRepository extends JpaRepository<ContractorToRole, ContractorToRoleId> {

    @Query("SELECT ctr FROM ContractorToRole ctr " +
            "LEFT JOIN FETCH ctr.role " +
            "WHERE ctr.contractorId = :contractorId AND ctr.isActive = true")
    List<ContractorToRole> findByContractorIsActive(UUID contractorId);

    @Query("SELECT ctr FROM ContractorToRole ctr " +
            "WHERE ctr.contractorId = :contractorId AND ctr.roleId = :roleId AND ctr.isActive = true")
    Optional<ContractorToRole> findByContractorIdAndRoleIdIsActive(UUID contractorId, String roleId);

}
