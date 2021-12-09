/**
 * 
 */
package com.gatech.fhir.analytics.web;

import java.util.HashMap;
import java.util.List;

/**
 * @author mzalmaariz
 *
 */
public class Types {
	
	public String status;
	public String identifier;
	public AccumulatedTypes accumulatedSdTypes; //Used by the UI
	public HashMap<String, List<String>> sdTypes; //Keeping for quick verification
	public HashMap<String, List<String>> csTypes;

}
