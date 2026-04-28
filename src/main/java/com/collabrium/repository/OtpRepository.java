package com.collabrium.repository;

import com.collabrium.model.Otp;
import com.collabrium.model.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByEmailAndTypeOrderByIdDesc(String email, OtpType type);

    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.email = :email AND o.type = :type")
    void deleteAllByEmailAndType(String email, OtpType type);
}
