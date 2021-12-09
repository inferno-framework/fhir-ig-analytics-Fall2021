/**
 * 
 */
package com.gatech.fhir.analytics.comparator;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author mzalmaariz
 *
 */
@JsonInclude(Include.NON_NULL)
public class IGSComparator {
	
	private String status;
	private String identifier;
	private String ig1;
	private String ig2;
	private List<IGComparator> sds; //Hold comparison for structure definitions
	private List<IGComparator> ccss; //Hold client comparison for capability statements
	private List<IGComparator> scss; //Hold server comparsion for capability statements
	
	private HashMap<String, HashMap<String, HashMap<String, String>>> capabilityStmtStats;
	
	public void addStats(String mode, String igname, String version, String fhirVersion, List<String> format) {
		if (capabilityStmtStats == null)
			capabilityStmtStats = new HashMap<>();
		
		HashMap<String, HashMap<String, String>> modeMap = capabilityStmtStats.get(mode);
		if (modeMap == null)
			modeMap = new HashMap<>();
		
		HashMap<String, String> versionMap = modeMap.get("version");
		if (versionMap == null)
			versionMap = new HashMap<>();
		
		versionMap.put(igname, version);
		modeMap.put("version", versionMap);
		
		HashMap<String, String> fhirVersionMap = modeMap.get("fhirVersion");
		if (fhirVersionMap == null)
			fhirVersionMap = new HashMap<>();
		
		fhirVersionMap.put(igname, fhirVersion);
		modeMap.put("fhirVersion", fhirVersionMap);
		
		HashMap<String, String> formatMap = modeMap.get("format");
		if (formatMap == null)
			formatMap = new HashMap<>();
		
		Collections.sort(format);
		formatMap.put(igname, format.toString());
		modeMap.put("format", formatMap);
		
		capabilityStmtStats.put(mode, modeMap);
		
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void addToStatus(String str) {
		if (status != null)
			status = status + " :: " + str;
		else
			status = str;
	}
	
	public HashMap<String, HashMap<String, HashMap<String, String>>> getCapabilityStmtStats() {
		return capabilityStmtStats;
	}
	
	public List<IGComparator> getSds() {
		return sds;
	}
	
	public List<IGComparator> getCcss() {
		return ccss;
	}
	
	public List<IGComparator> getScss() {
		return scss;
	}
	
	@JsonIgnore
	public IGSComparator(String ig1, String ig2, int initialize) {
		// TODO Auto-generated constructor stub
		this.ig1 = ig1;
		this.ig2 = ig2;
		
		if (initialize == 0) {
			this.sds = new ArrayList<>();
			this.ccss = new ArrayList<>();
			this.scss = new ArrayList<>();
		}
		
		if (initialize == 1)
			this.sds = new ArrayList<>();
		
		if (initialize == 2)
			this.ccss = new ArrayList<>();
		
	}
	
	@JsonIgnore
	public IGComparator getIgComparator(String type, int comparator_mode) {
		if (comparator_mode == 0) {
			for (IGComparator sdc : sds) {
				if (sdc.matches(type))
					return sdc;
			}

			IGComparator sdc = new IGComparator(type, ig1, ig2);
			sds.add(sdc);
			return sdc;
		}
		
		if (comparator_mode == 1) {
			for (IGComparator ccsc : ccss) {
				if (ccsc.matches(type))
					return ccsc;
			}
		

			IGComparator ccsc = new IGComparator(type, ig1, ig2);
			ccss.add(ccsc);
			return ccsc;
		}
		
		if (comparator_mode == 2) {
			for (IGComparator scsc : scss) {
				if (scsc.matches(type))
					return scsc;
			}
		

			IGComparator scsc = new IGComparator(type, ig1, ig2);
			scss.add(scsc);
			return scsc;
		}
		
		return null;
	}
	
	@JsonIgnore
	public void print() {
		String compareFile = "compare_" + ig1 + "_and_" + ig2 + ".json";
		System.out.println("Writing comparison into a JSON file " + compareFile);
	    
	    File jsonOutputFile = new File(compareFile);
	    
	    ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	    mapper.setSerializationInclusion(Include.NON_NULL);
        try (PrintWriter pw = new PrintWriter(jsonOutputFile)) {
            String json = mapper.writeValueAsString(this);
            pw.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
