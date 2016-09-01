package ws.biotea.hello;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import ws.biotea.ld2rdf.rdf.persistence.ConnectionLDModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String index() {
        return "Greetings from Biotea annotation web services, powered by Spring Boot!";
    }
    
    @RequestMapping(value= "/test", method = RequestMethod.GET)
    public @ResponseBody void getModel(HttpServletRequest request, Writer responseWriter) {
    	System.out.println(RequestContextHolder.currentRequestAttributes());
    	System.out.println(RequestContextHolder.getRequestAttributes());

    	try {
    		ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel();   	
    		model.write(responseWriter);//, "JSON-LD"
    	} catch (Exception e) {
    		e.printStackTrace();
    		Model model = ModelFactory.createDefaultModel();
    		model.write(responseWriter);
		}
    }

}