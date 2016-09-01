package ws.biotea.ld2rdf.annotation.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import ws.biotea.ld2rdf.annotation.exception.UnsupportedFormatException;
import ws.biotea.ld2rdf.util.annotation.Annotator;
import ws.biotea.ld2rdf.util.annotation.ConstantConfig;


public class BatchApplication {
	protected int poolSize;
    protected int maxPoolSize;
    protected long keepAliveTime;
    protected ThreadPoolExecutor threadPool = null;
    protected final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    private static final Logger LOGGER = Logger.getLogger(BatchApplication.class);
    /**
     * Default constructor, it defines an initial pool with 5 threads, a maximum of 10 threads,
     * and a keepAlive time of 300 seconds.
     */
	public BatchApplication() {
		this(10, 10, 300);
	}
	
	/**
     * Constructor with parameters, it enables definition of the initial pool size, maximum pool size,
     * and keep alive time in seconds; it initializes the ThreadPoolExecutor.
     * @param poolSize Initial pool size
     * @param maxPoolSize Maximum pool size
     * @param keepAliveTime Keep alive time in seconds
     */
    protected BatchApplication(int poolSize, int maxPoolSize, long keepAliveTime) {
    	this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);        
    }
    
    /**
     * Run a task with the thread pool and modifies the waiting queue list as needed.
     * @param task
     */
    protected void runTask(Runnable task) {
        threadPool.execute(task);
        LOGGER.debug("Task count: " + queue.size());
    }
    /**
     * Shuts down the ThreadPoolExecutor.
     */
    public void shutDown() {
        threadPool.shutdown();
    }

    /**
     * Informs whether or not the threads have finished all pending executions.
     * @return
     */
    public boolean isTerminated() {
    	//this.handler.getLogger().debug("Task count: " + queue.size());
        return this.threadPool.isTerminated();
    }

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();		

		String usage = "Usage: -in <input dir> (mandatory if input will be processed from a directory, "
				+ "not required if input will be process from annotator URL)"				
				+ "\n-out <output dir> (mandatory)"
				+ "\n-remote <document ids file> (mandatory if input will be processed from annotator URL, "
				+ "otherwise MUST NOT be used); "
				+ "if present is MUST provide a full path to a file with a document id list to be processed, "
				+ "one id per line"
				+ "\n-extension <file extension> (mandatory if the input is a directory, otherwise MUST NOT be used)"
				+ "\n-inStyle <either jats_file or rdf_file> (mandatory in the input is a "
				+ "directory and annotator is NCBO, otherwise MUST NOT be used. "
				+ "jats_file by default. If rdf_file only RDF/XML is accepted)"
				+ "\n-onto <either AO or OA> (AO annotation ontology or OA Open OpenAnnotation to model annotations, "
				+ "AO by default)"
				+ "\n-format <output-format> (optional, XML by default), "
				+ "either XML or JSON-LD, any other value will be dismissed and XML will be used"
				+ "\n-annotator <either cma or ncbo> (optional -ncbo by default), annotator"
				+ "\n-onlyTA (optional, false by default), if present, only title and abstract will be processed";
		if (args == null) {
			System.out.println(usage);
			System.exit(0);
		}
		//PropertyConfigurator.configure("log4j.properties");					
		
		int initPool = 10, maxPool = 10, keepAlive = 300;
		String inputDir = null, outputDir = null, idsFileLocation = null, extension = null;
		ConstantConfig inStyle = ConstantConfig.JATS_FILE, onto = ConstantConfig.AO;
		boolean onlyTA = false;
		Annotator annotator = Annotator.NCBO;
		RDFFormat format = RDFFormat.RDFXML_ABBREV;
		
		for (int i = 0; i < args.length; i++) {
			String str = args[i];
			if (str.equalsIgnoreCase("-in")) {
				inputDir = args[++i];
			} else if (str.equalsIgnoreCase("-out")) {
				outputDir = args[++i];
			} else if (str.equalsIgnoreCase("-extension")) {
				extension = args[++i];
			} else if (str.equalsIgnoreCase("-remote")) {
				idsFileLocation = args[++i];
			} else if (str.equalsIgnoreCase("-format")) {
				String fmt = args[++i];
				if (fmt.equalsIgnoreCase("JSON-LD")) {
					format = RDFFormat.JSONLD;
				}
			} else if (str.equalsIgnoreCase("-annotator")) {
				String annot = args[++i];
				try {
					annotator = Annotator.valueOf(annot.toUpperCase());
				} catch(IllegalArgumentException e) {
					annotator = Annotator.NCBO;
				}
			} else if (str.equalsIgnoreCase("-onlyTA")) {
				onlyTA = true;
			} else if (str.equalsIgnoreCase("-onto")) {
				String temp = args[++i].toUpperCase();
				try {
					onto = ConstantConfig.valueOf(temp);
				} catch(IllegalArgumentException e) {
					onto = ConstantConfig.AO;
				}				
			} else if (str.equalsIgnoreCase("-inStyle")) {
				String temp = args[++i].toUpperCase();
				try {
					inStyle = ConstantConfig.valueOf(temp);
				} catch(IllegalArgumentException e) {
					inStyle = ConstantConfig.JATS_FILE;
				}
			} else if (str.equalsIgnoreCase("-initPool")) {
				initPool = Integer.parseInt(args[++i]);
			} else if (str.equalsIgnoreCase("-maxPool")) {
				maxPool = Integer.parseInt(args[++i]);
			} else if (str.equalsIgnoreCase("-keepAlive")) {
				keepAlive = Integer.parseInt(args[++i]);
			}
		}
		
		if (
				((inputDir == null) && (idsFileLocation == null))
				|| ((inputDir != null) && (extension == null))
				|| (outputDir == null)
			) {
			System.out.println(usage);
			System.exit(0);
		}

		System.out.println("Execution variables: " +
				"\nInput " + inputDir + "\nOutput " + outputDir + 
				"\nIds document " + idsFileLocation + "\nInput Extension " + extension +
				"\nInput style " + inStyle + "\nOntology style " + onto + 
				"\nFormat " + format.getLang().getName() + "\nAnnotator " + annotator.name() +	
				"\nOnly title and abstract " + onlyTA + 
				"\nInitPool " + initPool + " MaxPool " + maxPool + " KeepAlive " + keepAlive);
		
		BatchApplication app = new BatchApplication(initPool, maxPool, keepAlive);
		app.parseInput(inputDir, outputDir, extension, idsFileLocation, format, annotator, onlyTA, onto, inStyle);
		app.shutDown();		
		while (!app.isTerminated()); //waiting
		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time: " + (endTime-startTime));
	}
	
	/**
	 * Parses the input parameters and place the output in the specified location.
	 * @param inputDir
	 * @param outputDir
	 * @param extension
	 * @param idsFileLocation
	 * @param format
	 * @param annotator
	 * @param onlyTA
	 */
	public void parseInput(String inputDir, String outputDir, String extension, String idsFileLocation, RDFFormat format, 
			Annotator annotator, boolean onlyTA, ConstantConfig onto, ConstantConfig inStyle) {
		if (extension == null) {
			this.parseURL(outputDir, idsFileLocation, format, annotator, onlyTA, onto);
		} else {
			this.parseDirectory(inputDir, outputDir, extension, format, annotator, onlyTA, onto, inStyle);
		}
	}
	/**
	 * Parses a directory.
	 * @param inputDir
	 * @param outputDir
	 * @param extension
	 * @param format
	 * @param annotator
	 * @param onlyTA
	 */
	private void parseDirectory(final String inputDir, final String outputDir, String extension, final RDFFormat format, 
			final Annotator annotator, final boolean onlyTA, final ConstantConfig onto, final ConstantConfig inStyle) {
		File dir = new File(inputDir); 
		final String dotExtension = "." + extension;
		int count = 1;
		for (final File file:dir.listFiles()) {			
			if (!file.isDirectory()) { //only one level
				if (file.getName().endsWith(dotExtension)) {
					this.runTask(new Runnable() {
		                public void run() {	
	                		AnnotationController controller = new AnnotationController();
							try {
								controller.annotatesFromFile(file, outputDir, format, annotator, onlyTA, onto, inStyle);
							} catch (UnsupportedFormatException e) {
								LOGGER.warn(file.getName() + " cannot be processed: " + e);
							}
							controller = null;		                	
		                }
		            });	
				}
				count++;
			}
			if (count % 1000 == 0) {
				System.gc();
			}
		}
	}
	/**
	 * Parses a URL
	 * @param inputURL
	 * @param outputDir
	 * @param idsFileLocation
	 * @param format
	 * @param annotator
	 * @param onlyTA
	 */
	private void parseURL(final String outputDir, String idsFileLocation, final RDFFormat format, final Annotator annotator, 
			final boolean onlyTA, final ConstantConfig onto) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(idsFileLocation));
			int count = 1;
			while (true) {				
				try {
					final String line = reader.readLine();
					if (line == null) {
				    	break;
				    }
				    
					this.runTask(new Runnable() {
		                public void run() {
		                	AnnotationController controller = new AnnotationController();
		                	try {
								controller.annotatesFromURL(outputDir, format, annotator, line, onlyTA, onto);
							} catch (UnsupportedFormatException e) {
								LOGGER.warn(line + " cannot be processed: " + e);
							}
		                	controller = null;
		                }
		            });
					
					count++;
					if (count % 1000 == 0) {
						System.gc();
					}
				} catch (IOException e) {
					LOGGER.warn("Document id in line " + count + " could not be processed. Cause: " + e);
				}			    
			}
			try {
				reader.close();
			} catch (Exception e) {}
		} catch (FileNotFoundException e1) {
			LOGGER.error(idsFileLocation + " cannot be processed. " + e1);
		} 		
	}

}
