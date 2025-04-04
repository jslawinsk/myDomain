package com.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.domain.model.Batch;
import com.domain.model.DbSync;
import com.domain.model.Domain;
import com.domain.model.GraphTypes;
import com.domain.model.MeasureType;
import com.domain.model.Measurement;
import com.domain.model.Process;
import com.domain.model.ResetToken;
import com.domain.model.Sensor;
import com.domain.model.SensorType;
import com.domain.model.Category;
import com.domain.model.User;
import com.domain.model.UserRoles;
import com.domain.model.VerificationToken;
import com.domain.repository.BatchRepository;
import com.domain.repository.MeasureTypeRepository;
import com.domain.repository.MeasurementRepository;
import com.domain.repository.ProcessRepository;
import com.domain.repository.ResetTokenRepository;
import com.domain.repository.SensorRepository;
import com.domain.repository.CategoryRepository;
import com.domain.repository.UserRepository;
import com.domain.repository.VerificationTokenRepository;
import com.domain.service.DataService;

@SpringBootTest( properties = { "blueTooth.enabled=false", "wiFi.enabled=false", "dataSynch.enabled=true" } )
@AutoConfigureMockMvc
class DataServiceTest {

	
	@MockBean
	CategoryRepository categoryRepository;
	@MockBean
	ProcessRepository processRepository;
	@MockBean
	MeasureTypeRepository measureTypeRepository;
	@MockBean
	BatchRepository batchRepository;
	@MockBean
	MeasurementRepository measurementRepository;
	@MockBean
	private SensorRepository sensorRepository;
	@MockBean
	private UserRepository userRepository;
	@MockBean
    private VerificationTokenRepository verificationTokenRepository;
	@MockBean
    private ResetTokenRepository resetTokenRepository;

	@MockBean
	JavaMailSender mailSender;

	@Autowired
	DataService dataService;
    
	private Domain testDomain = new Domain( 0L, 0, "Brewery", "sports_bar_white_48dp.svg", "Home Brewery", "Style", "Brewery", DbSync.ADD, null);
	private Category testCategory = new Category( "IPA", "18a", "Hoppy", DbSync.SYNCHED, "TestToken" );
	private Process process = new Process( "FRM", "Fermentation", false, DbSync.ADD );
	private MeasureType measureType = new MeasureType( "TMP", "Temperature", true, 0, 200, GraphTypes.GAUGE, DbSync.ADD  );
	private Batch testBatch = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, testDomain, new Date() );
	private Measurement measurement = new Measurement( 70.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
	private Sensor sensor = new Sensor( 2L, false, "test", "", "", "1234", "BLUETOOTH", "", null, process, measureType, new Date(), DbSync.UPDATE, "TestToken" );
	
	private User user = new User( "ADMIN", "admin", DbSync.ADD, UserRoles.ADMIN.toString() );
	
	//
	//	Category table test methods
	//
	@Test
	void getCategory() throws Exception
	{
        Mockito.when(categoryRepository.getOne( 1L )).thenReturn( testCategory );
        
        Category category = dataService.getCategory( 1L );
        assertEquals( category.getName(), "IPA");
	}	
	
	@Test
	void getCategoryToken() throws Exception
	{
        Mockito.when(categoryRepository.findCategoryBySynchToken( "TestToken" )).thenReturn( testCategory );
        
        Category category = dataService.getCategory( "TestToken" );
        assertEquals( category.getName(), "IPA");
        
        Mockito.when( categoryRepository.findCategoryBySynchToken( "TestToken" ) ).thenThrow( new IllegalArgumentException("Test") );
        category = dataService.getCategory( "TestToken" );
        assertNull( category );
	}	
	
	@Test
	void getAllCategories() throws Exception
	{
    	List<Category> catergories = new ArrayList<Category>();
    	catergories.add( testCategory );
		
		Mockito.when(categoryRepository.findAll( )).thenReturn( catergories );
        
		List<Category> catergoriesTest = dataService.getAllCategories( );
		assertEquals(  catergoriesTest.size(), 1 );
	}	
	
	@Test
	void getCategoriesToSynchronize() throws Exception
	{
    	List<Category> catergories = new ArrayList<Category>();
    	catergories.add( testCategory );
		
		Mockito.when(categoryRepository.findCategoryiesToSynchronize( )).thenReturn( catergories );
        
		List<Category> catergoriesTest = dataService.getCategoriesToSynchronize( );
		assertEquals(  catergoriesTest.size(), 1 );
	}	

	@Test
	void saveCategory() throws Exception
	{
        Mockito.when(categoryRepository.save( testCategory )).thenReturn( testCategory );
        testCategory.setDbSynchToken( "" );
        Category category = dataService.saveCategory( testCategory );
        assertEquals( category.getName(), "IPA");
        testCategory.setDbSynchToken( "TestToken" );
	}	

	@Test
	void saveCategoryEx() throws Exception
	{
        Mockito.when( categoryRepository.save( null ) ).thenThrow( new IllegalArgumentException("Test") );
        Category category = dataService.saveCategory( null );
        assertNull( category.getName() );
	}	
	
	@Test
	void updateCategory() throws Exception
	{
		testCategory.setId( 1L );
        Mockito.when(categoryRepository.getOne( 1L )).thenReturn( testCategory );
        Mockito.when(categoryRepository.save( testCategory )).thenReturn( testCategory );
        
        Category category = dataService.updateCategory( testCategory );
        assertEquals( category.getName(), "IPA");
        
		Category testCategory2 = new Category( "test", "", "", DbSync.ADD, "" );
		category = dataService.updateCategory( testCategory2 );
        assertNull( category );
	}	
	
	@Test
	void deleteCategory() throws Exception
	{
		testCategory.setId( 1L );

		Mockito.when(categoryRepository.getOne( 1L )).thenReturn( testCategory );
        dataService.deleteCategory( 1L );
        verify( categoryRepository, times(1)).getOne( 1L );

	}	

	@Test
	void deleteCategoryEx() throws Exception
	{
		Mockito.when(categoryRepository.getOne( -1L )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteCategory( -1L );
        verify( categoryRepository, times(1)).getOne( -1L );
	}	
	
	@Test
	void getCategoryBatchCount() throws Exception
	{
        Mockito.when(batchRepository.categoryCount( 1L )).thenReturn( 0L );
        
        Long count = dataService.getCategoryBatchCount( 1L );
        assertEquals( count.longValue(), 0L );
	}	
	
	//
	//	Process table test methods
	//
	//
	@Test
	void getProcess() throws Exception
	{
		Optional<Process> process2 = Optional.ofNullable( new Process( "FRM", "Fermentation", false, DbSync.ADD ) );
		
        Mockito.when(processRepository.findById( "FRM" )).thenReturn( process2 );
        
        Process tmpProcess = dataService.getProcess( "FRM" );
        assertEquals( tmpProcess.getCode(), "FRM");
        
        Optional<Process> process3 = Optional.empty();
        Mockito.when(processRepository.findById( "TMP3" )).thenReturn( process3 );
        tmpProcess = dataService.getProcess( "TMP3" );
        assertNull( tmpProcess );
        
	}	
	
	@Test
	void getAllProcesses() throws Exception
	{
    	List<Process> processes = new ArrayList<Process>();
    	processes.add( process );
		
		Mockito.when(processRepository.findAll( )).thenReturn( processes );
        
		List<Process> processesTest = dataService.getAllProcesses( );
		assertEquals(  processesTest.size(), 1 );
	}	
	
	@Test
	void getProcessesToSynchronize() throws Exception
	{
    	List<Process> processes = new ArrayList<Process>();
    	processes.add( process );
		
		Mockito.when(processRepository.findProcessToSynchronize( )).thenReturn( processes );
        
		List<Process> processesTest = dataService.getProcessesToSynchronize( );
		assertEquals(  processesTest.size(), 1 );
	}	

	@Test
	void saveProcess() throws Exception
	{
        Mockito.when(processRepository.save( process )).thenReturn( process );
        
        Process tmpProcess = dataService.saveProcess( process );
        assertEquals( tmpProcess.getCode(), "FRM");
	}	

	@Test
	void saveProcessEx() throws Exception
	{
        Mockito.when(processRepository.save( null )).thenThrow( new IllegalArgumentException("Test") );
        
        Process tmpProcess = dataService.saveProcess( null );
        assertNull( tmpProcess.getCode() );
	}	
	
	@Test
	void updateProcess() throws Exception
	{
        Mockito.when(processRepository.getOne( "FRM" )).thenReturn( process );
        Mockito.when(processRepository.save( process )).thenReturn( process );
        
        Process tmpProcess = dataService.updateProcess( process );
        assertEquals( tmpProcess.getCode(), "FRM");
        
    	Process process2 = new Process( "TST", "Test", DbSync.ADD );
        tmpProcess = dataService.updateProcess( process2 );
        assertEquals( tmpProcess.getCode(), "TST");        
	}	
	
	@Test
	void deleteProcess() throws Exception
	{
        Mockito.when(processRepository.getOne( "FRM" )).thenReturn( process );
        dataService.deleteProcess( "FRM" );
        verify( processRepository, times(1)).getOne( "FRM" );
	}	

	@Test
	void deleteProcessEx() throws Exception
	{
        Mockito.when(processRepository.getOne( null )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteProcess( null );
        verify( processRepository, times(1)).getOne( null );
	}	

	@Test
	void getProcessSensorCount() throws Exception
	{
        Mockito.when(sensorRepository.processCount("FRM" )).thenReturn( 0L );
        
        Long count = dataService.getProcessSensorCount( "FRM" );
        assertEquals( count.longValue(), 0L );
	}	
	
	@Test
	void getProcessMeasurementCount() throws Exception
	{
        Mockito.when(measurementRepository.processCount("FRM" )).thenReturn( 0L );
        
        Long count = dataService.getProcessMeasurementCount( "FRM" );
        assertEquals( count.longValue(), 0L );
	}	
	
	//
	//	MeasurementType table test methods
	//
	//
	@Test
	void getMeasureType() throws Exception
	{
		Optional<MeasureType> measureType2 = Optional.ofNullable(new MeasureType( "TMP", "Temperature", true, 0, 200, GraphTypes.GAUGE, DbSync.ADD  ));
		
        Mockito.when(measureTypeRepository.findById( "TMP" )).thenReturn( measureType2 );
        
        MeasureType tmpMeasureType = dataService.getMeasureType( "TMP" );
        assertEquals( tmpMeasureType.getCode(), "TMP");
        
        Optional<MeasureType> measureType3 = Optional.empty();
        Mockito.when(measureTypeRepository.findById( "TMP3" )).thenReturn( measureType3 );
        tmpMeasureType = dataService.getMeasureType( "TMP3" );
        assertNull( tmpMeasureType );
        
	}	
	
	@Test
	void getAllMeasureTypes() throws Exception
	{
    	List<MeasureType> measureTypes = new ArrayList<MeasureType>();
    	measureTypes.add( measureType );
		
		Mockito.when( measureTypeRepository.findAll( )).thenReturn( measureTypes );
        
		List<MeasureType> measureTypeTest = dataService.getAllMeasureTypes( );
		assertEquals(  measureTypeTest.size(), 1 );
	}	
	
	@Test
	void getMeasureTypesToSynchronize() throws Exception
	{
    	List<MeasureType> measureTypes = new ArrayList<MeasureType>();
    	measureTypes.add( measureType );
		
		Mockito.when( measureTypeRepository.findMeasureTypesToSynchronize( )).thenReturn( measureTypes );
        
		List<MeasureType> measureTypeTest = dataService.getMeasureTypesToSynchronize( );
		assertEquals(  measureTypeTest.size(), 1 );
	}	

	@Test
	void getMeasureTypesToGraph() throws Exception
	{
    	List<MeasureType> measureTypes = new ArrayList<MeasureType>();
    	measureTypes.add( measureType );
		
		Mockito.when( measureTypeRepository.findMeasureTypesToGraph( )).thenReturn( measureTypes );
        
		List<MeasureType> measureTypeTest = dataService.getMeasureTypesToGraph( );
		assertEquals(  measureTypeTest.size(), 1 );
	}	

	@Test
	void saveMeasureType() throws Exception
	{
        Mockito.when(measureTypeRepository.save( measureType )).thenReturn( measureType );
        
        MeasureType tmpMeasureType = dataService.saveMeasureType( measureType );
        assertEquals( tmpMeasureType.getCode(), "TMP");
	}	

	@Test
	void saveMeasureTypeEx() throws Exception
	{
        Mockito.when(measureTypeRepository.save( null )).thenThrow( new IllegalArgumentException("Test") );
        
        MeasureType tmpMeasureType = dataService.saveMeasureType( null );
        assertNull( tmpMeasureType.getCode() );
	}	
	
	@Test
	void updateMeasureType() throws Exception
	{
        Mockito.when(measureTypeRepository.getOne( "TMP" )).thenReturn( measureType );
        Mockito.when(measureTypeRepository.save( measureType )).thenReturn( measureType );
        
        MeasureType tmpMeasureType = dataService.updateMeasureType( measureType );
        assertEquals( tmpMeasureType.getCode(), "TMP");
        
    	MeasureType measureType2 = new MeasureType( "TST", "Test", false, false, 0, 200, GraphTypes.GAUGE, DbSync.ADD  );
        tmpMeasureType = dataService.updateMeasureType( measureType2 );
        assertEquals( tmpMeasureType.getCode(), "TST");
	}	
	
	@Test
	void deleteMeasureType() throws Exception
	{
        Mockito.when(measureTypeRepository.getOne( "TMP" )).thenReturn( measureType );

        dataService.deleteMeasureType( "TMP" );
        verify( measureTypeRepository, times(1)).getOne( "TMP" );
	}	
	
	@Test
	void deleteMeasureTypeEx() throws Exception
	{
        Mockito.when(measureTypeRepository.getOne( null )).thenThrow( new EntityNotFoundException("Test") );

        dataService.deleteMeasureType( null );
        verify( measureTypeRepository, times(1)).getOne( null );
	}	
	
	@Test
	void getMeasureTypeSensorCount() throws Exception
	{
        Mockito.when(sensorRepository.measureTypeCount("TMP" )).thenReturn( 0L );
        
        Long count = dataService.getMeasureTypeSensorCount( "TMP" );
        assertEquals( count.longValue(), 0L );
	}	

	
	@Test
	void getMeasureTypeMeasurementCount() throws Exception
	{
        Mockito.when(measurementRepository.measureTypeCount("TMP" )).thenReturn( 0L );
        
        Long count = dataService.getMeasureTypeMeasurementCount( "TMP" );
        assertEquals( count.longValue(), 0L );
	}	

	//
	//
	//	Batch table test methods	
	//
	@Test
	void getBatch() throws Exception
	{
		testBatch.setId( 1L );
        Mockito.when(batchRepository.getOne( 1L )).thenReturn( testBatch );
        
        Batch batch = dataService.getBatch( 1L );
        assertEquals( batch.getName(), "Joe's IPA");
	}	

	@Test
	void getBatchToken() throws Exception
	{
		testBatch.setId( 1L );
        Mockito.when(batchRepository.findBatchBySynchToken( "TestToken" )).thenReturn( testBatch );
        
        Batch batch = dataService.getBatch( "TestToken" );
        assertEquals( batch.getName(), "Joe's IPA");
	}	
	
	
	@Test
	void getAllBatches() throws Exception
	{
    	List<Batch> batches = new ArrayList<Batch>();
    	batches.add( testBatch );
		
		Mockito.when(batchRepository.findAll( )).thenReturn( batches );
        
		List<Batch> batchesTest = dataService.getAllBatches( );
		assertEquals(  batchesTest.size(), 1 );
	}	
	
	@Test
	void getActiveBatches() throws Exception
	{
    	List<Batch> batches = new ArrayList<Batch>();
    	batches.add( testBatch );
		
		Mockito.when(batchRepository.findActiveBatches( )).thenReturn( batches );
        
		List<Batch> batchesTest = dataService.getActiveBatches( );
		assertEquals(  batchesTest.size(), 1 );
	}	

	@Test
	void getBatchesToSynchronize() throws Exception
	{
    	List<Batch> batches = new ArrayList<Batch>();
    	batches.add( testBatch );
		
		Mockito.when(batchRepository.findBatchesToSynchronize( )).thenReturn( batches );
        
		List<Batch> batchesTest = dataService.getBatchesToSynchronize( );
		assertEquals(  batchesTest.size(), 1 );
	}	

	@Test
	void saveBatch() throws Exception
	{
        Mockito.when(batchRepository.save( testBatch )).thenReturn( testBatch );
        
        Batch batch = dataService.saveBatch( testBatch );
        assertEquals( batch.getName(), "Joe's IPA");
        
    	Batch batch2 = new Batch( false, "Test IPA", "Old School IPA", null, testDomain, new Date(), DbSync.ADD, "" );
        Mockito.when(batchRepository.save( batch2 )).thenReturn( batch2 );
        batch = dataService.saveBatch( batch2 );
        assertEquals( batch.getName(), "Test IPA");
	}	

	@Test
	void saveBatchEx() throws Exception
	{
    	Batch batch2 = new Batch( false, null, null, null, testDomain, new Date(), DbSync.ADD, "" );
        Mockito.when(batchRepository.save( batch2 )).thenThrow( new IllegalArgumentException("Test") );
        Batch batch = dataService.saveBatch( batch2 );
        assertNull( batch.getName() );
	}	

	@Test
	void updateBatch() throws Exception
	{
		testBatch.setId( 1L );
        Mockito.when(batchRepository.getOne( 1L )).thenReturn( testBatch );
        Mockito.when(batchRepository.save( testBatch )).thenReturn( testBatch );
        
        Batch batch = dataService.updateBatch( testBatch );
        assertEquals( batch.getName(), "Joe's IPA");

    	Batch batch2 = new Batch( false, "Test IPA3", "Old School IPA", null, testDomain, new Date(), DbSync.ADD, "" );
    	batch2.setId(2L);
        Mockito.when(batchRepository.getOne( 2L )).thenReturn( batch2 );
        Mockito.when(batchRepository.save( batch2 )).thenReturn( batch2 );
        batch = dataService.updateBatch( batch2 );
        assertEquals( batch.getName(), "Test IPA3");

    	batch2.setId( 22L );
        batch = dataService.updateBatch( batch2 );
        assertEquals( batch.getName(), "Test IPA3");
	}	
	
	@Test
	void deleteBatch() throws Exception
	{
		testBatch.setId( 1L );
        Mockito.when(batchRepository.getOne( 1L )).thenReturn( testBatch );

        dataService.deleteBatch( 1L );
        verify( batchRepository, times(1)).getOne( 1L );
	}	
	
	@Test
	void deleteBatchEx() throws Exception
	{
        Mockito.when(batchRepository.getOne( -1L )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteBatch( -1L );
        verify( batchRepository, times(1)).getOne( -1L );
	}	
	
	@Test
	void getBatchSensorCount() throws Exception
	{
        Mockito.when(sensorRepository.batchCount( 1L )).thenReturn( 0L );
        
        Long count = dataService.getBatchSensorCount( 1L );
        assertEquals( count.longValue(), 0L );
	}	

	
	//
	//	Measurement table test methods
	//
	//
	@Test
	void getMeasurement() throws Exception
	{
        Mockito.when(measurementRepository.getOne( 1L )).thenReturn( measurement );
        
        Measurement tmpMeasurement = dataService.getMeasurement( 1L );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":70.0}" );
	}	
	
	@Test
	void getMeasurementToken() throws Exception
	{
        Mockito.when(measurementRepository.findMeasurementBySynchToken( "TestToken" )).thenReturn( measurement );
        
        Measurement tmpMeasurement = dataService.getMeasurement( "TestToken" );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":70.0}" );
	}	

	@Test
	void getRecentMeasurement() throws Exception
	{
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add( measurement );
		
		Mockito.when(measurementRepository.findMostRecent( 1L )).thenReturn( measurements );
        
		List<Measurement> tmpMeasurements = dataService.getRecentMeasurement( 1L );
		assertEquals(  tmpMeasurements.size(), 1 );
	}	
	
	@Test
	void getMeasurementsByBatch() throws Exception
	{
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add( measurement );
		
		Mockito.when(measurementRepository.findByBatchId( 1L )).thenReturn( measurements );
        
		List<Measurement> tmpMeasurements = dataService.getMeasurementsByBatch( 1L );
		assertEquals(  tmpMeasurements.size(), 1 );
	}	
	
	@Test
	void getMeasurementsByBatchType() throws Exception
	{
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add( measurement );
		
		Mockito.when(measurementRepository.findByBatchIdType( 1L, "TMP" )).thenReturn( measurements );
        
		List<Measurement> tmpMeasurements = dataService.getMeasurementsByBatchType( 1L, "TMP" );
		assertEquals(  tmpMeasurements.size(), 1 );
	}	
	
	@Test
	void getMeasurementsPageByBatch() throws Exception
	{
		List<Measurement> measurementData = new ArrayList<>();
    	measurementData.add(measurement);		
		
       	int noOfRecords = 10;
    	PageRequest pageRequest = PageRequest.of( 0, noOfRecords );
        Pageable pageable = pageRequest;    	
        Page<Measurement> page = new PageImpl<>(measurementData);		
		
		Mockito.when(measurementRepository.findPageByBatchId( 1L, pageable )).thenReturn( page );
        
		Page<Measurement> tmpPage = dataService.getMeasurementsPageByBatch( 0, 1L );
		assertEquals(  tmpPage.getTotalPages(), 1 );
	}	
	
	@Test
	void getMeasurementsToSynchronize() throws Exception
	{
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add( measurement );
		
		Mockito.when(measurementRepository.findMeasurementsToSynchronize( )).thenReturn( measurements );
        
		List<Measurement> measurementTest = dataService.getMeasurementsToSynchronize( );
		assertEquals(  measurementTest.size(), 1 );
	}	

	@Test
	void saveMeasurement() throws Exception
	{
		measurement.setDbSynchToken( null );
        Mockito.when(measurementRepository.save( measurement )).thenReturn( measurement );
        
        Measurement tmpMeasurement = dataService.saveMeasurement( measurement );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":70.0}" );

		measurement.setDbSynchToken( "" );
        tmpMeasurement = dataService.saveMeasurement( measurement );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":70.0}" );
        
        measurement.setDbSynchToken( "TestToken" );
        tmpMeasurement = dataService.saveMeasurement( measurement );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":70.0}" );
	}	

	@Test
	void saveMeasurementEx() throws Exception
	{
        Mockito.when(measurementRepository.save( null )).thenThrow( new IllegalArgumentException("Test") );
        Measurement tmpMeasurement = dataService.saveMeasurement( null );
        assertNull( tmpMeasurement.getValueText() );
	}	

	@Test
	void updateMeasurement() throws Exception
	{
		measurement.setId( 1L );
        Mockito.when(measurementRepository.getOne( 1L )).thenReturn( measurement );
        Mockito.when(measurementRepository.save( measurement )).thenReturn( measurement );
        
        Measurement tmpMeasurement = dataService.updateMeasurement( measurement );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":70.0}" );
        //
        //	Null Batch Path
        //
    	Measurement measurement2 = new Measurement( 70.3, "{\"target\":77.0}", null, process, measureType, new Date(), DbSync.ADD, "" );
		measurement2.setId( 2L );
        Mockito.when(measurementRepository.getOne( 2L )).thenReturn( measurement2 );
        Mockito.when(measurementRepository.save( measurement2 )).thenReturn( measurement2 );
        tmpMeasurement = dataService.updateMeasurement( measurement2 );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":77.0}" );
    	//
        //	Data not found should generate exception
        //
		measurement2.setId( 3L );
        tmpMeasurement = dataService.updateMeasurement( measurement2 );
        assertEquals( tmpMeasurement.getValueText(), "{\"target\":77.0}" );
	}	
	
	@Test
	void deleteMeasurement() throws Exception
	{
		testCategory.setId( 1L );

		Mockito.when(measurementRepository.getOne( 1L )).thenReturn( measurement );
        dataService.deleteMeasurement( 1L );
        verify( measurementRepository, times(1)).getOne( 1L );
	}	
	
	@Test
	void deleteMeasurementEx() throws Exception
	{
		Mockito.when(measurementRepository.getOne( -1L )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteMeasurement( -1L );
        verify( measurementRepository, times(1)).getOne( -1L );
	}	

	@Test
	void deleteDuplicateMeasurements() throws Exception
	{
		
		Measurement measurement2 = new Measurement( 72.3, "{\"target\":72.0}", testBatch, process, measureType, new Date() );
		Measurement measurement3 = new Measurement( 72.3, "{\"target\":74.0}", testBatch, process, measureType, new Date() );
		
		List<Measurement> measurementData = new ArrayList<>();
    	measurementData.add(measurement);		
    	measurementData.add(measurement);		
    	measurementData.add(measurement2);		
    	measurementData.add(measurement3);		
    	measurement3 = new Measurement( 72.3, "{\"target\":74.0}", testBatch, process, measureType, new Date(), DbSync.SYNCHED, "TestToken" );
    	measurementData.add(measurement3);		
    	measurement3 = new Measurement( 72.3, "{\"target\":74.0}", testBatch, process, measureType, new Date(), DbSync.SYNCHED, "" );
    	measurementData.add(measurement3);		
		
		Mockito.when(measurementRepository.findByBatchId( 1L )).thenReturn( measurementData );
        dataService.deleteDuplicateMeasurements( 1L );
        verify( measurementRepository, times(1)).findByBatchId( 1L );
	}	
	
	
	//
	//	Sensor table test methods
	//
	@Test
	void getSensor() throws Exception
	{
		sensor.setId( 1L );
        Mockito.when(sensorRepository.getOne( 1L )).thenReturn( sensor );
        
        Sensor tmpSensor = dataService.getSensor( 1L );
        assertEquals( tmpSensor.getName(), "test");
	}	
	
	@Test
	void getSensorToken() throws Exception
	{
		sensor.setId( 1L );
        Mockito.when(sensorRepository.findSensorBySynchToken( "TestToken" )).thenReturn( sensor );
        
        Sensor tmpSensor = dataService.getSensor( "TestToken" );
        assertEquals( tmpSensor.getName(), "test");
	}	
	
	@Test
	void getAllSensors() throws Exception
	{
    	List<Sensor> sensors = new ArrayList<Sensor>();
    	sensors.add( sensor );
		
		Mockito.when(sensorRepository.findAll( )).thenReturn( sensors );
        
		List<Sensor> sensorsTest = dataService.getAllSensors( );
		assertEquals(  sensorsTest.size(), 1 );
	}	
	
	@Test
	void getEnabledSensors() throws Exception
	{
    	List<Sensor> sensors = new ArrayList<Sensor>();
    	sensors.add( sensor );
		
		Mockito.when(sensorRepository.findEnabledSensors( "Bluetooth" )).thenReturn( sensors );
		Mockito.when(sensorRepository.findEnabledSensors( "WiFi" )).thenReturn( sensors );
        
		List<Sensor> sensorsTest = dataService.getEnabledSensors( SensorType.BLUETOOTH );
		assertEquals(  sensorsTest.size(), 1 );

		sensorsTest = dataService.getEnabledSensors( SensorType.WIFI );
		assertEquals(  sensorsTest.size(), 1 );
	}	
	
	@Test
	void getSensorsToSynchronize() throws Exception
	{
    	List<Sensor> sensors = new ArrayList<Sensor>();
    	sensors.add( sensor );
		
		Mockito.when(sensorRepository.findSensorsToSynchronize( )).thenReturn( sensors );
        
		List<Sensor> sensorsTest = dataService.getSensorsToSynchronize( );
		assertEquals(  sensorsTest.size(), 1 );
	}	

	@Test
	void saveSensor() throws Exception
	{
		sensor.setId( 1L );
		sensor.setName( "test" );
		sensor.setBatch( testBatch );
        Mockito.when(sensorRepository.save( sensor )).thenReturn( sensor );
        
        Sensor tmpSensor = dataService.saveSensor( sensor );
        assertEquals( tmpSensor.getName(), "test");
        
        Sensor sensor2 = new Sensor( 2L, false, "test2", "", "", "1234", "BLUETOOTH", "", null, process, measureType, new Date() );
        Mockito.when(sensorRepository.save( sensor2 )).thenReturn( sensor2 );
        tmpSensor = dataService.saveSensor( sensor2 );
        assertEquals( tmpSensor.getName(), "test2");
	}	

	@Test
	void saveSensorEx() throws Exception
	{
        Sensor sensor2 = new Sensor( 2L, false, null, null, null, null, null, null, null, null, null, new Date() );
        Mockito.when(sensorRepository.save( sensor2 )).thenThrow( new IllegalArgumentException("Test") );
        Sensor tmpSensor = dataService.saveSensor( sensor2 );
        assertNull( tmpSensor.getName() );
	}	
	
	@Test
	void updateSensor() throws Exception
	{
		sensor.setId( 1L );
		sensor.setName( "test" );
        Mockito.when(sensorRepository.getOne( 1L )).thenReturn( sensor );
        Mockito.when(sensorRepository.save( sensor )).thenReturn( sensor );
        
        Sensor tmpSensor = dataService.updateSensor( sensor );
        assertEquals( tmpSensor.getName(), "test");

		sensor.setBatch( testBatch );
        tmpSensor = dataService.updateSensor( sensor );
        assertEquals( tmpSensor.getName(), "test");

        Sensor sensor2 = new Sensor( 2L, false, "test2", "", "", "1234", "BLUETOOTH", "", null, process, measureType, new Date(), DbSync.ADD, "" );
        Mockito.when(sensorRepository.save( sensor2 )).thenReturn( sensor2 );
        tmpSensor = dataService.updateSensor( sensor2 );
        assertEquals( tmpSensor.getName(), "test2");
	}	
	
	@Test
	void deleteSensor() throws Exception
	{
		sensor.setId( 1L );

		Mockito.when(sensorRepository.getOne( 1L )).thenReturn( sensor );
        dataService.deleteSensor( 1L );
        verify( sensorRepository, times(1)).getOne( 1L );
	}	
	
	@Test
	void deleteSensorEx() throws Exception
	{
		Mockito.when(sensorRepository.getOne( -1L )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteSensor( -1L );
        verify( sensorRepository, times(1)).getOne( -1L );
	}	

	//
	//	User table test methods
	//
	@Test
	void loadUserByUsername() throws Exception
	{
		user.setValidated( true );
        Mockito.when(userRepository.findByUsername( "ADMIN" )).thenReturn( user );
        
        UserDetails userDetails = dataService.loadUserByUsername( "ADMIN" );
        assertEquals( userDetails.getUsername(), "ADMIN");
	}	
	
	@Test( )
	void loadUserByUsernameEx() throws Exception
	{
        Mockito.when(userRepository.findByUsername( null )).thenReturn( null );
        assertThrows(
        		UsernameNotFoundException.class, 
    			() -> dataService.loadUserByUsername( null ) );        
	}	

	@Test
	void getUser() throws Exception
	{
        Mockito.when(userRepository.getOne( 1L )).thenReturn( user );
        
        User tmpUser = dataService.getUser( 1L );
        assertEquals( tmpUser.getUsername(), "ADMIN");
	}	
	
	@Test
	void getUserByName() throws Exception
	{
        Mockito.when(userRepository.findByUsername( "ADMIN" )).thenReturn( user );
        
        User tmpUser = dataService.getUserByName( "ADMIN" );
        assertEquals( tmpUser.getUsername(), tmpUser.getUsername(), "ADMIN");
	}	

	@Test
	void getAllUsers() throws Exception
	{
    	List<User> users = new ArrayList<User>();
    	users.add( user );
		
		Mockito.when(userRepository.findAll( )).thenReturn( users );
        
		List<User> usersTest = dataService.getAllUsers( );
		assertEquals(  usersTest.size(), 1 );
	}	
	
	@Test
	void getUsersToSynchronize() throws Exception
	{
    	List<User> users = new ArrayList<User>();
    	users.add( user );
		
		Mockito.when(userRepository.findUsersToSynchronize( )).thenReturn( users );
        
		List<User> usersTest = dataService.getUsersToSynchronize( );
		assertEquals(  usersTest.size(), 1 );
	}	

	@Test
	void saveUser() throws Exception
	{
        Mockito.when(userRepository.save( user )).thenReturn( user );
        
        User userTmp = dataService.saveUser( user );
        assertEquals( userTmp.getUsername(), "ADMIN" );
	}	

	@Test
	void saveUserEx() throws Exception
	{
        Mockito.when(userRepository.save( null )).thenThrow( new IllegalArgumentException("Test") );
        User userTmp = dataService.saveUser( null );
        assertNull( userTmp.getUsername() );
	}	

	@Test
	void updateUser() throws Exception
	{
		user.setId( 1L );
        Mockito.when(userRepository.getOne( 1L )).thenReturn( user );
        Mockito.when(userRepository.save( user )).thenReturn( user );
        
        User userTmp = dataService.updateUser( user );
        assertEquals( userTmp.getUsername(), "ADMIN");

    	User user2 = new User( "test2", "admin", UserRoles.ADMIN.toString() );
        user2.setId( 2L );
        userTmp = dataService.updateUser( user2 );
        assertEquals( userTmp.getUsername(), "test2" );
	}	
	
	@Test
	void deleteUser() throws Exception
	{
		user.setId( 1L );
        Mockito.when(userRepository.getOne( 1L )).thenReturn( user );
        dataService.deleteUser( 1L );
        verify( userRepository, times(1)).getOne( 1L );
	}	

	@Test
	void deleteUserEx() throws Exception
	{
        Mockito.when(userRepository.getOne( -1L )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteUser( -1L );
        verify( userRepository, times(1)).getOne( -1L );
	}	
	
	//
	//	VerificationToken Table test methods
	//
	@Test
	void getVerificationToken() throws Exception
	{
		VerificationToken verificationToken= new VerificationToken();
		verificationToken.setToken( "test123" );
		Mockito.when(verificationTokenRepository.findById( "test123" )).thenReturn( Optional.of(verificationToken) );
        
		VerificationToken tmpVerificationToken = dataService.getVerificationToken( "test123" );
        assertEquals( "test123", tmpVerificationToken.getToken() );
	}	
	
	@Test
	void saveVerificationToken() throws Exception
	{
		VerificationToken verificationToken= new VerificationToken();
		verificationToken.setToken( "test123" );
		Mockito.when(verificationTokenRepository.save( verificationToken )).thenReturn( verificationToken );
        
		VerificationToken verificationTokenTmp = dataService.saveVerificationToken( verificationToken );
        assertEquals( verificationTokenTmp.getToken(), "test123" );
        
        Mockito.when(verificationTokenRepository.save( null )).thenThrow( new IllegalArgumentException("Test") );
        verificationTokenTmp = dataService.saveVerificationToken( null );
        assertNull( verificationTokenTmp.getToken() );
	}	

	@Test
	void updateVerificationToken() throws Exception
	{
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken( "test123" );
        Mockito.when( verificationTokenRepository.getOne( "test123" )).thenReturn( verificationToken );
        Mockito.when(verificationTokenRepository.save( verificationToken )).thenReturn( verificationToken );
        
        VerificationToken verificationTokenTmp = dataService.updateVerificationToken( verificationToken );
        assertEquals( verificationTokenTmp.getToken(), "test123");

		VerificationToken verificationToken2 = new VerificationToken();
		verificationToken2.setToken( "test456" );
         
		verificationTokenTmp = dataService.updateVerificationToken( verificationToken2 );
        assertEquals( verificationTokenTmp.getToken(), "test456" );
	}	
	
	@Test
	void deleteVerificationToken() throws Exception
	{
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken( "test123" );
        Mockito.when( verificationTokenRepository.getOne( "test123" )).thenReturn( verificationToken );
        dataService.deleteVerificationToken( "test123" );
        verify( verificationTokenRepository, times(1)).getOne( "test123" );
        
        Mockito.when( verificationTokenRepository.getOne( "tes456" )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteVerificationToken( "tes456" );
        verify( verificationTokenRepository, times(1)).getOne( "tes456" );
	}	

	//
	//	ResetToken Table test methods
	//
	@Test
	void getResetToken() throws Exception
	{
		ResetToken resetToken= new ResetToken();
		resetToken.setToken( "test123" );
		Mockito.when(resetTokenRepository.findById( "test123" )).thenReturn( Optional.of(resetToken) );
        
		ResetToken tmpResetToken = dataService.getResetToken( "test123" );
        assertEquals( "test123", tmpResetToken.getToken() );
	}	
	
	@Test
	void saveResetToken() throws Exception
	{
		ResetToken resetToken= new ResetToken();
		resetToken.setToken( "test123" );
		Mockito.when(resetTokenRepository.save( resetToken )).thenReturn( resetToken );
        
		ResetToken resetTokenTmp = dataService.saveResetToken( resetToken );
        assertEquals( resetTokenTmp.getToken(), "test123" );
        
        Mockito.when( resetTokenRepository.save( null )).thenThrow( new IllegalArgumentException("Test") );
        resetTokenTmp = dataService.saveResetToken( null );
        assertNull( resetTokenTmp.getToken() );
	}	

	@Test
	void updateResetToken() throws Exception
	{
		ResetToken resetToken = new ResetToken();
		resetToken.setToken( "test123" );
        Mockito.when( resetTokenRepository.getOne( "test123" )).thenReturn( resetToken );
        Mockito.when(resetTokenRepository.save( resetToken )).thenReturn( resetToken );
         
        ResetToken resetTokenTmp = dataService.updateResetToken( resetToken );
        assertEquals( resetTokenTmp.getToken(), "test123");

        ResetToken resetToken2 = new ResetToken();
        resetToken2.setToken( "test456" );
         
        resetTokenTmp = dataService.updateResetToken( resetToken2 );
        assertEquals( resetTokenTmp.getToken(), "test456" );
	}	
	
	@Test
	void deleteResetToken() throws Exception
	{
		ResetToken resetToken = new ResetToken();
		resetToken.setToken( "test123" );
        Mockito.when( resetTokenRepository.getOne( "test123" )).thenReturn( resetToken );
        dataService.deleteResetToken( "test123" );
        verify( resetTokenRepository, times(1)).getOne( "test123" );
        
        Mockito.when( resetTokenRepository.getOne( "tes456" )).thenThrow( new EntityNotFoundException("Test") );
        dataService.deleteResetToken( "tes456" );
        verify( resetTokenRepository, times(1)).getOne( "tes456" );
	}	

	
	
}
