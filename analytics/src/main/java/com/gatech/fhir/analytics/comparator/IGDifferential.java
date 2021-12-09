/**
 * 
 */
package com.gatech.fhir.analytics.comparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author mzalmaariz
 * It holds the IDs ONLY found in the StructureDefinition of this IG
 *
 */
@JsonInclude(Include.NON_NULL)
public class IGDifferential {
	@JsonIgnore
	public String igname;
	public List<BaseElement> elements; //Record the IDs found only in this IG (igname)
	
	@JsonIgnore
	public IGDifferential(String igname) {
		this.igname = igname;
		elements = new ArrayList<>();
	}
	
	public BaseElement exists(String igname, BaseElement element) {
		for (BaseElement myElement : elements) {
			if (element.id.equals(myElement.id)) {
				//Compare MAX properties
				if (!StringUtils.equals(element.min, myElement.min)) {
					if (myElement.ig1 == null)
						myElement.ig1 = new ElementDifference();
					
					if (myElement.ig2 == null)
						myElement.ig2 = new ElementDifference();
					  
					myElement.ig1.min = myElement.min;
					myElement.ig2.min = element.min;
					
					myElement.min = null;
				}
				
				//Compare MAX properties
				if (!StringUtils.equals(element.max, myElement.max)) {
					if (myElement.ig1 == null)
						myElement.ig1 = new ElementDifference();
					
					if (myElement.ig2 == null)
						myElement.ig2 = new ElementDifference();
					
					myElement.ig1.max = myElement.max;
					myElement.ig2.max = element.max;
					
					myElement.max = null;
				}
				
				//Compare mustSupport properties
				boolean elementMustSupport = BooleanUtils.toBoolean(element.mustSupport);
				boolean myElementMustSupport = BooleanUtils.toBoolean(myElement.mustSupport);
				
				if (elementMustSupport ^ myElementMustSupport) {
					if (myElement.mustSupport != null) {
						if (myElement.ig1 == null)
							myElement.ig1 = new ElementDifference();
					
						myElement.ig1.mustSupport = myElement.mustSupport;
					}
					
					if (element.mustSupport != null) {
						if (myElement.ig2 == null)
							myElement.ig2 = new ElementDifference();
					
						myElement.ig2.mustSupport = element.mustSupport;
					}
					
					myElement.mustSupport = null;
				}
				
				HashMap<String, String> interactions = getIntersection(myElement.interactions, element.interactions);
				if (!MapUtils.isEmpty(myElement.interactions)) {
					if (myElement.ig1 == null)
						myElement.ig1 = new ElementDifference();
					
					myElement.ig1.interactions = myElement.interactions;
				}
					
				if (!MapUtils.isEmpty(element.interactions)) {
					if (element.ig2 == null)
						element.ig2 = new ElementDifference();
					
					element.ig2.interactions = element.interactions;
				}
				
				myElement.interactions = interactions;
					
				//}
				
				return myElement;
			}
		}
		
		return null;
	}
	
	@JsonIgnore
	private HashMap<String, String> getIntersection(HashMap<String, String> map1, HashMap<String, String> map2) {
		if (MapUtils.isEmpty(map1) || MapUtils.isEmpty(map2))
			return null;
		
		HashMap<String, String> intersection = new HashMap<>();
		for (String key : map1.keySet()) {
			if (map2.containsKey(key)) {
				String value1 = map1.get(key);
				String value2 = map2.get(key);
				
				if (StringUtils.equals(value1, value2)) {
					intersection.put(key, value1);
				}
			}
		}
		
		for (String key : intersection.keySet()) {
			map1.remove(key);
			map2.remove(key);
		}
		
		return intersection;
	}

}
