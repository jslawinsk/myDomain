package com.domain.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domain.model.Batch;
import com.domain.model.DbSync;
import com.domain.model.Domain;
import com.domain.model.DomainCategory;
import com.domain.model.DomainProcess;
import com.domain.model.MeasureType;
import com.domain.model.Measurement;
import com.domain.model.Process;
import com.domain.model.ResetToken;
import com.domain.model.Sensor;
import com.domain.model.SensorType;
import com.domain.model.Category;
import com.domain.model.User;
import com.domain.model.VerificationToken;
import com.domain.repository.BatchRepository;
import com.domain.repository.DomainRepository;
import com.domain.repository.MeasureTypeRepository;
import com.domain.repository.MeasurementRepository;
import com.domain.repository.ProcessRepository;
import com.domain.repository.ResetTokenRepository;
import com.domain.repository.SensorRepository;
import com.domain.repository.CategoryRepository;
import com.domain.repository.DomainCategoryRepository;
import com.domain.repository.DomainProcessRepository;
import com.domain.repository.UserRepository;
import com.domain.repository.VerificationTokenRepository;

@Service
@Transactional
public class DataService implements UserDetailsService {

    private Logger LOG = LoggerFactory.getLogger(DataService.class);
    
    @Value("${dataSynch.enabled}")
    private boolean dataSynchEnabled;

    @Autowired
    private DomainRepository domainRepository;
    
    @Autowired
    private DomainCategoryRepository domainCategoryRepository;
    
    @Autowired
    private DomainProcessRepository domainProcessRepository;
    
	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ProcessRepository processRepository;
	
	@Autowired
	private MeasureTypeRepository measureTypeRepository;
	
	@Autowired
	private BatchRepository batchRepository;

	@Autowired
	private MeasurementRepository measurementRepository;

	@Autowired
	private SensorRepository sensorRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    private ResetTokenRepository resetTokenRepository;
    
	//
	//	Domain table access methods
	//
	//
    public Domain getDomain( Long id ) {
        LOG.info("Getting Domain, id:" + id);
    	Optional<Domain> optionalDomain = domainRepository.findById( id );
        if (optionalDomain.isPresent()) {
	    	return optionalDomain.get();
        } else {
	    	LOG.info( "Domain not found" );
        	return null;
        }								    	
    }
    
    public List<Domain> getAllDomains() {
    	return domainRepository.findAll();
    }

    public List<Domain> getDomainsToSynchronize() {
    	return domainRepository.findDomainsToSynchronize();
    }
    
    public Domain saveDomain( Domain domain ) {
    	Domain domainToSave;
        LOG.info("Saving Domain:" + domain);
        try {
        	if( domain.getDbSynchToken() == null || domain.getDbSynchToken().length() <= 0 ) {
        		domain.setDbSynchToken( getSynchToken() );
        	}
            domainToSave = domainRepository.save( domain );
            LOG.info("Saved Domain:" + domainToSave );
            return domainToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveDomain: " + e.getMessage());
        }
        return new Domain();
    }

    public Domain updateDomain(Domain domainToUpdate ) {
        LOG.info("Update Process:" + domainToUpdate);
    	Optional<Domain> result = domainRepository.findById( domainToUpdate.getId() );
    	if( result.isPresent() ) {
    		Domain foundDomain = result.get();
	        try {
	        	foundDomain.setPosition( domainToUpdate.getPosition() );
	        	foundDomain.setName( domainToUpdate.getName() );
	        	foundDomain.setImage( domainToUpdate.getImage() );
	        	foundDomain.setDescription( domainToUpdate.getDescription() );
	        	foundDomain.setCategoryLabel( domainToUpdate.getCategoryLabel() );
	        	foundDomain.setRoleLabel( domainToUpdate.getRoleLabel() );
	        	foundDomain.setDbSynch( domainToUpdate.getDbSynch() );
	        	foundDomain.setDbSynchToken( domainToUpdate.getDbSynchToken() );
	            return domainRepository.save( foundDomain );
	        } catch (Exception e) {
	            LOG.error("DataService: Exception: updateDomain: " + e.getMessage());
	        }
    	}
        return domainToUpdate;
    }

    public void deleteDomain( Long id ) {
        try {
        	Optional<Domain> result = domainRepository.findById( id);
        	if( result.isPresent() ) {
        		Domain foundDomain = result.get();
        		domainRepository.delete( foundDomain );
        	}
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteDomain: " + e.getMessage());
        }
    }
 
    public Long getDomainBatchCount( Long id ) {
        Long count = batchRepository.domainCount( id );
        LOG.info("getDomainBatchCount, id:" + id + " batches: " + count );
        return count;
    }
    
	//
	//	DomainCategory table access methods
	//
	//
    public DomainCategory getDomainCategory( Long id ) {
        LOG.info("Getting DomainCategory, id: " + id);
        return domainCategoryRepository.getById(id);
    }

    public DomainCategory getDomainCategory( Long domainId, Long categoryId ) {
        LOG.info("Getting DomainCategory, Id: " + domainId, " Category Id: " + categoryId );
        return domainCategoryRepository.findDomainCategory( domainId, categoryId );
    }
    
    public DomainCategory getDomainCategory( String dbSynchToken ) {
        LOG.info("Getting DomainCategory, SynchToken: " + dbSynchToken );
        try {
        	DomainCategory domainCategory = domainCategoryRepository.findBySynchToken( dbSynchToken );
	        LOG.info("getDomainCategory: " + domainCategory );
	        return domainCategory;
        }
        catch( Exception e ) {
        	LOG.error( "getDomainCategory: Execption",  e );
        }
        return null;
    }
    
    public List<DomainCategory> getAllDomainCategories() {
    	return domainCategoryRepository.findAll();
    }

    public List<Category> getCategoriesForDomain( Long id ) {
    	ArrayList<Category> categories = new ArrayList<Category>();
    	List<DomainCategory> domainCategories = domainCategoryRepository.findDomainCategoryiesByDomainId( id );
    	
    	for( DomainCategory domainCategory:domainCategories)  {
    		categories.add( domainCategory.getCategory() );
    	}
    	return categories;
    }
    
    public List<DomainCategory> getDomainCategoriesToSynchronize() {
    	return domainCategoryRepository.findDomainCategoryiesToSynchronize();
    }

    public DomainCategory saveDomainCategory( DomainCategory domainCategory ) {
    	DomainCategory domainCategoryToSave;
        try {
            LOG.info("Saving DomainCategory: " + domainCategory );
        	if( domainCategory.getDbSynchToken() == null || domainCategory.getDbSynchToken().length() <= 0 ) {
        		domainCategory.setDbSynchToken( getSynchToken() );
        	}
        	domainCategory.setUpdateTime( new Date() );
        	domainCategoryToSave = domainCategoryRepository.save( domainCategory );
            return domainCategoryToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveDomainCategory: " + e.getMessage());
        }
        return new DomainCategory();
    }

    public DomainCategory updateDomainCategory( DomainCategory domainCategoryToUpdate ) {
        LOG.info("Update DomainCategory: " + domainCategoryToUpdate );
        DomainCategory foundDomainCategory = domainCategoryRepository.getById( domainCategoryToUpdate.getId() );
        try {
        	foundDomainCategory.setDomain( domainCategoryToUpdate.getDomain() );
        	foundDomainCategory.setCategory( domainCategoryToUpdate.getCategory() );
        	foundDomainCategory.setUpdateTime( new Date() );
        	foundDomainCategory.setDbSynch( domainCategoryToUpdate.getDbSynch() );
        	foundDomainCategory.setDbSynchToken( domainCategoryToUpdate.getDbSynchToken() );
            return domainCategoryRepository.save( foundDomainCategory );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateDomainCategory: " + e.getMessage());
        }
        return foundDomainCategory;
    }

    public void deleteDomainCategory( Long id ) {
        try {
        	DomainCategory foundDomainCategory = domainCategoryRepository.getById( id );
        	domainCategoryRepository.delete( foundDomainCategory );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteDomainCategory: " + e.getMessage());
        }
    }

    public Long getDomainCategoryDomainCount( Long id ) {
        Long count = domainCategoryRepository.domainCount( id );
        LOG.info("getDomainCategoryDomainCount, id:" + id + " domains: " + count );
        return count;
    }

    public Long getDomainCategoryCategoryCount( Long id ) {
        Long count = domainCategoryRepository.categoryCount( id );
        LOG.info("getDomainCategoryCategoryCount, id:" + id + " categories: " + count );
        return count;
    }
    
	//
	//	DomainProcess table access methods
	//
	//
    public DomainProcess getDomainProcess( Long id ) {
        LOG.info("Getting DomainProcess, id: " + id);
        return domainProcessRepository.getById(id);
    }

    public DomainProcess getDomainProcess( Long domainId, String processCode ) {
        LOG.info("Getting DomainProcess, Id: " + domainId, " Process Id: " + processCode );
        return domainProcessRepository.findDomainProcess( domainId, processCode );
    }
    
    public DomainProcess getDomainProcess( String dbSynchToken ) {
        LOG.info("Getting DomainProcess, SynchToken: " + dbSynchToken );
        try {
        	DomainProcess domainProcess = domainProcessRepository.findBySynchToken( dbSynchToken );
	        LOG.info("getDomainProcess: " + domainProcess );
	        return domainProcess;
        }
        catch( Exception e ) {
        	LOG.error( "getDomainProcess: Execption",  e );
        }
        return null;
    }
    
    public List<DomainProcess> getAllDomainProcesses() {
    	return domainProcessRepository.findAll();
    }

    public List<Process> getProcessesForDomain( Long id ) {
    	ArrayList<Process> processes = new ArrayList<Process>();
    	List<DomainProcess> domainProcesses = domainProcessRepository.findDomainProcessesByDomainId( id );
    	
    	for( DomainProcess domainProcess:domainProcesses)  {
    		processes.add( domainProcess.getProcess() );
    	}
    	return processes;
    }
    
    public List<DomainProcess> getDomainProcessesToSynchronize() {
    	return domainProcessRepository.findDomainProcessesToSynchronize();
    }

    public DomainProcess saveDomainProcess( DomainProcess domainProcess ) {
    	DomainProcess domainProcessToSave;
        try {
            LOG.info("Saving DomainProcess: " + domainProcess );
        	if( domainProcess.getDbSynchToken() == null || domainProcess.getDbSynchToken().length() <= 0 ) {
        		domainProcess.setDbSynchToken( getSynchToken() );
        	}
        	domainProcess.setUpdateTime( new Date() );
        	domainProcessToSave = domainProcessRepository.save( domainProcess );
            return domainProcessToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveDomainProcess: " + e.getMessage());
        }
        return new DomainProcess();
    }

    public DomainProcess updateDomainProcess( DomainProcess domainProcessToUpdate ) {
        LOG.info("Update DomainProcess: " + domainProcessToUpdate );
        DomainProcess foundDomainProcess = domainProcessRepository.getById( domainProcessToUpdate.getId() );
        try {
        	foundDomainProcess.setDomain( domainProcessToUpdate.getDomain() );
        	foundDomainProcess.setProcess( domainProcessToUpdate.getProcess() );
        	foundDomainProcess.setUpdateTime( new Date() );
        	foundDomainProcess.setDbSynch( domainProcessToUpdate.getDbSynch() );
        	foundDomainProcess.setDbSynchToken( domainProcessToUpdate.getDbSynchToken() );
            return domainProcessRepository.save( domainProcessToUpdate );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateDomainProcess: " + e.getMessage());
        }
        return foundDomainProcess;
    }

    public void deleteDomainProcess( Long id ) {
        try {
        	DomainProcess foundDomainProcess = domainProcessRepository.getById( id );
        	domainProcessRepository.delete( foundDomainProcess );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteDomainProcess: " + e.getMessage());
        }
    }

    public Long getDomainProcessDomainCount( Long id ) {
        Long count = domainProcessRepository.domainCount( id );
        LOG.info("getDomainProcessDomainCount, id:" + id + " domains: " + count );
        return count;
    }

    public Long getDomainProcessProcessCount( String code ) {
        Long count = domainProcessRepository.processCount( code );
        LOG.info("getDomainProcessProcessCount, code:" + code + " processes: " + count );
        return count;
    }
    
	//
	//	Category table access methods
	//
	//
    public Category getCategory( Long id ) {
        LOG.info("Getting Category, id: " + id);
        return categoryRepository.getOne(id);
    }
    
    public Category getCategory( String dbSynchToken ) {
        LOG.info("Getting Category, SynchToken: " + dbSynchToken );
        try {
	        Category category = categoryRepository.findCategoryBySynchToken( dbSynchToken );
	        LOG.info("getCategory: " + category );
	        return category;
        }
        catch( Exception e ) {
        	LOG.error( "getCategory findCategoryBySynchToken: Execption",  e );
        }
        return null;
    }
    
    public List<Category> getAllCategories() {
    	return categoryRepository.findAll();
    }

    public List<Category> getCategoriesToSynchronize() {
    	return categoryRepository.findCategoryiesToSynchronize();
    }

    public Category saveCategory( Category category ) {
    	Category categoryToSave;
        try {
            LOG.info("Saving Category: " + category );
        	if( category.getDbSynchToken() == null || category.getDbSynchToken().length() <= 0 ) {
        		category.setDbSynchToken( getSynchToken() );
        	}
        	categoryToSave = categoryRepository.save(category);
            return categoryToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveCategory: " + e.getMessage());
        }
        return new Category();
    }

    public Category updateCategory( Category categoryToUpdate ) {
        LOG.info("Update Category: " + categoryToUpdate );
    	Category foundCategory = categoryRepository.getOne( categoryToUpdate.getId() );
        try {
        	foundCategory.setName( categoryToUpdate.getName() );
        	foundCategory.setReference( categoryToUpdate.getReference() );
        	foundCategory.setDescription( categoryToUpdate.getDescription() );
        	foundCategory.setDbSynch( categoryToUpdate.getDbSynch() );
        	foundCategory.setDbSynchToken( categoryToUpdate.getDbSynchToken() );
            return categoryRepository.save( foundCategory );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateCategory: " + e.getMessage());
        }
        return foundCategory;
    }

    public void deleteCategory( Long id ) {
        try {
        	Category foundCategory = categoryRepository.getOne( id );
        	categoryRepository.delete( foundCategory );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteCategory: " + e.getMessage());
        }
    }

    public Long getCategoryBatchCount( Long id ) {
        Long count = batchRepository.categoryCount( id );
        LOG.info("getCategoryBatchCount, id: " + id + " batches: " + count );
        return count;
    }
    
    public Long getCategoryBatchCount( Long categoryId, Long domainId ) {
        Long count = batchRepository.categoryCount( categoryId, domainId );
        LOG.info("getCategoryBatchCount, categoryId: " + categoryId + " domain Id: " + domainId + " batches: " + count );
        return count;
    }
    
	//
	//	Process table access methods
	//
	//
    public Process getProcess( String code ) {
        LOG.info("Getting Process, code:" + code);
    	Optional<Process> optionalProcess = processRepository.findById( code );
        if (optionalProcess.isPresent()) {
	    	return optionalProcess.get();
        } else {
	    	LOG.info( "Process not found" );
        	return null;
        }								    	
    }
    
    public List<Process> getAllProcesses() {
    	return processRepository.findAll();
    }

    public List<Process> getProcessesToSynchronize() {
    	return processRepository.findProcessToSynchronize();
    }
    
    public Process saveProcess( Process process ) {
    	Process processToSave;
        LOG.info("Saving Process:" + process);
        try {
            LOG.info("Saving Process: " + process );
            processToSave = processRepository.save( process );
            return processToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveProcess: " + e.getMessage());
        }
        return new Process();
    }

    public Process updateProcess( Process processToUpdate ) {
        LOG.info("Update Process:" + processToUpdate);
    	Process foundProcess = processRepository.getOne( processToUpdate.getCode() );
        try {
        	foundProcess.setName( processToUpdate.getName() );
        	foundProcess.setVoiceAssist( processToUpdate.isVoiceAssist() );
        	foundProcess.setDbSynch( processToUpdate.getDbSynch() );
            return processRepository.save( foundProcess );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateProcess: " + e.getMessage());
        }
        return processToUpdate;
    }

    public void deleteProcess( String code ) {
        try {
        	Process foundProcess = processRepository.getOne( code );
        	processRepository.delete( foundProcess );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteProcess: " + e.getMessage());
        }
    }
    
    public Long getProcessSensorCount( String code ) {
        Long count = sensorRepository.processCount( code );
        LOG.info("getProcessSensorCount, code:" + code + " sensors: " + count );
        return count;
    }

    public Long getProcessMeasurementCount( String code ) {
        Long count = measurementRepository.processCount( code );
        LOG.info("getProcessMeasurementCount, code:" + code + " measurements: " + count );
        return count;
    }
    
	//
	//	MeasurementType table access methods
	//
	//
    public MeasureType getMeasureType( String code ) {
        LOG.info("Getting MeasureType, code:" + code);
    	Optional<MeasureType> optionalMeasureType = measureTypeRepository.findById( code );
        if (optionalMeasureType.isPresent()) {
	    	return optionalMeasureType.get();
        } else {
	    	LOG.info( "MeasureType not found" );
        	return null;
        }								    	
    }

    public List<MeasureType> getAllMeasureTypes() {
    	return measureTypeRepository.findAll();
    }

    public List<MeasureType> getMeasureTypesToSynchronize() {
    	return measureTypeRepository.findMeasureTypesToSynchronize();
    }

    public List<MeasureType> getMeasureTypesToGraph() {
    	return measureTypeRepository.findMeasureTypesToGraph();
    }
    
    public MeasureType saveMeasureType( MeasureType measureType ) {
    	MeasureType measureTypeToSave;
        try {
            LOG.info("Saving MeasureType: " + measureType );
            measureTypeToSave = measureTypeRepository.save( measureType );
            return measureTypeToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveMeasureType: " + e.getMessage());
        }
        return new MeasureType();
    }

    public MeasureType updateMeasureType( MeasureType measureTypeToUpdate ) {
    	MeasureType foundMeasureType = measureTypeRepository.getOne( measureTypeToUpdate.getCode() );
        try {
        	foundMeasureType.setName( measureTypeToUpdate.getName() );
        	foundMeasureType.setDbSynch( measureTypeToUpdate.getDbSynch() );
        	foundMeasureType.setVoiceAssist( measureTypeToUpdate.isVoiceAssist() );
        	foundMeasureType.setGraphData( measureTypeToUpdate.isGraphData() );
        	foundMeasureType.setMinValue( measureTypeToUpdate.getMinValue() );
        	foundMeasureType.setMaxValue( measureTypeToUpdate.getMaxValue() );
        	foundMeasureType.setGraphType( measureTypeToUpdate.getGraphType() );
            return measureTypeRepository.save( foundMeasureType );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateMeasureType: " + e.getMessage());
        }
        return measureTypeToUpdate;
    }

    public void deleteMeasureType( String code ) {
        try {
        	MeasureType foundMeasureType = measureTypeRepository.getOne( code );
        	measureTypeRepository.delete( foundMeasureType );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteMeasureType: " + e.getMessage());
        }
    }

    public Long getMeasureTypeSensorCount( String code ) {
        Long count = sensorRepository.measureTypeCount( code );
        LOG.info("getMeasureTypeSensorCount, code:" + code + " sensors: " + count );
        return count;
    }

    public Long getMeasureTypeMeasurementCount( String code ) {
        Long count = measurementRepository.measureTypeCount( code );
        LOG.info("getMeasureTypeMeasurementCount, code:" + code + " measurements: " + count );
        return count;
    }
    
	//
	//	Batch table access methods
	//
	//
    public Batch getBatch( Long id ) {
        LOG.info("Getting Batch, id:" + id );
        return batchRepository.getOne( id );
    }

    public Batch getBatch( String dbSynchToken ) {
        LOG.info("Getting Batch, SynchToken: " + dbSynchToken );
        return batchRepository.findBatchBySynchToken( dbSynchToken );
    }
    
    public List<Batch> getAllBatches() {
    	return batchRepository.findAll();
    }

    public List<Batch> getAllBatches( Long domainId ) {
    	return batchRepository.findBatchesForDomain( domainId );
    }
    
    public List<Batch> getActiveBatches() {
    	return batchRepository.findActiveBatches();
    }

    public List<Batch> getActiveBatches( Long domainId ) {
    	return batchRepository.findActiveBatchesForDomain( domainId );
    }
    
    public List<Batch> getBatchesToSynchronize() {
    	return batchRepository.findBatchesToSynchronize();
    }
    
    public Batch saveBatch( Batch batch ) {
    	Batch batchToSave;
        try {
            LOG.info("Saving Batch: " + batch );
        	if( batch.getDbSynchToken() == null || batch.getDbSynchToken().length() <= 0 ) {
        		batch.setDbSynchToken( getSynchToken() );
        	}
            batchToSave = batchRepository.save( batch );
            return batchToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveBatch: " + e.getMessage());
        }
        return new Batch();
    }

    public Batch updateBatch( Batch batchToUpdate ) {
        LOG.info("Update Batch: " + batchToUpdate );
    	Batch foundBatch = batchRepository.getOne( batchToUpdate.getId() );
        try {
            foundBatch.setCategory( batchToUpdate.getCategory() );     
            foundBatch.setDomain( batchToUpdate.getDomain() );
        	foundBatch.setDbSynchToken( batchToUpdate.getDbSynchToken() );
        	foundBatch.setActive( batchToUpdate.isActive() );
        	foundBatch.setName( batchToUpdate.getName() );
        	foundBatch.setDescription( batchToUpdate.getDescription() );
        	foundBatch.setStartTime( batchToUpdate.getStartTime() );
        	foundBatch.setDbSynch( batchToUpdate.getDbSynch() );
            return batchRepository.save( foundBatch );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateBatch: " + e.getMessage());
        }
        return batchToUpdate;
    }

    public void deleteBatch( Long id ) {
        try {
        	Batch foundBatch = batchRepository.getOne( id );
        	measurementRepository.deleteByBatchId( id );
        	batchRepository.delete( foundBatch );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteBatch: " + e.getMessage());
        }
    }
    
    public Long getBatchSensorCount( Long id ) {
        Long count = sensorRepository.batchCount( id );
        LOG.info("getBatchSensorCount, id:" + id + " sensors: " + count );
        return count; 
    }

	//
	//	Measurement table access methods
	//
	//
    public Measurement getMeasurement( Long id ) {
        LOG.info("Getting Measurement, id:" + id );
        return measurementRepository.getOne( id );
    }

    public Measurement getMeasurement( String dbSynchToken ) {
        LOG.info("Getting Measurement, SynchToken:" + dbSynchToken );
        return measurementRepository.findMeasurementBySynchToken( dbSynchToken );
    }
    
    public List<Measurement> getRecentMeasurement( Long id ) {
        LOG.info("Getting recent Measurement" );
        return measurementRepository.findMostRecent( id );
    }
    
    public List<Measurement> getMeasurementsByBatch( Long id ) {
    	return measurementRepository.findByBatchId( id );
    }

    public List<Measurement> getMeasurementsByBatchType( Long id, String type ) {
    	return measurementRepository.findByBatchIdType( id, type );
    }
    
    public Page<Measurement> getMeasurementsPageByBatch( int pageNo, Long id ) {
    	int noOfRecords = 10;
    	PageRequest pageRequest = PageRequest.of( pageNo, noOfRecords );
        Pageable page = pageRequest;
        
        Page<Measurement> pagedResult = (Page<Measurement>) measurementRepository.findPageByBatchId(id, page);
        return pagedResult;
        // changing to List
/*        if(pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<Measurement>();
        }
*/        
    }
    
    public List<Measurement> getMeasurementsToSynchronize( ) {
    	return measurementRepository.findMeasurementsToSynchronize();
    }
    
    public Measurement saveMeasurement( Measurement measurement ) {
    	Measurement measurementToSave;
        try {
            LOG.info("Saving Measurement: " + measurement );
        	if( measurement.getDbSynchToken() == null || measurement.getDbSynchToken().length() <= 0 ) {
        		measurement.setDbSynchToken( getSynchToken() );
        	}
            measurementToSave = measurementRepository.save( measurement );
            return measurementToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveMeasurement: " + e.getMessage());
        }
        return new Measurement();
    }

    public Measurement updateMeasurement( Measurement measurementToUpdate ) {
    	LOG.info("updateMeasurement:" + measurementToUpdate );
    	Measurement foundMeasurement = measurementRepository.getOne( measurementToUpdate.getId() );
        try {
            foundMeasurement.setBatch( measurementToUpdate.getBatch() );        		
        	foundMeasurement.setDbSynchToken( measurementToUpdate.getDbSynchToken() );
        	foundMeasurement.setValueNumber( measurementToUpdate.getValueNumber() );
        	foundMeasurement.setValueText( measurementToUpdate.getValueText() );
        	foundMeasurement.setProcess( measurementToUpdate.getProcess() );
        	foundMeasurement.setType(measurementToUpdate.getType() );
        	foundMeasurement.setMeasurementTime( measurementToUpdate.getMeasurementTime() );
        	foundMeasurement.setDbSynch( measurementToUpdate.getDbSynch() );
            return measurementRepository.save( foundMeasurement );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateMeasurement: " + e.getMessage());
        }
        return measurementToUpdate;
    }

    public void deleteMeasurement( Long id ) {
        try {
        	Measurement foundMeasurement = measurementRepository.getOne( id );
        	measurementRepository.delete( foundMeasurement );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteMeasurement: " + e.getMessage());
        }
    }

    public void deleteDuplicateMeasurements( Long batchId ) {
    	List<Measurement> measurements = measurementRepository.findByBatchId( batchId );
    	
    	Measurement baseMeasurement = null;
    	for( Measurement measurement:measurements) {
    		if( baseMeasurement != null) {
    			if( baseMeasurement.getType().getCode().equals( measurement.getType().getCode() )
    					&& baseMeasurement.getProcess().getCode().equals( measurement.getProcess().getCode() )
    					&& baseMeasurement.getValueNumber() == measurement.getValueNumber()
    					&& baseMeasurement.getValueText().equals( measurement.getValueText() ) 
    			) {
    		    	if( dataSynchEnabled && measurement.getDbSynchToken() != null && measurement.getDbSynchToken().length() > 0 ) {
    		    		LOG.info( "Measurement " + measurement.getId() + " scheduled for deletion." );
    		    		measurement.setDbSynch( DbSync.DELETE );
    			    	updateMeasurement( measurement );
    		    	}
    		    	else {
        		    	LOG.info("Delete Measurement:" + measurement );
        		    	deleteMeasurement( measurement.getId() );
    		    	}
    			}
    		}
			baseMeasurement = measurement;    				
    	}
    }
    
	//
	//	Sensor table access methods
	//
	//
    public Sensor getSensor( Long id ) {
        LOG.info("Getting Sensor, id:" + id);
        return sensorRepository.getOne(id);
    }

    public Sensor getSensor( String dbSynchToken ) {
        LOG.info("Getting Sensor, SynchToken:" + dbSynchToken );
        return sensorRepository.findSensorBySynchToken( dbSynchToken );
    }
    
    public List<Sensor> getAllSensors() {
    	return sensorRepository.findAll();
    }

    public List<Sensor> getEnabledSensors( SensorType sensorType ) {
    	String type = "None";
    	if( sensorType == SensorType.BLUETOOTH ) {
    		type = "Bluetooth";
    	}
    	else if( sensorType == SensorType.WIFI ) {
    		type = "WiFi";
    	}
    	return sensorRepository.findEnabledSensors( type );
    }

    public List<Sensor> getSensorsToSynchronize() {
    	return sensorRepository.findSensorsToSynchronize();
    }
    
    public Sensor saveSensor( Sensor sensor ) {
    	Sensor sensorToSave;
        try {
            LOG.info("Saving Sensor...");
        	if( sensor.getDbSynchToken() == null || sensor.getDbSynchToken().length() <= 0 ) {
        		sensor.setDbSynchToken( getSynchToken() );
        	}
            sensor.setUpdateTime( new Date() );
            sensorToSave = sensorRepository.save(sensor);
            return sensorToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveSensor: " + e.getMessage());
        }
        return new Sensor();
    }

    public Sensor updateSensor( Sensor sensorToUpdate ) {
    	Sensor foundSensor = sensorRepository.getOne( sensorToUpdate.getId() );
        try {
            foundSensor.setBatch( sensorToUpdate.getBatch() );
        	foundSensor.setEnabled( sensorToUpdate.isEnabled() );
        	foundSensor.setName( sensorToUpdate.getName() );
        	foundSensor.setUrl( sensorToUpdate.getUrl() );
        	foundSensor.setUserId( sensorToUpdate.getUserId() );
        	foundSensor.setPin( sensorToUpdate.getPin() );
        	foundSensor.setCommunicationType( sensorToUpdate.getCommunicationType() );
        	foundSensor.setTrigger( sensorToUpdate.getTrigger() );
        	foundSensor.setProcess( sensorToUpdate.getProcess() );
        	foundSensor.setMeasureType( sensorToUpdate.getMeasureType() );
        	foundSensor.setUpdateTime( new Date() );
        	foundSensor.setDbSynch( sensorToUpdate.getDbSynch() );
        	foundSensor.setDbSynchToken( sensorToUpdate.getDbSynchToken() );
            return sensorRepository.save( foundSensor );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateSensor: " + e.getMessage());
        }
        return sensorToUpdate;
    }

    public void deleteSensor( Long id ) {
        try {
        	Sensor foundSensor = sensorRepository.getOne( id );
        	sensorRepository.delete( foundSensor );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteSensor: " + e.getMessage());
        }
    }
    
    //
    //	User table access methods
    //
    @Override
    public UserDetails loadUserByUsername(String username) {

    	LOG.info("loadUserByUsername: " + username );    	
    	User user = userRepository.findByUsername(username);
        
        UserBuilder builder = null;
        if (user != null && user.isValidated() ) {
          builder = org.springframework.security.core.userdetails.User.withUsername(username);
          builder.password( user.getPassword() );
          builder.roles(user.getRoles());
        } else {
          throw new UsernameNotFoundException("User not found.");
        }
        return builder.build();        
    }
    
    public User getUser( Long id ) {
        LOG.info("Getting User, id:" + id);
        return userRepository.getOne(id);
    }

    public User getUserByName(String username) {

    	LOG.info("getUserByName: " + username );    	
    	return userRepository.findByUsername(username);
    }
    
    
    public List<User> getAllUsers() {
    	return userRepository.findAll();
    }

    public List<User> getUsersToSynchronize() {
    	return userRepository.findUsersToSynchronize();
    }
    
    public User saveUser( User user ) {
    	User userToSave;
        try {
            LOG.info("Saving User...");
            userToSave = userRepository.save(user);
            return userToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveUser: " + e.getMessage());
        }
        return new User();
    }

    public User updateUser( User user ) {
    	User foundUser = userRepository.getOne( user.getId() );
        try {
        	foundUser.setUsername( user.getUsername() );
        	foundUser.setPassword( user.getPassword() );
        	foundUser.setRoles( user.getRoles() );
        	foundUser.setDbSynch( user.getDbSynch() );
            return userRepository.save( foundUser );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateUser: " + e.getMessage());
        }
        return user;
    }

    public void deleteUser( Long id ) {
        try {
        	User user = userRepository.getOne( id );
        	userRepository.delete( user );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteUser: " + e.getMessage());
        }
    }
    
	//
	//	VerificationToken table access methods
	//
	//
    public VerificationToken getVerificationToken( String token ) {
        LOG.info("Getting VerificationToken:" + token );
	    return verificationTokenRepository.findById( token ).orElse(null);
    }
    
    public VerificationToken saveVerificationToken( VerificationToken verificationToken ) {
    	VerificationToken verificationTokenToSave;
        LOG.info("Saving VerificationToken:" + verificationToken);
        try {
            LOG.info("Saving VerificationToken: " + verificationToken );
            verificationTokenToSave = verificationTokenRepository.save( verificationToken );
            return verificationTokenToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveVerificationToken: " + e.getMessage());
        }
        return new VerificationToken();
    }

    public VerificationToken updateVerificationToken( VerificationToken verificationTokenToUpdate ) {
        LOG.info("Update VerificationToken:" + verificationTokenToUpdate);
        VerificationToken foundVerificationToken = verificationTokenRepository.getOne( verificationTokenToUpdate.getToken() );
        try {
        	foundVerificationToken.setUsername( verificationTokenToUpdate.getUsername() );
        	foundVerificationToken.setExpiryDate( verificationTokenToUpdate.getExpiryDate() );
            return verificationTokenRepository.save( foundVerificationToken );
        } catch (Exception e) {
            LOG.error("DataService: Exception: updateVerificationToken: " + e.getMessage());
        }
        return verificationTokenToUpdate;
    }

    public void deleteVerificationToken( String token ) {
        try {
        	VerificationToken foundVerificationToken = verificationTokenRepository.getOne( token );
        	verificationTokenRepository.delete( foundVerificationToken );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteVerificationToken: " + e.getMessage());
        }
    }

	//
	//	ResetToken table access methods
	//
	//
    public ResetToken getResetToken( String token ) {
        LOG.info("Getting ResetTokenRepository:" + token );
	    return resetTokenRepository.findById( token ).orElse(null);
    }
    
    public ResetToken saveResetToken( ResetToken resetToken ) {
    	ResetToken resetTokenToSave;
        LOG.info("Saving VerificationToken:" + resetToken);
        try {
            LOG.info("Saving ResetToken: " + resetToken );
            resetTokenToSave = resetTokenRepository.save( resetToken );
            return resetTokenToSave;
        } catch (Exception e) {
            LOG.error("DataService: Exception: saveResetToken: " + e.getMessage());
        }
        return new ResetToken();
    }

    public ResetToken updateResetToken( ResetToken resetTokenToUpdate ) {
        LOG.info("Update ResetToken:" + resetTokenToUpdate);
        ResetToken foundResetToken= resetTokenRepository.getOne( resetTokenToUpdate.getToken() );
        try {
        	foundResetToken.setUsername( resetTokenToUpdate.getUsername() );
        	foundResetToken.setEmail( resetTokenToUpdate.getEmail() );
            return resetTokenRepository.save( foundResetToken );
        } catch (Exception e) { 
            LOG.error("DataService: Exception: updateResetToken: " + e.getMessage());
        }
        return resetTokenToUpdate;
    }

    public void deleteResetToken( String token ) {
        try {
        	ResetToken foundResetToken = resetTokenRepository.getOne( token );
        	resetTokenRepository.delete( foundResetToken );
        } catch (Exception e) {
            LOG.error("DataService: Exception: deleteResetToken: " + e.getMessage());
        }
    }

    //
    //
    //
    //
    private String getSynchToken() {
    	String synchToken = "";
    	
    	try {
			String hostName = InetAddress.getLocalHost().getHostName();
			synchToken = synchToken + hostName + ":";
		} catch (UnknownHostException e) {
            LOG.error("DataService: getSynchToken getHostName Exception: " + e.getMessage());
		}
        synchToken = synchToken + UUID.randomUUID().toString();
    	return synchToken;
    }
    
    
}
