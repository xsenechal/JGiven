package com.tngtech.jgiven.report.json;

import java.io.File;
import java.util.Iterator;

import com.google.gson.JsonSyntaxException;
import com.tngtech.jgiven.exception.JGivenWrongUsageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tngtech.jgiven.report.ReportGenerator;
import com.tngtech.jgiven.report.model.CompleteReportModel;
import com.tngtech.jgiven.report.model.ReportModel;
import com.tngtech.jgiven.report.model.ReportModelFile;
import com.tngtech.jgiven.report.model.ScenarioCaseModel;
import com.tngtech.jgiven.report.model.ScenarioModel;

public class ReportModelReader implements ReportModelFileHandler {
    private static final Logger log = LoggerFactory.getLogger( ReportModelReader.class );
    private final ReportGenerator.Config config;

    private CompleteReportModel completeModelReport = new CompleteReportModel();

    public ReportModelReader( ReportGenerator.Config config ) {
        this.config = config;
    }

    public CompleteReportModel readDirectory( File sourceDir ) {
        try {
            new JsonModelTraverser().traverseModels(sourceDir, this);
        } catch (ScenarioJsonReader.JsonReaderException e) {
            throw new JGivenWrongUsageException("Error while reading file\n "+e.file+":\n "+e.getCause().getMessage()+".\n\n" +
                    "There are three reasons why this could happen: \n" +
                    "\n" +
                    "  1. You use a version of the JGiven report generator that is incompatible to the JGiven core version.\n" +
                    "     Please ensure that both versions are the same. \n" +
                    "  2. You did not specify the '--sourceDir' option and the JGiven report generator read JSON files that\n" +
                    "     have not been generated by JGiven.\n" +
                    "     Please set the option to a folder that only contains JSON files generated by JGiven\n" +
                    "  3. JGiven could not read the file for some other IO-related reason\n\n");
        }
        return completeModelReport;
    }

    public void handleReportModel( ReportModelFile modelFile ) {
        if( modelFile.model.getClassName() == null ) {
            log.error( "ClassName in report model is null for file " + modelFile.file + ". Skipping." );
            return;
        }

        if( config.getExcludeEmptyScenarios() ) {
            log.info( "Removing empty scenarios..." );
            removeEmptyScenarios( modelFile.model );
            if( !modelFile.model.getScenarios().isEmpty() ) {
                log.debug( "File " + modelFile.file + " has only empty scenarios. Skipping." );
                completeModelReport.addModelFile( modelFile );
            }
        } else {
            completeModelReport.addModelFile( modelFile );
        }
    }

    void removeEmptyScenarios( ReportModel modelFile ) {
        Iterator<ScenarioModel> scenarios = modelFile.getScenarios().iterator();
        while( scenarios.hasNext() ) {
            ScenarioModel scenarioModel = scenarios.next();
            removeEmptyCase( scenarioModel );
            if( scenarioModel.getScenarioCases().isEmpty() ) {
                scenarios.remove();
            }
        }
    }

    private void removeEmptyCase( ScenarioModel scenarioModel ) {
        Iterator<ScenarioCaseModel> cases = scenarioModel.getScenarioCases().iterator();
        while( cases.hasNext() ) {
            ScenarioCaseModel theCase = cases.next();
            if( theCase.getSteps().isEmpty() ) {
                cases.remove();
            }
        }
    }

}
