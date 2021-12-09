/**
 * 
 */
package com.gatech.fhir.analytics.json;

import java.util.List;

/**
 * @author ybennani3
 *
 */
public class CapabilityStatement {

	public String version;
	public String fhirVersion;
	public List<String> format;
	public List<Rest> rest = null;
}
