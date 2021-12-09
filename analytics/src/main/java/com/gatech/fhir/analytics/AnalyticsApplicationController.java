/**
 * 
 */
package com.gatech.fhir.analytics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.gatech.fhir.analytics.comparator.IGComparator;
import com.gatech.fhir.analytics.comparator.IGSComparator;
import com.gatech.fhir.analytics.json.CapabilityStatement;
import com.gatech.fhir.analytics.json.Element;
import com.gatech.fhir.analytics.json.Files;
import com.gatech.fhir.analytics.json.Index;
import com.gatech.fhir.analytics.json.Interaction;
import com.gatech.fhir.analytics.json.Resource;
import com.gatech.fhir.analytics.json.Rest;
import com.gatech.fhir.analytics.json.StructureDefinition;
import com.gatech.fhir.analytics.parser.JSONParserUtil;
import com.gatech.fhir.analytics.util.CmdUtil;
import com.gatech.fhir.analytics.web.AccumulatedTypes;
import com.gatech.fhir.analytics.web.Types;

/**
 * @author mzalmaariz
 *
 */
@Component
@Produces({ MediaType.APPLICATION_JSON + "; charset=UTF-8" })
@Path("/fhirAnalytics")
public class AnalyticsApplicationController {
	private static Log log = LogFactory.getLog(AnalyticsApplicationController.class);
	
	public static final String BASE_DIR = System.getProperty("user.dir");
	
	@GET
	@Path("getTypes")
	public Types getTypes(@Context UriInfo uriInfo, @Context HttpServletRequest request, @Context HttpServletResponse response) {
		
		Types types = new Types();
		
		HashMap<String, String> queryMap = new HashMap<>();
		MultivaluedMap<String, String> multiMap = uriInfo.getQueryParameters();
		for (String key : multiMap.keySet()) {
			if (CollectionUtils.isNotEmpty(multiMap.get(key))) {
				queryMap.put(key, multiMap.get(key).get(0));
			}
		}
		
		String extract_error = extract_packages(queryMap);
		if (extract_error != null) {
			types.status = extract_error;
			log.error(request.getRemoteHost() + " | Error in GetTypes -identifier:" + queryMap.get("identifier") 
				+ " -ig1:" + queryMap.get("ig1") + " -ig2: " + queryMap.get("ig2") + " -status: " + types.status);
			
			return types;
		}
		
		types.status = "success";
		types.identifier = queryMap.get("identifier");
		
		HashMap<String, HashMap<String, List<String>>> allTypes = new HashMap<>();
		
		for (String ig : new String[]{"ig1", "ig2"}) {
			String packageDir = BASE_DIR + File.separator + types.identifier + File.separator + ig + File.separator + "package" + File.separator;
			
			Index index = JSONParserUtil.getObject(packageDir + ".index.json", Index.class);
			for (Files file : index.files) {
				if (file.resourceType.equals("CapabilityStatement")) {
					if (types.csTypes == null)
						types.csTypes = new HashMap<>();
					
					List<String> igCsFiles = types.csTypes.get(ig);
					if (igCsFiles == null)
						igCsFiles = new ArrayList<>();
					
					igCsFiles.add(file.filename);
					types.csTypes.put(ig, igCsFiles);
					
					if (types.accumulatedCsTypes == null) {
						types.accumulatedCsTypes = new AccumulatedTypes();
						types.accumulatedCsTypes.uiConflicts = new ArrayList<>();
					}
					
					types.accumulatedCsTypes.uiConflicts.add(ig + ":" + file.filename);
				}
				
				if (file.resourceType.equals("StructureDefinition")) {
					StructureDefinition sd = JSONParserUtil.getObject(packageDir + File.separator + file.filename, StructureDefinition.class);
					
					HashMap<String, List<String>> typeHash = allTypes.get(sd.type);
					if (typeHash == null)
						typeHash = new HashMap<>();
						
					List<String> subTypes = typeHash.get(ig);
					if (subTypes == null)
						subTypes = new ArrayList<>();
					
					subTypes.add(sd.id);
					Collections.sort(subTypes);
					typeHash.put(ig, subTypes);
					allTypes.put(sd.type, typeHash);
					
					if (types.sdTypes == null)
						types.sdTypes = new HashMap<>();
					
					List<String> igSdTypes = types.sdTypes.get(ig);
					if (igSdTypes == null)
						igSdTypes = new ArrayList<>();
					
					igSdTypes.add(sd.type);
					types.sdTypes.put(ig, igSdTypes);
				}	
			}
		}
		
		types.accumulatedSdTypes = new AccumulatedTypes();
		
		for (String key : allTypes.keySet()) {
			HashMap<String, List<String>> typeHash = allTypes.get(key);
			boolean expand = false; // We will expand types if each IG recorded multiple of the same type or one of the IG doesnt have the type
			if (typeHash.size() == 2) { //Found in both
				for (String igKey : typeHash.keySet()) {
					List<String> igValues = typeHash.get(igKey);
					if (igValues.size() != 1) {
						expand = true;
						break;
					}
				}
			} else  //One of the IG does have the type
				expand = true;
				
			if (expand) {
				HashMap<String, HashMap<String, List<String>>> map = new HashMap<>();
				map.put(key, typeHash);
				if (types.accumulatedSdTypes.conflicts == null)
					types.accumulatedSdTypes.conflicts = new ArrayList<>();
				
				types.accumulatedSdTypes.conflicts.add(map);
				
				for (String _key : map.keySet()) {
					HashMap<String, List<String>> igValues = map.get(_key);
					
					for (String igKey : igValues.keySet()) {
						String showValue = key + ":" + igKey;
						List<String> values = igValues.get(igKey);
						for (String sdName : values) {
							String showValueName = showValue + ":" + sdName;
							if (types.accumulatedSdTypes.uiConflicts == null)
								types.accumulatedSdTypes.uiConflicts = new ArrayList<>();
						
							types.accumulatedSdTypes.uiConflicts.add(showValueName);
						}
					}
					
				}
				
			} else {
				if (types.accumulatedSdTypes.similarities == null)
					types.accumulatedSdTypes.similarities = new ArrayList<>();
				
				types.accumulatedSdTypes.similarities.add(key);
			}
		}
		
		if (CollectionUtils.isNotEmpty(types.accumulatedSdTypes.conflicts))
			types.status = "Extraction successful. Please resolve conflicts to have a more meaningful comparison.";
		
		if (CollectionUtils.isNotEmpty(types.accumulatedSdTypes.conflicts)) {
			Collections.sort(types.accumulatedSdTypes.conflicts , new Comparator<Object>() {
				@SuppressWarnings("unchecked")
				public int compare(Object o1, Object o2) {
					String key1 = null;
					String key2 = null;
					if (o1 instanceof HashMap) {
						key1 = ((HashMap<String, HashMap<String, List<String>>>) o1).keySet().iterator().next();
					} else
						key1 = (String) o1;

					if (o2 instanceof HashMap) {
						key2 = ((HashMap<String, HashMap<String, List<String>>>) o2).keySet().iterator().next();
					} else
						key2 = (String) o2;

					return key1.compareTo(key2);
				}
			});
		}
		
		if (CollectionUtils.isNotEmpty(types.accumulatedSdTypes.similarities))
			Collections.sort(types.accumulatedSdTypes.similarities);
		
		if (CollectionUtils.isNotEmpty(types.accumulatedSdTypes.uiConflicts))
			Collections.sort(types.accumulatedSdTypes.uiConflicts);
		
		log.info(request.getRemoteHost() + " | GetTypes -identifier:" + queryMap.get("identifier") 
			+ " -ig1:" + queryMap.get("ig1") + " -ig2: " + queryMap.get("ig2") + " -status: " + types.status);
		
		return types;
	}
	
	@GET
	@Path("/compare")
	public IGSComparator getComparison(@Context UriInfo uriInfo, @Context HttpServletRequest request,
			@Context HttpServletResponse response) {
		
		HashMap<String, String> queryMap = new HashMap<>();
		MultivaluedMap<String, String> multiMap = uriInfo.getQueryParameters();
		for (String key : multiMap.keySet()) {
			if (CollectionUtils.isNotEmpty(multiMap.get(key))) {
				queryMap.put(key, multiMap.get(key).get(0));
			}
		}
		
		String[] package_names = new String[] {"ig1", "ig2"};
		String extract_error = extract_packages(queryMap);
		if (extract_error != null) {
			log.error(request.getRemoteHost() + " | " + "Error in extracting igs in compare -identifier:" + queryMap.get("identifier") 
				+ " -ig1:" + queryMap.get("ig1") + " -ig2: " + queryMap.get("ig2") + " -status: " + extract_error);
			return errorComparator(package_names, extract_error);
		}
		
		String identifier = queryMap.get("identifier");
		
		String csTypesStr = queryMap.get("csTypes");
		String sdTypesStr = queryMap.get("sdTypes");
		List<String> csTypes = null;
		List<String> sdTypes = null;
		
		if (StringUtils.isNotBlank(sdTypesStr) || StringUtils.isNotBlank(csTypesStr)) {
			csTypes = new ArrayList<>();
			sdTypes = new ArrayList<>();
			
			if (StringUtils.isNotBlank(csTypesStr))
				csTypes = Arrays.asList(StringUtils.split(csTypesStr, ","));
			
			if (StringUtils.isNotBlank(sdTypesStr))
				sdTypes = Arrays.asList(StringUtils.split(sdTypesStr, ","));
		}

		IGSComparator result = getCompare(package_names, identifier, sdTypes, csTypes);
		
		log.info(request.getRemoteHost() + " | " + "Compare -identifier:" + queryMap.get("identifier") 
			+ " -ig1:" + queryMap.get("ig1") + " -ig2: " + queryMap.get("ig2") + " -sdTypes: " + sdTypes 
			+ " -csTypes: " + csTypes + " -status: " + result.getStatus());
		
		return result;
	}

	@GET
	@Path("/downloadFile")
	public void downloadFile(@Context UriInfo uriInfo, @Context HttpServletRequest request,
												 @Context HttpServletResponse response) throws IOException {

		HashMap<String, String> queryMap = new HashMap<>();
		MultivaluedMap<String, String> multiMap = uriInfo.getQueryParameters();
		for (String key : multiMap.keySet()) {
			if (CollectionUtils.isNotEmpty(multiMap.get(key))) {
				queryMap.put(key, multiMap.get(key).get(0));
			}
		}

		String identifier = queryMap.get("identifier");


		String filename = "compare_result.json";
		String jsonFile = BASE_DIR + File.separator + identifier + File.separator + filename;

		File targetFile = new File(jsonFile);

		InputStream targetInput = new FileInputStream(targetFile);
		InputStream inputStream = new BufferedInputStream(targetInput);

		response.setHeader("Content-Disposition",
				String.format("inline; filename=\"" + filename + "\""));
		response.setContentLength((int) targetFile.length());
		response.setContentType("application/octet-stream");

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		log.info(request.getRemoteHost() + " | " + "Downloaded -identifier:" + queryMap.get("identifier"));
	}
	
	private String extract_packages(HashMap<String, String> queryMap) {
		String identifier = queryMap.get("identifier");
		String ig1Url = queryMap.get("ig1"); //Here
		String ig2Url = queryMap.get("ig2");
		
		if (StringUtils.isBlank(identifier)) {
			int hash = CmdUtil.hashFn(ig1Url,ig2Url);
			if (hash == 0)
				return "Please specify two IGs or an identifier";
			
			identifier = String.valueOf(hash); 
		}
			
		String identifierDir = BASE_DIR + File.separator + identifier + File.separator;
		
		String[] package_names = new String[] {"ig1", "ig2"};
		boolean redownload = false;
	
		//Make Sure IG1 and IG2 directories exists already.
		for (String pkg_name : package_names) {
			if (! new File(identifierDir + pkg_name).exists()) {
				System.out.println("Package will be downloaded: " + identifierDir + pkg_name + " Missing");
				redownload = true;
			}
		}
		
		if (redownload) {
			if (StringUtils.isBlank(ig1Url))
				return "IG1 URL is missing";
			
			if (StringUtils.isBlank(ig2Url))
				return "IG2 URL is missing";
			
			int exitCode = 0;
			
			String[] mkdirCmd = {"mkdir", "-p", identifierDir};
			exitCode = CmdUtil.executeCmd(true, mkdirCmd);
			if (exitCode != 0)
				return "Error creating identifier dir";
		
			exitCode = wgetUrl(ig1Url, identifierDir + "ig1.tgz");
			if (exitCode != 0)
				return "Error downloading IG1";
			
			exitCode = wgetUrl(ig2Url, identifierDir + "ig2.tgz");
			if (exitCode != 0)
				return "Error downloading IG2";
	
			exitCode = unpack_packages(BASE_DIR + File.separator + identifier, 
				BASE_DIR + File.separator + identifier + File.separator + "ig1.tgz", 
				BASE_DIR + File.separator + identifier + File.separator + "ig2.tgz");
			if (exitCode != 0)
				return "Error extracting IG1 or IG2"; //TODO separate error for IG1 or IG2
		}
		
		queryMap.put("identifier", identifier); //Update queryMap to put newly generated identifier
		
		return null;
	}
	
	private void addIntoMap(String key, String value, HashMap<String, List<String>> map) {
		List<String> values = map.get(key);
		if (values == null)
			values = new ArrayList<>();
		
		values.add(value);
		map.put(key, values);
	}
	
	private IGSComparator getCompare(String[] package_names, String identifier, List<String> sdTypes, List<String> csTypes) {
		IGSComparator igsComparator = new IGSComparator(package_names[0], package_names[1], 0);
		
		HashMap<String, List<String>> csTypesMap = null;
		if (csTypes != null) {
			csTypesMap = new HashMap<>();
			for (String csType : csTypes) {
				String[] csTypeArr = StringUtils.split(csType, ":");
				if (csTypeArr.length == 1) {
					for (String package_name : package_names)
						addIntoMap(package_name, csTypeArr[0], csTypesMap);
				
				} else {
					String igname = csTypeArr[0]; //Indicates the IG of filtered capability statement
					String value = csTypeArr[1]; //Indicates the capability statement
					addIntoMap(igname, value, csTypesMap);
				}
			}
		}
		
		HashMap<String, List<String>> sdTypesMap = null;
		if (sdTypes != null) {
			sdTypesMap = new HashMap<>();
			for (String sdType : sdTypes) {
				String[] sdTypeArr = StringUtils.split(sdType, ":");
				if (sdTypeArr.length == 1) {
					for (String package_name : package_names)
						addIntoMap(package_name, sdTypeArr[0], sdTypesMap);
				
				} else if (sdTypeArr.length == 2) {
					String igname = sdTypeArr[0];
					String type = sdTypeArr[1];
					
					addIntoMap(igname, type, sdTypesMap);
				
				} else {	
					String type = sdTypeArr[0]; //Structure definition Type
					String igname = sdTypeArr[1];
					String typeName = sdTypeArr[2]; //Structure Definition name
					
					addIntoMap(igname, type + ":" + typeName, sdTypesMap);
				}
			}
		}
			
		HashMap<String, List<String>> conflictMap = new HashMap<>();
		boolean haveConflictMsg = false;
		
		for (String ig : package_names) {
			String packageDir = BASE_DIR + File.separator + identifier + File.separator + ig + File.separator + "package" + File.separator;
			Index index = JSONParserUtil.getObject(packageDir + File.separator + ".index.json", Index.class);
			
			if (index == null || CollectionUtils.isEmpty(index.files)) {
				igsComparator.addToStatus("No index JSON for " + ig);
				continue;
			}
			
			for (Files file : index.files) {
				if (file.resourceType.equals("CapabilityStatement")) {
					if (csTypesMap != null) {
						List<String> filterCs = csTypesMap.get(ig);
						if (filterCs == null || !filterCs.contains(file.filename)) //This CS for this IG should be filtered
							continue;
					}
					
					CapabilityStatement cs = JSONParserUtil.getObject(packageDir + File.separator + file.filename, CapabilityStatement.class);
					if (cs == null || CollectionUtils.isEmpty(cs.rest)) {
						igsComparator.addToStatus("No Rest in Capability Statment for " + ig);
						continue;
					}
					
					if (StringUtils.equalsIgnoreCase(cs.rest.get(0).mode, "server")) {
						igsComparator.addStats("server", ig, cs.version, cs.fhirVersion, cs.format);
					}
					
					if (StringUtils.equalsIgnoreCase(cs.rest.get(0).mode, "client")) {
						igsComparator.addStats("client", ig, cs.version, cs.fhirVersion, cs.format);
					}
					
					for (Rest rest : cs.rest) {
						if (CollectionUtils.isEmpty(rest.resource))
							continue;
						
						if (StringUtils.equalsIgnoreCase(rest.mode, "server")) {
							for (Resource resource : rest.resource) {
								IGComparator igComparator = igsComparator.getIgComparator(resource.type, 2);
								HashMap<String, String> interactions = new HashMap<>();
								
								if (CollectionUtils.isNotEmpty(resource.interaction)) {
									for (Interaction interaction : resource.interaction) {
										interactions.put(interaction.code, CollectionUtils.isNotEmpty(interaction.extension) ? interaction.extension.get(0).valueCode 
												: "UnDefined");
									}
								}
								
								igComparator.add(ig, "server-rest-interactions", interactions);
							}
						}
						
						if (StringUtils.equalsIgnoreCase(rest.mode, "client")) {
							for (Resource resource : rest.resource) {
								IGComparator igComparator = igsComparator.getIgComparator(resource.type, 1);
								HashMap<String, String> interactions = new HashMap<>();
								if (CollectionUtils.isNotEmpty(resource.interaction)) {
									for (Interaction interaction : resource.interaction) {
										interactions.put(interaction.code, CollectionUtils.isNotEmpty(interaction.extension) ? interaction.extension.get(0).valueCode 
												: "UnDefined");
									}
								}
								
								igComparator.add(ig, "client-rest-interactions", interactions);
							}
						}
					}
				}
				
				if (file.resourceType.equals("StructureDefinition")) {
					StructureDefinition sd = JSONParserUtil.getObject(packageDir + File.separator + file.filename, StructureDefinition.class);
					if (sd == null || sd.snapshot == null || CollectionUtils.isEmpty(sd.snapshot.elements)) {
						igsComparator.addToStatus("No SD snapshot for " + ig);
						continue;
					}
					else {
						if (sdTypesMap != null) {
							List<String> filterSds = sdTypesMap.get(ig);
							if (MapUtils.isNotEmpty(sdTypesMap) && filterSds == null)
								continue;

							if (filterSds != null && !(filterSds.contains(sd.type) || filterSds.contains(sd.type + ":" + sd.id)))
								continue;
						}
						
						if (!haveConflictMsg) {
							List<String> igSdTypes = conflictMap.get(ig);
							if (igSdTypes == null)
								igSdTypes = new ArrayList<>();
							else {
								haveConflictMsg = igSdTypes.contains(sd.type);
								if (haveConflictMsg)
									igsComparator.addToStatus("At least one Conflicting SDType exists. Please resolve conflicts by fetching getTypes and then compare");
							}
							
							igSdTypes.add(sd.type);
							conflictMap.put(ig, igSdTypes);
						}
								
						IGComparator igComparator = igsComparator.getIgComparator(sd.type, 0);
						for (Element element : sd.snapshot.elements) {
							igComparator.add(ig, element);
						}
					}
				}
			}
		}

		IGSComparator result = sanatize(igsComparator);
		result.setIdentifier(identifier);
		if (result.getStatus() == null)
			result.setStatus("success");
		
		String absolute_path = BASE_DIR + File.separator + identifier + File.separator + "compare_result.json";
		CmdUtil.saveToFile(result, absolute_path);
		return result;
	}
	
	private IGSComparator errorComparator(String[] package_names, String status) {
		IGSComparator igsComparator = new IGSComparator(package_names[0], package_names[1], -1);
		igsComparator.addToStatus(status);
		
		return igsComparator;
	}
		
	private IGSComparator sanatize(IGSComparator igsComparator) {
		for (IGComparator comparator : igsComparator.getSds()) {
			if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig1").elements) &&
					(CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig2").elements))) {
				comparator.setBlankMap();
			}
			
			else {
				if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig1").elements))
					comparator.getIgDifferentialMap().get("ig1").elements = null;
			
				if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig2").elements))
					comparator.getIgDifferentialMap().get("ig2").elements = null;
			}
		}
		
		for (IGComparator comparator : igsComparator.getCcss()) {
			if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig1").elements) &&
					(CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig2").elements))) {
				comparator.setBlankMap();
			}
			
			else {
				if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig1").elements))
					comparator.getIgDifferentialMap().get("ig1").elements = null;
			
				if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig2").elements))
					comparator.getIgDifferentialMap().get("ig2").elements = null;
			}
		}	
		

		for (IGComparator comparator : igsComparator.getScss()) {
			if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig1").elements) &&
					(CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig2").elements))) {
				comparator.setBlankMap();
			}
			
			else {
				if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig1").elements))
					comparator.getIgDifferentialMap().get("ig1").elements = null;
			
				if (CollectionUtils.isEmpty(comparator.getIgDifferentialMap().get("ig2").elements))
					comparator.getIgDifferentialMap().get("ig2").elements = null;
			}
		}

		return igsComparator;
	}
	

	private int wgetUrl(String url, String output) {
		String wget_bin_path = "/usr/bin/wget";
		String wget_local_bin_path = "/usr/local/bin/wget";

		if (new File(wget_bin_path).exists()){
			String[] cmd = {wget_bin_path, url, "-O", output};
			return CmdUtil.executeCmd(true, cmd);
		}
		else if (new File(wget_local_bin_path).exists()){
			String[] cmd = {wget_local_bin_path, url, "-O", output};
			return CmdUtil.executeCmd(true, cmd);
		}
		else {
			System.out.println("ERROR: wget not found on the system");
			System.exit(1);
			return 1;
		}
	}

	private static int unpack_packages(String ig_dir, String... packages) {
		for (String pkg: packages) {
			//Create pkg directory and unpack into that directory
			String ig = pkg.substring(pkg.lastIndexOf("/") + 1, pkg.lastIndexOf('.'));
			File pkg_dir = new File(ig_dir + File.separator + ig);

			if (!pkg_dir.exists()) {
				pkg_dir.mkdir();
			}

			String tar_bin_path = "/usr/bin/tar";
			String tar_local_bin_path = "/usr/local/bin/tar";

			if (new File(tar_bin_path).exists()){
				String[] unpack_cmd = {tar_bin_path, "xvf", pkg, "-C", ig_dir + File.separator + ig};
				int exit_code = CmdUtil.executeCmd(true, unpack_cmd);
				if (exit_code != 0) {
					System.out.println("ERROR: cannot unpack " + ig);
					return exit_code;
				}
			}
			else if (new File(tar_local_bin_path).exists()){
				String[] unpack_cmd = {tar_local_bin_path, "xvf", pkg, "-C", ig_dir + File.separator + ig};
				int exit_code = CmdUtil.executeCmd(true, unpack_cmd);
				if (exit_code != 0) {
					System.out.println("ERROR: cannot unpack " + ig);
					return exit_code;
				}
			}
			else {
				System.out.println("ERROR: tar executable not found on the system");
				System.exit(1);
				return 1;
			}

		}

		return 0;
	}
}
