package com.gatech.fhir.analytics.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author skeshapragada3
 *
 */

public class Snapshot {
	
	@JsonProperty("element")
	public List<Element> elements;

}
