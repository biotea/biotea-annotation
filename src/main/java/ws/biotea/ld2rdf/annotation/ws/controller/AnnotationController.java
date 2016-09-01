package ws.biotea.ld2rdf.annotation.ws.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.biotea.ld2rdf.annotation.exception.ErrorResource;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.ParameterException;
import ws.biotea.ld2rdf.annotation.parser.AnnotatorParser;
import ws.biotea.ld2rdf.annotation.parser.CMAParser;
import ws.biotea.ld2rdf.annotation.parser.NCBOParser;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.persistence.AnnotationDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationOWLDAO;
import ws.biotea.ld2rdf.rdf.persistence.oa.AnnotationOWLOA;
import ws.biotea.ld2rdf.rdf.persistence.ConnectionLDModel;
import ws.biotea.ld2rdf.util.annotation.AnnotationResourceAdditionalConfig;
import ws.biotea.ld2rdf.util.annotation.AnnotationResourceConfig;
import ws.biotea.ld2rdf.util.annotation.Annotator;
import ws.biotea.ld2rdf.util.annotation.ConstantConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

@RestController
public class AnnotationController {
	private static final Logger LOGGER = Logger.getLogger(AnnotationController.class);
	private final static String PARAMETER_ERROR = "Please verify your parameters. <id> is mandatory. "
			+ "[db] is optional, accepted values are 'pubmed' or 'pmc' (default value). "
			+ "[annotator] is optional, accepted values are 'cma', 'ncbo'(default). "
			+ "[format] is optional, accepted values 'xml' or 'json'(default)";
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from Biotea web services, powered by Spring Boot!";
    }
    
    @RequestMapping(value= "/annotation", method = RequestMethod.GET)
    public @ResponseBody void getModel(HttpServletResponse response
    		, @RequestParam(value = "db", required = true, defaultValue = "pmc") String db
    		, @RequestParam(value = "id", required = true) String id
    		, @RequestParam(value = "annotator", required = true, defaultValue = "ncbo") String annotator
    		, @RequestParam(value = "onto", required = true, defaultValue = "ao") String ontology
    		, @RequestParam(value = "format", required = true, defaultValue = "xml") String format) 
    				throws Exception {
    	
    	ConstantConfig onto;
    	//Verify parameters
    	ontology = ontology.toUpperCase();
		try {
			onto = ConstantConfig.valueOf(ontology);
		} catch(IllegalArgumentException e) {
			onto = ConstantConfig.AO;
		}
		
    	annotator = annotator.toUpperCase();    	
		if ( !(db.equals("pubmed") || db.equals("pmc"))) {
			throw new ParameterException(PARAMETER_ERROR);
		}
		if ( !(annotator.equals("CMA") || annotator.equals("NCBO"))) {
			throw new ParameterException(PARAMETER_ERROR);
		}
		
		RDFFormat rdfFormat;
		String langFormat;
		if (format.equals("json")) {
			response.setContentType("application/json;charset=UTF-8"); 
			rdfFormat = RDFFormat.JSONLD;
			langFormat = "JSON-LD";
		} else {
			response.setContentType("text/xml;charset=UTF-8");
			//response.setHeader("Content-type","application/xhtml+xml");
			rdfFormat = RDFFormat.RDFXML_ABBREV;
			langFormat = "RDF/XML-ABBREV";
		}
		
		boolean cached = false;
		try {    		
    		if (AnnotationResourceConfig.getAnnotationCaching(Annotator.valueOf(annotator))) {
    			File file = new File (AnnotationResourceAdditionalConfig.getAnnotationCachingFileName(id, Annotator.valueOf(annotator), rdfFormat));    		
    			if (file.exists()) {  
    				FileInputStream fis = new FileInputStream(file);
    				StreamUtils.copy(fis, response.getOutputStream());
    				cached = true;    				
    			}    			
			}
		} catch (IOException e) {
			cached = false;
		}
		
		try {
    		if (!cached) {
    			ConnectionLDModel conn = new ConnectionLDModel();
        		Model model = conn.openJenaModel();
        		AnnotationDAO dao;
        		if (onto == ConstantConfig.OA) {
        			dao = new AnnotationOWLOA();
        		} else {
        			dao = new AnnotationOWLDAO();
        		}    							        	
        		//Verify annotator
        		AnnotatorParser parser = null;
        		if (Annotator.valueOf(annotator) == Annotator.CMA) {    			    				
        			if (db.equals("pubmed")) {
        				parser = new CMAParser(true, true, true, true, true);
        			} else {
        				parser = new CMAParser(true, true, true, false, true);
        			}     				    		
        		} else if (Annotator.valueOf(annotator) == Annotator.NCBO) {
        			if (db.equals("pubmed")) {
        				parser = new NCBOParser(false, true, ConstantConfig.JATS_PAGE);
        			} else {
        				parser = new NCBOParser(false, false, ConstantConfig.JATS_PAGE);
        			} 
        		} 
        		parser.parse(id);
				parser.serializeToModel(model, dao, false);
        		
        		this.saveToFile(Annotator.valueOf(annotator), db, id, rdfFormat, conn);
        		
        		model.write(response.getWriter(), langFormat);
    		}
		} catch (ClassNotFoundException
				| OntologyLoadException | RDFModelIOException | IOException | URISyntaxException e) {
    		LOGGER.error(e);
    		throw e;
		} catch (IllegalArgumentException e) {
			throw new ParameterException(PARAMETER_ERROR);
		}
    }
    
    @ExceptionHandler(ParameterException.class)
	public void handleParameterError(Exception exception, HttpServletResponse response) throws IOException {
    	LOGGER.error(exception);
    	
		ErrorResource error = new ErrorResource(HttpStatus.BAD_REQUEST, exception.getMessage(), 
				"/annotation", exception.getClass().getName());

		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();		
		mapper.writeValue(response.getWriter(), error);		
	}
    
    @ExceptionHandler(NoResponseException.class)
	public void handleNoResponseError(Exception exception, HttpServletResponse response) throws IOException {
    	LOGGER.error(exception);
    	
		ErrorResource error = new ErrorResource(HttpStatus.NO_CONTENT, exception.getMessage(), 
				"/annotation", exception.getClass().getName());

		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();		
		mapper.writeValue(response.getWriter(), error);		
	}
    
    /**
     * Save to file, if any error just log and go ahead with the service response.
     * @param annotator
     * @param db
     * @param id
     * @param extension
     * @param format
     * @param conn
     */
    private void saveToFile(Annotator annotator, String db, String id, RDFFormat format, ConnectionLDModel conn) {    	
    	if (AnnotationResourceConfig.getAnnotationSaving(annotator)) {
    		String outName = AnnotationResourceAdditionalConfig.getAnnotationSavingFileName(id, annotator, format);
    		File file = new File(outName);
    		if ((file.exists() && AnnotationResourceConfig.getAnnotationSavingReplace(annotator)) 
				|| (!file.exists()) ){
    			try {
					conn.closeAndWriteJenaModel(outName, format);
				} catch (Exception e) {
					LOGGER.warn(annotator.getName() + " annotations could not be saved for " + db + ":" + id + " article");
				}
    		}
    	}
    }
}