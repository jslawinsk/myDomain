package com.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.MeasureType;

@Repository
public interface MeasureTypeRepository extends JpaRepository<MeasureType, String> {

	 @Query( value = "SELECT * FROM domain.measure_type WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' )" , nativeQuery = true )
	 List<MeasureType> findMeasureTypesToSynchronize( );	

	 @Query( value = "SELECT * FROM domain.measure_type WHERE graph_data = true" , nativeQuery = true )
	 List<MeasureType> findMeasureTypesToGraph( );	

	 
}
