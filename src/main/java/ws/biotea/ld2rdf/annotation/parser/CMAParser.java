package ws.biotea.ld2rdf.annotation.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.ao.FoafAgent;
import ws.biotea.ld2rdf.rdf.model.ao.FoafDocument;
import ws.biotea.ld2rdf.rdf.model.ao.OffsetRangeTextSelector;
import ws.biotea.ld2rdf.rdf.model.ao.Selector;
import ws.biotea.ld2rdf.rdf.model.ao.Topic;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;
import ws.biotea.ld2rdf.util.ResourceConfig;
import ws.biotea.ld2rdf.util.annotation.AnnotationResourceConfig;
import ws.biotea.ld2rdf.util.annotation.Annotator;
import ws.biotea.ld2rdf.util.annotation.BioOntologyConfig;

public class CMAParser implements AnnotatorParser {
	Logger logger = Logger.getLogger(this.getClass());
	private static final String NO_RESPONSE = "\"<?xml version='1.0'?><response lexicon='umls2012-AB'>\\n</response>\\n\"";
	private boolean onlyUMLS;
	private boolean titleTwice;
	private boolean onlyTitleAndAbstract;
	private boolean withSTY;
	private final int titleWeight = 2;
	private List<String> noUmlsCui;
	private List<AnnotationE> lstAnnotations;
	
	private FoafAgent creator, author;
	private String annotator = "CMA";
	
	private String inputLocation;
	private boolean fromURL;

	/**
	 * Constructor.
	 * @param fromURL
	 * @param onlyUMLS
	 * @param titleTwice
	 * @param onlyTitleAndAbstract
	 * @param withSTY
	 */
	public CMAParser(Boolean fromURL, Boolean onlyUMLS, Boolean titleTwice, Boolean onlyTitleAndAbstract, Boolean withSTY) {
		this.fromURL = fromURL;
		if (this.fromURL) {
			this.inputLocation = Annotator.CMA.getServiceURI();
		}		
		this.onlyUMLS = onlyUMLS;
		this.titleTwice = titleTwice;
		this.onlyTitleAndAbstract = onlyTitleAndAbstract;
		this.withSTY = withSTY;
		
		this.noUmlsCui = new ArrayList<String>();
		this.noUmlsCui.add("C0000000");				
	}
	
	/**
	 * Initializes author and creator for parsed annotations.
	 * @throws URISyntaxException
	 * @throws MalformedURLException 
	 */
	public void init() throws URISyntaxException, MalformedURLException {
		if (this.author == null) {
			this.author = new FoafAgent();
			this.author.setId(new URI(AnnotationResourceConfig.getCMAAnnotationService()));				
		}		
		if (this.creator == null) {
			this.creator = new FoafAgent();
			this.creator.setId(new URI(ResourceConfig.BIOTEA_RDFIZATOR));
		}
	}
	/**
	 * Parses a CMA response in order to extract its annotations.
	 * Annotations provided by CMA look like: 
	 * <e id='PMC4246611.title.e1' src='UMLS' cui='C0000000' type='T043' grp='PHYS' offset='0' len='12' score='1.0000' idf='8'>Reactivation</e>
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(String documentId) throws IOException, URISyntaxException, NoResponseException {
		this.init();
						
		BufferedReader reader;
		if (this.fromURL) {	//TODO
			URL url;
			if (this.onlyTitleAndAbstract) {
				url = new URL(this.inputLocation + ResourceConfig.getTitleAbstractPrefix() + documentId);
			} else {
				url = new URL(this.inputLocation + ResourceConfig.getFullTextPrefix() + documentId);
			}
			reader = new BufferedReader(new InputStreamReader(url.openStream()));		
		} else {
			throw new IOException("DocumentId parser cannot be used if CMAParser has been configured to parse from files");
		}		
		this.parse(reader, documentId);
		try {
			reader.close();
		} catch (Exception e) {}
		return this.lstAnnotations;
	}
	
	/**
	 * Parses a CMA response in order to extract its annotations.
	 * Annotations provided by CMA look like: 
	 * <e id='PMC4246611.title.e1' src='UMLS' cui='C0000000' type='T043' grp='PHYS' offset='0' len='12' score='1.0000' idf='8'>Reactivation</e>
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(File file) throws IOException, NoResponseException, URISyntaxException {
		this.init();
		
		if (this.fromURL) {
			throw new IOException("File parser cannot be used if CMAParser has been configured to parse from a URL");
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));	
		String documentId;
		int extension = file.getName().lastIndexOf('.');
		if (extension != -1) {
			documentId = file.getName().substring(0, extension);
		} else {
			documentId = file.getName();
		}
		this.parse(reader, documentId);
		try {
			reader.close();
		} catch (Exception e) {}
		return this.lstAnnotations;
	}
	
	/**
	 * Parses the content of a reader.
	 * @param reader
	 * @param documentId
	 * @throws IOException
	 * @throws NoResponseException
	 * @throws URISyntaxException
	 */
	private void parse(BufferedReader reader, String documentId) throws IOException, NoResponseException, URISyntaxException {
		this.lstAnnotations = new ArrayList<AnnotationE>();
		String documentURL = ResourceConfig.getDocRdfUri(documentId);
		String response = reader.readLine();
		if (response.equals(NO_RESPONSE)) {
			throw new NoResponseException("No annotation has been retrieved for " + documentId + ", please verify that the provided id is valid in the selected database, i.e., pmc or pubmed, or try later");
		}
		while (response != null) {
			String[] splitLines = response.split("<e id='");
			if (splitLines.length > 1) {
				for (int i=1; i < splitLines.length ; i++) {
					String line = splitLines[i];
					try {
						int posInit = line.indexOf("cui=");
						int posEnd = line.indexOf("'", posInit+5);
						String cui = line.substring(posInit+5, posEnd);
						if (this.onlyUMLS) {
							if (this.noUmlsCui.contains(cui)) {
								continue;
							}
						}
						
						posInit = line.indexOf("idf=");
						posEnd = line.indexOf("'", posInit+5);
						double idf = Double.parseDouble(line.substring(posInit+5, posEnd));
						
						//in case we only want title and abstract
						boolean isTitleOrAbstract = false;
						//Titles may have a higher weight in the term frequency
						int addFrequency = 1;
						if (this.titleTwice || this.onlyTitleAndAbstract) {
							posInit = line.indexOf("id=");
							posEnd = line.indexOf("'", posInit+4);
							String section = line.substring(posInit+4, posEnd);
							if (this.titleTwice && (section.indexOf("title") != -1)) {
								addFrequency = this.titleWeight;
							}
							if (onlyTitleAndAbstract && ((section.indexOf("title") != -1) || (section.indexOf("abstract") != -1))) {
								isTitleOrAbstract = true;
							}
						}
						
						String sty = null;
						if (this.withSTY) {
							posInit = line.indexOf("type=");
							posEnd = line.indexOf("'", posInit+6);
							sty = line.substring(posInit+6, posEnd);
						}
						
						posInit = line.indexOf("offset=");
						posEnd = line.indexOf("'", posInit+8);
						int offset = Integer.parseInt(line.substring(posInit+8, posEnd));
						posInit = line.indexOf("len=");
						posEnd = line.indexOf("'", posInit+5);
						int range = Integer.parseInt(line.substring(posInit+5, posEnd));
						OffsetRangeTextSelector selector = new OffsetRangeTextSelector(null);
						selector.setOffset(offset);
						selector.setRange(range);
						
						posInit = line.indexOf('>');
						posEnd = line.lastIndexOf('<');
						String body = line.substring(posInit + 1, posEnd);
						//Store the annotation
						if ((onlyTitleAndAbstract && isTitleOrAbstract) || !onlyTitleAndAbstract) {
							AnnotationE annot = this.createAnnotation(body, documentURL, cui, sty, idf);
							annot.addContext(selector);
							
							int pos = lstAnnotations.indexOf(annot);
							if (pos != -1) {//annotation already exist
								annot = lstAnnotations.get(pos);
								boolean addInfo = true;
								for (Topic topic: annot.getTopics()) {
									if (topic.getURL().toString().endsWith(cui)) {
										//It is the same annotation, just another sty
										if (!topic.getUmlsType().contains(sty)) {
											topic.getUmlsType().add(sty);
										}
										addInfo = false;
									}
								}
								if (!addInfo) {
									for (Selector sel: annot.getContext()) {
										if (!sel.equals(selector)) {
											annot.getContext().clear();
											annot.getContext().add(selector);
											addInfo = true;
											break;
										}
									}
									
								}
								if (addInfo) {
									annot.setFrequency(annot.getFrequency() + addFrequency);
									annot.getBodies().add(body);
								}							
							} else {
								annot.setFrequency(addFrequency);
								lstAnnotations.add(annot);
							}						
						}
					} catch (StringIndexOutOfBoundsException e) {
						logger.error("==ERROR getting annotations CMA== " + line + " for article " + documentURL);
					} 
				}
			}
			response = reader.readLine();
		}		
		logger.debug(lstAnnotations);
	}
	
	/**
	 * Serializes annotations to a file.
	 * @param fileName
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 */
	public List<AnnotationE> serializeToFile(String fullPathName, RDFFormat format, AnnotationDAO dao, boolean empty, boolean blankNode) throws RDFModelIOException {
		return dao.insertAnnotations(ResourceConfig.BIOTEA_DATASET, AnnotationResourceConfig.getBaseURLAnnotator(this.annotator), this.lstAnnotations, fullPathName, format, empty, blankNode);
	}
	
	/**
	 * Serializes annotations to a model.
	 * @param model
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 */
	public List<AnnotationE> serializeToModel(Model model, AnnotationDAO dao, boolean blankNode) throws RDFModelIOException {
		return dao.insertAnnotations(ResourceConfig.BIOTEA_DATASET, AnnotationResourceConfig.getBaseURLAnnotator(this.annotator), this.lstAnnotations, model, blankNode);
	}
	
	/**
	 * Creates an annotation.
	 * @param body
	 * @param documentURL
	 * @param cui
	 * @param sty
	 * @param idf
	 * @return
	 * @throws URISyntaxException
	 */
	private AnnotationE createAnnotation(String body, String documentURL, String cui, String sty, double idf) throws URISyntaxException {
		AnnotationE annot = new AnnotationE();
		annot.setCreator(this.creator);
		annot.setAuthor(this.author);	
		annot.setCreationDate(Calendar.getInstance());
		annot.setIDF(idf);
		
		annot.getBodies().add(body);
		
		FoafDocument document = new FoafDocument();
		document.setId(new URI(documentURL));
		annot.setResource(document);
		
		Collection<Topic> topics = new ArrayList<Topic>();
		Topic topic = new Topic();
		topic.setNameSpace(new URI(BioOntologyConfig.getNS("UMLS")));  
		topic.setURL(new URI(BioOntologyConfig.getURL("UMLS") + cui));
		topic.getUmlsType().add(sty);
		topics.add(topic);
		annot.setTopics(topics);
		
		return annot;
	}

	/**
	 * @param inputLocation the inputLocation to set
	 */
	public void setInputLocation(String inputLocation) {
		this.inputLocation = inputLocation;
	}
}
