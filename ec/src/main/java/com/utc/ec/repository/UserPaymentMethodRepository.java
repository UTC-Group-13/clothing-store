package com.utc.ec.repository;

import com.utc.ec.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, Integer> {
}
