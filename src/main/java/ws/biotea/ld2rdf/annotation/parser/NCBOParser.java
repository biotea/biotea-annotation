package ws.biotea.ld2rdf.annotation.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.InputException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.UnsupportedFormatException;
import ws.biotea.ld2rdf.annotation.model.ArticleElement;
import ws.biotea.ld2rdf.annotation.model.ContextParagraph;
import ws.biotea.ld2rdf.annotation.model.JATSArticle;
import ws.biotea.ld2rdf.annotation.model.NCBOAnnotation;
import ws.biotea.ld2rdf.annotation.model.PositionLocator;
import ws.biotea.ld2rdf.exception.DTDException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.ao.ElementSelector;
import ws.biotea.ld2rdf.rdf.model.ao.ExactQualifier;
import ws.biotea.ld2rdf.rdf.model.ao.FoafAgent;
import ws.biotea.ld2rdf.rdf.model.ao.FoafDocument;
import ws.biotea.ld2rdf.rdf.model.ao.Topic;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.AnnotationDAO;
import ws.biotea.ld2rdf.rdf.persistence.AnnotationDAOUtil;
import ws.biotea.ld2rdf.rdf.persistence.ConstantConfig;
import ws.biotea.ld2rdf.rdfGeneration.jats.GlobalArticleConfig;
import ws.biotea.ld2rdf.util.ClassesAndProperties;
import ws.biotea.ld2rdf.util.ResourceConfig;
import ws.biotea.ld2rdf.util.annotation.AnnotationResourceConfig;
import ws.biotea.ld2rdf.util.ncbo.annotator.Ontology;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.AnnotationCollection;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotations;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Empty;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Links;
import ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.LinksCollection;

public class NCBOParser implements AnnotatorParser {
	private static Logger logger = Logger.getLogger(NCBOParser.class);
	private List<AnnotationE> lstAnnotations;

	private FoafAgent creator, author;
	private String annotator = "NCBO";
	private static final String annotatorURL = AnnotationResourceConfig.getNCBOServiceURL(); // "http://rest.bioontology.org/obs/annotator";
	private static final String ontologiesToAnnotate = Ontology.getInstance().getAllAcronymFromNCBO(); // Ontology.getAllVirtualId();
	public static final String BASE_FOAF_NCBO_ANNOTATOR = AnnotationResourceConfig.getNCBOAnnotatorURL();// "http://bioportal.bioontology.org/annotator/";
	private final static String stopWords = AnnotationResourceConfig.getNCBOStopwords(); // http://www.ranks.nl/resources/stopwords.html
	private final static Pattern excludedSections = Pattern.compile(
			"([aA]cknowledgements)|([cC]ompeting[-]interests)|([aA]uthor)(s-|--|-s|)(-contributions)|([aA]bbreviations)"); // we
																															// are
																															// replacing
																															// spaces
																															// and
																															// other
																															// chars
																															// such
																															// as
																															// '
																															// by
																															// -

	private static final List<String> BIBO_DOCUMENT_TYPES_URL = Arrays.asList(ClassesAndProperties.BIBO_ACADEMIC_ARTICLE.getURLValue(),
																				ClassesAndProperties.BIBO_ARTICLE.getURLValue(),
																				ClassesAndProperties.BIBO_COLLECTION.getURLValue(),
																				ClassesAndProperties.BIBO_THESIS.getURLValue(),
																				ClassesAndProperties.BIBO_LETTER.getURLValue(),
																				ClassesAndProperties.BIBO_REPORT.getURLValue(),
																				ClassesAndProperties.BIBO_DOCUMENT.getURLValue(),
																				ClassesAndProperties.BIBO_BOOK.getURLValue(),
																				ClassesAndProperties.BIBO_PROCEEDINGS.getURLValue(),
																				ClassesAndProperties.BIBO_MANUAL.getURLValue(),
																				ClassesAndProperties.BIBO_MANUSCRIPT.getURLValue(),
																				ClassesAndProperties.BIBO_PATENT.getURLValue(),
																				ClassesAndProperties.BIBO_SPECIFICATION.getURLValue(),
																				ClassesAndProperties.BIBO_NOTE.getURLValue(),
																				ClassesAndProperties.BIBO_STANDARD.getURLValue());
	
	private StringBuffer articleURI;
	private String articleId;
	private JATSArticle article;

	// private String inputLocation;
	private boolean fromURL;
	private boolean onlyTitleAndAbstract;
	private ConstantConfig inStyle;

	public NCBOParser() {
	}

	/**
	 * Constructor.
	 * 
	 * @param fromURL
	 * @param onlyTitleAndAbstract
	 */
	public NCBOParser(Boolean fromURL, Boolean onlyTitleAndAbstract, ConstantConfig inStyle) {
		this.articleURI = new StringBuffer();
		this.lstAnnotations = new ArrayList<>();
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
	public List<AnnotationE> parse(String documentId)
			throws IOException, URISyntaxException, NoResponseException, ArticleParserException {
		throw new ArticleParserException("URL parsing currently not supported for NCBO");
		/*
		 * this.init();
		 * 
		 * this.articleId = documentId; File file = null; if (this.fromURL) {
		 * URL url; if (this.onlyTitleAndAbstract) { this.inputLocation = "";
		 * //pubmed url = new URL(this.inputLocation +
		 * ResourceConfig.getTitleAbstractPrefix() + documentId); } else {
		 * this.inputLocation = ""; //PMC-OA url = new URL(this.inputLocation +
		 * ResourceConfig.getFullTextPrefix() + documentId); } //file = new
		 * FileInputStream(url.openStream())); } else { throw new
		 * IOException("DocumentId parser cannot be used if NCBOParser has been configured to parse from files"
		 * ); } try { this.article.createArticleFromFile(file, this.articleURI,
		 * this.onlyTitleAndAbstract); } catch (InputException | DTDException |
		 * JAXBException e) { logger.fatal("File corresponding to "+ documentId
		 * + " could not be parsed"); throw new
		 * ArticleParserException("File corresponding to "+ documentId +
		 * " could not be parsed"); } this.parseParagraphs(); return
		 * this.lstAnnotations;
		 */
	}

	/**
	 * Parser a Biotea RDF file in order to annotate its content. Note: It only
	 * parses Biotea RDF files, Bio2RDF as well as any other mappings are not
	 * supported.
	 */
	@Override
	public List<AnnotationE> parse(File file)
			throws IOException, URISyntaxException, NoResponseException, ArticleParserException {
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
			logger.fatal("File corresponding to " + file.getAbsolutePath() + " could not be parsed");
			throw new ArticleParserException(
					"File corresponding to " + file.getAbsolutePath() + " could not be parsed");
		}
		return this.lstAnnotations;
	}

	private int parseRDFParagraph(Resource res, Property prop, String urlToAnnotate, int length,
			List<ContextParagraph> context, StringBuffer textToAnnotate) {
		try {
			String paragraphToAnnotate = res.getProperty(prop).getObject().toString();
			if ((paragraphToAnnotate != null) && (paragraphToAnnotate.length() != 0)) {
				paragraphToAnnotate = this.prepareParagraph(paragraphToAnnotate);
				length += paragraphToAnnotate.length();
				context.add(new ContextParagraph(urlToAnnotate == null ? -1 : length, urlToAnnotate));
				textToAnnotate.append(paragraphToAnnotate);
			}
			return length;
		} catch (Exception e) {
			return -1;
		}
	}

	private List<Resource> getBiboDocumentsExceptingReferences(Model model){
		Set<Resource> documents = new HashSet<Resource>();
		
		Property rdfType = model.getProperty(ClassesAndProperties.RDF_TYPE.getURLValue());
		String articleBaseUri = ResourceConfig.getBioteaURL(null) + "pmcdoc/" + ResourceConfig.getDatasetPrefix();
		for(String biboTypeUri : BIBO_DOCUMENT_TYPES_URL){
			Resource biboType = model.createResource(biboTypeUri);
			for(Resource res : model.listResourcesWithProperty(rdfType, biboType).toSet()){
				String uri = res.getURI();
				if(uri != null && uri.contains(articleBaseUri)){
					documents.add(res);
				}
			}
		}
		
		return new ArrayList<Resource> (documents);
	}
	
	private void parseRDFFile(File file) throws FileNotFoundException, ArticleParserException, URISyntaxException {
		List<ContextParagraph> context = new ArrayList<ContextParagraph>();
		StringBuffer textToAnnotate = new StringBuffer();
		int length = 0;

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

		/* Get the root document that contains the sections */
		List<Resource> biboDocuments = this.getBiboDocumentsExceptingReferences(model);
		if (biboDocuments == null || biboDocuments.isEmpty()){
			throw new ArticleParserException("No bibo document present in " + file);
		} else if(biboDocuments.size() > 1){
			logger.warn(String.format("More than one document found in '%s' file. %s selected.", file, biboDocuments.get(0)));
		}
		Resource res = biboDocuments.get(0);
		this.articleURI.delete(0, articleURI.length());
		this.articleURI.append(res.getURI().toString());
		articleStringURI = this.articleURI.toString();
		this.articleId = GlobalArticleConfig.getArticleIdFromRdfUri(ResourceConfig.getBioteaBase(null),
				articleStringURI);

		int temp = parseRDFParagraph(res, titleProp, null, length, context, textToAnnotate);
		if (temp != -1) {
			length = temp;
		}

		Resource sectionClass = model.createResource(ClassesAndProperties.DOCO_SECTION.getURLValue());
		ResIterator resItr = model.listResourcesWithProperty(rdfType, sectionClass);
		while (resItr.hasNext()) {
			res = resItr.next();
			String urlToAnnotate = res.getURI().toString();
			temp = parseRDFParagraph(res, titleProp, urlToAnnotate, length, context, textToAnnotate);
			if (temp != -1) {
				length = temp;
			} else {
				continue;
			}
		}

		Property textProp = model.getProperty(ClassesAndProperties.TEXT_PROPERTY);
		resItr = model.listResourcesWithProperty(textProp);
		while (resItr.hasNext()) {
			res = resItr.next();
			Matcher matcher = NCBOParser.excludedSections.matcher(res.getURI().toString());
			if (matcher.find()) {
				continue; // excluded sections will not be annotated
			} else {
				// paragraph by paragraph
				String urlToAnnotate = res.getURI().toString();
				temp = parseRDFParagraph(res, textProp, urlToAnnotate, length, context, textToAnnotate);
				if (temp != -1) {
					length = temp;
				} else {
					continue;
				}
			}
		}

		boolean writeDown = annotateWithNCBO(textToAnnotate.toString(), context, articleStringURI);
		if (!writeDown) {
			logger.warn("- WARNING SUBTITLE - NCBO annotations for " + this.articleId);
		}
	}

	private String prepareParagraph(String paragraph) throws UnsupportedEncodingException {
		String para = paragraph.replaceAll("[^\\p{Alpha}\\p{Z}\\p{P}\\p{N}]", "_");
		para = URLEncoder.encode(para, ResourceConfig.UTF_ENCODING);
		para = para.replace("+", " ");
		return para;
	}

	private void parseParagraphs() {
		List<ContextParagraph> context = new ArrayList<ContextParagraph>();
		StringBuffer textToAnnotate = new StringBuffer();
		int length = 0;

		String articleStringURI = this.articleURI.toString();
		for (ArticleElement element : this.article.getElements()) {
			// paragraph by paragraph
			String paragraphToAnnotate = element.getText();
			String urlToAnnotate = element.getIdentifier();
			try {
				if ((paragraphToAnnotate != null) && (paragraphToAnnotate.length() != 0)) {
					paragraphToAnnotate = this.prepareParagraph(paragraphToAnnotate);
					length += paragraphToAnnotate.length();
					context.add(new ContextParagraph(length, urlToAnnotate));
					textToAnnotate.append(paragraphToAnnotate);
				}
			} catch (UnsupportedEncodingException uee) {
				// we annotate as much as we can, some paragraphs can be omitted
				logger.warn("WARNING PARAGRAPH - NCBO annotations for " + this.articleId + "(" + urlToAnnotate
						+ ") could not be processed");
			}
		}
		boolean writeDown = annotateWithNCBO(textToAnnotate.toString(), context, articleStringURI);
		if (!writeDown) {
			logger.warn("WARNING NCBO annotations for " + articleStringURI + " could not be processed");
		}
		logger.info("===SECTIONS ANNOTATED=== " + this.articleId);
	}

	/**
	 * Annotate a short paragraph corresponding only to one context.
	 */
	private boolean annotateWithNCBO(String text, List<ContextParagraph> urlContext, String articleStringURI) {

		// System.out.println("TO ANNOT: " + urlContext + "\n" + paragraph);
		HttpClient client = new HttpClient();
		
		/* Set this string for your application */
		client.getParams().setParameter(HttpMethodParams.USER_AGENT, "Annotator Client Scientific Publications");

		PostMethod method = new PostMethod(annotatorURL);
		try {
			// Configure the form parameters
			method.addParameter("stop_words", stopWords);
			method.addParameter("minimum_match_length", "3");
			method.addParameter("ontologies", ontologiesToAnnotate);
			method.addParameter("text", text);
			method.addParameter("format", "xml"); // Options are 'text', 'xml',
													// 'tabDelimited'
			method.addParameter("apikey", AnnotationResourceConfig.getNCBOAPIKey());

			// Execute the POST method
			int statusCode = client.executeMethod(method);

			if (statusCode != -1) {
				try {
					InputStream annotatedParagraph = method.getResponseBodyAsStream();
					if (annotatedParagraph != null) {
						// Reader reader = new StringReader(annotatedParagraph);
						JAXBContext jc = JAXBContext
								.newInstance("ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated");
						Unmarshaller unmarshaller = jc.createUnmarshaller();
						AnnotationCollection xml;
						Object obj = new Object();
						try {
							obj = unmarshaller.unmarshal(annotatedParagraph);
							if (obj instanceof Empty) {
								return true; // no annotations were found but
												// everything was ok with the
												// response
							}
							xml = (AnnotationCollection) obj; // otherwise,
																// AnnotationCollection
																// should be the
																// unmarshalled
																// object
						} catch (Exception e) {
							logger.fatal("- FATAL DTD ERROR ANNOTATOR - NCBO annotations for " + this.articleId
									+ " cannot be unmarshalled: " + e.getMessage() + " - class: " + obj.getClass());
							return false;
						}
						logger.debug("---Annotations unmarshalled---");
						List<NCBOAnnotation> lstNCBOAnnotations = new ArrayList<>();
						List<ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotation> lstAnn = xml
								.getAnnotation();
						for (ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Annotation ann : lstAnn) {
							String fullId = ann.getAnnotatedClass().getId();
							String ontologyAcronym = this.getOntologyAcronym(ann.getAnnotatedClass().getLinksCollection());
							List<Annotations> lstRetrievedAnnotations = ann.getAnnotationsCollection().getAnnotations();
							for (Annotations annot : lstRetrievedAnnotations) {
								NCBOAnnotation ncboAnnot = new NCBOAnnotation(annot.getText());
								int pos = lstNCBOAnnotations.indexOf(ncboAnnot);
								if (pos != -1) {
									ncboAnnot = lstNCBOAnnotations.get(pos);
								} else {
									lstNCBOAnnotations.add(ncboAnnot);
								}
								ncboAnnot.getAnnotatedClassIds().add(fullId);
								ncboAnnot.getSourceOntologies().add(ontologyAcronym);
								ncboAnnot.getAnnotationFromTo()
										.add(new PositionLocator(annot.getFrom().intValue(), annot.getTo().intValue()));
							}
						}

						mergeAnnotations(lstNCBOAnnotations, articleStringURI, urlContext);
					}
				} catch (Exception e) {
					// e.printStackTrace();
					logger.info("===ERROR NCBO Annotator (" + this.articleId + ")=== " + e.getMessage());
					return false;
				}
			} else {
				logger.info("===ERROR??? NCBO Annotator (" + this.articleId + ")=== (status code != 1) ");
				return false;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			logger.info("===ERROR NCBO Annotator (" + this.articleId + " at " + urlContext + ")=== (http call)"
					+ e.getMessage());
			return false;
		} finally {
			if (method != null){
				method.releaseConnection();
			}
			if(client != null){
				SimpleHttpConnectionManager connManager = (SimpleHttpConnectionManager) client.getHttpConnectionManager();
				if (connManager != null){
					connManager.shutdown();
				}
			}
			method = null;
		}
		return true;
	}

	private String getOntologyAcronym(LinksCollection linksCollection) {
		String ontologyAcronym = null;
		for (Links links : linksCollection.getLinks()){
			ws.biotea.ld2rdf.util.ncbo.annotator.jaxb.newgenerated.Ontology ontology = links.getOntology();
			if (ontology != null){
				ontologyAcronym = new File(URI.create(ontology.getHref()).getPath()).getName();
			}
		}
		return ontologyAcronym;
	}

	private String getURLContext(List<ContextParagraph> urlContext, int lastPosition) {
		for (ContextParagraph context : urlContext) {
			if (lastPosition <= context.getLastPosition()) {
				return context.getContextURL();
			}
		}
		return null;
	}

	private void mergeAnnotations(List<NCBOAnnotation> lstNCBOAnnotations, String articleStringURI,
			List<ContextParagraph> allContext) throws URISyntaxException {
		for (NCBOAnnotation ncboAnnot : lstNCBOAnnotations) {
			// annot: creator, body, resource, date
			ExactQualifier annot = new ExactQualifier();
			annot.setAuthor(this.author);
			annot.setCreator(this.creator);
			annot.getBodies().add(ncboAnnot.getAnnotationText());
			FoafDocument document = new FoafDocument();
			document.setUri(new URI(articleStringURI));
			annot.setResource(document);
			annot.setDocumentID(this.articleId);
			annot.setCreationDate(Calendar.getInstance());
			annot.setFrequency(ncboAnnot.getFrequency());

			for (int i = 0; i < ncboAnnot.getAnnotatedClassIds().size(); i++ ) {	
				String fullId = ncboAnnot.getAnnotatedClassIds().get(i);
				String partialId = fullId.substring(Math.max(fullId.lastIndexOf("/"), fullId.lastIndexOf("#")) + 1);
				String sourceOntologyAcronym = ncboAnnot.getSourceOntologies().get(i);
				
				// topics
				Topic topic = new Topic();
				topic.setNameSpace(new URI(sourceOntologyAcronym + ":" + partialId));
				topic.setURL(new URI(fullId));
				topic.setSourceOntology(sourceOntologyAcronym);
				annot.addTopic(topic);
			}

			// Go to the annotations table
			if (annot != null) {
				int pos = this.lstAnnotations.indexOf(annot);
				if (pos != -1) {
					AnnotationE a = this.lstAnnotations.get(pos);
					for (PositionLocator locator : ncboAnnot.getAnnotationFromTo()) {
						String urlContext = this.getURLContext(allContext, locator.getTo());
						if (urlContext != null) {
							ElementSelector ses = new ElementSelector(a.getResource());
							ses.setElementURI(urlContext);
							if (!a.getContext().contains(ses)) {
								a.addContext(ses);
								a.setFrequency(a.getFrequency() + annot.getFrequency());
							}
						}
					}
				} else {
					for (PositionLocator locator : ncboAnnot.getAnnotationFromTo()) {
						String urlContext = this.getURLContext(allContext, locator.getTo());
						if (urlContext != null) {
							// context (selector)
							ElementSelector ses = new ElementSelector(annot.getResource());
							ses.setElementURI(urlContext);
							annot.addContext(ses);
						}
					}
					this.lstAnnotations.add(annot);
				}
			}
		}
	}

	@Override
	public List<AnnotationE> serializeToFile(String fullPathName, RDFFormat format, String base, ConstantConfig onto,
			boolean empty, boolean blankNode) throws RDFModelIOException, UnsupportedFormatException {
		List<AnnotationE> lst = null;
		try {
			AnnotationDAO dao = AnnotationDAOUtil.getDAO(base, onto);
			lst = dao.insertAnnotations(base, AnnotationResourceConfig.getBaseURLAnnotator(base, this.annotator),
					this.lstAnnotations, fullPathName, format, empty, blankNode);
			int error = this.lstAnnotations.size() - lst.size();
			if (error != 0) {
				logger.info("==ERROR writing annotations NCBO== " + error
						+ " annotations were not created, check the logs starting by 'OpenAnnotation not inserted' for more information");
			}
		} catch (Exception e) {
			logger.error("===ERROR=== Annotations for " + this.articleId + " with base " + base + " not serialized: "
					+ e.getMessage());
		}
		return lst;
	}

	@Override
	public List<AnnotationE> serializeToModel(Model model, String base, ConstantConfig onto, boolean blankNode)
			throws RDFModelIOException, UnsupportedFormatException {
		List<AnnotationE> lst = null;
		try {
			AnnotationDAO dao = AnnotationDAOUtil.getDAO(base, onto);
			lst = dao.insertAnnotations(base, AnnotationResourceConfig.getBaseURLAnnotator(base, this.annotator),
					this.lstAnnotations, model, blankNode);
			int error = this.lstAnnotations.size() - lst.size();
			if (error != 0) {
				logger.info("==ERROR writing annotations NCBO== " + error
						+ " annotations were not created, check the logs starting by 'OpenAnnotation not inserted' for more information");
			}
		} catch (Exception e) {
			logger.error("===ERROR=== Annotations for " + this.articleId + " with base " + base + " not serialized: "
					+ e.getMessage());
		}
		return lst;
	}

	@Override
	public String getArticleId() {
		return this.articleId;
	}

}