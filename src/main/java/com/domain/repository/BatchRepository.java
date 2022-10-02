package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.Batch;
import com.domain.model.Category;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
	
	 @Query( value = "SELECT * FROM domain.batch WHERE active = true", nativeQuery = true )
	 List<Batch> findActiveBatches( );	

	 @Query( value = "SELECT * FROM domain.batch WHERE active = true AND domain_id = ?1", nativeQuery = true )
	 List<Batch> findActiveBatchesForDomain( Long id );	

	 @Query( value = "SELECT * FROM domain.batch WHERE domain_id = ?1", nativeQuery = true )
	 List<Batch> findBatchesForDomain( Long id );	
	 
	 @Query( value = "SELECT * FROM domain.batch WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )", nativeQuery = true )
	 List<Batch> findBatchesToSynchronize( );	

	 @Query( value = "SELECT * FROM domain.batch WHERE name = ?", nativeQuery = true )
	 Batch findBatchByName( String name );	
	 
	 @Query( value = "SELECT * FROM domain.batch WHERE db_synch_token = ?" , nativeQuery = true )
	 Batch findBatchBySynchToken( String dbSynchToken );	
	 
	@Query(value = "SELECT count(id) FROM domain.batch where category_id = ?1", nativeQuery = true)
	public Long categoryCount( Long id );	 

	@Query(value = "SELECT count(id) FROM domain.batch where category_id = ?1 AND domain_id = ?2", nativeQuery = true)
	public Long categoryCount( Long categoryId, Long domainId );	 
	
	@Query(value = "SELECT count(id) FROM domain.batch where domain_id = ?1", nativeQuery = true)
	public Long domainCount( Long id );	 
	
}
