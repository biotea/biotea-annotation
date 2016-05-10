package ws.biotea.ld2rdf.util.annotation;

import org.apache.jena.riot.RDFFormat;

public class AnnotationResourceAdditionalConfig {	
    
    public static String getAnnotationCachingFileName(String id, Annotator annotator, RDFFormat format) {
    	String extension = format == RDFFormat.JSONLD ? ".json" : ".rdf";
    	return AnnotationResourceConfig.getAnnotationCachingPath(annotator) + id + extension;
    }
    
    
    public static String getAnnotationSavingFileName(String id, Annotator annotator, RDFFormat format) {
    	String extension = format == RDFFormat.JSONLD ? ".json" : ".rdf";
    	return AnnotationResourceConfig.getAnnotationSavingPath(annotator) + id + extension;
    }
}
