/**
 * 
 */
package com.gatech.fhir.analytics.comparator;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author mzalmaariz
 *
 */
@JsonInclude(Include.NON_NULL)
public class BaseElement {
	
	public String id;
	public String min;
	public String max;
	public String mustSupport;
	public HashMap<String, String> interactions;
	
	public ElementDifference ig1;
	public ElementDifference ig2;

}
