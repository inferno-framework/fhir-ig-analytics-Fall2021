/**
 * 
 */
package com.gatech.fhir.analytics.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mzalmaariz
 *
 */
public class Index {
	@JsonProperty("index-version")
	public int index_version  = 0;
	public List<Files> files = null;
}
