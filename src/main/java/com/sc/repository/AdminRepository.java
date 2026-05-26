package com.sc.repository;

import com.sc.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {

    // We use Long as ID type (the internal PK)
    // But we frequently need to check by the public adminId

    Optional<AdminEntity> findByAdminId(String adminId);

    boolean existsByAdminId(String adminId);


    Optional<AdminEntity> findByAdminMobileNumber(String adminMobileNumber);

    boolean existsByAdminMobileNumber(String adminMobileNumber);

}