/**
 * 
 */
package com.gatech.fhir.analytics.json;

import java.util.List;

/**
 * @author ybennani3
 *
 */
public class Resource {
	
	public String type;
	
	public List<Extension> extension = null;
	public List<Interaction> interaction = null;

}
