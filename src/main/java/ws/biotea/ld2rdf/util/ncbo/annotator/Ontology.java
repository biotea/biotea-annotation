package ws.biotea.ld2rdf.util.ncbo.annotator;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ws.biotea.ld2rdf.util.annotation.AnnotationResourceConfig;
import ws.biotea.ld2rdf.util.annotation.BioOntologyConfig;

public class Ontology {
	private static final Ontology instance = new Ontology();
	//private Logger logger = Logger.getLogger(Ontology.class);
	private Map<String,NCBOOntology> ncboOntologyMap= new HashMap<String, NCBOOntology>();
	private Map<String,AgroPortalOntology> agroportalOntologyMap= new HashMap<String, AgroPortalOntology>();
	private Ontology() {
		InputStream is = null;
        try {
        	Properties propFile = new Properties();
            is = this.getClass().getResourceAsStream("/ontologies.properties");
            propFile.load(is);
            for (Object key: propFile.keySet()) {
            	String prop = key.toString();
            	if (BioOntologyConfig.isNCBO(prop)) {
            		NCBOOntology onto = new NCBOOntology(BioOntologyConfig.getVirtualId(prop), BioOntologyConfig.getDescription(prop), BioOntologyConfig.getNS(prop), BioOntologyConfig.getURL(prop), BioOntologyConfig.getAcronym(prop));
            		this.ncboOntologyMap.put(prop, onto);
            	} else if(BioOntologyConfig.isAgroPortal(prop)){
            		AgroPortalOntology onto = new AgroPortalOntology(BioOntologyConfig.getVirtualId(prop), BioOntologyConfig.getDescription(prop), BioOntologyConfig.getNS(prop), BioOntologyConfig.getURL(prop), BioOntologyConfig.getAcronym(prop));
            		this.agroportalOntologyMap.put(prop, onto);
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
	}
	public static Ontology getInstance() {
		return instance;
	}
	public NCBOOntology getOntologyByURL(String url) {
		Map<String,NCBOOntology> combinedMap = new HashMap<String, NCBOOntology>();
		combinedMap.putAll(this.agroportalOntologyMap);
		combinedMap.putAll(this.ncboOntologyMap);
		for (NCBOOntology o: combinedMap.values()) {			
			if (url.startsWith(o.getURL())) {
				return (o);
			}
		}
		return null;
	}
	
	public String getAllVirtualIdFromNCBO(){
		return getAllVirtualOrAcronymFromNCBO(true, this.ncboOntologyMap);
	}
	
	public String getAllAcronymFromNCBO(){
		return getAllVirtualOrAcronymFromNCBO(false, this.ncboOntologyMap);
	}
	
	public String getAllVirtualIdFromAgroPortal(){
		return getAllVirtualOrAcronymFromNCBO(true, this.agroportalOntologyMap);
	}
	
	public String getAllAcronymFromAgroPortal(){
		return getAllVirtualOrAcronymFromNCBO(false, this.agroportalOntologyMap);
	}
	
	private String getAllVirtualOrAcronymFromNCBO(boolean virtual, Map<String,? extends NCBOOntology> ontologyMap) {
		String str = "";
		String[] includeOnly = AnnotationResourceConfig.getNCBOAnnotatorIncludeOnly();
		String[] exclude = AnnotationResourceConfig.getNCBOAnnotatorExclude();
		if (includeOnly == null) {
			for (String key: ontologyMap.keySet()) {
				if (exclude != null) {
					boolean excludeOnto = false;
					for (String no:exclude) {
						if (key.equals(no)) {
							excludeOnto = true;
							break;
						}
					}
					if (!excludeOnto) {
						if (virtual) {
							str += ontologyMap.get(key).getVirtualId() + ",";
						} else {
							str += ontologyMap.get(key).getAcronym() + ",";
						}
					}
				} else {
					if (virtual) {
						str += ontologyMap.get(key).getVirtualId() + ",";
					} else {
						str += ontologyMap.get(key).getAcronym() + ",";
					}
				}			
			}
		} else {
			for (String ns:includeOnly) {
				NCBOOntology onto = ontologyMap.get(ns);
				if (onto != null) { //ontology indeed exists
					if (exclude != null) {
						boolean excludeOnto = false;
						for (String no:exclude) {
							if (ns.equals(no)) {
								excludeOnto = true;
								break;
							}
						}
						if (!excludeOnto) {
							if (virtual) {
								str += onto.getVirtualId() + ",";
							} else {
								str += onto.getAcronym() + ",";
							}
						}
					} else {
						if (virtual) {
							str += onto.getVirtualId() + ",";
						} else {
							str += onto.getAcronym() + ",";
						}
					}	
				}
			}
		}
		str = str.substring(0, str.length()-1);
		return str;
	}
	
	public static void main(String[] args) {//1224,1053,1516,1352 no 1070
		Ontology o = new Ontology();
		System.out.println(o.getAllAcronymFromNCBO());
	}
	
}
