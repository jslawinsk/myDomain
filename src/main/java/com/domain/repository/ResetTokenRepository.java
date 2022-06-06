package com.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.domain.model.ResetToken;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, String> {
	
}
