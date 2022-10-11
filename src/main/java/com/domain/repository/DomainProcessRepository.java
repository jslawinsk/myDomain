package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.DomainProcess;

@Repository
public interface DomainProcessRepository extends JpaRepository<DomainProcess, Long> {
	
	 @Query( value = "SELECT * FROM domain.domain_process WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<DomainProcess> findDomainProcessesToSynchronize( );	

	 @Query( value = "SELECT * FROM domain.domain_process WHERE domain_id = ?1 and process_code = ?2" , nativeQuery = true )
	 DomainProcess findDomainProcess( Long domainId, String processCode );	

	 @Query( value = "SELECT * FROM domain.domain_process WHERE domain_id = ?1" , nativeQuery = true )
	 List<DomainProcess> findDomainProcessesByDomainId( Long id );	
	 
	 @Query( value = "SELECT * FROM domain.domain_process WHERE db_synch_token = ?" , nativeQuery = true )
	 DomainProcess findBySynchToken( String dbSynchToken );	
	 
	@Query(value = "SELECT count(id) FROM domain.domain_process where domain_id = ?1", nativeQuery = true)
	public Long domainCount( Long id );	 
	 
	@Query(value = "SELECT count(id) FROM domain.domain_process where process_code = ?1", nativeQuery = true)
	public Long processCount( String code );	 
	 
}
