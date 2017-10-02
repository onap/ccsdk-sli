package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.common.QName;
import java.util.List;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResult;
import org.opendaylight.yangtools.yang.binding.Augmentable;

/**
 * Test results
 *
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * container test-results {
 *     list test-result {
 *         key "test-identifier"
 *         leaf test-identifier {
 *             type string;
 *         }
 *         leaf-list results {
 *             type string;
 *         }
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/test-results</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResultsBuilder}.
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResultsBuilder
 *
 */
public interface TestResults
    extends
    ChildOf<SLIAPIData>,
    Augmentable<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "test-results").intern();

    /**
     * @return <code>java.util.List</code> <code>testResult</code>, or <code>null</code> if not present
     */
    List<TestResult> getTestResult();

}

