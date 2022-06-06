package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	 @Query( value = "SELECT * FROM domain.category WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<Category> findCategoryiesToSynchronize( );	

	 @Query( value = "SELECT * FROM domain.category WHERE name = ?" , nativeQuery = true )
	 Category findCategoryByName( String name );	
	 
	 @Query( value = "SELECT * FROM domain.category WHERE db_synch_token = ?" , nativeQuery = true )
	 Category findCategoryBySynchToken( String dbSynchToken );	
	 
}
