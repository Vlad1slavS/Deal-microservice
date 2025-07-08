package io.github.dealmicroservice.service;

import io.github.dealmicroservice.model.dto.DealDTO;
import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.dto.DealSaveDTO;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Интерфейс сервиса для управления сделками
 */
public interface DealService {

    /**
     * Сохраняет новую или обновляет существующую сделку
     * Для новой сделки устанавливается статус "DRAFT"
     * @param request DTO с данными для сохранения сделки
     * @return DTO сохраненной сделки
     */
    DealDTO saveDeal(DealSaveDTO request);

    /**
     * Изменяет статус сделки
     * @param id       идентификатор сделки
     * @param statusId идентификатор нового статуса
     * @return DTO обновленной сделки
     */
    DealDTO changeStatus(UUID id, String statusId);

    /**
     * Получает сделку по идентификатору со всеми связанными данными
     * @param id идентификатор сделки
     * @return DTO сделки с полной информацией
     */
    DealDTO getDealById(UUID id);

    /**
     * Осуществляет поиск сделок по заданным фильтрам с пагинацией и сортировкой
     * @param request DTO с параметрами поиска, пагинации и сортировки
     * @return страница с результатами поиска
     */
    Page<DealDTO> searchDeals(DealSearchDTO request);
}