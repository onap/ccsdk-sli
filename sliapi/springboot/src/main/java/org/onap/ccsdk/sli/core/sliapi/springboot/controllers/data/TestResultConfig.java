package org.onap.ccsdk.sli.core.sliapi.springboot.controllers.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Entity
public class TestResultConfig {


    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private String testIdentifier;
    private String results;

    public TestResultConfig()
    {

    }
    public TestResultConfig(String testIdentifier, String results) {
        this.testIdentifier = testIdentifier;
        this.results = results;
    }

    public String getTestIdentifier() {
        return testIdentifier;
    }

    public void setTestIdentifier(String testIdentifier) {
        this.testIdentifier = testIdentifier;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }




}
