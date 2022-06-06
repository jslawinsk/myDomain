package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.Process;

@Repository
public interface ProcessRepository extends JpaRepository<Process, String> {
	
	 @Query( value = "SELECT * FROM domain.process WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<Process> findProcessToSynchronize( );	
	
}
