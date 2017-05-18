package de.aquadiva.joyce.webapp.pages;


import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.aquadiva.joyce.webapp.entities.Preference;
import de.aquadiva.joyce.webapp.entities.Result;
import de.aquadiva.joyce.webapp.entities.RunConfiguration;
import de.aquadiva.joyce.base.data.IOntology;
import de.aquadiva.joyce.base.data.OntologySet;
import de.aquadiva.joyce.base.data.ScoreType;
import de.aquadiva.joyce.evaluation.services.IBiOSSComparator;
import de.aquadiva.joyce.processes.services.SelectionParameters;

import java.util.Date;

/**
 * Start page of application ad-ontology-webapp.
 */
public class Index
{
  @Inject
  private Logger logger;

  @Inject
  private AjaxResponseRenderer ajaxResponseRenderer;

  @Property
  @Inject
  @Symbol(SymbolConstants.TAPESTRY_VERSION)
  private String tapestryVersion;

  @InjectPage
  private About about;

  @Inject
  private Block block;
  
  @Property
  private RunConfiguration runconfiguration;
  
  @Property 
  @Persist
  private ArrayList<Result> gridResults;

  // Handle call with an unwanted context
  Object onActivate(EventContext eventContext)
  {
    return eventContext.getCount() > 0 ?
        new HttpError(404, "Resource not found") :
        null;
  }


//  Object onActionFromLearnMore()
//  {
//    about.setLearn("LearnMore");
//
//    return about;
//  }

  @Log
  void onComplete()
  {
    logger.info("Complete call on Index page");
  }

  @Log
  void onAjax()
  {
    logger.info("Ajax call on Index page");

    ajaxResponseRenderer.addRender("middlezone", block);
  }
  
  @InjectService("BiOSSComparator")
  IBiOSSComparator biossComparatorService;
  
  Object onSuccess() {
	  	  
		  //get selection parameters
		  
		  //create SelectionParameters object
		  SelectionParameters parameters = new SelectionParameters();
		  parameters.sampleSize = runconfiguration.sampleSize;
		  parameters.maxElementsPerSet = runconfiguration.maxNumberOfOntologies;
		  parameters.preferences = new Integer[3];
		  parameters.preferences[0] = getPreferenceAsInteger(runconfiguration.preferenceCoverage);
		  parameters.preferences[1] = getPreferenceAsInteger(runconfiguration.preferenceOverhead);
		  parameters.preferences[2] = getPreferenceAsInteger(runconfiguration.preferenceOverlap);
		  parameters.selectionType = runconfiguration.typeOfObjectToSelect;
		  parameters.scoreTypesToConsider = new ScoreType[3];
		  parameters.scoreTypesToConsider[0] = ScoreType.TERM_COVERAGE;
		  parameters.scoreTypesToConsider[1] = ScoreType.CLASS_OVERHEAD;
		  parameters.scoreTypesToConsider[2] = ScoreType.CLASS_OVERLAP;
		  
		  String inputTerms = runconfiguration.keywords;
		  
		  System.out.println();
		  
		  //calling JOYCE to retrieve the results and transforming these to something to display
		  transformJOYCEResult(biossComparatorService.getADOSResults(inputTerms, parameters));
	//	  transformJOYCEResult(null);
		  
	      return this;
  }

  public Date getCurrentTime()
  {
    return new Date();
  }
  
  public List<Result> getResults()
  {  
      return gridResults;
  }
  
  private Integer getPreferenceAsInteger(Preference pref) {
	  if(pref.compareTo(Preference.LOW)==0) {
		  return 0;
	  } else if(pref.compareTo(Preference.MEDIUM)==0) {
		  return 1;
	  } else if(pref.compareTo(Preference.HIGH)==0) {
		  return 2;
	  }
	  
	  return null;
  }
  
  private void transformJOYCEResult(List<OntologySet> joyceResults) {
	  System.out.println("transformJOYCEResult");
	  
	  gridResults = new ArrayList<Result>();
	  
	  //iterate over the retrieved results
	  for( OntologySet result : joyceResults) {
		  
		  System.out.println("processing result ...");
		  
		  Result r = new Result();
		  
		  //create and set list of ontologies contained in this result 
		  String ontologies = "";
		  Iterator<IOntology> it = result.getOntologies().iterator();
		  while(it.hasNext()) {
			  ontologies += it.next().getId();
			  System.out.println("ontologies=" + ontologies);
			  if(it.hasNext()) {
				  ontologies += ", ";
			  }
		  }
		  r.ontologies = ontologies;
		  
		  //set number of ontologies contained in this result
		  r.numOfOntologies = result.getOntologies().size();
		  
		  //set coverage score of this result
		  r.coverage = getRoundedPercentage(result.getScores().get(ScoreType.TERM_COVERAGE).doubleValue(), false);
		  
		  //set overhead score of this result
		  r.overhead = getRoundedPercentage(result.getScores().get(ScoreType.CLASS_OVERHEAD).doubleValue(), true);
		  
		  //set overlap score of this result
		  r.overlap = getRoundedPercentage(result.getScores().get(ScoreType.CLASS_OVERLAP).doubleValue(), true);
		  
		  gridResults.add(r);
		  System.out.println("added one result");
	  }
	  
//	  Result r1 = new Result();
//	  r1.ontologies = "ENVO, GO, OBOE";
//	  r1.numOfOntologies = 3;
//	  r1.coverage = 0.7;
//	  r1.overhead = 0.1;
//	  r1.overlap = 0.05;
//	  gridResults.add(r1);
//	  
//	  Result r2 = new Result();
//	  r2.ontologies = "ENVO, GO, OBOE, CHEBI";
//	  r2.numOfOntologies = 4;
//	  r2.coverage = 0.7;
//	  r2.overhead = 0.1;
//	  r2.overlap = 0.05;
//	  gridResults.add(r2);
  }
  
  private static double getRoundedPercentage(double score, boolean negative) {
	    BigDecimal bdScore = new BigDecimal(score*100.0);
	    bdScore = bdScore.setScale(0, RoundingMode.HALF_UP);
	    if(negative){bdScore = bdScore.abs();}
	    return bdScore.doubleValue();
	}
}