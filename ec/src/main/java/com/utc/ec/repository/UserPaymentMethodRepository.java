package com.utc.ec.repository;

import com.utc.ec.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Integer> {

    List<UserPaymentMethod> findByUserId(Integer userId);

    Optional<UserPaymentMethod> findByIdAndUserId(Integer id, Integer userId);

    Optional<UserPaymentMethod> findByUserIdAndIsDefault(Integer userId, Integer isDefault);

    boolean existsByIdAndUserId(Integer id, Integer userId);
}
