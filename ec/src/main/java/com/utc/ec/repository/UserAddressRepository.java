package com.utc.ec.repository;

import com.utc.ec.entity.UserAddress;
import com.utc.ec.entity.UserAddressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UserAddressId> {

    List<UserAddress> findByUserId(Integer userId);

    boolean existsByUserIdAndAddressId(Integer userId, Integer addressId);
}
