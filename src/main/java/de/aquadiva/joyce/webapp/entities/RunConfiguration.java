package de.aquadiva.joyce.webapp.entities;

import org.apache.tapestry5.beaneditor.DataType;
import org.apache.tapestry5.beaneditor.Validate;
//import org.apache.tapestry5.beaneditor.Width;

import de.aquadiva.joyce.processes.services.SelectionParameters.SelectionType;

public class RunConfiguration {
	
	@Validate("required")
	@DataType("longtext")
//	@Width(value = 50)
	public String keywords = "land management wetting cycle mineral-organic precipitation heat travel time depth bacterial FT-ICR-MS organic material production environmental condition reactive transport fatty acid below ground Si carbohydrate hypothesis deicing chemical degradation process hotspot surface-derived soil solution floor biocolloid plant altitude formation environment substrate biofilm";
	
	@Validate("required")
	public SelectionType typeOfObjectToSelect = SelectionType.ONTOLOGY;
	
	@Validate("required")
	public Integer maxNumberOfOntologies = new Integer(3);
	
	@Validate("required")
	public Integer sampleSize = new Integer(50);
	
	@Validate("required")
	public Preference preferenceCoverage = Preference.HIGH;
	
	@Validate("required")
	public Preference preferenceOverhead = Preference.MEDIUM;
	
	@Validate("required")
	public Preference preferenceOverlap = Preference.MEDIUM;
}
