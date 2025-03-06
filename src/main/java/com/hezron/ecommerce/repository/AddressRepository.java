package com.hezron.ecommerce.repository;

import com.hezron.ecommerce.model.Address;
import com.hezron.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    Optional<Address> findByIdAndUser(Long id, User user);

    Optional<Address> findByUserAndIsDefaultTrue(User user);

}
