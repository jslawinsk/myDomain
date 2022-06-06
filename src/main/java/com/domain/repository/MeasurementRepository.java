package com.domain.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.domain.model.Measurement;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
	
	 @Query( value = "SELECT * FROM domain.measurement WHERE batch_id = ?1 ORDER BY process_code, type_code, start_time ASC", nativeQuery = true )
	 List<Measurement> findByBatchId( Long id );	

	 @Query( value = "SELECT * FROM domain.measurement WHERE batch_id = ?1 ORDER BY start_time DESC", nativeQuery = true )
	 Page<Measurement> findPageByBatchId( Long id, Pageable pageable );	
	 
	 @Query( value = "SELECT * FROM domain.measurement WHERE db_synch IN ( 'ADD', 'UPDATE', 'DELETE' ) ORDER BY batch_id, process_code, type_code, start_time ASC", nativeQuery = true )
	 List<Measurement> findMeasurementsToSynchronize( );
	 
	 @Query( value = "SELECT DISTINCT ON (type_code) type_code, start_time, id, db_synch, value_number, value_text, batch_id, process_code, db_synch_token FROM domain.measurement WHERE batch_id = ?1 ORDER BY type_code, start_time DESC LIMIT 10", nativeQuery = true )
	 List<Measurement> findMostRecent( Long id );

	 @Query( value = "SELECT type_code, start_time, id, db_synch, value_number, value_text, batch_id, process_code, db_synch_token FROM domain.measurement WHERE batch_id = ?1 AND type_code = ?2 ORDER BY type_code, start_time", nativeQuery = true )
	 List<Measurement> findByBatchIdType( Long id, String type );

	 @Query( value = "SELECT * FROM domain.measurement WHERE db_synch_token = ?" , nativeQuery = true )
	 Measurement findMeasurementBySynchToken( String dbSynchToken );	
	 
	 @Modifying
	 @Query( value = "DELETE FROM domain.measurement WHERE batch_id = ?1", nativeQuery = true )
	 int deleteByBatchId( Long id );	
	 
	 @Query(value = "SELECT count(id) FROM domain.measurement where process_code = ?1", nativeQuery = true)
	 public Long processCount( String code);	 

	 @Query(value = "SELECT count(id) FROM domain.measurement where type_code = ?1", nativeQuery = true)
	 public Long measureTypeCount( String code);	 
}
