package ws.biotea.ld2rdf.annotation.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.InputException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.UnsupportedFormatException;
import ws.biotea.ld2rdf.annotation.model.ArticleElement;
import ws.biotea.ld2rdf.annotation.model.JATSArticle;
import ws.biotea.ld2rdf.exception.DTDException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.ao.ElementSelector;
import ws.biotea.ld2rdf.rdf.model.ao.ExactQualifier;
import ws.biotea.ld2rdf.rdf.model.ao.FoafAgent;
import ws.biotea.ld2rdf.rdf.model.ao.FoafDocument;
import ws.biotea.ld2rdf.rdf.model.ao.Topic;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;
import ws.biotea.ld2rdf.rdfGeneration.jats.GlobalArticleConfig;
import ws.biotea.ld2rdf.util.ClassesAndProperties;
import ws.biotea.ld2rdf.util.ResourceConfig;
import ws.biotea.ld2rdf.util.annotation.AnnotationResourceConfig;
import ws.biotea.ld2rdf.util.annotation.BioOntologyConfig;
import ws.biotea.ld2rdf.util.annotation.ConstantConfig;
import ws.biotea.ld2rdf.util.ncbo.annotator.Ontology;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.AnnotationCollection;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotations;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Empty;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

public class NCBOParser implements AnnotatorParser {
	private static Logger logger = Logger.getLogger(NCBOParser.class);
	private List<AnnotationE> lstAnnotations;
	
	private FoafAgent creator, author;
	private String annotator = "NCBO";
	private static final String annotatorURL = AnnotationResourceConfig.getNCBOServiceURL(); //"http://rest.bioontology.org/obs/annotator";
	private static final String ontologiesToAnnotate = Ontology.getInstance().getAllAcronym(); //Ontology.getAllVirtualId();
	public static final String BASE_FOAF_NCBO_ANNOTATOR = AnnotationResourceConfig.getNCBOAnnotatorURL();//"http://bioportal.bioontology.org/annotator/";
	private final static String stopWords = AnnotationResourceConfig.getNCBOStopwords(); //http://www.ranks.nl/resources/stopwords.html
	private final static Pattern excludedSections = Pattern.compile("([aA]cknowledgements)|([cC]ompeting[-]interests)|([aA]uthor)(s-|--|-s|)(-contributions)|([aA]bbreviations)"); //we are replacing spaces and other chars such as ' by -
	
	private StringBuffer articleURI;
	private String articleId;
	private JATSArticle article;
	
	private String inputLocation;
	private boolean fromURL;
	private boolean onlyTitleAndAbstract;
	private ConstantConfig onto;
	private ConstantConfig inStyle;
	
	public NCBOParser() {
		
	}
	
	/**
	 * Constructor.
	 * @param fromURL
	 * @param onlyTitleAndAbstract
	 */
	public NCBOParser(Boolean fromURL, Boolean onlyTitleAndAbstract, ConstantConfig onto, ConstantConfig inStyle) {
		this.articleURI = new StringBuffer();		
		this.lstAnnotations = new ArrayList<>();
		
		this.onto = onto;
		this.inStyle = inStyle;
		
		this.fromURL = fromURL;
		this.onlyTitleAndAbstract = onlyTitleAndAbstract;
	}

	@Override
	public void init() throws URISyntaxException, MalformedURLException {
		if (this.author == null) {
			this.author = new FoafAgent();
			this.author.setId(new URI(AnnotationResourceConfig.getNCBOAnnotatorURL()));				
		}		
		if (this.creator == null) {
			this.creator = new FoafAgent();
			this.creator.setId(new URI(ResourceConfig.BIOTEA_RDFIZATOR));
		}
	}

	@Override
	public List<AnnotationE> parse(String documentId) throws IOException,
			URISyntaxException, NoResponseException, ArticleParserException {
		throw new ArticleParserException("URL parsing currently not supported for NCBO");
		/*
		this.init();
		
		this.articleId = documentId;
		File file = null;
		if (this.fromURL) {
			URL url;
			if (this.onlyTitleAndAbstract) {
				this.inputLocation = ""; //pubmed
				url = new URL(this.inputLocation + ResourceConfig.getTitleAbstractPrefix() + documentId);
			} else {
				this.inputLocation = ""; //PMC-OA
				url = new URL(this.inputLocation + ResourceConfig.getFullTextPrefix() + documentId);
			}
			//file = new FileInputStream(url.openStream()));					
		} else {
			throw new IOException("DocumentId parser cannot be used if NCBOParser has been configured to parse from files");
		}
		try {
			this.article.createArticleFromFile(file, this.articleURI, this.onlyTitleAndAbstract);
		} catch (InputException | DTDException | JAXBException e) {
			logger.fatal("File corresponding to "+ documentId + " could not be parsed");
			throw new ArticleParserException("File corresponding to "+ documentId + " could not be parsed");
		}		
		this.parseParagraphs();		
		return this.lstAnnotations;
		*/
	}

	@Override
	public List<AnnotationE> parse(File file) throws IOException,
			URISyntaxException, NoResponseException, ArticleParserException {
		this.init();
		
		if (this.fromURL) {
			throw new IOException("File parser cannot be used if NCBOParser has been configured to parse from a URL");
		}
		
		try {
			if (this.inStyle == ConstantConfig.JATS_FILE) {
				this.article = new JATSArticle();
				this.articleId = this.article.createArticleFromFile(file, this.articleURI, this.onlyTitleAndAbstract);
				this.parseParagraphs();
			} else if (this.inStyle == ConstantConfig.RDF_FILE) {
				this.parseRDFFile(file);
			} else {
				throw new ArticleParserException("Input syle not supported: " + this.inStyle);
			}			
		} catch (InputException | DTDException | JAXBException e) {
			logger.fatal("File corresponding to "+ file.getAbsolutePath() + " could not be parsed");
			throw new ArticleParserException("File corresponding to "+ file.getAbsolutePath() + " could not be parsed");
		}				
		return this.lstAnnotations;
	}	
	
	private void parseRDFFile(File file) throws FileNotFoundException, ArticleParserException, URISyntaxException {
		// create an empty model
		Model model = ModelFactory.createDefaultModel();
		// use the FileManager to find the input file
		InputStream in = new FileInputStream(file);
		// read the RDF/XML file
		model.read(in, null);
		logger.debug("===RDF READ=== " + file);
		
		Property rdfType = model.getProperty(ClassesAndProperties.RDF_TYPE.getURLValue());
		Property titleProp = model.getProperty(ClassesAndProperties.DCTERMS_PROP_TITLE.getURLValue());
		String articleStringURI = "";
		
		Resource articleClass = model.createResource(ClassesAndProperties.BIBO_ACADEMIC_ARTICLE.getURLValue());
		ResIterator resItr = model.listResourcesWithProperty(rdfType, articleClass); 
		if (resItr.hasNext()) {
			Resource res = resItr.next();			
			
			this.articleURI.delete(0, articleURI.length());
			this.articleURI.append(res.getURI().toString());
			articleStringURI = this.articleURI.toString();
			this.articleId = GlobalArticleConfig.getArticleIdFromRdfUri(articleStringURI);			
			
			String textToAnnotate = res.getProperty(titleProp).getString();			
			if ((textToAnnotate != null) && (textToAnnotate.length() != 0)) {
    			boolean writeDown = annotateWithNCBO(textToAnnotate, null, articleStringURI);
            	if (!writeDown) {
            		logger.warn("- WARNING MAIN TITLE - NCBO annotations for " + this.articleId);
            	}
    		}
		} else {
			throw new ArticleParserException("No id was retrieved from " + file);
		}
		
		Resource sectionClass = model.createResource(ClassesAndProperties.DOCO_SECTION.getURLValue());
		resItr = model.listResourcesWithProperty(rdfType, sectionClass);
		while (resItr.hasNext()) {
			Resource res = resItr.next();
			try {
				String textToAnnotate = res.getProperty(titleProp).getString();
				String urlToAnnotate = res.getURI().toString();
	    		if ((textToAnnotate != null) && (textToAnnotate.length() != 0)) {
	    			boolean writeDown = annotateWithNCBO(textToAnnotate, urlToAnnotate, articleStringURI);
	            	if (!writeDown) {
	            		logger.warn("- WARNING SUBTITLE - NCBO annotations for " + this.articleId + "(" + urlToAnnotate + ")");
	            	}
	    		}
			} catch (Exception e) {
				continue;
			}
		}
		
		Property text = model.getProperty(ClassesAndProperties.TEXT_PROPERTY);
		resItr = model.listResourcesWithProperty(text);
		while (resItr.hasNext()) {
			Resource res = resItr.next();
			Matcher matcher = NCBOParser.excludedSections.matcher(res.getURI().toString());
        	if (matcher.find()) {
        		continue; //excluded sections will not be annotated
        	} else {        		
        		//paragraph by paragraph
        		String textToAnnotate = res.getProperty(text).getObject().toString();
        		String urlToAnnotate = res.getURI().toString();
        		if ((textToAnnotate != null) && (textToAnnotate.length() != 0)) {
        			boolean writeDown = annotateWithNCBO(textToAnnotate, urlToAnnotate, articleStringURI);
                	if (!writeDown) {
                		logger.warn("- WARNING PARAGRAPH - NCBO annotations for " + this.articleId + "(" + urlToAnnotate + ")");
                	}
        		}
        	}
		}
	}
	
	private void parseParagraphs() throws IOException, NoResponseException, URISyntaxException {
		String articleStringURI = this.articleURI.toString();
		for (ArticleElement element: this.article.getElements()) {			
    		//paragraph by paragraph
    		String textToAnnotate = element.getText();
    		String urlToAnnotate = element.getIdentifier();
    		if ((textToAnnotate != null) && (textToAnnotate.length() != 0)) {
    			boolean writeDown = annotateWithNCBO(textToAnnotate, urlToAnnotate, articleStringURI);
            	if (!writeDown) {
            		logger.warn("WARNING PARAGRAPH - NCBO annotations for " + this.articleId + "(" + urlToAnnotate + ") could not be processed");
            	}
    		}
		}
		//we annotate as much as we can, some paragraphs can be omitted
		logger.info("===SECTIONS ANNOTATED=== " + this.articleId);
	}
	
	/**
	 * Annotate a short paragraph corresponding only to one context.
	 */
    private boolean annotateWithNCBO(String paragraph, String urlContext, String articleStringURI) {
        try {        	        	
        	paragraph = paragraph.replaceAll("[^\\p{Alpha}\\p{Z}\\p{P}\\p{N}]", "_");        	
        	paragraph = URLEncoder.encode(paragraph, ResourceConfig.UTF_ENCODING);
        	paragraph = paragraph.replace("+", " ");
        	//System.out.println("TO ANNOT: " + urlContext + "\n" + paragraph);
            HttpClient client = new HttpClient();
            client.getParams().setParameter(HttpMethodParams.USER_AGENT, "Annotator Client Scientific Publications");  //Set this string for your application 
            
            PostMethod method = new PostMethod(annotatorURL);
            
            // Configure the form parameters
            method.addParameter("stop_words",stopWords);
            method.addParameter("minimum_match_length","3");
            method.addParameter("ontologies", ontologiesToAnnotate);            
            method.addParameter("text", paragraph);
            method.addParameter("format", "xml"); //Options are 'text', 'xml', 'tabDelimited'   
            method.addParameter("apikey", AnnotationResourceConfig.getNCBOAPIKey());

            // Execute the POST method
            int statusCode = client.executeMethod(method);
            
            if( statusCode != -1 ) {
                try {
	                InputStream annotatedParagraph = method.getResponseBodyAsStream();           
	                if (annotatedParagraph != null) {	                	
	                	//Reader reader = new StringReader(annotatedParagraph);
	            		JAXBContext jc = JAXBContext.newInstance("ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated");
	            		Unmarshaller unmarshaller = jc.createUnmarshaller();
	            		AnnotationCollection xml;
	            		Object obj = new Object();
	            		try {	            			
	            			obj = unmarshaller.unmarshal(annotatedParagraph);
	            			if (obj instanceof Empty) {
	            				return true; //no annotations were found but everything was ok with the response
	            			}
	            			xml = (AnnotationCollection)obj; //otherwise, AnnotationCollection should be the unmarshalled object
	            		} catch (Exception e) {
            				logger.fatal("- FATAL DTD ERROR ANNOTATOR - NCBO annotations for " + this.articleId + "(" + urlContext + ") cannot be unmarshalled: " + e.getMessage() + " - class: " + obj.getClass());
	            			return false;			
	            		}
	            		logger.debug("---Annotations unmarshalled---");	
	            		List<ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotation> lstAnn = xml.getAnnotation();
	            		for (ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotation ann: lstAnn) {
	            			String fullId = ann.getAnnotatedClass().getId();
	            			List<Annotations> lstAnnotations = ann.getAnnotationsCollection().getAnnotations();
	            			if (lstAnnotations.size() != 0) {
	            				String body = lstAnnotations.get(0).getText();
								ws.biotea.ld2rdf.util.ncbo.annotator.NCBOOntology onto = ws.biotea.ld2rdf.util.ncbo.annotator.Ontology.getInstance().getOntologyByURL(fullId);
								if (onto == null) { //For NDFRT is possible to get UMLS elements, those are excluded
									continue;
								}
								String partialId = fullId.substring(onto.getURL().length());
								if (partialId.startsWith("/")) {
									partialId = partialId.substring(1);
								}
								//annot: creator, body, resource, date
								ExactQualifier annot = new ExactQualifier();
								annot.setAuthor(this.author);
								annot.setCreator(this.creator);	            							
								annot.getBodies().add(body);
		            			FoafDocument document = new FoafDocument();
		            			document.setId(new URI(articleStringURI));
		            			annot.setResource(document);
		            			annot.setDocumentID(this.articleId);
		            			annot.setCreationDate(Calendar.getInstance());		            			
		            			//topic
		        				Topic topic = new Topic();
		        				topic.setNameSpace(new URI(onto.getNS() + ":" + partialId));
		        				topic.setURL(new URI(onto.getURL() + partialId));
		        				Topic topic2 = null;
		        				if (onto.getNS().equals(BioOntologyConfig.getNS("NCBITaxon"))) {
		        					topic2 = new Topic();
		                			topic2.setNameSpace(new URI(BioOntologyConfig.getNS("UNIPROT_TAXONOMY") + ":" + partialId)); //species: http://purl.uniprot.org/taxonomy/
		                			topic2.setURL(new URI(BioOntologyConfig.getURL("UNIPROT_TAXONOMY") + partialId));
		        				}
		        				annot.setFrequency(lstAnnotations.size());	
		        				//Go to the annotations table
		        				if (annot != null) {
		        	            	int pos = this.lstAnnotations.indexOf(annot);
		        	            	if (pos != -1) {
		        	            		AnnotationE a = this.lstAnnotations.get(pos);
		        	            		if (!a.getTopics().contains(topic)) {
		        	            			a.addTopic(topic);		        	            			
		        	            		}
		        	            		if (topic2 != null) {
		        	            			if (!a.getTopics().contains(topic2)) {
	    	        	            			a.addTopic(topic2);
	    	        	            		}
		        	            		}
		        	            		if (urlContext != null) {
		        	                		//context (selector)
		        	            			ElementSelector ses = new ElementSelector(a.getResource());
		        	            			ses.setElementURI(urlContext);		        	            			
		        	            			if (!a.getContext().contains(ses)) {
		        	            				a.addContext(ses); 
		        	            				a.setFrequency(a.getFrequency() + annot.getFrequency());
		        	            			}
		        	                	}  
		        	            	} else {
	        	            			annot.addTopic(topic);
	        	            			if (topic2 != null) {
	        	            				annot.addTopic(topic2);
	        	            			}
	        	            			if (urlContext != null) {                        		
	        	                    		//context (selector)
	        	                			ElementSelector ses = new ElementSelector(annot.getResource());
	        	                			ses.setElementURI(urlContext);
	        	                        	annot.addContext(ses);         		
	        	                    	}
	        	            			this.lstAnnotations.add(annot);
		        	            	}
		        	            }
	            			}						
	            		}
	                }
	                method.releaseConnection();
	                method = null;
                } catch( Exception e ) {
                    //e.printStackTrace();
                    logger.info("===ERROR NCBO Annotator (" + this.articleId + ")=== " +e.getMessage());
                    return false;
                }
            } else {
            	logger.info("===ERROR??? NCBO Annotator (" + this.articleId + ")=== (status code != 1) ");
                return false;
            }
        } catch ( Exception e ) {
        	//e.printStackTrace();
            logger.info("===ERROR NCBO Annotator (" + this.articleId + " at " + urlContext + ")=== (http call)" + e.getMessage());
            return false;
        }
        return true;
    }

	@Override
	public List<AnnotationE> serializeToFile(String fullPathName,
			RDFFormat format, AnnotationDAO dao, boolean empty,
			boolean blankNode) throws RDFModelIOException, UnsupportedFormatException {
		List<AnnotationE> lst = null;
		if (this.onto == ConstantConfig.AO) {
			lst = dao.insertAnnotations(ResourceConfig.BIOTEA_DATASET, AnnotationResourceConfig.getBaseURLAnnotator(this.annotator), this.lstAnnotations, fullPathName, format, empty, blankNode);
		} else {
			throw new UnsupportedFormatException("Ontology style not supported"); //TODO support Open Annotation
		}		
		int error = this.lstAnnotations.size() - lst.size();
		if (error != 0) {
			logger.info("==ERROR writing annotations NCBO== " + error + " annotations were not created, check the logs starting by 'Annotation not inserted' for more information");			
		}
		return lst;
	}

	@Override
	public List<AnnotationE> serializeToModel(Model model, AnnotationDAO dao,
			boolean blankNode) throws RDFModelIOException, UnsupportedFormatException {
		List<AnnotationE> lst = null;
		if (this.onto == ConstantConfig.AO) {
			lst = dao.insertAnnotations(ResourceConfig.BIOTEA_DATASET, AnnotationResourceConfig.getBaseURLAnnotator(this.annotator), this.lstAnnotations, model, blankNode);
		} else {
			throw new UnsupportedFormatException("Ontology style not supported"); //TODO support Open Annotation
		}		
		int error = this.lstAnnotations.size() - lst.size();
		if (error != 0) {
			logger.info("==ERROR writing annotations NCBO== " + error + " annotations were not created, check the logs starting by 'Annotation not inserted' for more information");			
		}
		return lst;
	}

	@Override
	public String getArticleId() {
		return this.articleId;
	}

}