package com.gatech.fhir.analytics.web;

import java.util.HashMap;
import java.util.List;

public class AccumulatedTypes {
	public List<String> similarities;
	public List<String> uiConflicts;
	public List<HashMap<String, HashMap<String, List<String>>>> conflicts;
}
