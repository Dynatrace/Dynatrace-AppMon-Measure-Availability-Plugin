package com.dynatrace.diagnostics.plugin.availabilitycheck;

import java.io.IOException;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import org.apache.commons.io.IOUtils;

import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Status.StatusCode;
import com.dynatrace.diagnostics.plugin.availabilitycheck.domain.AvailabilityPluginProperties;
import com.dynatrace.diagnostics.plugin.availabilitycheck.utils.AvailabilityConstants;
import com.dynatrace.diagnostics.sdk.resources.BaseConstants;

public class MeasureAvailabilityExecutor implements AvailabilityConstants{
	private static final Logger log = Logger.getLogger(MeasureAvailabilityExecutor.class.getName());
	static final String[] EMPTY_STRINGS = {""};
	public static final String ls = System.getProperty("line.separator");
	AvailabilityPluginProperties pp;
	
	protected Status setup(MonitorEnvironment env) throws Exception {
		Status status;
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setup method");
		}
		
		if (env == null) {
			log.severe(ENV_IS_NULL);
			return new Status(StatusCode.ErrorInternalException, ENV_IS_NULL, ENV_IS_NULL);
		}
		
		pp = new AvailabilityPluginProperties();
		
		// set plugin's configuration parameters
		try {
			if ((status = setConfiguration(env)).getStatusCode().getBaseCode() > Status.StatusCode.PartialSuccess.getBaseCode()) {
				log.severe("setup method: " + status.getMessage());
				return status;
			}
		} catch (Exception e) {
			log.severe("setup method: " + e.getMessage());
			return new Status(StatusCode.ErrorInternalException, e.getMessage(), e.getMessage(), e);
		}
		
		return STATUS_SUCCESS;
	}
	
	protected Status execute(MonitorEnvironment env) throws Exception {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering execute method");
		}
		
		// get dashboard as a XML file
		String xml;
		Map<String, Boolean> map;
		try {
			xml = getStringFromUrl(pp.getUri(), pp.getUser(), pp.getPassword());
			if (log.isLoggable(Level.FINER)) {
				log.finer("execute method: xml string is '" + xml + "'");
			}
		
			// get Map of available measures
			map = setAvailabilityIndicators(xml);
		} catch (Exception e) {
			log.severe("execute method: " + e.getMessage());
			return new Status(StatusCode.ErrorInternalConfigurationProblem, e.getMessage(), e.getMessage(), e);
		}
		
		// get map with new measure names
		Map<String, String> newNames = pp.getNewMeasureNameMap();

		// populate availability measures
		Collection<MonitorMeasure> monitorMeasures = env.getMonitorMeasures(METRIC_GROUP_NAME, METRIC_NAME);
		for (MonitorMeasure monitorMeasure : monitorMeasures) {
			monitorMeasure.setValue(pp.getAvailableValue());
			
			MonitorMeasure dynamicMeasure;
			for (String measure : pp.getMeasures()) {
				// setup dynamic measure from the based measure name (i.e add prefix and suffix fields)
				dynamicMeasure = env.createDynamicMeasure(monitorMeasure, SPLIT_QUEUE, newNames.get(measure));
				if (map.containsKey(measure)) {
					// measure is available
					dynamicMeasure.setValue(pp.getAvailableValue());
					if (log.isLoggable(Level.FINER)) {
						log.finer("execute method: dynamic measure was set up for measure '" + measure + "', new name is '" + newNames.get(measure) + "', value is " + MEASURE_IS_AVAILABLE);
					}
				} else {
					// set base measure value to not available value
					monitorMeasure.setValue(pp.getUnavailableValue());
					// measure is not available
					dynamicMeasure.setValue(pp.getUnavailableValue());
					if (log.isLoggable(Level.FINER)) {
						log.finer("execute method: dynamic measure was set up for measure '" + measure + "', new name is '" + newNames.get(measure) + "', value is " + pp.getUnavailableValue());
					}
				}
			}
		}

		return STATUS_SUCCESS;
	}
	
	protected void teardown(MonitorEnvironment env) throws Exception {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering teardown method");
		}
	}
	
	private Status setConfiguration(MonitorEnvironment env) {
	    if (log.isLoggable(Level.FINER)) {
	    	log.finer("Entering setConfiguration method");
		}
	    
	    String value;
	    
	    // set measures
	    List<String> list = Arrays.asList((value = env.getConfigString(CONFIG_MEASURES)) != null && !value.trim().isEmpty() ? value.split(BaseConstants.SCOLON) : EMPTY_STRINGS);
	    pp.setMeasures(trimMeasures(list));
	    if (pp.getMeasures().isEmpty()) {
	    	log.severe("setConfiguration method: " + LIST_OF_MEASURES_EMPTY);
	    	return new Status(StatusCode.ErrorInternalConfigurationProblem, LIST_OF_MEASURES_EMPTY, LIST_OF_MEASURES_EMPTY); 
	    }
	    // set prefix
	    pp.setPrefix(env.getConfigString(CONFIG_PREFIX));
	    
	    // set suffix
	    pp.setSuffix(env.getConfigString(CONFIG_SUFFIX));
	    
	    // set available value
	    pp.setAvailableValue(env.getConfigLong(CONFIG_AVAILABLE_VALUE));
	    if (pp.getAvailableValue() == null) {
	    	log.severe("setConfiguration method: " + AVAILABLE_VALUE_IS_NULL);
	    	return new Status(StatusCode.ErrorInternalConfigurationProblem, AVAILABLE_VALUE_IS_NULL, AVAILABLE_VALUE_IS_NULL); 
	    }
	    
	    // set unavailable value
	    pp.setUnavailableValue(env.getConfigLong(CONFIG_UNAVAILABLE_VALUE));
	    if (pp.getUnavailableValue() == null) {
	    	log.severe("setConfiguration method: " + UNAVAILABLE_VALUE_IS_NULL);
	    	return new Status(StatusCode.ErrorInternalConfigurationProblem, UNAVAILABLE_VALUE_IS_NULL, UNAVAILABLE_VALUE_IS_NULL); 
	    }
	    
	    // set dtHost
	    pp.setDtHost(env.getHost().getAddress());
	 	
	 	// set dtport
	 	Long port;
	 	pp.setDtPort(port = env.getConfigLong(CONFIG_DT_PORT));
	 	if (port == null || port < 1) {
	 		String message = new StringBuilder(DEFAULT_STRING_LENGTH)
	 			.append("Value of the 'Port' parameter '").append(port).append("' is incorrect.").toString();
	 		log.severe("setConfiguration method: " + message);		
	 		return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, message, message);	
	 	} else {
	 		pp.setDtPort(port);
	 	}
	 	
	 	// set dashboard
	 	pp.setDashboard(value = (env.getConfigString(CONFIG_DASHBOARD) != null ? env.getConfigString(CONFIG_DASHBOARD).trim() : null));
	 	if (value == null || value.isEmpty()) {
	 		log.severe("setConfiguration method: " + DASHBOARD_IS_EMPTY);
	 		return new Status(Status.StatusCode.ErrorInternalConfigurationProblem, DTHOST_IS_EMPTY, DTHOST_IS_EMPTY);
	 	}
	    
	    // set uri
//	    pp.setUri((value = env.getConfigString(CONFIG_URI)) != null ? value.trim() : null);
	    
	    // set user
	    pp.setUser((value = env.getConfigString(CONFIG_USER)) != null ? value.trim() : null);
	    
	    // set password
	    pp.setPassword(env.getConfigPassword(CONFIG_PASSWORD));
	    
	    // build new measure name
	    setNewMeasureName();
	    
	    // set dashboard url
	    pp.setUri(buildDashboardUrl());
	    
		if (log.isLoggable(Level.FINER)) {
			log.finer(new StringBuilder(DEFAULT_STRING_LENGTH).append("setConfiguration method: Availability Monitor Plugin properties:")
					.append(" measures are ").append(pp.getMeasures() != null ? "'" + Arrays.toString(pp.getMeasures().toArray()) + "'" : "null").append(";").append(ls)
					.append(" prefix is ").append(pp.getPrefix() != null ? "'" + pp.getPrefix() + "'" : "null").append(";").append(ls)
					.append(" suffix is ").append(pp.getSuffix() != null ? "'" + pp.getSuffix() + "'" : "null").append(";").append(ls)
					.append(" available value is ").append(pp.getAvailableValue()).append(";").append(ls)
					.append(" unavailable value is ").append(pp.getUnavailableValue()).append(";").append(ls)
					.append(" dthost is ").append(pp.getDtHost() != null ? "'" + pp.getDtHost() + "'" : "null").append(";").append(ls)
					.append(" dtport is ").append(pp.getDtPort()).append(";").append(ls)
					.append(" dashboard is ").append(pp.getDashboard() != null ? "'" + pp.getDashboard() + "'" : "null").append(";").append(ls)
					.append(" uri is ").append(pp.getUri() != null ? "'" + pp.getUri() + "'" : "null").append(";").append(ls)
					.append(" user is ").append(pp.getUser() != null ? "'" + pp.getUser() + "'" : "null").append(";").append(ls)
					.append(" password is ").append(pp.getPassword() != null ? "'" + pp.getPassword() + "'" : "null").append(";").append(ls)
					.toString());
		}
			
		return STATUS_SUCCESS;
	}
	
	private List<String> trimMeasures(List<String> list) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering trimMeasures method");
		}
		List<String> newList = new ArrayList<String>();
		
		if (list != null && !list.isEmpty()) {
			for (String element : list) {
				if (element == null || element.trim().isEmpty()) {
					// skip null or empty element
					if (log.isLoggable(Level.FINER)) {
						log.finer("trimMeasures method: measure is null or empty, skipping it.");
					}
					continue;
				}
				if (log.isLoggable(Level.FINER)) {
					log.finer("trimMeasures method: original measure is '" + element + "', trimmed measure is '" + element.trim() + "'");
				}
				newList.add(element.trim());
			}
			return newList;
		} else {
			if (log.isLoggable(Level.FINER)) {
				log.finer("trimMeasures method: returnning original list of measures without trimming because it is null or empty");
			}
			return list;
		}
	}
	
	private void setNewMeasureName() {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setNewMeasureName method");
		}
			
		Map<String, String> map = pp.getNewMeasureNameMap();
		String prefix = pp.getPrefix();
		String suffix = pp.getSuffix();
		for (String measure : pp.getMeasures()) {
			map.put(measure, new StringBuilder(prefix).append(measure).append(suffix).toString());
			if (log.isLoggable(Level.FINER)) {
				log.finer("setNewMeasureName method: measure is '" + measure + "', new measure name is '" + map.get(measure) + "'");
			}
		}
	}
	
	private String buildDashboardUrl() {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setDashboardUrl method");
		}
		
		StringBuilder sb = new StringBuilder(DEFAULT_STRING_LENGTH);
		return sb.append(REST_REPORT_PROTOCOL).append(pp.getDtHost()).append(BaseConstants.COLON).append(pp.getDtPort()).append(REST_REPORT_URL)
				.append(pp.getDashboard().replaceAll(" ", "%20")).append(REST_REPORT_URL_SUFFIX).toString();
	}
		
	private String getStringFromUrl(String strUrl, final String user, final String pwd) {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering getFileFromUrl method");
		}

		String xml = "";
		URL url;
		
		if (log.isLoggable(Level.FINER)) {
			log.finer("getFileFromUrl method: contacting URL : '" + strUrl + "'; user is '" + user + "'; password is '" + pwd + "'");
		}

		AuthCacheValue.setAuthCache(new AuthCacheImpl());
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				PasswordAuthentication pa = new PasswordAuthentication(user, pwd.toCharArray());
				if (log.isLoggable(Level.FINER)) {
					log.finer("getPasswordAuthentication method: user is '"	+ pa.getUserName() + "'; password is '"
							+ new String(pa.getPassword()) + "'; class is '" + pa.getClass().getCanonicalName() + "'");
				}

				return pa;
			}
		});
		
		try {
			url = new URL(strUrl);
			URLConnection urlConnection = url.openConnection();
			if (urlConnection != null && log.isLoggable(Level.FINER)) {
				log.finer("getFileFromUrl method: contentType is : " + urlConnection.getContentType());
			}
			
			// Checking whether the file is an XML file
			if (urlConnection != null && !urlConnection.getContentType().equalsIgnoreCase(REPORT_CONTENT_TYPE)) {
				log.severe("getFileFromUrl method:FAILED : Retrieved dashboard report is not a XML type");
				throw new RuntimeException("getFileFromUrl method:FAILED : Retrieved dashboard report is not a XML type");
			}
			
			xml = IOUtils.toString(url.openStream());
		} catch (MalformedURLException e) {
			log.severe("getFileFromUrl method: " + e.getMessage());
			throw new RuntimeException(e.getMessage(), e);
		} catch (IOException ioe) {
			log.severe("getFileFromUrl method: " + ioe.getMessage());
			throw new RuntimeException(ioe.getMessage(), ioe);
		} 

		return xml;
	}
	
	private Map<String, Boolean> setAvailabilityIndicators(String xml) throws Exception {
		if (log.isLoggable(Level.FINER)) {
			log.finer("Entering setAvailabilityIndicators method");
		}
		int count;
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		
		// Create stream reader
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(new StringReader(xml));
		// Main event loop
		while (xmlr.hasNext()) {
			// Process single event
			switch (xmlr.getEventType()) {
			// Process start tags
			case XMLStreamReader.START_ELEMENT:
				// skip all tags but "measure"
				if (xmlr.hasName() && xmlr.getLocalName().equals(TAG_NAME_MEASURE)) {
					// check if this tag has attribute name which is equals "measure"
					if ((count = xmlr.getAttributeCount()) > 0) {
						for (int i = 0; i < count; i++) {
							xmlr.getAttributeName(i).toString();
							if (xmlr.getAttributeName(i).toString().equals(ATTRIBUTE_NAME_MEASURE)) {
								// get value of the "measure" attribute
								map.put(xmlr.getAttributeValue(i), Boolean.TRUE);
							}
						}
					}
				}
				break;
			default:
					
			}	
			// Move to next event
			xmlr.next();
		}
		
		return map;
	}
}
