/**
 * 
 */
package com.gatech.fhir.analytics.comparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gatech.fhir.analytics.json.Element;

/**
 * @author mzalmaariz
 *
 */


@JsonInclude(Include.NON_NULL)
public class IGComparator {
	@JsonIgnore
	private String ig1;
	@JsonIgnore
	private String ig2;
	private String type;  //SD Type of SD ID
	@JsonIgnore
	private List<BaseElement> ids;  //Hold the IGs found in both the IGs
	private HashMap<String, IGDifferential> igDifferentialMap = new HashMap<>();
	
	public String getType() {
		return type;
	}
	
	@JsonProperty("similarities")
	public List<BaseElement> getIds() {
		return ids;
	}
	
	@JsonProperty("differences")
	public HashMap<String, IGDifferential> getIgDifferentialMap() {
		return igDifferentialMap;
	}
	
	public void setBlankMap() {
		igDifferentialMap = null;
	}
	
	@JsonIgnore
	public IGComparator(String type, String ig1, String ig2) {
		this.type = type;
		this.ig1 = ig1;
		this.ig2 = ig2;
		this.ids = new ArrayList<>();
		this.igDifferentialMap.put(ig1, new IGDifferential(ig1));
		this.igDifferentialMap.put(ig2, new IGDifferential(ig2));
	}
	
	@JsonIgnore
	public boolean matches(String _type) {
		return type != null && type.equals(_type);
	}
	
	@JsonIgnore
	public void add(String igname, Element element) {
		IGDifferential igDifferential = igDifferentialMap.get(igname);
		if (igDifferential == null) {
			//Should Not come here
			System.out.println("ERROR: " + igname + " not found in differential map");
			return;
		}
		
		BaseElement sdElement = new BaseElement();
		sdElement.id = element.id;
		sdElement.min = element.min;
		sdElement.max = element.max;
		sdElement.mustSupport = element.mustSupport;
		
		
		BaseElement existsElement = elementExistsInAnotherIg(igname, sdElement);
		
		if (existsElement != null) {
			ids.add(existsElement);
		}
		else
			igDifferential.elements.add(sdElement);
		
	}
	
	@JsonIgnore
	public void add(String igname, String resourceType, HashMap<String, String> interactions) {
		IGDifferential igDifferential = igDifferentialMap.get(igname);
		if (igDifferential == null) {
			//Should Not come here
			System.out.println("ERROR: " + igname + " not found in differential map");
			return;
		}
		
		BaseElement csElement = new BaseElement();
		csElement.interactions = interactions;
		csElement.id = resourceType;
		
		BaseElement existsElement = elementExistsInAnotherIg(igname, csElement);
		
		if (existsElement != null) {
			ids.add(existsElement);
		}
		else
			igDifferential.elements.add(csElement);
		
	}
	
	
	@JsonIgnore
	private BaseElement elementExistsInAnotherIg(String igname, BaseElement sdElement) {
		for (String name : igDifferentialMap.keySet()) {
			if (name.equals(igname))
				continue;
			
			IGDifferential differential = igDifferentialMap.get(name);
			
			BaseElement _element = differential.exists(igname, sdElement);
			if (_element != null) {
				//Remove from differential as element exists in both IGs
				differential.elements.remove(_element);
				return _element;
			}
		}
		
		return null;
	}

}
