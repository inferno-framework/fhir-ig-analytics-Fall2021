/**
 * 
 */
package com.gatech.fhir.analytics.util;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Array;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gatech.fhir.analytics.comparator.IGSComparator;
import org.apache.commons.lang3.StringUtils;

/**
 * @author mzalmaariz
 *
 */
public class CmdUtil {
	private static String DEV_NULL = "/dev/null";
	public static final int SEED = 23;
	
	/**
	 * Returns the exitCode of the specified command.
	 * Command is waited to be finished to receive the exit code.
	 * All the streams are closed before exit
	 * 
	 * @param enableLog - {@link Boolean} if the command to be logged in log file as a {@link String}
	 * @param cmd - Array {@link String} of the command attributes that to be executed.
	 * @return - {@link Integer} exit code of the command
	 */
	public static int executeCmd(boolean enableLog, String... cmd) {
		if (cmd == null)
			return -100;
		
		if (enableLog)
			System.out.println("Execute cmd " + StringUtils.join(cmd, ' '));
		
		Process process = null;
		try {
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.command(cmd);
			
			File NULL_FILE = new File(DEV_NULL);
			processBuilder.redirectError(NULL_FILE);
			processBuilder.redirectOutput(NULL_FILE);
			
			process = processBuilder.start();
			return process.waitFor();
			
		} catch (Exception ex) {
			System.out.println("Error in executing cmd " + StringUtils.join(cmd, ' '));
			ex.printStackTrace();
			return -100;
		} finally {
			try {
				process.getInputStream().close();
				process.getErrorStream().close();
				process.getOutputStream().close();
			} catch (Exception ignore) {;}
		}
	}
	
	public static int hashFn(String... strings){
		int myHash = SEED;
		for (String str : strings) {
			if (StringUtils.isBlank(str))
				return 0;
				
			myHash = myHash + str.hashCode();
		}
			
		return myHash;
	}
	
	public static int hash( int aSeed , Object aObject ) {
		int result = aSeed;
		if ( aObject == null) {
			result = hash(result, 0);
		}
		else if ( ! aObject.getClass().isArray()) {
			result = hash(result, aObject.hashCode());
		}
		else {
			int length = Array.getLength(aObject);
			for ( int idx = 0; idx < length; ++idx ) {
				Object item = Array.get(aObject, idx);
				//recursive call!
				result = hash(result, item);
			}
		}
		return result;
	}

	public static void saveToFile(IGSComparator igsComparator, String absolute_path) {


		File jsonOutputFile = new File(absolute_path);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (PrintWriter pw = new PrintWriter(jsonOutputFile)) {
			String json = mapper.writeValueAsString(igsComparator);
			pw.println(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
