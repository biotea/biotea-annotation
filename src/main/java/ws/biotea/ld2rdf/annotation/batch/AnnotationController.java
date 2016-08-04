package ws.biotea.ld2rdf.annotation.batch;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.parser.AnnotatorParser;
import ws.biotea.ld2rdf.annotation.parser.CMAParser;
import ws.biotea.ld2rdf.annotation.parser.NCBOParser;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationOWLDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.ConnectionLDModel;
import ws.biotea.ld2rdf.util.annotation.Annotator;

public class AnnotationController {
	private static final Logger LOGGER = Logger.getLogger(AnnotationController.class);
	private static final String JSON_EXTENSION = ".json";
	private static final String RDF_EXTENSION = ".rdf";
	
	public void annotatesFromFile(File inFile, String outputDir, RDFFormat format, Annotator annotator, boolean onlyTA) {
		String path = inFile.toString();
		
		
		try {
			String outExtension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String outName = outputDir + "/" + inFile.getName() + outExtension; 
			path = inFile.getCanonicalPath();
			ConnectionLDModel conn = new ConnectionLDModel();
			Model model = conn.openJenaModel(outName, true, format);
			AnnotationDAO dao = new AnnotationOWLDAO();	
			AnnotatorParser parser = null;    		
			
			if (annotator == Annotator.CMA) {			
				if (onlyTA) {
					parser = new CMAParser(false, true, true, true, true);
				} else {
					parser = new CMAParser(false, true, true, false, true);
				} 						
			} else if (annotator == Annotator.NCBO) {
				if (onlyTA) {
					parser = new NCBOParser(false, true);
				} else {
					parser = new NCBOParser(false, false);
				} 	
			}
			parser.parse(inFile);
			parser.serializeToModel(model, dao, false);
			conn.closeAndWriteJenaModel(format);
		} catch (ClassNotFoundException | OntologyLoadException | URISyntaxException | ArticleParserException | RDFModelIOException e1) {
			LOGGER.error("There was an error processing " + path + ". Error was: " + e1);
		} catch (NoResponseException e) {
			LOGGER.warn("No annotation has been retrieved for " + path);
		} catch (IOException e) {
			LOGGER.error("There was an error processing a file. Error was: " + e);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			LOGGER.fatal("Annotator parser not initialized. Error was: " + npe);
		}
	}
	
	public void annotatesFromURL(String outputDir, RDFFormat format, Annotator annotator, String docId, boolean onlyTA) {
		try {
			String extension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String outName = outputDir + "/" + docId + extension; 
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		AnnotationDAO dao = new AnnotationOWLDAO();	
    		AnnotatorParser parser = null;    		
    		
			if (annotator == Annotator.CMA) {			
				if (onlyTA) {
					parser = new CMAParser(true, true, true, true, true);
				} else {
					parser = new CMAParser(true, true, true, false, true);
				} 									
			} else if (annotator == Annotator.NCBO) {
				if (onlyTA) {
					parser = new NCBOParser(true, true);
				} else {
					parser = new NCBOParser(true, false);
				}
			}
			parser.parse(docId);
			parser.serializeToModel(model, dao, false);
			conn.closeAndWriteJenaModel(format);
		} catch (ClassNotFoundException | OntologyLoadException | IOException | URISyntaxException | RDFModelIOException e) {
			LOGGER.error("There was an error processing " + docId + ". Error was: " + e);
		} catch (NoResponseException e) {
			LOGGER.warn("No annotation has been retrieved for " + docId);
		} catch (ArticleParserException e) {
			LOGGER.error("There was an error processing " + docId + ". Error was: " + e);
		} catch (NullPointerException npe) {
			LOGGER.fatal("Annotator parser not initialized. Error was: " + npe);
		}
	}

}
