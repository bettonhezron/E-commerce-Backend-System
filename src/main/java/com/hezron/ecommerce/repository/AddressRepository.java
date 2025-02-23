package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findUserById(Long userId);
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}
