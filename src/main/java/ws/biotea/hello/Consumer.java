package ws.biotea.hello;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Consumer {
	public static void main(String[] args) throws IOException {
		URL url = new URL("http://localhost:8080/jena");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String inputLine, output = "";
		while ((inputLine = in.readLine()) != null) {
		    output += inputLine;
		}
		
		in.close();
		System.out.println(output);
	}
}
