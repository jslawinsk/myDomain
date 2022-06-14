package com.domain;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.domain.actuator.BluetoothStatus;
import com.domain.actuator.DataSynchStatus;
import com.domain.actuator.WiFiStatus;
import com.domain.core.BluetoothThread;
import com.domain.core.DataSynchThread;
import com.domain.core.WiFiThread;
import com.domain.model.Batch;
import com.domain.model.DbSync;
import com.domain.model.Domain;
import com.domain.model.GraphTypes;
import com.domain.model.MeasureType;
import com.domain.model.Measurement;
import com.domain.model.Process;
import com.domain.model.Category;
import com.domain.model.User;
import com.domain.model.UserRoles;
import com.domain.repository.BatchRepository;
import com.domain.repository.MeasureTypeRepository;
import com.domain.repository.MeasurementRepository;
import com.domain.repository.ProcessRepository;
import com.domain.repository.CategoryRepository;
import com.domain.repository.UserRepository;
import com.domain.service.DataService;

@SpringBootApplication
public class Application implements CommandLineRunner {

    static private Logger LOG = LoggerFactory.getLogger( Application.class );

    @Autowired
    private DataService dataService;
		    	
    @Value("${blueTooth.enabled}")
    private boolean blueToothEnabled;

    @Value("${wiFi.enabled}")
    private boolean wiFiEnabled;
    
    @Value("${dataSynch.enabled}")
    private boolean dataSynchEnabled;
    
    @Value("${testdata.create}")
    private boolean createTestData;

    @Value("${testdata.createAdmin}")
    private boolean createTestAdmin;
    
    @Autowired
    private TaskExecutor taskExecutor;
    
    @Autowired
    private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @Autowired
    private BluetoothStatus bluetoothStatus;

    @Autowired
    private WiFiStatus wiFiStatus;

    @Autowired
    private DataSynchStatus dataSynchStatus;
    
    @Autowired
    private JavaMailSender mailSender;

    @Override
	public void run(String... strings) throws Exception {
		
		// Populate test database
		//
		if( createTestData ) {
			Domain domain = new Domain( 0L, 0, "Brewery", "sports_bar_white_36dp.svg", "Home Brewery", "Style", "Brewery", DbSync.ADD, null);
			dataService.saveDomain(domain);

			domain = new Domain( 0L, 1, "Greenhouse", "yard_white_36dp.svg", "Backyard Greenhouse", "Cataegory", "Greenhouse", DbSync.ADD, null);
			dataService.saveDomain(domain);
			
			Category testCategory = new Category( "IPA", "18a", "Hoppy" );
			dataService.saveCategory( testCategory );
	
			Category testCategory2 = new Category( "Stout", "21", "Malty" );
			dataService.saveCategory( testCategory2 );
			
			Process process = new Process( "FRM", "Fermentation" );
			dataService.saveProcess( process );
			
			process = new Process( "MSH", "Mash" );
			dataService.saveProcess( process );
			
			MeasureType measureType = new MeasureType( "PH", "PH", true, 0, 14, GraphTypes.SOLID_GUAGE, DbSync.ADD );
			dataService.saveMeasureType( measureType );
			
			measureType = new MeasureType( "TA", "Total Alcalinity" );
			dataService.saveMeasureType( measureType );
			
			measureType = new MeasureType( "TMP", "Temperature", true, 0, 200, GraphTypes.GAUGE, DbSync.ADD  );
			dataService.saveMeasureType( measureType );
	
			Batch testBatch = new Batch( true, "Joe's IPA", "Old School IPA", testCategory, new Date() );
			dataService.saveBatch( testBatch );
			
			Measurement measurement = new Measurement( 70.3, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
			dataService.saveMeasurement( measurement );
			
			measurement = new Measurement( 70.5, "{\"target\":70.0}", testBatch, process, measureType, new Date() );
			dataService.saveMeasurement( measurement );
	
			Batch testBatch2 = new Batch( false, "Joe's Stout", "Old School Stout", testCategory2, new Date() );
			dataService.saveBatch( testBatch2 );
			
			measurement = new Measurement( 60.5, "{\"target\":70.0}", testBatch2, process, measureType, new Date() );
			dataService.saveMeasurement( measurement );
			
			User user = new User( "ADMIN", "admin@domainservices.com", new BCryptPasswordEncoder().encode( "admin" ), DbSync.ADD, UserRoles.ADMIN.toString(), true );
			dataService.saveUser( user );
		}
		if ( createTestAdmin ) {
			User user = new User( "ADMIN", "admin@domainservices.com", new BCryptPasswordEncoder().encode( "admin" ), DbSync.ADD, UserRoles.ADMIN.toString(), true );
			dataService.saveUser( user );
		}

		bluetoothStatus.setUp(true);	
		if( blueToothEnabled ) {
			LOG.info("Bluetooth Initializing" );
			BluetoothThread btThread = applicationContext.getBean( BluetoothThread.class );
			bluetoothStatus.setMessage( "Initializing" );
			taskExecutor.execute( btThread );
		}
		else {
			LOG.info("Bluetooth Not Enabled" );
			bluetoothStatus.setMessage( "Not Enabled" );
		}

		wiFiStatus.setUp(true);	
		if( wiFiEnabled ) {
			LOG.info("WiFi Initializing" );
			WiFiThread wfThread = applicationContext.getBean( WiFiThread.class );
			wiFiStatus.setMessage( "Initializing" );
			taskExecutor.execute( wfThread );
		}
		else {
			LOG.info("WiFi Not Enabled" );
			wiFiStatus.setMessage( "Not Enabled" );
		}
		
		dataSynchStatus.setUp( true );
		if( dataSynchEnabled ) {
			LOG.info("Data Synch Enabled" );
			DataSynchThread dbSyncThread = applicationContext.getBean( DataSynchThread.class );
			dataSynchStatus.setMessage( "Enabled" );
			taskExecutor.execute( dbSyncThread );
		}
		else {
			LOG.info("Data Synch Not Enabled" );
			dataSynchStatus.setMessage( "Not Enabled" );
		}
		
	}
}
