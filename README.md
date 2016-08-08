# biotea-annotation
Refactorization for the annotation code at https://github.com/alexgarciac/biotea.
RDF annotation for PubMed and PMC using entity recognition tools such as the [NCBO Annotator](http://www.bioontology.org/annotator-service) (http://www.bioontology.org/annotator-service) and CMA (http://journal.sepln.org/sepln/ojs/ojs/index.php/pln/article/view/810/664). CMA is not a public service thus this documentation refers to annotations with [NCBO Annotator](http://www.bioontology.org/annotator-service)

## Dependencies
Most of the dependendies are configured with Maven. There is however a couple of local dependencies to [biotea-utilities](https://github.com/biotea/biotea-utilities) and [biotea-ao](https://github.com/biotea/biotea-ao).

#### How run this project using the batch option
* Clone [biotea-utilities](https://github.com/biotea/biotea-utilities)
* Clone [biotea-ao](https://github.com/biotea/biotea-ao)
* Clone this repository
* In your IDE, create a dependency from this project to [biotea-utilities](https://github.com/biotea/biotea-utilities) and [biotea-ao](https://github.com/biotea/biotea-ao)
* Modify configuration files, i.e., config.properties, in [biotea-utilities](https://github.com/biotea/biotea-utilities) resources folder (path-to-biotea-utilities/src/main/resources/config.properties). If you are generating annotations for RDFized articles with [biotea-rdfization](https://github.com/biotea/biotea-rdfization), make sure you use the same configuration there. Most of the time you only need to change the following properties:
  * biotea.dataset.prefix: Either pmc or pubmed
  * biotea.dataset: For instance dataset/pmc or dataset/pubmed or bio2rdf_dataset:bio2rdf-pmc-vrX or bio2rdf_dataset:bio2rdf-pubmed-vrX. This will be used in the VOiD properties of the generated dataset.
  * biotea.base: For instance biotea.ws or bio2rdf.org. This will be used to generate the URI to resources. bio2rdf will generate URIs compatible with Bio2RDF URI style.
  * ncbo.annotator.exclude: Aliases for those ontologies that should not be used by the [NCBO Annotator](http://www.bioontology.org/annotator-service). All the aliases are defined as properties at path-to-biotea-utilities/src/main/resources/ontologies.properties.
* Specify a valid API-KEY to use the [NCBO Annotator](http://www.bioontology.org/annotator-service) at path-to-biotea-utilities/src/main/resources/apikey.properties
* Make sure you include the [biotea-utilities](https://github.com/biotea/biotea-utilities) resources folder in your classpath
