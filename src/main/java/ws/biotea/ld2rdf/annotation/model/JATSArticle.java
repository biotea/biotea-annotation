package ws.biotea.ld2rdf.annotation.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import pubmed.openAccess.jaxb.generated.Abstract;
import pubmed.openAccess.jaxb.generated.Article;
import pubmed.openAccess.jaxb.generated.Bold;
import pubmed.openAccess.jaxb.generated.Italic;
import pubmed.openAccess.jaxb.generated.ListItem;
import pubmed.openAccess.jaxb.generated.NamedContent;
import pubmed.openAccess.jaxb.generated.P;
import pubmed.openAccess.jaxb.generated.Sc;
import pubmed.openAccess.jaxb.generated.Sec;
import pubmed.openAccess.jaxb.generated.Sub;
import pubmed.openAccess.jaxb.generated.Sup;
import pubmed.openAccess.jaxb.generated.SupplementaryMaterial;
import pubmed.openAccess.jaxb.generated.Underline;
import ws.biotea.ld2rdf.annotation.exception.InputException;
import ws.biotea.ld2rdf.exception.DTDException;
import ws.biotea.ld2rdf.rdfGeneration.jats.GlobalArticleConfig;
import ws.biotea.ld2rdf.util.Conversion;
import ws.biotea.ld2rdf.util.ResourceConfig;

public class JATSArticle {
	private static Logger logger = Logger.getLogger(JATSArticle.class);
	private List<ArticleElement> elements;
	private GlobalArticleConfig global;
	private int elementCount = 1;
	
	public JATSArticle() {
		this.elements = new ArrayList<>();
	}
	
	public List<ArticleElement> getElements() {
		return this.elements;
	}
	
	public String createArticleFromFile(File paper, StringBuffer articleURI, boolean onlyTitleAndAbstract) throws InputException, DTDException, JAXBException {
		String articleId = "";
		final String PREFIX = ResourceConfig.getDatasetPrefix().toUpperCase();
		
		Article article;
		JAXBContext jc = JAXBContext.newInstance("pubmed.openAccess.jaxb.generated");
		Unmarshaller unmarshaller = jc.createUnmarshaller(); 	
		try {
			article = (Article) unmarshaller.unmarshal(paper);
		} catch (Exception e) {
			logger.fatal("- FATAL DTD ERROR - " + paper.getName() + " cannot be unmarshalled: " + e.getMessage());
			throw new DTDException (e);
		}
		for(pubmed.openAccess.jaxb.generated.ArticleId id: article.getFront().getArticleMeta().getArticleIds()) {
			if (id.getPubIdType().equals(ResourceConfig.getIdTag())) {
				articleId = id.getContent();
				break;
			} 				
		}
		if (articleId == null) {
			throw new InputException("No " + PREFIX + " id was found, file cannot be processed");
		}
		
		this.global = new GlobalArticleConfig(articleId);	
		articleURI.delete(0, articleURI.length());
		articleURI.append(GlobalArticleConfig.getArticleRdfUri(articleId));
		
		//Title
    	try {    		
    	    String title = "";
    		for (Object ser: article.getFront().getArticleMeta().getTitleGroup().getArticleTitle().getContent()) {
    			if (ser instanceof String) {
    				title += ser.toString();
    			} else if (ser instanceof JAXBElement<?>) {
    				JAXBElement<?> elem = (JAXBElement<?>)ser;
    				title += processElement(elem);
    			}			
    		}
    		this.elements.add(new ArticleElement(null, title));
    	} catch (Exception e) {
    		logger.info(paper + ": Article title not processed");
    	}
    	
    	String docAbstract = "";
		for (Abstract ab: article.getFront().getArticleMeta().getAbstracts()) {
			docAbstract += processAbstractAsSection(ab);			
		}
		String title = "Abstract";		
		String[] params = {title};
		String paragraphURI = Conversion.replaceParameter(this.global.BASE_URL_PARAGRAPH, params) + "1";
		this.elements.add(new ArticleElement(paragraphURI, docAbstract));
		
		if (!onlyTitleAndAbstract) {
			//process not-in-section-paragraphs
			Iterator<Object> itrPara = article.getBody().getAddressesAndAlternativesAndArraies().iterator();			
			processElementsInSection("undefined-section", itrPara);
			
			//process sections
			for (Sec section:article.getBody().getSecs()) {										
				processSection(section, null);
			}
		}		
		
		return articleId;
	}
	
	private String processElement(Object articleElem) {
		if (articleElem instanceof String) {
			return articleElem.toString();
		} else if (articleElem instanceof NamedContent) {
			try {
				String str = ((NamedContent)articleElem).getContent().get(0).toString();
				if (str.startsWith("pubmed.openAccess.jaxb.generated")) {
					return processListOfElements(((NamedContent)articleElem).getContent());
				} else {
					return str;
				}
			} catch (Exception e) {
				return "";
			}	
		}
		if (articleElem instanceof Sc) {
			try {
				String str = ((Sc)articleElem).getContent().get(0).toString();
				if (str.startsWith("pubmed.openAccess.jaxb.generated")) {
					return processListOfElements(((Sc)articleElem).getContent());
				} else {
					return str;
				}
			} catch (Exception e) {
				return "";
			}	
		} else if (articleElem instanceof Bold) {
			try {
				String str = ((Bold)articleElem).getContent().get(0).toString();
				if (str.startsWith("pubmed.openAccess.jaxb.generated")) {
					return processListOfElements(((Bold)articleElem).getContent());
				} else {
					return str;
				}
			} catch (Exception e) {
				return "";
			}			
		} else if (articleElem instanceof Italic) {
			try {
				String str = ((Italic)articleElem).getContent().get(0).toString();
				if (str.startsWith("pubmed.openAccess.jaxb.generated")) {
					return processListOfElements(((Italic)articleElem).getContent());
				} else {
					return str;
				}
			} catch (Exception e) {
				return "";
			}			
		} else if (articleElem instanceof Underline) {
			try {
				String str = ((Underline)articleElem).getContent().get(0).toString();
				if (str.startsWith("pubmed.openAccess.jaxb.generated")) {
					return processListOfElements(((Underline)articleElem).getContent());
				} else {
					return str;
				}
			} catch (Exception e) {
				return "";
			}					
		} else if (articleElem instanceof pubmed.openAccess.jaxb.generated.List) {
			pubmed.openAccess.jaxb.generated.List list = (pubmed.openAccess.jaxb.generated.List)articleElem;
			String str = "";
			for (Object object: list.getListItemsAndXS()) {
				if (object instanceof ListItem) {
					ListItem listItem = (ListItem)object;
					for (Object item: listItem.getPSAndDefListsAndLists()) {
						str += "(*) " + processElement(item) + ". "; 
					}
				}
			}
			return (str);
		} else if (articleElem instanceof pubmed.openAccess.jaxb.generated.P) {
			pubmed.openAccess.jaxb.generated.P paragraph = (pubmed.openAccess.jaxb.generated.P)articleElem;
			String str = "";
			for (Object p: paragraph.getContent()) {
				str += processElement(p);
			}
			return str;
		} else if (articleElem instanceof Sup) {
			Sup sup = (Sup)articleElem;
			String str = "";
			for (Object object: sup.getContent()) {
				str += processElement(object);
			}
			return str;			
		} else if (articleElem instanceof Sub) {
			Sub sub = (Sub)articleElem;
			String str = "";
			for (Object object: sub.getContent()) {
				str += processElement(object);
			}
			return str;			
		} else if (articleElem instanceof JAXBElement<?>) {
			JAXBElement<?> elem = (JAXBElement<?>)articleElem;
			if (elem.getValue() instanceof String) {
				return elem.getValue().toString();
			} else if (elem.getValue() instanceof List<?>){
				String str = "";
				List<?> lst = (List<?>) elem;
				for (Object elemLst: lst){
					if (elemLst instanceof String) {
						str += elemLst.toString();
					} else {
						str += processElement((JAXBElement<?>)elemLst);
					}
				}
				return (str);
			} else if (elem.getValue() instanceof JAXBElement){
				@SuppressWarnings("rawtypes")
				JAXBElement internal = (JAXBElement) elem; 
				return (internal.getValue().toString());
			} else {
				return (elem.toString());
			}
		} else {
			return "";
		}
	}
	
	/**
	 * Processes a list of objects.
	 * @param list
	 * @return
	 */
	private String processListOfElements(List<Object> list){
		String str = "";
		for (Object obj: list) {
			str += processElement(obj);
		}
		return str;
	}

	/**
	 * Processes the abstract as a section.
	 * @param ab
	 * @param model
	 * @param document
	 * @param parent
	 */
	private String processAbstractAsSection(Abstract ab) {		
		Iterator<Object> itrPara = ab.getAddressesAndAlternativesAndArraies().iterator();		
		String text = "";
		if (itrPara.hasNext()){
			Object obj = itrPara.next();
			if (obj instanceof P) {				
				text += processParagraphInAbstract((P)obj);				
			} 	    	
	    }
		Iterator<Sec> itrSec = ab.getSecs().iterator();
		while (itrSec.hasNext()) {
			Sec sec = itrSec.next();
			try {
				text += sec.getTitle().getContent().get(0).toString() + ": ";
				Iterator<Object> itrParaSec = sec.getAddressesAndAlternativesAndArraies().iterator();		
				if (itrParaSec.hasNext()){
					Object obj = itrParaSec.next();
					if (obj instanceof P) {				
						text += processParagraphInAbstract((P)obj) + " ";				
					} 	    	
			    }
			} catch (Exception e) {;}
		}		
		
		return text;
	}
	
	/**
	 * Processes a paragraph within the abstract.
	 * @param para Paragraph to be processed
	 * @return
	 */
	private String processParagraphInAbstract(P para) {
		String text = "";

		for (Object paraObj: para.getContent()) {
			String str = "";
			if (paraObj instanceof String) {
				str = paraObj.toString();
			} else {
				str = processElement(paraObj);
			}			 
			text += str + " ";
		}
		return text;
	}

	private void processElementsInSection(String titleInURL, Iterator<Object> itrPara) {
		int countPara = 0;
		while (itrPara.hasNext()){
			Object obj = itrPara.next(); 
			if (obj instanceof P) {
				countPara++;
				processParagraph(titleInURL, (P)obj, countPara);
			} else if (obj instanceof SupplementaryMaterial) {
				SupplementaryMaterial supp = (SupplementaryMaterial)obj;				
				for (Object o: supp.getDispFormulasAndDispFormulaGroupsAndChemStructWraps()) {
					if (o instanceof P) {
						countPara++;
						processParagraph(titleInURL, (P)o, countPara);
					}
				}
			}	
	    }
	}
	
	/**
	 * Processes one section.
	 * @param section
	 * @param model
	 * @param document
	 * @param parent
	 */
	private void processSection(Sec section, String parentTitleInURL) {
		//Title		
		String title = "";
		try {
			title = processListOfElements(section.getTitle().getContent());
		} catch (NullPointerException npe) {
			if (section.getSecType() == null) {
				title = section.getId() == null ? "id_" + (elementCount++): section.getId();				
			} else {
				title = section.getId() == null ? "id_" + (elementCount++): section.getId();
				if (!section.getSecType().equalsIgnoreCase("supplementary-material")) {
					title += section.getSecType() == null ? "secType_" + (elementCount++) : section.getSecType();
				} else {
					title = null;
				}
			}
		}
				
		if ((title == null) || (title.length() == 0)) {
			title = "no-title_" + (elementCount++);
		}
		String titleInURL = title.replaceAll(ResourceConfig.CHAR_NOT_ALLOWED, "-");
		if (titleInURL.length() == 0) {
			title = section.getId();
			titleInURL = title.replaceAll(ResourceConfig.CHAR_NOT_ALLOWED, "-");
		}
		
		String sectionURI;
		if (parentTitleInURL == null) {
			sectionURI = global.BASE_URL_SECTION + titleInURL;
		} else {
			sectionURI = global.BASE_URL_SECTION + parentTitleInURL + "_" + titleInURL;
		}
		
		this.elements.add(new ArticleElement(sectionURI, titleInURL));

		//process paragraphs
		Iterator<Object> itrPara = section.getAddressesAndAlternativesAndArraies().iterator();	
		if (parentTitleInURL == null) {
			processElementsInSection(titleInURL, itrPara);
		} else {
			processElementsInSection(parentTitleInURL + "_" + titleInURL, itrPara);
		}
				
		//process sections
		Iterator<Sec> itr = section.getSecs().iterator();
		while (itr.hasNext()){
			Sec sec = itr.next();
			if (parentTitleInURL == null) {
				processSection(sec, titleInURL);
			} else {
				processSection(sec, parentTitleInURL + "_" + titleInURL);
			}
			
	    }
	}
	
	private void processParagraph(String titleInURL, P para, int countPara) {
		String[] params = {titleInURL};
		String paragraphURI = Conversion.replaceParameter(global.BASE_URL_PARAGRAPH, params) + countPara;					
		String text = "";
		
		for (Object paraObj: para.getContent()) {
			String str = processElement(paraObj); 
			text += str;
		}		
		this.elements.add(new ArticleElement(paragraphURI, text));		
	}
}
