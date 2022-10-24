package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.DomainMeasureType;

@Repository
public interface DomainMeasureTypeRepository extends JpaRepository<DomainMeasureType, Long> {
	
	 @Query( value = "SELECT * FROM domain.domain_measure_type WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<DomainMeasureType> findToSynchronize( );	

	 @Query( value = "SELECT * FROM domain.domain_measure_type WHERE domain_id = ?1 and measure_type_code = ?2" , nativeQuery = true )
	 DomainMeasureType findDomainMeasureType( Long domainId, String code );	

	 @Query( value = "SELECT * FROM domain.domain_measure_type WHERE domain_id = ?1" , nativeQuery = true )
	 List<DomainMeasureType> findDomainMeasureTypes( Long id );	
	 
	 @Query( value = "SELECT * FROM domain.domain_measure_type WHERE db_synch_token = ?" , nativeQuery = true )
	 DomainMeasureType findBySynchToken( String dbSynchToken );	
	 
	@Query(value = "SELECT count(id) FROM domain.domain_measure_type where domain_id = ?1", nativeQuery = true)
	public Long domainCount( Long id );	 
	 
	@Query(value = "SELECT count(id) FROM domain.domain_measure_type where measure_type_code = ?1", nativeQuery = true)
	public Long measureTypeCount( String code );	 
	 
}
