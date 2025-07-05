package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Query("SELECT c FROM Currency c WHERE c.id = :s AND c.isActive = true")
    Optional<Currency> findByIdIsActive(String s);

}
