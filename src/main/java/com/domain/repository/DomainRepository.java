package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.Domain;

@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {
	
	 @Query( value = "SELECT * FROM domain.domain WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<Domain> findDomainsToSynchronize( );	
	
}
