package com.domain.controller;

import com.domain.dto.ChartAttributes;
import com.domain.model.Batch;
import com.domain.model.MeasureType;
import com.domain.model.Measurement;
import com.domain.model.Process;
import com.domain.model.Sensor;
import com.domain.model.Category;
import com.domain.model.User;
import com.domain.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/")
@Tag( name = "Domain Services REST API", description = "RESTful API for Domain Monitoring and Control Services." )
public class RestApiController {

    private DataService dataService;

    private Logger LOG = LoggerFactory.getLogger(RestApiController.class);

    @Autowired
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    //
    // Heartbeat - Used to verify service is running
    //
    @RequestMapping(value = "/heartBeat", method = RequestMethod.GET)
    public String heartBeat(){
        return "ACK";
    }    

    //
    // API service methods to get current summary of measurements
    //
    //    
    @RequestMapping(path = "summary", method = RequestMethod.GET)
    @Operation( summary = "Gets Domain summary", 
    			description = "Return summarey data for all active batches. Most recent measurement is included." )    
    public List<Measurement> getMeasurementSummary( ) {
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	List<Batch> batches = dataService.getActiveBatches();
    	for( Batch batch:batches) {
    		List<Measurement> batchMeasurements = dataService.getRecentMeasurement( batch.getId() );
    		if( !batchMeasurements.isEmpty() ) {
    			measurements.addAll( batchMeasurements );
    		}
    	}    	
        LOG.info("RestApiController: getMeasurementSummary: " + measurements );    	

        return measurements;
    }

    
    //
    // Category table API service methods
    //
    //    
    @RequestMapping(path = "category/{id}", method = RequestMethod.GET)
    @Operation( summary = "Gets the category with specific id")
    public Category getCategory(@PathVariable(name = "id") Long id) {
        return dataService.getCategory(id);
    }

    @RequestMapping(path = "category", method = RequestMethod.GET)
    @Operation( summary = "Gets all categories")
    public List<Category> getAllCategories( ) {
        return dataService.getAllCategories();
    }
    
    @RequestMapping( path = "category", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Category saveCategory( @RequestBody Category categoryToSave ) {
        return dataService.saveCategory( categoryToSave );
    }

    @RequestMapping(path = "category", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Category updateCategory( @RequestBody Category categoryToUpdate ) {
    	Category category = new Category();
    	if( categoryToUpdate.getDbSynchToken() != null && categoryToUpdate.getDbSynchToken().length() > 0 ) {
    		Category foundCategory = dataService.getCategory( categoryToUpdate.getDbSynchToken() );
    		if( foundCategory != null ) {
    			foundCategory.setName( categoryToUpdate.getName() );
    			foundCategory.setReference( categoryToUpdate.getReference() );
    			foundCategory.setDescription( categoryToUpdate.getDescription() );
    			foundCategory.setDbSynch( categoryToUpdate.getDbSynch() );
	        	category = dataService.updateCategory( foundCategory );
    		}
    	}
    	else {
    		category = dataService.updateCategory( categoryToUpdate );
    	}
        return category;
    }

    @RequestMapping(path = "category/{id}", method = RequestMethod.DELETE)
    public void deleteCategory(@PathVariable(name = "id") Long id) {
    	dataService.deleteCategory(id);
    }

    @RequestMapping(path = "category/synchToken/{token}", method = RequestMethod.DELETE)
    public void deleteCategoryRemote(@PathVariable(name = "token") String token) {
		Category foundCategory = dataService.getCategory( token );
        LOG.info("RestApiController: deleteCategoryRemote: " + foundCategory );    	
        if( foundCategory != null ) {
        	dataService.deleteCategory( foundCategory.getId() );
        }
    }
    
    //
    // Process table API service methods
    //
    //    
    @RequestMapping(path = "process/{code}", method = RequestMethod.GET)
    @Operation( summary = "Gets the process with specific code")
    public Process getProcess(@PathVariable(name = "code") String code) {
        return dataService.getProcess( code );
    }

    @RequestMapping(path = "process", method = RequestMethod.GET)
    @Operation( summary = "Gets all processes")
    public List<Process> getAllProcesses( ) {
        return dataService.getAllProcesses();
    }
    
    @RequestMapping( path = "process", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Process saveProcess( @RequestBody Process processToSave ) {
        return dataService.saveProcess( processToSave );
    }

    @RequestMapping(path = "process", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Process updateProcess( @RequestBody Process processToUpdate ) {
        return dataService.updateProcess(processToUpdate);
    }

    @RequestMapping(path = "process/{code}", method = RequestMethod.DELETE)
    public void deleteProcess(@PathVariable(name = "code") String code ) {
        LOG.info("RestApiController: Delete Process: " + code );    	
        dataService.deleteProcess( code );
    }
    
    //
    // MeasureType table API service methods
    //
    //    
    @RequestMapping(path = "measureType/{code}", method = RequestMethod.GET)
    @Operation( summary = "Gets the MeasureType with specific code")
    public MeasureType getMeasureType(@PathVariable(name = "code") String code) {
        return dataService.getMeasureType( code );
    }
    
    @RequestMapping(path = "measureType", method = RequestMethod.GET)
    public List<MeasureType> getAllMeasureTypes() {
    	return ( List<MeasureType> )dataService.getAllMeasureTypes();
    }

    @RequestMapping( path = "measureType", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MeasureType saveMeasureType( @RequestBody MeasureType measureTypeToSave ) {
        return dataService.saveMeasureType( measureTypeToSave );
    }

    @RequestMapping(path = "measureType", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public MeasureType updateMeasureType( @RequestBody MeasureType measureTypeToUpdate ) {
        return dataService.updateMeasureType( measureTypeToUpdate );
    }

    @RequestMapping(path = "measureType/{code}", method = RequestMethod.DELETE)
    public void deleteMeasureType(@PathVariable(name = "code") String code ) {
        LOG.info("RestApiController: Delete MeasureType: " + code );    	
        dataService.deleteMeasureType( code );
    }
    
    //
    // Batch table API service methods
    //
    //    
    @RequestMapping(path = "batch/{id}", method = RequestMethod.GET)
    @Operation( summary = "Gets the batch with specific id")
    public Batch getBatch(@PathVariable(name = "id") Long id) {
        return dataService.getBatch(id);
    }

    @RequestMapping( path = "batch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Batch saveBatch( @RequestBody Batch batchToSave ) {
    	Category foundCategory = null;
    	if( batchToSave.getCategory().getDbSynchToken() != null && batchToSave.getCategory().getDbSynchToken().length() > 0 ) {
    		foundCategory = dataService.getCategory( batchToSave.getCategory().getDbSynchToken() );
    		batchToSave.setCategory( foundCategory );
    	}
        return dataService.saveBatch( batchToSave );
    }

    @RequestMapping(path = "batch", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Batch updateBatch( @RequestBody Batch batchToUpdate ) {
    	Batch batch = new Batch();
    	
    	Category foundCategory = null;
    	if( batchToUpdate.getCategory().getDbSynchToken() != null && batchToUpdate.getCategory().getDbSynchToken().length() > 0 ) {
    		foundCategory = dataService.getCategory( batchToUpdate.getCategory().getDbSynchToken() );
    	}
    	else {
    		foundCategory = batchToUpdate.getCategory();
    	}
    	
    	if( batchToUpdate.getDbSynchToken() != null && batchToUpdate.getDbSynchToken().length() > 0 ) {
    		Batch foundBatch = dataService.getBatch( batchToUpdate.getDbSynchToken() );
    		if( foundBatch != null ) {
    			LOG.info( "updateBatch: found by token: " + foundBatch );
            	foundBatch.setActive( batchToUpdate.isActive() );
            	foundBatch.setName( batchToUpdate.getName() );
            	foundBatch.setDescription( batchToUpdate.getDescription() );
            	foundBatch.setStartTime( batchToUpdate.getStartTime() );
            	foundBatch.setDbSynch( batchToUpdate.getDbSynch() );
            	foundBatch.setCategory( foundCategory );        		
	        	batch = dataService.updateBatch( foundBatch );
    		}
    	}
    	else {
    		batchToUpdate.setCategory( foundCategory );        		
    		batch = dataService.updateBatch( batchToUpdate );
    	}
        return batch;
    }

    @RequestMapping(path = "batch/{id}", method = RequestMethod.DELETE)
    public void deleteBatch(@PathVariable(name = "id") Long id) {
    	dataService.deleteBatch(id);
    }

    @RequestMapping(path = "batch/synchToken/{token}", method = RequestMethod.DELETE)
    public void deleteBatchRemote( @PathVariable(name = "token") String token ) {
    	Batch foundBatch = dataService.getBatch( token );
        LOG.info("RestApiController: deleteBatchRemote: " + foundBatch );    	
        if( foundBatch != null ) {
        	dataService.deleteBatch( foundBatch.getId() );
        }
    }
    
    //
    // Measurement table API service methods
    //
    //    
    @RequestMapping(path = "measurement/{id}", method = RequestMethod.GET)
    @Operation( summary = "Gets the measurement with specific id")
    public Measurement getMeasurement(@PathVariable(name = "id") Long id,
    			@RequestParam(value="pageNo", defaultValue="0") Integer pageNo 
    		) {
        return dataService.getMeasurement(id);
    }

    @RequestMapping( path = "measurement", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Measurement saveMeasurement( @RequestBody Measurement measurementToSave ) {
    	LOG.info("RestApiController:saveMeasurement: " + measurementToSave );
    	Batch foundBatch = null;
    	if( measurementToSave.getBatch().getDbSynchToken() != null && measurementToSave.getBatch().getDbSynchToken().length() > 0 ) {
    		foundBatch = dataService.getBatch( measurementToSave.getBatch().getDbSynchToken() );
    		measurementToSave.setBatch( foundBatch );
    	}
        return dataService.saveMeasurement( measurementToSave );
    }

    @RequestMapping(path = "measurement", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Measurement updateMeasurement( @RequestBody Measurement measurementToUpdate ) {
    	Measurement measurement = new Measurement();
    	
    	Batch foundBatch = null;
    	if( measurementToUpdate.getBatch().getDbSynchToken() != null && measurementToUpdate.getBatch().getDbSynchToken().length() > 0 ) {
    		foundBatch = dataService.getBatch( measurementToUpdate.getBatch().getDbSynchToken() );
    	}
    	else {
    		foundBatch = measurementToUpdate.getBatch();
    	}

    	if( measurementToUpdate.getDbSynchToken() != null && measurementToUpdate.getDbSynchToken().length() > 0 ) {
    		Measurement foundMeasurement = dataService.getMeasurement( measurementToUpdate.getDbSynchToken() );
    		if( foundMeasurement != null ) {
    			LOG.info( "updateMeasurement: found by token: " + foundMeasurement );
    			foundMeasurement.setBatch( foundBatch );
    			foundMeasurement.setDbSynch( measurementToUpdate.getDbSynch() );
    			foundMeasurement.setMeasurementTime( measurementToUpdate.getMeasurementTime() );
    			foundMeasurement.setProcess( measurementToUpdate.getProcess() );
    			foundMeasurement.setType( measurementToUpdate.getType() );
    			foundMeasurement.setValueNumber( measurementToUpdate.getValueNumber() );
    			foundMeasurement.setValueText( measurementToUpdate.getValueText() );
	        	measurement = dataService.updateMeasurement( foundMeasurement );
    		}
    	}
    	else {
    		measurementToUpdate.setBatch( foundBatch );        		
    		measurement = dataService.updateMeasurement( measurementToUpdate );
    	}
        return measurement;
    }

    @RequestMapping(path = "measurement/{id}", method = RequestMethod.DELETE)
    public void deleteMeasurement(@PathVariable(name = "id") Long id) {
    	dataService.deleteMeasurement( id );
    }

    @RequestMapping(path = "measurement/synchToken/{token}", method = RequestMethod.DELETE)
    public void deleteMeasurementRemote( @PathVariable(name = "token") String token ) {
    	Measurement foundMeasurement= dataService.getMeasurement( token );
        LOG.info("RestApiController: deleteMeasurementRemote: " + foundMeasurement );    	
        if( foundMeasurement != null ) {
        	dataService.deleteMeasurement( foundMeasurement.getId() );
        }
    }
    
    //
    // Sensor table API service methods
    //
    //    
    @RequestMapping(path = "sensor/{id}", method = RequestMethod.GET)
    @Operation( summary = "Gets the sensor with specific id")
    public Sensor getSensor(@PathVariable(name = "id") Long id ) {
        return dataService.getSensor( id );
    }

    @RequestMapping( path = "sensor", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Sensor saveSensor( @RequestBody Sensor sensorToSave ) {
    	Batch foundBatch = null;
    	if( sensorToSave.getBatch().getDbSynchToken() != null && sensorToSave.getBatch().getDbSynchToken().length() > 0 ) {
			foundBatch = dataService.getBatch( sensorToSave.getBatch().getDbSynchToken() );
			sensorToSave.setBatch( foundBatch );
		}
        return dataService.saveSensor( sensorToSave );
    }

    @RequestMapping(path = "sensor", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Sensor updateSensor( @RequestBody Sensor sensorToUpdate ) {    	
    	
    	Sensor sensor = null;
    	Batch foundBatch = null;
    	if( sensorToUpdate.getBatch().getDbSynchToken() != null && sensorToUpdate.getBatch().getDbSynchToken().length() > 0 ) {
			foundBatch = dataService.getBatch( sensorToUpdate.getBatch().getDbSynchToken() );
		}
		else {
			foundBatch = sensorToUpdate.getBatch();
		}
    	
    	if( sensorToUpdate.getDbSynchToken() != null && sensorToUpdate.getDbSynchToken().length() > 0 ) {
    		Sensor foundSensor = dataService.getSensor( sensorToUpdate.getDbSynchToken() );
    		if( foundSensor != null ) {
    			LOG.info( "updateSensor: found by token: " + foundSensor );
    			foundSensor.setBatch( foundBatch );
    			foundSensor.setCommunicationType( sensorToUpdate.getCommunicationType() );
    			foundSensor.setDbSynch( sensorToUpdate.getDbSynch() );
    			foundSensor.setEnabled( sensorToUpdate.isEnabled() );
    			foundSensor.setMeasureType( sensorToUpdate.getMeasureType() );
    			foundSensor.setName( sensorToUpdate.getName() );
    			foundSensor.setPin( sensorToUpdate.getPin() );
    			foundSensor.setProcess( sensorToUpdate.getProcess() );
    			foundSensor.setTrigger( sensorToUpdate.getTrigger() );
    			foundSensor.setUpdateTime( sensorToUpdate.getUpdateTime() );
    			foundSensor.setUrl( sensorToUpdate.getUrl() );
    			foundSensor.setUserId( sensorToUpdate.getUserId() );
        		sensor = dataService.updateSensor( foundSensor );
    		}
    	}
    	else {
    		sensorToUpdate.setBatch( foundBatch );        		
    		sensor = dataService.updateSensor( sensorToUpdate );
    	}
        return sensor;
    }

    @RequestMapping(path = "sensor/{id}", method = RequestMethod.DELETE)
    public void deleteSensor(@PathVariable( name = "id" ) Long id ) {
    	dataService.deleteSensor( id );
    }
    
    @RequestMapping(path = "sensor/synchToken/{token}", method = RequestMethod.DELETE)
    public void deleteSensorRemote(@PathVariable( name = "token" ) String token ) {
		Sensor foundSensor = dataService.getSensor( token );
        LOG.info("RestApiController: deleteSensorRemote: " + foundSensor );    	
        if( foundSensor != null ) {
        	dataService.deleteSensor( foundSensor.getId() );
        }
    }
    
    //
    // API Authentication methods
    //
    //    
	@PostMapping("authorize")
	public User login(@RequestParam("user") String username, @RequestParam("password") String pwd) {
		User foundUser = dataService.getUserByName( username );
		if( foundUser != null ) {
			if( passwordEncoder.matches( pwd, foundUser.getPassword() ) ) {
				LOG.info( "Passwords Match");
				String token = getJWTToken( foundUser );
				foundUser.setToken(token);		
				return foundUser;
			}
		}			
		return null;
	}

    @RequestMapping( path = "notauthenticated", method = RequestMethod.GET)
    public String notAuthenticated( ) {
    	return "{error: Not Autheticated}";
    }
    
	
	private String getJWTToken( User user ) {
		String secretKey = "mySecretKey";
		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList( user.getRoles() );
		
		String token = Jwts
				.builder()
				.setId("softtekJWT")
				.setSubject( user.getUsername() )
				.claim("authorities",
						grantedAuthorities.stream()
								.map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 600000))
				.signWith(SignatureAlgorithm.HS512,
						secretKey.getBytes()).compact();

		return "Bearer " + token;
	}
    
}
