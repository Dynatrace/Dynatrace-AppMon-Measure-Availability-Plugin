package com.dynatrace.diagnostics.plugin.availabilitycheck.utils;

import com.dynatrace.diagnostics.pdk.Status;

/**
 * @author eugene.turetsky
 * 
 */
public interface AvailabilityConstants {
	// Plugin's configuration parameter's constants
	public static final String CONFIG_MEASURES = "measures";
	public static final String CONFIG_PREFIX = "prefix";
	public static final String CONFIG_SUFFIX = "suffix";
	public static final String CONFIG_AVAILABLE_VALUE = "availableValue";
	public static final String CONFIG_UNAVAILABLE_VALUE = "unavailableValue";
	public static final String CONFIG_DT_HOST = "dthost";
	public static final String CONFIG_DT_PORT = "dtport";
	public static final String CONFIG_DASHBOARD = "dashboard";
//	public static final String CONFIG_URI = "uri";
	public static final String CONFIG_USER = "user";
	public static final String CONFIG_PASSWORD = "password";
	
	// Miscellaneous
	public static final Status STATUS_SUCCESS = new Status(Status.StatusCode.Success);
//	public static final Status STATUS_PARTIAL_SUCCESS = new Status(Status.StatusCode.PartialSuccess);
	public static final int DEFAULT_STRING_LENGTH = 256;
	public static final String QUEUE_NAME_1 = "Queue Name 1";
	public static final String QUEUE_NAME_2 = "Queue Name 2";
	public static final String TAG_NAME_MEASURE = "measure";
	public static final String ATTRIBUTE_NAME_MEASURE = "measure";
	public static final String METRIC_GROUP_NAME = "AvailabilityMetricGroup";
	public static final String METRIC_NAME = "Availability";
	public static final double MEASURE_IS_AVAILABLE = 200;
	public static final String SPLIT_QUEUE = "Availability Split Measures";
	public final static String TEMP_FILE_REPORT_PREFIX = "tempReport";
	public final static String REPORT_TYPE_XML = ".xml";
	public final static String REPORT_CONTENT_TYPE = "application/xml";
	public final static String REST_REPORT_PROTOCOL = "http://";
	public final static String REST_REPORT_URL = "/rest/management/reports/create/"; 
	public final static String REST_REPORT_URL_SUFFIX = "?type=XML";

	// Error Messages
	public static final String ENV_IS_NULL = "MonitorEnvironment object should not be null";
	public static final String LIST_OF_MEASURES_EMPTY = "List of measures must not be empty";
	public static final String AVAILABLE_VALUE_IS_NULL = "Available value is null";
	public static final String UNAVAILABLE_VALUE_IS_NULL = "Unavailable value is null";
	public static final String DTHOST_IS_EMPTY = "The '" + CONFIG_DT_HOST + "' parameter is null or empty";
	public static final String PASSWORD_IS_EMPTY = "The 'password' parameter is null or empty";
	public static final String DASHBOARD_IS_EMPTY = "The 'dashboard' parameter is null or empty";
}
