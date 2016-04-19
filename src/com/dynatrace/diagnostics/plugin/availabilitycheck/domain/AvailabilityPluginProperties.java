package com.dynatrace.diagnostics.plugin.availabilitycheck.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author eugene.turetsky
 * 
 */
public class AvailabilityPluginProperties {
	
	private List<String> measures;
	private String prefix;
	private String suffix;
	private Long availableValue;
	private Long unavailableValue;
	private String dtHost;
	private Long dtPort;
	private String dashboard;
	private String uri;
	private String user;
	private String password;
	private Map<String, String> newMeasureNameMap = new HashMap<String, String>();
	
	public List<String> getMeasures() {
		return measures;
	}
	public void setMeasures(List<String> measures) {
		this.measures = measures;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public Long getUnavailableValue() {
		return unavailableValue;
	}
	public Long getAvailableValue() {
		return availableValue;
	}
	public void setAvailableValue(Long availableValue) {
		this.availableValue = availableValue;
	}
	public void setUnavailableValue(Long unavailableValue) {
		this.unavailableValue = unavailableValue;
	}
	public String getDtHost() {
		return dtHost;
	}
	public void setDtHost(String dtHost) {
		this.dtHost = dtHost;
	}
	public Long getDtPort() {
		return dtPort;
	}
	public void setDtPort(Long dtPort) {
		this.dtPort = dtPort;
	}
	public String getDashboard() {
		return dashboard;
	}
	public void setDashboard(String dashboard) {
		this.dashboard = dashboard;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Map<String, String> getNewMeasureNameMap() {
		return newMeasureNameMap;
	}	
}
