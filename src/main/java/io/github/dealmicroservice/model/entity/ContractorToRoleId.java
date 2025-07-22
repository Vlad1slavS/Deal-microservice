package io.github.dealmicroservice.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность уникального идентификатора для связи между контрагентом и ролью
 */
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ContractorToRoleId implements Serializable {

    private UUID contractorId;

    private String roleId;

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContractorToRoleId that = (ContractorToRoleId) o;
        return Objects.equals(contractorId, that.contractorId) &&
                Objects.equals(roleId, that.roleId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(contractorId, roleId);
    }

}
