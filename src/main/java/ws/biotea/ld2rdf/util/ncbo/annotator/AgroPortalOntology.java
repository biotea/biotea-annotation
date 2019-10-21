package ws.biotea.ld2rdf.util.ncbo.annotator;

/**
 * Class representing ontologies from agroportal. Agroportal ontologies have the
 * same structure than NCBO (bioportal) ontologies
 * 
 * @author fabad
 *
 */
public class AgroPortalOntology extends NCBOOntology {

	public AgroPortalOntology(String virtualId, String description, String ns, String url, String acronym) {
		super(virtualId, description, ns, url, acronym);
	}

}
