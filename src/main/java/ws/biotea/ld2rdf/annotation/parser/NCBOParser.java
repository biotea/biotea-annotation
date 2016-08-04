package ws.biotea.ld2rdf.annotation.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import ws.biotea.ld2rdf.util.ResourceConfig;
import ws.biotea.ld2rdf.util.annotation.AnnotationResourceConfig;
import ws.biotea.ld2rdf.util.annotation.BioOntologyConfig;
import ws.biotea.ld2rdf.util.ncbo.annotator.Ontology;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.AnnotationCollection;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotations;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Empty;

import com.hp.hpl.jena.rdf.model.Model;

public class NCBOParser implements AnnotatorParser {
	private static Logger logger = Logger.getLogger(NCBOParser.class);
	private List<AnnotationE> lstAnnotations;
	
	private FoafAgent creator, author;
	private String annotator = "NCBO";
	private static final String annotatorURL = AnnotationResourceConfig.getNCBOServiceURL(); //"http://rest.bioontology.org/obs/annotator";
	private static final String ontologiesToAnnotate = Ontology.getInstance().getAllAcronym(); //Ontology.getAllVirtualId();
	public static final String BASE_FOAF_NCBO_ANNOTATOR = AnnotationResourceConfig.getNCBOAnnotatorURL();//"http://bioportal.bioontology.org/annotator/";
	private final static String stopWords = AnnotationResourceConfig.getNCBOStopwords(); //http://www.ranks.nl/resources/stopwords.html	
	
	private StringBuffer articleURI;
	private String articleId;
	private JATSArticle article;
	
	private String inputLocation;
	private boolean fromURL;
	private boolean onlyTitleAndAbstract;
	
	public NCBOParser() {
		
	}
	
	/**
	 * Constructor.
	 * @param fromURL
	 * @param onlyTitleAndAbstract
	 */
	public NCBOParser(Boolean fromURL, Boolean onlyTitleAndAbstract) {
		this.articleURI = new StringBuffer();
		this.article = new JATSArticle();
		this.lstAnnotations = new ArrayList<>();
		
		this.fromURL = fromURL;
		this.onlyTitleAndAbstract = onlyTitleAndAbstract;
		
		if (this.fromURL) {
			//TODO: Either PubMed or PMC-OA get article web services
			if (this.onlyTitleAndAbstract) {
				this.inputLocation = "";
			} else {
				this.inputLocation = "";
			}
		}
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
		this.init();
		
		this.articleId = documentId;
		
		File file = null;
		if (this.fromURL) {	//TODO
			URL url;
			if (this.onlyTitleAndAbstract) {
				url = new URL(this.inputLocation + ResourceConfig.getTitleAbstractPrefix() + documentId);
			} else {
				url = new URL(this.inputLocation + ResourceConfig.getFullTextPrefix() + documentId);
			}
			//TODO
			//file = new FileInputStream(url.openStream()));		
		} else {
			throw new IOException("DocumentId parser cannot be used if NCBOParser has been configured to parse from files");
		}
		try {
			this.article.createArticleFromFile(file, this.articleURI);
		} catch (InputException | DTDException | JAXBException e) {
			e.printStackTrace();
			logger.fatal("File corresponding to "+ documentId + " could not be parsed");
			throw new ArticleParserException("File corresponding to "+ documentId + " could not be parsed");
		}		
		this.parseParagraphs();
		return this.lstAnnotations;
	}

	@Override
	public List<AnnotationE> parse(File file) throws IOException,
			URISyntaxException, NoResponseException, ArticleParserException {
		this.init();
		
		if (this.fromURL) {
			throw new IOException("File parser cannot be used if NCBOParser has been configured to parse from a URL");
		}
		
		try {
			this.articleId = this.article.createArticleFromFile(file, this.articleURI);
		} catch (InputException | DTDException | JAXBException e) {
			e.printStackTrace();
			logger.fatal("File corresponding to "+ file.getAbsolutePath() + " could not be parsed");
			throw new ArticleParserException("File corresponding to "+ file.getAbsolutePath() + " could not be parsed");
		}		
		this.parseParagraphs();
		return this.lstAnnotations;
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
    	/*System.out.print(paragraph);
    	System.out.print("\t" + urlContext);
    	System.out.println("\t" + articleStringURI);*/
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
			boolean blankNode) throws RDFModelIOException { //TODO annotation model
		List<AnnotationE> lst = dao.insertAnnotations(ResourceConfig.BIOTEA_DATASET, AnnotationResourceConfig.getBaseURLAnnotator(this.annotator), this.lstAnnotations, fullPathName, format, empty, blankNode);
		int error = this.lstAnnotations.size() - lst.size();
		if (error != 0) {
			logger.info("==ERROR writing annotations NCBO== " + error + " annotations were not created, check the logs starting by 'Annotation not inserted' for more information");			
		}
		return lst;
	}

	@Override
	public List<AnnotationE> serializeToModel(Model model, AnnotationDAO dao,
			boolean blankNode) throws RDFModelIOException { //TODO annotation model
		List<AnnotationE> lst = dao.insertAnnotations(ResourceConfig.BIOTEA_DATASET, AnnotationResourceConfig.getBaseURLAnnotator(this.annotator), this.lstAnnotations, model, blankNode);
		int error = this.lstAnnotations.size() - lst.size();
		if (error != 0) {
			logger.info("==ERROR writing annotations NCBO== " + error + " annotations were not created, check the logs starting by 'Annotation not inserted' for more information");			
		}
		return lst;
	}

}