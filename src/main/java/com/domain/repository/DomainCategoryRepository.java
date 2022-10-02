package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.DomainCategory;

@Repository
public interface DomainCategoryRepository extends JpaRepository<DomainCategory, Long> {
	
	 @Query( value = "SELECT * FROM domain.domain_category WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<DomainCategory> findDomainCategoryiesToSynchronize( );	

	 @Query( value = "SELECT * FROM domain.domain_category WHERE domain_id = ?1 and category_id = ?2" , nativeQuery = true )
	 DomainCategory findDomainCategory( Long domainId, Long categoryId );	

	 @Query( value = "SELECT * FROM domain.domain_category WHERE domain_id = ?1" , nativeQuery = true )
	 List<DomainCategory> findDomainCategoryiesByDomainId( Long id );	
	 
	 @Query( value = "SELECT * FROM domain.domain_category WHERE db_synch_token = ?" , nativeQuery = true )
	 DomainCategory findBySynchToken( String dbSynchToken );	
	 
	@Query(value = "SELECT count(id) FROM domain.domain_category where domain_id = ?1", nativeQuery = true)
	public Long domainCount( Long id );	 
	 
	@Query(value = "SELECT count(id) FROM domain.domain_category where category_id = ?1", nativeQuery = true)
	public Long categoryCount( Long id );	 
	 
}
