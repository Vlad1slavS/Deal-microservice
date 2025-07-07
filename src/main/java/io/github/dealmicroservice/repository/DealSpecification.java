package io.github.dealmicroservice.repository;

import io.github.dealmicroservice.model.dto.DealSearchDTO;
import io.github.dealmicroservice.model.entity.Deal;
import io.github.dealmicroservice.model.entity.DealContractor;
import io.github.dealmicroservice.model.entity.DealSum;
import io.github.dealmicroservice.model.entity.ContractorToRole;
import io.github.dealmicroservice.model.entity.ContractorRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class DealSpecification {

    public static Specification<Deal> buildSpecification(DealSearchDTO request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            if (request.getDealId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), request.getDealId()));
            }

            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("description"), request.getDescription()));
            }

            if (request.getAgreementNumber() != null && !request.getAgreementNumber().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("agreementNumber")),
                        "%" + request.getAgreementNumber().toLowerCase() + "%"
                ));
            }

            if (request.getAgreementDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("agreementDate"), request.getAgreementDateFrom()));
            }
            if (request.getAgreementDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("agreementDate"), request.getAgreementDateTo()));
            }

            if (request.getAvailabilityDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("availabilityDate"), request.getAvailabilityDateFrom()));
            }
            if (request.getAvailabilityDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("availabilityDate"), request.getAvailabilityDateTo()));
            }

            if (request.getType() != null && !request.getType().isEmpty()) {
                predicates.add(root.get("typeId").in(request.getType()));
            }

            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                predicates.add(root.get("statusId").in(request.getStatus()));
            }

            if (request.getCloseDtFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("closeDt"), request.getCloseDtFrom()));
            }
            if (request.getCloseDtTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("closeDt"), request.getCloseDtTo()));
            }

            if (request.getBorrowerSearch() != null && !request.getBorrowerSearch().trim().isEmpty()) {
                Subquery<Long> borrowerSubquery = query.subquery(Long.class);
                Root<DealContractor> contractorRoot = borrowerSubquery.from(DealContractor.class);
                Join<DealContractor, ContractorToRole> roleJoin = contractorRoot.join("roles");
                Join<ContractorToRole, ContractorRole> contractorRoleJoin = roleJoin.join("role");

                borrowerSubquery.select(criteriaBuilder.literal(1L))
                        .where(
                                criteriaBuilder.and(
                                        criteriaBuilder.equal(contractorRoot.get("dealId"), root.get("id")),
                                        criteriaBuilder.equal(contractorRoleJoin.get("category"), "BORROWER"),
                                        criteriaBuilder.isTrue(contractorRoot.get("isActive")),
                                        criteriaBuilder.isTrue(roleJoin.get("isActive")),
                                        criteriaBuilder.or(
                                                criteriaBuilder.like(criteriaBuilder.lower(contractorRoot.get("contractorId")),
                                                        "%" + request.getBorrowerSearch().toLowerCase() + "%"),
                                                criteriaBuilder.like(criteriaBuilder.lower(contractorRoot.get("name")),
                                                        "%" + request.getBorrowerSearch().toLowerCase() + "%"),
                                                criteriaBuilder.like(criteriaBuilder.lower(contractorRoot.get("inn")),
                                                        "%" + request.getBorrowerSearch().toLowerCase() + "%")
                                        )
                                )
                        );

                predicates.add(criteriaBuilder.exists(borrowerSubquery));
            }

            if (request.getWarrantySearch() != null && !request.getWarrantySearch().trim().isEmpty()) {
                Subquery<Long> warrantySubquery = query.subquery(Long.class);
                Root<DealContractor> contractorRoot = warrantySubquery.from(DealContractor.class);
                Join<DealContractor, ContractorToRole> roleJoin = contractorRoot.join("roles");
                Join<ContractorToRole, ContractorRole> contractorRoleJoin = roleJoin.join("role");

                warrantySubquery.select(criteriaBuilder.literal(1L))
                        .where(
                                criteriaBuilder.and(
                                        criteriaBuilder.equal(contractorRoot.get("dealId"), root.get("id")),
                                        criteriaBuilder.equal(contractorRoleJoin.get("category"), "WARRANTY"),
                                        criteriaBuilder.isTrue(contractorRoot.get("isActive")),
                                        criteriaBuilder.isTrue(roleJoin.get("isActive")),
                                        criteriaBuilder.or(
                                                criteriaBuilder.like(criteriaBuilder.lower(contractorRoot.get("contractorId")),
                                                        "%" + request.getWarrantySearch().toLowerCase() + "%"),
                                                criteriaBuilder.like(criteriaBuilder.lower(contractorRoot.get("name")),
                                                        "%" + request.getWarrantySearch().toLowerCase() + "%"),
                                                criteriaBuilder.like(criteriaBuilder.lower(contractorRoot.get("inn")),
                                                        "%" + request.getWarrantySearch().toLowerCase() + "%")
                                        )
                                )
                        );

                predicates.add(criteriaBuilder.exists(warrantySubquery));
            }

            if (request.getSum() != null) {
                Subquery<Long> sumSubquery = query.subquery(Long.class);
                Root<DealSum> sumRoot = sumSubquery.from(DealSum.class);

                List<Predicate> sumPredicates = new ArrayList<>();
                sumPredicates.add(criteriaBuilder.equal(sumRoot.get("dealId"), root.get("id")));
                sumPredicates.add(criteriaBuilder.isTrue(sumRoot.get("isActive")));
                sumPredicates.add(criteriaBuilder.isTrue(sumRoot.get("isMain")));

                if (request.getSum().getValue() != null) {
                    sumPredicates.add(criteriaBuilder.equal(sumRoot.get("sum"), request.getSum().getValue()));
                }

                if (request.getSum().getCurrency() != null && !request.getSum().getCurrency().trim().isEmpty()) {
                    sumPredicates.add(criteriaBuilder.equal(sumRoot.get("currencyId"), request.getSum().getCurrency()));
                }

                sumSubquery.select(criteriaBuilder.literal(1L))
                        .where(criteriaBuilder.and(sumPredicates.toArray(new Predicate[0])));

                predicates.add(criteriaBuilder.exists(sumSubquery));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
