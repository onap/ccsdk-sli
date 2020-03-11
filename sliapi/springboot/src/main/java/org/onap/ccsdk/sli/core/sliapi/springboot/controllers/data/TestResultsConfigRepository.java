package org.onap.ccsdk.sli.core.sliapi.springboot.controllers.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TestResultsConfigRepository extends CrudRepository<TestResultConfig, Long> {

    List<TestResultConfig> findByTestIdentifier(String testIdentifier);


}
