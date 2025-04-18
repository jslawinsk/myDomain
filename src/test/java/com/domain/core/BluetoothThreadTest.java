package com.domain.core;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.io.StreamConnection;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import com.domain.model.Message;
import com.domain.model.Sensor;
import com.domain.model.SensorType;
import com.domain.service.DataService;
import com.domain.util.BluetoothUtil;

@SpringBootTest( properties = { "blueTooth.enabled=false", 
		"blueTooth.scanSeconds=0", "wiFi.enabled=false"
} )
@AutoConfigureMockMvc
class BluetoothThreadTest {

	@MockBean
	DataService dataService;
	
	@MockBean
    BluetoothUtil bluetoothUtil;
	
	@MockBean
	JavaMailSender mailSender;

	@Autowired
	BluetoothThread bluetoothThread;
	
	@Test
	public void run() throws Exception
	{
		Sensor sensor = new Sensor();
		sensor.setName( "TEST" );
		sensor.setUrl( "testurl" );
    	List<Sensor> sensors = new ArrayList<Sensor>();
    	sensors.add( sensor );
		Mockito.when( dataService.getEnabledSensors( SensorType.BLUETOOTH ) ).thenReturn( sensors );
		
		Message message = new Message( sensor.getName(), "COMMAND:CONTROL:COOL_ON" );
//		message.setTarget( sensor.getName() );
//		message.setData( "COMMAND:CONTROL:COOL_ON" );
		BluetoothThread.sendMessage( message );		
		
		BufferedReader bufferedReader = Mockito.mock( BufferedReader.class );
		StreamConnection streamConnection = Mockito.mock( StreamConnection.class );
		OutputStream outStream = Mockito.mock( OutputStream.class );
		InputStream inStream = Mockito.mock( InputStream.class );

		Mockito.when( bluetoothUtil.getStreamConnection( Mockito.any( String.class ) ) ).thenReturn( streamConnection );
		Mockito.when( bluetoothUtil.getBufferedReader( Mockito.any( InputStream.class ) ) ).thenReturn( bufferedReader );
		Mockito.when( streamConnection.openInputStream( ) ).thenReturn( inStream );
		Mockito.when( streamConnection.openOutputStream( ) ).thenReturn( outStream );
		Mockito.when( bufferedReader.readLine( ) ).thenReturn( "{\"temperature\":68.0, \"target\":70.0}" );
		
		bluetoothThread.run();
		
		verify( dataService, atLeast(1)).getEnabledSensors( SensorType.BLUETOOTH );		
	}

	@Test
	public void runExceptions() throws Exception
	{
		Sensor sensor = new Sensor();
		sensor.setName( "TEST" );
		sensor.setUrl( "testurl" );
    	List<Sensor> sensors = new ArrayList<Sensor>();
    	sensors.add( sensor );
		Mockito.when( dataService.getEnabledSensors( SensorType.BLUETOOTH ) ).thenReturn( sensors );
		
		BufferedReader bufferedReader = Mockito.mock( BufferedReader.class );
		StreamConnection streamConnection = Mockito.mock( StreamConnection.class );
		OutputStream outStream = Mockito.mock( OutputStream.class );
		InputStream inStream = Mockito.mock( InputStream.class );

		Mockito.when( bluetoothUtil.getStreamConnection( Mockito.any( String.class ) ) ).thenReturn( streamConnection );
		Mockito.when( bluetoothUtil.getBufferedReader( Mockito.any( InputStream.class ) ) ).thenReturn( bufferedReader );
		Mockito.when( streamConnection.openInputStream( ) ).thenReturn( inStream );
		Mockito.when( streamConnection.openOutputStream( ) ).thenReturn( outStream );

		Mockito.when( bufferedReader.readLine( ) ).thenThrow( new IOException( "test" ) );
		Mockito.doThrow( new IOException( "test") ).when( inStream ).close();
		bluetoothThread.run();
		verify( dataService, atLeast(1)).getEnabledSensors( SensorType.BLUETOOTH );		

	}
	
	
}
