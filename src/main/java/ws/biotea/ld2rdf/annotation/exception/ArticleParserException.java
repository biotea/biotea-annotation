package ws.biotea.ld2rdf.annotation.exception;

public class ArticleParserException extends Exception {
	private static final long serialVersionUID = 1L;

	public ArticleParserException() {
	}

	public ArticleParserException(String arg0) {
		super(arg0);
	}

	public ArticleParserException(Throwable arg0) {
		super(arg0);
	}

	public ArticleParserException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ArticleParserException(String arg0, Throwable arg1,
			boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
