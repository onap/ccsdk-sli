package org.onap.ccsdk.sli.core.sliapi.springboot.controllers.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TestResultsOperationalRepository extends CrudRepository<TestResultOperational, Long> {

    List<TestResultOperational> findByTestIdentifier(String testIdentifier);


}
