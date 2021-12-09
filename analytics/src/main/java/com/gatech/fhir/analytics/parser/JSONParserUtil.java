/**
 * 
 */
package com.gatech.fhir.analytics.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author mzalmaariz
 *
 */
public class JSONParserUtil {
	
	public static <T> T getObject(String file, Class<T> returnClass) {
		
		//T response = null;
		try (InputStream is = new FileInputStream(new File(file))){
			JsonFactory jsonFactory = new JsonFactory();
			jsonFactory.configure(Feature.AUTO_CLOSE_TARGET,false);

			ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
			
			return objectMapper.readValue(is, returnClass);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

}
