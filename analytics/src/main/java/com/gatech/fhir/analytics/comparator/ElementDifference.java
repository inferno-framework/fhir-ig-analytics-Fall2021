package com.gatech.fhir.analytics.comparator;

import java.util.HashMap;

import org.apache.commons.collections4.MapUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ElementDifference {

	//public String name;   //Holds the name of IGs
	public String min;
	public String max;
	public String mustSupport;
	
	public HashMap<String, String> interactions;
	
	public void addIntoInteractions(String key, String value) {
		if (MapUtils.isEmpty(interactions))
			interactions = new HashMap<>();
		
		interactions.put(key, value);
	}
}
