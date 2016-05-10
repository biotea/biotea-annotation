package ws.biotea.ld2rdf.annotation.parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;

import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;

import com.hp.hpl.jena.rdf.model.Model;

public interface AnnotatorParser {
	/**
	 * Initializes author and creator for parsed annotations.
	 * @throws URISyntaxException
	 */
	public void init() throws URISyntaxException, MalformedURLException;
	/**
	 * Parses a response from a URL in order to extract its annotations.
	 * Annotations provided by CMA look like: 
	 * <e id='PMC4246611.title.e1' src='UMLS' cui='C0000000' type='T043' grp='PHYS' offset='0' len='12' score='1.0000' idf='8'>Reactivation</e>
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(String documentId) throws IOException, URISyntaxException, NoResponseException;
	
	/**
	 * Parses a response from a file in order to extract its annotations.
	 * Annotations provided by CMA look like: 
	 * <e id='PMC4246611.title.e1' src='UMLS' cui='C0000000' type='T043' grp='PHYS' offset='0' len='12' score='1.0000' idf='8'>Reactivation</e>
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(File file) throws IOException, URISyntaxException, NoResponseException;
	
	/**
	 * Serializes annotations to a file.
	 * @param fileName
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 */
	public List<AnnotationE> serializeToFile(String fullPathName, RDFFormat format, AnnotationDAO dao, boolean empty, boolean blankNode) throws RDFModelIOException ;
	
	/**
	 * Serializes annotations to a model.
	 * @param model
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 */
	public List<AnnotationE> serializeToModel(Model model, AnnotationDAO dao, boolean blankNode) throws RDFModelIOException;
}
