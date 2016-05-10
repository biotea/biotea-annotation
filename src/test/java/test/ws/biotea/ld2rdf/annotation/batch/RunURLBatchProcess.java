package test.ws.biotea.ld2rdf.annotation.batch;

import java.io.IOException;

public class RunURLBatchProcess {
	public static void main(String[] args) throws IOException {
		Runtime rt = Runtime.getRuntime();
		String classPath = "D:/workspace/biotea-annotation/target/classes/ws/biotea/ld2rdf/annotation/batch/BatchApplication";
		rt.exec("java " + classPath 
			+ ".class -in http://krono.act.uji.es/annotations/ "
			+ "-out d:/workspace/biotea-annotation/src/test/resources/ "
			+ "-remote d:/workspace/biotea-annotation/src/test/resources/idsToProcess.txt "
			+ "-annotator cma");
	}
}
