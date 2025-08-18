package io.github.dealmicroservice.service;

import io.github.dealmicroservice.exception.EntityNotFoundException;
import io.github.dealmicroservice.model.dto.ContractorMessageDTO;
import io.github.dealmicroservice.model.dto.ContractorToRoleDTO;
import io.github.dealmicroservice.model.dto.DealContractorDTO;

import java.util.UUID;

/**
 * Интерфейс сервиса для управления контрагентами в сделках
 */
public interface DealContractorService {

    /**
     * Сохраняет или обновляет контрагента сделки.
     * Если указан текущий контрагент как основной, предыдущий основной контрагент становится неосновным
     * @param request DTO с данными контрагента
     * @return DTO сохраненного контрагента
     * @throws EntityNotFoundException если сделка или контрагент не найдены
     */
    DealContractorDTO saveDealContractor(DealContractorDTO request);

    /**
     * Помечает контрагента сделки как неактивного
     * @param contractorId идентификатор контрагента
     * @throws EntityNotFoundException если контрагент не найден
     */
    void deleteDealContractor(UUID contractorId);

    /**
     * Добавляет роль контрагенту в сделке
     * @param contractorId идентификатор контрагента
     * @param roleId идентификатор роли
     * @return DTO с информацией о назначенной роли
     * @throws EntityNotFoundException если контрагент или роль не найдены
     */
    ContractorToRoleDTO addRoleToContractor(UUID contractorId, String roleId);

    /**
     * Удаляет роль у контрагента в сделке
     * @param contractorId идентификатор контрагента
     * @param roleId идентификатор роли
     * @throws EntityNotFoundException если связь контрагента с ролью не найдена
     */
    void deleteRoleFromContractor(UUID contractorId, String roleId);

    void updateContractorInDeals(ContractorMessageDTO contractorMessage);

}
