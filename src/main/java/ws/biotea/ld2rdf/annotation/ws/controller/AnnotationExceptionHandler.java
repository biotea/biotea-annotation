/**
 * Original code at http://stackoverflow.com/questions/25356781/spring-boot-remove-whitelabel-error-page
 * Answer provided by http://stackoverflow.com/users/2116739/acohen
 */
package ws.biotea.ld2rdf.annotation.ws.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class AnnotationExceptionHandler implements ErrorController {	
	private final ErrorAttributes errorAttributes;
	private final String PATH = "/error";

    @Override
    public String getErrorPath() {
        return PATH;
    }
    
	
    @Autowired
    public AnnotationExceptionHandler(ErrorAttributes errorAttributes) {
    	if (errorAttributes == null) {
    		this.errorAttributes = new DefaultErrorAttributes();
    	} else {
    		this.errorAttributes = errorAttributes;
    	}      
    }

    @RequestMapping
    public Map<String, Object> error(HttpServletRequest aRequest){
      Map<String, Object> body = getErrorAttributes(aRequest,   
      getTraceParameter(aRequest));
      return body;
    }

    private boolean getTraceParameter(HttpServletRequest request) {
      String parameter = request.getParameter("trace");
      if (parameter == null) {
          return false;
      }
      return !"false".equals(parameter.toLowerCase());
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest aRequest, boolean includeStackTrace) {
      RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
      return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
}
