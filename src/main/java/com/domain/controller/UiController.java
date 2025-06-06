package com.domain.controller;

import com.domain.core.BluetoothThread;
import com.domain.dto.ChartAttributes;
import com.domain.model.Batch;
import com.domain.model.DbSync;
import com.domain.model.Domain;
import com.domain.model.DomainCategory;
import com.domain.model.DomainMeasureType;
import com.domain.model.DomainProcess;
import com.domain.model.Info;
import com.domain.model.MeasureType;
import com.domain.model.Measurement;
import com.domain.model.Message;
import com.domain.model.Password;
import com.domain.model.Process;
import com.domain.model.ProfilePassword;
import com.domain.model.ResetToken;
import com.domain.model.Sensor;
import com.domain.model.Category;
import com.domain.model.User;
import com.domain.service.BlueToothService;
import com.domain.service.DataService;
import com.domain.service.UserService;
import com.domain.service.WiFiService;
import com.domain.util.OnCreateUserEvent;
import com.domain.util.OnPasswordResetEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@Controller
@RequestMapping( "")
public class UiController {

    private Logger LOG = LoggerFactory.getLogger( UiController.class );
	
    private DataService dataService;
    @Autowired
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    private BlueToothService blueToothService;
    @Autowired
    public void setBlueToothService(BlueToothService blueToothService) {
        this.blueToothService = blueToothService;
    }

    private WiFiService wifiService;
    @Autowired
    public void setWiFiService(WiFiService wifiService) {
        this.wifiService = wifiService;
    }
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Value("${blueTooth.enabled}")
    private boolean blueToothEnabled;
	
    @Value("${wiFi.enabled}")
    private boolean wiFiEnabled;
    
    @Value("${dataSynch.enabled}")
    private boolean dataSynchEnabled;
    
    @RequestMapping(path = { "/", "/{domainId}" } )
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String index( Model model, @PathVariable(value = "domainId") Optional<Long> domainId ) {

    	Long selectedDomain = 0L;
    	if( domainId.isPresent() ) {
    		selectedDomain = domainId.get();
    	}
    	List<Measurement> measurements = new ArrayList<Measurement>();

    	List<Batch> batches;
    	if( selectedDomain == 0 ) {
    		batches = dataService.getActiveBatches();
    	}
    	else {
    		batches = dataService.getActiveBatches( selectedDomain );
    	}
    	for( Batch batch:batches) {
    		List<Measurement> batchMeasurements = dataService.getRecentMeasurement(  batch.getId() );
    		if( !batchMeasurements.isEmpty() ) {
    			measurements.addAll( batchMeasurements );
    		}
    	}    	
        
        List<ChartAttributes> gauges = new ArrayList<ChartAttributes>();
        ObjectMapper objectMapper = new ObjectMapper();
        for( Measurement measurement:measurements) {
        	if( !measurement.getType().getGraphType().toString().equals( "NONE" ) ){
	        	double target = 60;
	        	ChartAttributes gauge = new ChartAttributes();
	
	        	boolean btarget = false;
	        	String temp = measurement.getValueText();
	        	if( temp.indexOf("target") >= 0 ) {
		        	Map<String, Double> map;
					try {
						map = objectMapper.readValue(temp, Map.class);
						target = (double)map.get( "target" );
						btarget = true;
					} catch (Exception e) {
						LOG.error( "index: Exception", e );
					}
	        	}
	        	
	        	gauge.setTitle( measurement.getBatch().getName() + " " + measurement.getProcess().getName() );
	        	gauge.setValueType( measurement.getType().getName() );
	        	gauge.setValueNumber( measurement.getValueNumber() );
            	gauge.setGaugeType( measurement.getType().getGraphType().toString() );
            	gauge.setMaxValue( measurement.getType().getMaxValue() );
            	gauge.setMinValue( measurement.getType().getMinValue() );
	            LOG.info("UiController: Gauge: Measurement:" + measurement.getType().getGraphType() );   	
	        	if( measurement.getType().getGraphType().toString().equals("SOLID_GUAGE") ) {
	                LOG.info("UiController: Adding Solid Gauge: " );   	
	            	gauge.setStartAngle( -90 );
	            	gauge.setEndAngle( 90 );
	            	gauge.addStop( 0,     "#FC1B2B" );	// Red
	            	gauge.addStop( 0.067, "#FD542B" );	// Pink
	            	gauge.addStop( 0.133, "#FDA529" );	// Orange
	            	gauge.addStop( 0.2,   "#FECE2F" );	// Beige
	            	gauge.addStop( 0.267, "#DBE030" );	// Yellow
	            	gauge.addStop( 0.333, "#72D628" );	// Lime Green
	            	gauge.addStop( 0.4,   "#1CB321" );	// Green
	            	gauge.addStop( 0.467, "#159A19" );	// Dark Green
	            	gauge.addStop( 0.533, "#17A45B" );	// Turquoise
	            	gauge.addStop( 0.6,   "#20BEB5" );	// Pale Blue
	            	gauge.addStop( 0.667, "#1888CE" );	// Blue
	            	gauge.addStop( 0.733, "#0F4FC5" );	// Dark Blue
	            	gauge.addStop( 0.8,   "#342BB7" );	// Violet
	            	gauge.addStop( 0.867, "#342BA5" );	// Purple
	            	gauge.addStop( 0.933, "#4A1590" );	// Deep Purple
	        	}
	        	else if( measurement.getType().getGraphType().toString().equals("GAUGE") ){
	                LOG.info("UiController: Adding Gauge: " );   	
	                if( btarget ) {
		            	gauge.addPlotBand( 0, (long)target-12, "#DF5353" );					// Red
		            	gauge.addPlotBand( (long)target-12, (long)target-6, "#e0790b" );	// Orange
		            	gauge.addPlotBand( (long)target-6, (long)target-3, "#f2f20c" );		// Yellow
		            	gauge.addPlotBand( (long)target-3, (long)target-1, "#92f20c" );		// Light Green
		            	gauge.addPlotBand( (long)target-1, (long)target+2, "#55BF3B" );		// Green
		            	gauge.addPlotBand( (long)target+2, (long)target+4, "#92f20c" );		// Light Green
		            	gauge.addPlotBand( (long)target+4, (long)target+7, "#f2f20c" );		// Yellow
		            	gauge.addPlotBand( (long)target+7, (long)target+13, "#e0790b" );	// Orange
		            	gauge.addPlotBand( (long)target+13, 200, "#DF5353" );				// Red
	                }
	        	}
            	gauges.add( gauge );
        	}
        }
        model.addAttribute("gaugeAttrs", gauges );
    	
        List<Domain> domains = dataService.getAllDomains();
        model.addAttribute("domains", domains );
        model.addAttribute("selectedDomain", selectedDomain );
      
        return "index";
    }

    //
    //	Domain table UI routines
    //
    //
    @RequestMapping(path = "/domain/{domainId}", method = RequestMethod.GET)
    public String getAllDomains( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "domains";
    }
    
    @RequestMapping(path = "/domain/add/{domainId}", method = RequestMethod.GET)
    public String createDomain( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("domain", new Domain());
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "domainAdd";
    }

    @RequestMapping(path = "/domain/{domainId}", method = RequestMethod.POST)
    public String saveDomain( Domain domain, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: Domain Post: " );   	
    	dataService.saveDomain(domain);
        return "redirect:/domain/" + domainId;
    }
    
    @RequestMapping(path = "/domain/edit/{id}/{domainId}", method = RequestMethod.GET)
    public String editDomain( Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	Domain domain = dataService.getDomain(id);
        model.addAttribute("domain", domain );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "domainEdit";
    }
    
    @RequestMapping(path = "/domain/delete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteDomain( RedirectAttributes redirectAttributes, @PathVariable(name = "id" ) Long id, @PathVariable(value = "domainId") Long domainId ) {
        Info info = new Info();
        String message = "";
        
    	Long batchCount = dataService.getDomainBatchCount( id );
    	Long categoryCount = dataService.getDomainCategoryDomainCount( id );
        if( batchCount == 0L && categoryCount == 0L) {
	    	Domain domain = dataService.getDomain(id);
	    	if( dataSynchEnabled && domain.getDbSynchToken() != null && domain.getDbSynchToken().length() > 0 ) {
	    		message = message + "Domain " + id + " scheduled for deletion.";
	    		domain.setDbSynch( DbSync.DELETE );
	        	dataService.updateDomain( domain );
	    	}
	    	else {
	    		message = message + "Domain " + id + " deleted.";
	    		dataService.deleteDomain(id);
	    	}
    	}
    	else {
    		if( batchCount > 0L ) {
    			message = message + "Domain " + id + " has " + batchCount + " batch" + ((batchCount > 1L) ? "es" : "" ) + " configured. ";
    		}
    		if( categoryCount > 0L ) {
    			message = message + "Domain " + id + " has " + categoryCount + " categor" + ((categoryCount > 1L) ? "ies" : "y" ) + " configured. ";
    		}
            message = message + "Associations must be removed before deleting domain.";
    	}
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/domain/" + domainId;
    }
    

    //
    //	Category table UI routines
    //
    //
    @RequestMapping(path = "/category/add/{domainId}", method = RequestMethod.GET)
    public String createCategory( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("category", new Category());
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "categoryAdd";
    }

    @RequestMapping(path = "/category/link/{domainId}", method = RequestMethod.POST)
    public String linkCategory( RedirectAttributes redirectAttributes, @RequestParam("linkCategory") Long categoryId, Model model, @PathVariable(value = "domainId") Long domainId ) {

        Info info = new Info();
        String message = "";
        LOG.info("UiController: Category Link: " + categoryId );   	
        DomainCategory domainCategory = dataService.getDomainCategory(domainId, categoryId);
        if( domainCategory == null ) {
        	Domain domain = dataService.getDomain( domainId );
        	Category category = dataService.getCategory( categoryId );
			domainCategory = new DomainCategory( domain, category, new Date(), DbSync.ADD, null );
			dataService.saveDomainCategory( domainCategory );
        	message = "Category " + category.getName() + " linked to " + domain.getName();
        }
        else {
        	message = "Category already linked";
        }
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/category/" + domainId;
    }
    
    @RequestMapping(path = "/category/unlink/{categoryId}/{domainId}", method = RequestMethod.GET)
    public String unlinkCategory( RedirectAttributes redirectAttributes, Model model, @PathVariable(value = "categoryId") Long categoryId, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: Category unLink: " + categoryId );   	
        Info info = new Info();
        String message = "";
        
    	Long batchCount = dataService.getCategoryBatchCount( categoryId, domainId );
        if( batchCount == 0L ) {
	        DomainCategory domainCategory = dataService.getDomainCategory(domainId, categoryId);
	        if( domainCategory != null ) {
		    	if( dataSynchEnabled && domainCategory.getDbSynchToken() != null && domainCategory.getDbSynchToken().length() > 0 ) {
	        		message = message + "Category link" + categoryId + " scheduled for deletion.";
	        		domainCategory.setDbSynch( DbSync.DELETE );
		        	dataService.updateDomainCategory( domainCategory );
		    	}
		    	else {
	        		message = message + "Category " + categoryId + " unLinked.";
		        	dataService.deleteDomainCategory( domainCategory.getId() );
		    	}
	        }
        }
    	else {
        	message = message + "Category " + categoryId + " has " + batchCount + " batch" + ((batchCount > 1L) ? "es" : "" ) + " configured. ";
            message = message + "Associations must be removed before unlinking category.";
    	}
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/category/" + domainId;
    }
    
    @RequestMapping(path = "/category/{domainId}", method = RequestMethod.POST)
    public String saveCategory( Category category, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: Category Post: " );   	
        Category newCategory = dataService.saveCategory(category);
        if( domainId != 0 ) {
        	Domain domain = dataService.getDomain( domainId );
        	DomainCategory domainCategory = new DomainCategory( domain, newCategory, new Date(), DbSync.ADD, null );
			dataService.saveDomainCategory( domainCategory );
        }
        return "redirect:/category/" + domainId;
    }
    
    @RequestMapping(path = "/category/update/{domainId}", method = RequestMethod.POST)
    public String updateCategory( Category category, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: updateCategory: " + category );
        if( category.getDbSynch() != DbSync.ADD ) {
        	category.setDbSynch( DbSync.UPDATE );
        }
    	dataService.updateCategory( category );
        return "redirect:/category/" + domainId;
    }
    
    @RequestMapping(path = "/category/{domainId}", method = RequestMethod.GET)
    public String getAllCategories( Model model, @PathVariable(value = "domainId") Long domainId ) {
    	if( domainId == 0L ) {
    		model.addAttribute("categories",  dataService.getAllCategories() );
    	}
    	else {
    		model.addAttribute("categories",  dataService.getCategoriesForDomain( domainId ) );
    	}
		model.addAttribute("categoryList",  dataService.getAllCategories() );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "categories";
    }

    @RequestMapping(path = "/category/edit/{id}/{domainId}", method = RequestMethod.GET)
    public String editCategory( Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
		Category category = dataService.getCategory(id);
        model.addAttribute("category", category );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "categoryEdit";
    }

    @RequestMapping(path = "/category/delete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteCategory( RedirectAttributes redirectAttributes, @PathVariable(name = "id" ) Long id, @PathVariable(value = "domainId") Long domainId ) {
        Info info = new Info();
        String message = "";
        
    	Long batchCount = dataService.getCategoryBatchCount( id );
    	Long domainCount = dataService.getDomainCategoryCategoryCount( id );
		Category category = dataService.getCategory(id);
        if( batchCount == 0L && domainCount == 0L ) {
	    	if( dataSynchEnabled && category.getDbSynchToken() != null && category.getDbSynchToken().length() > 0 ) {
        		message = message + "Category " + id + " scheduled for deletion.";
        		category.setDbSynch( DbSync.DELETE );
	        	dataService.updateCategory( category );
	    	}
	    	else {
        		message = message + "Category " + id + " deleted.";
	    		dataService.deleteCategory(id);
	    	}
    	}
    	else {
    		if( batchCount != 0L ) {
    			message = message + "Category " + id + " has " + batchCount + " batch" + ((batchCount > 1L) ? "es" : "" ) + " configured. ";
    		}
    		if( domainCount != 0L ) {
    			message = message + "Category " + id + " has " + domainCount + " domain" + ((domainCount > 1L) ? "s" : "" ) + " configured. ";
    		}
            message = message + "Associations must be removed before deleting category.";
    	}
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/category/" + domainId;
    }
    
    //
    //	Process table UI routines
    //
    //
    @RequestMapping(path = "/process/add/{domainId}", method = RequestMethod.GET)
    public String createProcess( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("process", new Process());
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "processAdd";
    }

    @RequestMapping(path = "/process/link/{domainId}", method = RequestMethod.POST)
    public String linkProcess( RedirectAttributes redirectAttributes, @RequestParam("linkProcess") String processCode, Model model, @PathVariable(value = "domainId") Long domainId ) {

        Info info = new Info();
        String message = "";
        LOG.info("UiController: Process Link: " + processCode );   	
        DomainProcess domainProcess = dataService.getDomainProcess(domainId, processCode);
        if( domainProcess == null ) {
        	Domain domain = dataService.getDomain( domainId );
        	Process process = dataService.getProcess( processCode );
        	domainProcess = new DomainProcess( domain, process, new Date(), DbSync.ADD, null );
			dataService.saveDomainProcess( domainProcess );
        	message = "Process " + process.getName() + " linked to " + domain.getName();
        }
        else {
        	message = "Process already linked";
        }
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/process/" + domainId;
    }
    
    @RequestMapping(path = "/process/unlink/{processCode}/{domainId}", method = RequestMethod.GET)
    public String unlinkProcess( RedirectAttributes redirectAttributes, Model model, @PathVariable(value = "processCode") String processCode, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: Process unLink: " + processCode );   	
        Info info = new Info();
        String message = "";
        
        DomainProcess domainProcess = dataService.getDomainProcess(domainId, processCode);
        if( domainProcess != null ) {
	    	if( dataSynchEnabled && domainProcess.getDbSynchToken() != null && domainProcess.getDbSynchToken().length() > 0 ) {
        		message = message + "Process link" + processCode + " scheduled to be unlinked.";
        		domainProcess.setDbSynch( DbSync.DELETE );
	        	dataService.updateDomainProcess( domainProcess );
	    	}
	    	else {
        		message = message + "Process " + processCode + " unLinked.";
	        	dataService.deleteDomainProcess( domainProcess.getId() );
	    	}
        }
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/process/" + domainId;
    }
    
    @RequestMapping(path = "/process/{domainId}", method = RequestMethod.POST)
    public String saveProcess(Process process, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: saveProcess Process: " + process );   	
        Process newProcess = dataService.saveProcess(process);
        if( domainId != 0 ) {
        	Domain domain = dataService.getDomain( domainId );
        	DomainProcess domainProcess = new DomainProcess( domain, newProcess, new Date(), DbSync.ADD, null );
			dataService.saveDomainProcess( domainProcess );
        }
        return "redirect:/process/" + domainId;
    }

    @RequestMapping(path = "/process/update/{domainId}", method = RequestMethod.POST)
    public String updateProcess( Process process, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: updateProcess: " + process );   	
        if( process.getDbSynch() != DbSync.ADD ) {
        	process.setDbSynch( DbSync.UPDATE );
        }
    	dataService.updateProcess( process );
        return "redirect:/process/" + domainId;
    }
        
    @RequestMapping(path = "/process/{domainId}", method = RequestMethod.GET)
    public String getAllProcesses( Model model, @PathVariable(value = "domainId") Long domainId ) {
    	if( domainId == 0L ) {    	
    		model.addAttribute("processes",  dataService.getAllProcesses() );
    	}
    	else {
    		model.addAttribute("processes",  dataService.getProcessesForDomain( domainId ) );
    	}
		model.addAttribute("processList",  dataService.getAllProcesses() );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "processes";
    }

    @RequestMapping(path = "/process/edit/{code}/{domainId}", method = RequestMethod.GET)
    public String editProcess( Model model, @PathVariable(value = "code") String code, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("process", dataService.getProcess( code ) );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "processEdit";
    }

    @RequestMapping(path = "/process/delete/{code}/{domainId}", method = RequestMethod.GET)
    public String deleteProcess( RedirectAttributes redirectAttributes, Model model, @PathVariable(name = "code") String code, @PathVariable(value = "domainId") Long domainId ) {
        Info info = new Info();
        String message = "";

    	Long sensorCount = dataService.getProcessSensorCount( code );
    	Long measurementCount = dataService.getProcessMeasurementCount( code );
    	Long domainCount = dataService.getDomainProcessProcessCount(code);
       if( sensorCount == 0L && measurementCount == 0L && domainCount == 0L ) {
	    	if( dataSynchEnabled ) {
        		message = message + "Process " + code + " scheduled for deletion.";
	    		Process process = dataService.getProcess( code );
	    		process.setDbSynch( DbSync.DELETE );
		    	dataService.updateProcess( process );
	    	}
	    	else {
        		message = message + "Process " + code + " deleted.";
	    		dataService.deleteProcess( code );
	    	}
    	}
    	else {
        	if( sensorCount > 0L ) {            
        		message = message + "Process " + code + " has " + sensorCount + " sensor" + ((sensorCount > 1L) ? "s" : "" ) + " configured. ";
        	}
        	if( measurementCount > 0L ) {            
        		message = message + "Process " + code + " has " + measurementCount + " measurement" + ((measurementCount > 1L) ? "s" : "" ) + " logged. ";
        	}
    		if( domainCount != 0L ) {
    			message = message + "Process " + code + " has " + domainCount + " domain" + ((domainCount > 1L) ? "s" : "" ) + " configured. ";
    		}
            message = message + "Associations must be removed before deleting process.";
    	}
        info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
    	return "redirect:/process/" + domainId;
    }

    //
    //	Measurement Type table UI routines
    //
    //
    @RequestMapping(path = "/measureType/add/{domainId}", method = RequestMethod.GET)
    public String createMeasureType(Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("measureType", new MeasureType() );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "measureTypeAdd";
    }
    
    @RequestMapping(path = "/measureType/link/{domainId}", method = RequestMethod.POST)
    public String linkMeasureType( RedirectAttributes redirectAttributes, @RequestParam("linkMeasureType") String code, Model model, @PathVariable(value = "domainId") Long domainId ) {

        Info info = new Info();
        String message = "";
        LOG.info("UiController: MeasureType Link: " + code );   	
        DomainMeasureType domainMeasureType = dataService.getDomainMeasureType( domainId, code );
        if( domainMeasureType == null ) {
        	Domain domain = dataService.getDomain( domainId );
        	MeasureType measureType = dataService.getMeasureType( code );
        	domainMeasureType = new DomainMeasureType( domain, measureType, new Date(), DbSync.ADD, null );
			dataService.saveDomainMeasureType( domainMeasureType );
        	message = "MeasureType " + measureType.getName() + " linked to " + domain.getName();
        }
        else {
        	message = "MeasureType already linked";
        }
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/measureType/" + domainId;
    }
    
    @RequestMapping(path = "/measureType/unlink/{code}/{domainId}", method = RequestMethod.GET)
    public String unlinkMeasureType( RedirectAttributes redirectAttributes, Model model, @PathVariable(value = "code") String code, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: MeasureType unLink: " + code );   	
        Info info = new Info();
        String message = "";
        
        DomainMeasureType domainMeasureType = dataService.getDomainMeasureType( domainId, code );
        if( domainMeasureType != null ) {
	    	if( dataSynchEnabled && domainMeasureType.getDbSynchToken() != null && domainMeasureType.getDbSynchToken().length() > 0 ) {
        		message = message + "MeasureType " + code + " scheduled to be unlinked.";
        		domainMeasureType.setDbSynch( DbSync.DELETE );
	        	dataService.updateDomainMeasureType( domainMeasureType );
	    	}
	    	else {
        		message = message + "MeasureType " + code + " unLinked.";
	        	dataService.deleteDomainMeasureType( domainMeasureType.getId() );
	    	}
        }
    	info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/measureType/" + domainId;
    }
    
    @RequestMapping(path = "/measureType/{domainId}", method = RequestMethod.POST)
    public String saveMeasureType( MeasureType measureType, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: saveMeasureType MeasureType: " + measureType );   	
        MeasureType newMeasureType = dataService.saveMeasureType(measureType);
        if( domainId != 0 ) {
        	Domain domain = dataService.getDomain( domainId );
        	DomainMeasureType domainMeasureType = new DomainMeasureType( domain, newMeasureType, new Date(), DbSync.ADD, null );
			dataService.saveDomainMeasureType( domainMeasureType );
        }
        return "redirect:/measureType/" + domainId;
    }

    @RequestMapping(path = "/measureType/update/{domainId}", method = RequestMethod.POST)
    public String updateMeasureType( MeasureType measureType, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: updateMeasureType MeasureType: " + measureType );   	
        if( measureType.getDbSynch() != DbSync.ADD ) {
        	measureType.setDbSynch( DbSync.UPDATE );
        }
    	dataService.updateMeasureType( measureType );
        return "redirect:/measureType/" + domainId;
    }
    
    @RequestMapping(path = "/measureType/{domainId}", method = RequestMethod.GET)
    public String getAllMeasureTypes( Model model, @PathVariable(value = "domainId") Long domainId ) {
    	if( domainId == 0L ) {    	
            model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
    	}
    	else {
            model.addAttribute("measureTypes",  dataService.getMeasureTypesForDomain( domainId ) );
    	}
		model.addAttribute("measureTypeList",  dataService.getAllMeasureTypes() );
		model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "measureTypes";
    }

    @RequestMapping(path = "/measureType/edit/{code}/{domainId}", method = RequestMethod.GET)
    public String editMeasureType( Model model, @PathVariable(value = "code") String code, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("measureType", dataService.getMeasureType( code ) );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "measureTypeEdit";
    }

    @RequestMapping(path = "/measureType/delete/{code}/{domainId}", method = RequestMethod.GET)
    public String deleteMeasureType( RedirectAttributes redirectAttributes, @PathVariable(name = "code") String code, @PathVariable(value = "domainId") Long domainId ) {
        Info info = new Info();
        String message = "";

    	Long sensorCount = dataService.getMeasureTypeSensorCount( code );
    	Long measurementCount = dataService.getMeasureTypeMeasurementCount( code );
    	Long domainCount = dataService.getDomainMeasureTypeMeasureTypeCount( code );
        if( sensorCount == 0L && measurementCount == 0L && domainCount ==0L ) {
	    	if( dataSynchEnabled ) {
        		message = message + "Measure Type " + code + " scheduled for deletion.";
        		MeasureType measureType = dataService.getMeasureType( code );
        		measureType.setDbSynch( DbSync.DELETE );
		    	dataService.updateMeasureType( measureType );
	    	}
	    	else {
        		message = message + "Measure Type " + code + " deleted.";
        		dataService.deleteMeasureType(code);
	    	}
    	}
    	else {
        	if( sensorCount > 0L ) {            
        		message = message + "Measure Type " + code + " has " + sensorCount + " sensor" + ((sensorCount > 1L) ? "s" : "" ) + " configured. ";
        	}
        	if( measurementCount > 0L ) {            
        		message = message + "Measure Type " + code + " has " + measurementCount + " measurement" + ((measurementCount > 1L) ? "s" : "" ) + " logged. ";
        	}
    		if( domainCount != 0L ) {
    			message = message + "Measure Type " + code + " has " + domainCount + " domain" + ((domainCount > 1L) ? "s" : "" ) + " configured. ";
    		}
            message = message + "Associations must be removed before deleting Measure Type.";
    	}
        info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
	    return "redirect:/measureType/" + domainId;
    }

    //
    //	Batch table UI routines
    //
    //
    @RequestMapping( path = { "/batch/{domainId}"}, method = RequestMethod.GET)
    public String getAllBatches(Model model, @PathVariable(value = "domainId") Long domainId ) {
    	List<Batch> batches;
    	if( domainId == 0 ) {
    		batches = dataService.getAllBatches();
    	}
    	else {
    		batches = dataService.getAllBatches( domainId );
    	}
    	
        model.addAttribute("batches",  batches );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "batches";
    }
    
    @RequestMapping(path = "/batch/add/{domainId}", method = RequestMethod.GET)
    public String createBatch(Model model, @PathVariable(value = "domainId") Long domainId ) {
    	Batch batch = new Batch();
    	batch.setId( 0L );
    	batch.setActive( false );
    	batch.setStartTime( new Date() );
        model.addAttribute("batch", batch );
        model.addAttribute("categories",  dataService.getAllCategories() );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "batchAdd";
    }

    @RequestMapping(path = "/batch/{domainId}", method = RequestMethod.POST)
    public String saveBatch( Batch batch, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: saveBatch Batch: " + batch );   
    	dataService.saveBatch(batch);
        return "redirect:/batch/" + domainId;
    }

    @RequestMapping(path = "/batch/update/{domainId}", method = RequestMethod.POST)
    public String updateBatch( Batch batch, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: updateBatch Batch: " + batch );   
        if( batch.getDbSynch() != DbSync.ADD ) {
        	batch.setDbSynch( DbSync.UPDATE );
        }
    	dataService.updateBatch( batch );
        return "redirect:/batch/" + domainId;
    }
    
    @RequestMapping(path = "/batch/chart/{id}/{domainId}", method = RequestMethod.GET)
    public String getBatchChart(Model model, @PathVariable(value = "id") long id,
    		@PathVariable(value = "domainId") Long domainId,
    		@RequestParam("page") Optional<Integer> page     		
    		) {
    	
    	ChartAttributes chartAttributes = new ChartAttributes();
    	
    	List<MeasureType> measureTypes = dataService.getMeasureTypesToGraph();
        for( MeasureType measureType:measureTypes) {
        	ChartAttributes.SeriesInfo seriesInfo = chartAttributes.new SeriesInfo( measureType.getName() );
	    	List<Measurement> measurements = dataService.getMeasurementsByBatchType( id, measureType.getCode() );
	        for( Measurement measurement:measurements) {
	        	chartAttributes.addSeriesData( measurement.getMeasurementTime().getTime(), measurement.getValueNumber() );
	        	seriesInfo.addData( measurement.getMeasurementTime().getTime(), measurement.getValueNumber()  );
	        }    	
	        chartAttributes.addSeriesInfo( seriesInfo );
        }
        LOG.info("UiController: getBatchChart Guage: " + chartAttributes );        
        model.addAttribute("chartAttributes", chartAttributes );    	
        model.addAttribute("batch", dataService.getBatch(id) );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
       return "batchChart";
    }
    
    @RequestMapping(path = "/batch/edit/{id}/{domainId}", method = RequestMethod.GET)
    public String editBatch(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("batch", dataService.getBatch(id) );
        model.addAttribute("categories",  dataService.getAllCategories() );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "batchEdit";
    }

    @RequestMapping(path = "/batch/delete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteBatch( RedirectAttributes redirectAttributes, @PathVariable(name = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	
        Info info = new Info();
        String message = "";
    	Long sensorCount = dataService.getBatchSensorCount( id );
		Batch batch =  dataService.getBatch( id );
        if( sensorCount == 0L ) {
	    	if( dataSynchEnabled && batch.getDbSynchToken() != null && batch.getDbSynchToken().length() > 0 ) {
        		message = message + "Batch " + id + " scheduled for deletion.";
        		batch.setDbSynch( DbSync.DELETE );
		    	dataService.updateBatch( batch );
	    	}
	    	else {
        		message = message + "Batch " + id + " deleted.";
            	dataService.deleteBatch( id );
	    	}
    	}
    	else {
        	message = message + "Batch ID " + id + " has " + sensorCount + " sensor" + ((sensorCount > 1L) ? "s" : "" ) + " configured. ";
            message = message + "Associations must be removed before deleting batch.";
    	}
        info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/batch/" + domainId;
    }

    //
    //	Measurement table UI routines
    //
    //
    @RequestMapping(path = "/measurement/add/{id}/{domainId}", method = RequestMethod.GET)
    public String createMeasurement( Model model, @PathVariable(name = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	Batch batch = dataService.getBatch( id );
    	Measurement measurement = new Measurement();
    	measurement.setId( 0L );
    	measurement.setMeasurementTime( new Date() );
    	measurement.setBatch( batch );
        model.addAttribute("measurement", measurement );
        model.addAttribute("batches",  dataService.getAllBatches() );
        model.addAttribute("processes",  dataService.getAllProcesses() );
        model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "measurementAdd";
    }

    @RequestMapping(path = "/measurement/{domainId}", method = RequestMethod.POST)
    public String saveMeasurement( Measurement measurement, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: saveMeasurement Measurement: " + measurement );   
        Measurement measurementS = dataService.saveMeasurement( measurement );
        return "redirect:/measurement/batch/" + measurementS.getBatch().getId() + '/' + domainId;
    }
    
    @RequestMapping(path = "/measurement/update/{domainId}", method = RequestMethod.POST)
    public String updateMeasurement( Measurement measurement, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: updateMeasurement Measurement: " + measurement );   
        if( measurement.getDbSynch() != DbSync.ADD ) {
        	measurement.setDbSynch( DbSync.UPDATE );
        }
        Measurement measurementS = dataService.updateMeasurement( measurement );
        return "redirect:/measurement/batch/" + measurementS.getBatch().getId() + '/' + domainId;
    }
    
    @RequestMapping(path = "/measurement/batch/{id}/{domainId}", method = RequestMethod.GET)
    public String getMeasurementForBatch(Model model, @PathVariable(value = "id") long id,
    		@PathVariable(value = "domainId") Long domainId,
    		@RequestParam("page") Optional<Integer> page     		
    		) {
    	int currentPage = page.orElse( 0 );
    	
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	Page<Measurement> pagedResult = dataService.getMeasurementsPageByBatch( currentPage, id );
	    if(pagedResult.hasContent()) {
	    	measurements = (List<Measurement>) pagedResult.getContent();
	    } 
        model.addAttribute("measurements", measurements );
        model.addAttribute("batch", dataService.getBatch(id) );
        
        int totalPages = pagedResult.getTotalPages();
        List<Integer> pageNumbers = new ArrayList<Integer>();
        if (totalPages > 0) {
            pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());
        }        
        model.addAttribute("totalPages", totalPages );
        model.addAttribute("pageNumbers", pageNumbers );
        model.addAttribute("currentPage", currentPage );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "measurements";
    }

    @RequestMapping(path = "/measurement/edit/{id}/{domainId}", method = RequestMethod.GET)
    public String editMeasurement(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("measurement", dataService.getMeasurement(id) );
        model.addAttribute("batches",  dataService.getAllBatches() );
        model.addAttribute("processes",  dataService.getAllProcesses() );
        model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "measurementEdit";
    }

    @RequestMapping(path = "/measurement/delete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteMeasurement( RedirectAttributes redirectAttributes, @PathVariable(name = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        Info info = new Info();
        String message = "";

		Measurement measurement =  dataService.getMeasurement( id );
    	if( dataSynchEnabled && measurement.getDbSynchToken() != null && measurement.getDbSynchToken().length() > 0 ) {
    		message = message + "Measurement " + id + " scheduled for deletion.";
    		measurement.setDbSynch( DbSync.DELETE );
	    	dataService.updateMeasurement( measurement );
    	}
    	else {
    		message = message + "Measurement " + id + " deleted.";
            dataService.deleteMeasurement( id );
    	}
        info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/measurement/batch/" + measurement.getBatch().getId() + '/' + domainId;
    }

    @RequestMapping(path = "/measurement/duplicatedelete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteDuplicateMeasurements( RedirectAttributes redirectAttributes, @PathVariable(name = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        Info info = new Info();
        String message = "";
    	if( dataSynchEnabled ) {
    		message = message + "Duplicate measurements scheduled for deletion.";
    	}
    	else {
    		message = message + "Duplicate deasurements deleted.";
    	}
        info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );

    	dataService.deleteDuplicateMeasurements( id );
        return "redirect:/measurement/batch/" + id + '/' + domainId;
    }

    //
    //	Sensor table UI routines
    //
    //
    @RequestMapping(path = "/sensor/add/{domainId}", method = RequestMethod.GET)
    public String createSensor( Model model, @PathVariable(value = "domainId") Long domainId ) {
    	Sensor sensor = new Sensor();
    	sensor.setId( 0L );
    	sensor.setEnabled( false );
    	sensor.setCommunicationType("Bluetooth");
    	sensor.setTrigger( "Auto" );
    	sensor.setUpdateTime( new Date() );
        model.addAttribute("sensor", sensor );
        model.addAttribute("batches",  dataService.getAllBatches() );
        model.addAttribute("processes",  dataService.getAllProcesses() );
        model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "sensorAdd";
    }
    
    @RequestMapping(path = "/sensor/{domainId}", method = RequestMethod.POST)
    public String saveSensor( Sensor sensor, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: saveSensor: " + sensor );   
    	dataService.saveSensor( sensor );
        return "redirect:/sensor/" + domainId;
    }
    
    @RequestMapping(path = "/sensor/update/{domainId}", method = RequestMethod.POST)
    public String updateSensor( Sensor sensor, @PathVariable(value = "domainId") Long domainId ) {
        LOG.info("UiController: updateSensor: " + sensor );   
        if( sensor.getDbSynch() != DbSync.ADD ) {
        	sensor.setDbSynch( DbSync.UPDATE );
        }
    	dataService.updateSensor( sensor );
        return "redirect:/sensor/" + domainId;
    }
    
    @RequestMapping(path = "/sensor/{domainId}", method = RequestMethod.GET)
    public String getAllSensors( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("sensors", dataService.getAllSensors() );
        model.addAttribute("blueToothEnabled", blueToothEnabled );
        model.addAttribute("wiFiEnabled", blueToothEnabled );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
       return "sensors";
    }

    @RequestMapping(path = "/sensor/scan/{domainId}", method = RequestMethod.GET)
    public String discoverSensors( Model model, @PathVariable(value = "domainId") Long domainId )  {
        try {
			model.addAttribute("sensors", blueToothService.discoverSensors() );
		} catch( Exception e) {
			LOG.error( "discoverWifiSensors: Exception", e );
		}
        model.addAttribute("blueToothEnabled", blueToothEnabled );
        model.addAttribute("wiFiEnabled", blueToothEnabled );

        Sensor sensor = new Sensor();
    	sensor.setId( 0L );
    	sensor.setEnabled( false );
    	sensor.setCommunicationType("Bluetooth");
    	sensor.setTrigger( "Auto" );
    	sensor.setUpdateTime( new Date() );
        model.addAttribute("sensor", sensor );
        model.addAttribute("batches",  dataService.getAllBatches() );
        model.addAttribute("processes",  dataService.getAllProcesses() );
        model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "sensorSelect";
    }

    @RequestMapping(path = "/sensor/scanwifi/{domainId}", method = RequestMethod.GET)
    public String discoverWifiSensors( Model model, @PathVariable(value = "domainId") Long domainId )  {
        try {
			model.addAttribute("sensors", wifiService.discoverSensors( "" ) );
		} catch ( Exception e ) {
			LOG.error( "discoverWifiSensors: Exception", e );
		}
        model.addAttribute("blueToothEnabled", blueToothEnabled );
        model.addAttribute("wiFiEnabled", blueToothEnabled );

        Sensor sensor = new Sensor();
    	sensor.setId( 0L );
    	sensor.setEnabled( false );
    	sensor.setCommunicationType("WiFi");
    	sensor.setTrigger( "Auto" );
    	sensor.setUpdateTime( new Date() );
        model.addAttribute("sensor", sensor );
        model.addAttribute("batches",  dataService.getAllBatches() );
        model.addAttribute("processes",  dataService.getAllProcesses() );
        model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "sensorSelect";
    }
   
    @RequestMapping(path = "/sensor/pair/{id}/{domainId}", method = RequestMethod.GET)
    public String pairSensor(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        Sensor sensor = dataService.getSensor( id );
        model.addAttribute("title", sensor.getName() + " Pairing" );
        boolean result = false;
		try {
			result = blueToothService.pairSensor( sensor.getName(), sensor.getPin() );
		} catch ( Exception e) {
			LOG.error( "pairSensor: Exception", e );
		}
        model.addAttribute("message",  "Pair " +  (result ? "successful" : "failed") );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "results";
    }

    @RequestMapping(path = "/sensor/edit/{id}/{domainId}", method = RequestMethod.GET)
    public String editSensor(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("sensor", dataService.getSensor( id ) );
        model.addAttribute("batches",  dataService.getAllBatches() );
        model.addAttribute("processes",  dataService.getAllProcesses() );
        model.addAttribute("measureTypes",  dataService.getAllMeasureTypes() );
        model.addAttribute("blueToothEnabled", blueToothEnabled );
        model.addAttribute("wiFiEnabled", blueToothEnabled );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "sensorEdit";
    }

    @RequestMapping(path = "/sensor/delete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteSensor( RedirectAttributes redirectAttributes, @PathVariable(name = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	
        Info info = new Info();
        String message = "";
        Sensor sensor = dataService.getSensor( id );
    	if( dataSynchEnabled && sensor.getDbSynchToken() != null && sensor.getDbSynchToken().length() > 0 ) {
    		message = message + "Sensor " + id + " scheduled for deletion.";
    		sensor.setDbSynch( DbSync.DELETE );
	    	dataService.updateSensor( sensor );
    	}
    	else {
    		message = message + "Sensor " + id + " deleted.";
        	dataService.deleteSensor( id );
    	}
        info.setMessage( message );
        redirectAttributes.addFlashAttribute( "info", info );
        return "redirect:/sensor/" + domainId;
    }

    @RequestMapping(path = "/sensor/controlauto/{id}/{domainId}", method = RequestMethod.GET)
    public String sensorControlAuto(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId) {
        Sensor sensor = dataService.getSensor( id );
        model.addAttribute("title", sensor.getName()  );
        Message message = new Message();
        message.setTarget( sensor.getName() );
        message.setData( "COMMAND:CONTROL:AUTO" );
        BluetoothThread.sendMessage( message );
        model.addAttribute("message",  "Command Auto Control sent "  );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "results";
    }
    
    @RequestMapping(path = "/sensor/controlheat/{id}/{domainId}", method = RequestMethod.GET)
    public String sensorControlHeat(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        Sensor sensor = dataService.getSensor( id );
        model.addAttribute("title", sensor.getName()  );
        Message message = new Message();
        message.setTarget( sensor.getName() );
        message.setData( "COMMAND:CONTROL:HEAT_ON" );
        BluetoothThread.sendMessage( message );
        model.addAttribute("message",  "Command override Heat On sent "  );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "results";
    }

    @RequestMapping(path = "/sensor/controlcool/{id}/{domainId}", method = RequestMethod.GET)
    public String sensorControlCool(Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
        Sensor sensor = dataService.getSensor( id );
        model.addAttribute("title", sensor.getName()  );
        Message message = new Message();
        message.setTarget( sensor.getName() );
        message.setData( "COMMAND:CONTROL:COOL_ON" );
        BluetoothThread.sendMessage( message );
        model.addAttribute("message",  "Command override Cool On sent "  );
        model.addAttribute("domains",  dataService.getAllDomains() );        
        model.addAttribute("selectedDomain", domainId );
        return "results";
    }
    
    //
    //	Login
    //
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
    }
    
    @GetMapping("/validate")
    public String validateUser( Model model, @RequestParam("token") String token, HttpServletRequest request) {
        LOG.info("UiController: validateUser: " + token );   	
        Info info = new Info();
        info.setHeader( "User Validation");
        if( userService.confirmUser( token ) ) {
        	info.setMessage( "User validation successful." );
        	info.setHrefLink( "/" );
        	info.setHrefText( "Login" );
        }
        else {
        	info.setMessage( "User validation failed. Contact administator to generate a new verification token." );
        }
        model.addAttribute("info", info );
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }        
    	return "info";
    }
    
    @GetMapping("password")
    public String getPasswordReset(@ModelAttribute("password") Password password) {
        return "password";
    }
    
    @PostMapping("password")
    public String sendEmailToReset( Model model, @Valid @ModelAttribute("password") Password password, BindingResult result) {
    	User user = dataService.getUserByName( password.getUsername() );
    	if( user != null && user.getEmail().length() > 0 && user.getEmail().equals( password.getEmail() ) ){
		    String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();    
	        eventPublisher.publishEvent(new OnPasswordResetEvent(password, serverUrl, "password"));
            Info info = new Info();
            info.setHeader( "Password Reset");
        	info.setMessage( "Password reset token sent to registered email." );
            model.addAttribute("info", info );
        	return "info";
    	}
        Password passwordNew = new Password();
        model.addAttribute("password", passwordNew );
        model.addAttribute("error", true );
        return "password";
    }
    

    @GetMapping("passwordReset")
    public String getNewPassword( Model model, @RequestParam("token") String token) {
        //verify token
        Password password = new Password();
        password.setToken(token);
        model.addAttribute("password", password );

        return "passwordReset";
    }

    @PostMapping("passwordReset")
    public String saveNewPassword( Model model, @ModelAttribute("password") Password password) {
    	// verify user name
        //verify token
        Info info = new Info( "Password Reset", "Password Reset Error" );
    	info.setHrefLink( "/" );
    	info.setHrefText( "Login" );

        ResetToken resetToken = dataService.getResetToken( password.getToken() );
        dataService.deleteResetToken(  password.getToken() );
        if (resetToken.getExpiryDate().after(new Date())) {
        	if( password.getUsername().equals( resetToken.getUsername() )) {
	        	User user = dataService.getUserByName( password.getUsername() );
	    		user.setPassword( passwordEncoder.encode( password.getPassword() ) );
	        	dataService.saveUser(user);
	        	info.setMessage( "Password reset successfully." );
        	} else {
	        	info.setMessage( "Password reset failed." );        		
        	}
        } else {
        	info.setMessage( "Password reset token expired." );
        }
        model.addAttribute("info", info );
        return "info";
    }    
    
    //
    //	User table Profile UI routines
    //
    //
    @RequestMapping(path = "/profile/edit/{domainId}", method = RequestMethod.GET)
    public String editProfile( Model model, @PathVariable(value = "domainId") Long domainId ) {
    	
    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();    	
    	
    	User user = dataService.getUserByName( userDetails.getUsername() );
        model.addAttribute("user", user );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "profileEdit";
    }
    
    @RequestMapping(path = "/profile/{domainId}", method = RequestMethod.POST)
    public String saveProfile( Model model, User user, HttpServletRequest request, @PathVariable(value = "domainId") Long domainId ) {

    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();  
        Info info = new Info( "Profile Update", "Profile updated." );
        
	    String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
	            .replacePath(null)
	            .build()
	            .toUriString();
	    
        LOG.info( "saveProfile: baseUrl: " + baseUrl );
        
    	if( user.getUsername().equals( userDetails.getUsername() ) ) {
    		dataService.saveUser(user);
        	if( !user.isValidated() ) {
        	    String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();    
        		eventPublisher.publishEvent( new OnCreateUserEvent( user, serverUrl, "validate" ) );
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                info.setMessage( "Profile updated. An email has been sent to validate your profile.");
        	}
    	}
        model.addAttribute("info", info );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "info";
    }
    
    @RequestMapping(path = "/profile/password/{domainId}", method = RequestMethod.GET)
    public String profilePassword( Model model, @PathVariable(value = "domainId") Long domainId ) {
    	
    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();    	
    	
    	User user = dataService.getUserByName( userDetails.getUsername() );
    	ProfilePassword profilePassword = new ProfilePassword( user.getUsername() );
        model.addAttribute("profilePassword", profilePassword );
        
        Info info = new Info();
        model.addAttribute("info", info );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
       
        return "profilePassword";
    }
    
    @RequestMapping(path = "/profile/password/{domainId}", method = RequestMethod.POST)
    public String profileUpdatePw( Model model, ProfilePassword profilePassword, @PathVariable(value = "domainId") Long domainId ) {
    	
    	UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();    	
        Info info = new Info();

    	User user = dataService.getUserByName( userDetails.getUsername() );

    	if( passwordEncoder.matches( profilePassword.getPassword(), user.getPassword() ) ) {
			LOG.info( "Passwords Match");
    		user.setPassword( passwordEncoder.encode( profilePassword.getNewPassword() ) );
        	dataService.saveUser(user);
		}
		else {
			LOG.info( "Invalid current password");
	    	profilePassword = new ProfilePassword( user.getUsername() );
	        model.addAttribute("profilePassword", profilePassword );
	        info.setMessage( "Password change failed, please enter current password." );
	        model.addAttribute("info", info );
	        model.addAttribute("domains", dataService.getAllDomains() );
	        model.addAttribute("selectedDomain", domainId );
	        return "profilePassword";
		}
        model.addAttribute("info", info );
        return "redirect:/" + domainId;
    }
    
    //
    //	User table UI routines
    //
    //
    @RequestMapping(path = "/user/add/{domainId}", method = RequestMethod.GET)
    public String createUser( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("user", new User());
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
       return "userAdd";
    }

    @RequestMapping(path = "/user/add/{domainId}", method = RequestMethod.POST)
    public String saveNewUser( User user, @PathVariable(value = "domainId") Long domainId ) {
    	String pw = user.getPassword();
    	if( pw != null && pw.length() > 0 ) {
    		user.setPassword( passwordEncoder.encode( user.getPassword() ) );
        	dataService.saveUser(user);
        	if( !user.isValidated() ) {
        	    String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();    
        		eventPublisher.publishEvent( new OnCreateUserEvent( user, serverUrl, "validate" ) );
        	}
    	}
        return "redirect:/user/" + domainId;
    }
    
    @RequestMapping(path = "/user/{domainId}", method = RequestMethod.POST)
    public String saveUser( User user, @PathVariable(value = "domainId") Long domainId ) {
        dataService.saveUser(user);
    	if( !user.isValidated() ) {
    	    String serverUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();    
    		eventPublisher.publishEvent( new OnCreateUserEvent( user, serverUrl, "validate" ) );
    	}
        return "redirect:/user/" + domainId;
    }
    
    @RequestMapping(path = "/user/{domainId}", method = RequestMethod.GET)
    public String getAllUsers( Model model, @PathVariable(value = "domainId") Long domainId ) {
        model.addAttribute("users",  dataService.getAllUsers() );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "users";
    }

    @RequestMapping(path = "/user/edit/{id}/{domainId}", method = RequestMethod.GET)
    public String editUser( Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	User user = dataService.getUser(id);
        model.addAttribute("user", user );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "userEdit";
    }

    @RequestMapping(path = "/user/password/{id}/{domainId}", method = RequestMethod.GET)
    public String editUserPassword( Model model, @PathVariable(value = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	User user = dataService.getUser(id);
    	user.setPassword( "" );
        model.addAttribute("user", user );
        model.addAttribute("domains", dataService.getAllDomains() );
        model.addAttribute("selectedDomain", domainId );
        return "userPasswordEdit";
    }

    @RequestMapping(path = "/user/password/{domainId}", method = RequestMethod.POST)
    public String updateUserPw( User user, @PathVariable(value = "domainId") Long domainId ) {
    	String pw = user.getPassword();
    	if( pw != null && pw.length() > 0 ) {
        	User userToUpdate = dataService.getUser( user.getId() );
            userToUpdate.setPassword( passwordEncoder.encode( user.getPassword() ) );
        	dataService.saveUser( userToUpdate );
    	}
        return "redirect:/user/" + domainId;
    }
    
    @RequestMapping(path = "/user/delete/{id}/{domainId}", method = RequestMethod.GET)
    public String deleteUser( @PathVariable(name = "id") Long id, @PathVariable(value = "domainId") Long domainId ) {
    	dataService.deleteUser(id);
        return "redirect:/user/" + domainId;
    }
    
}
