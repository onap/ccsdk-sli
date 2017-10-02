package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Identifiable;

/**
 * <p>This class represents the following YANG schema fragment defined in module <b>SLI-API</b>
 * <pre>
 * list test-result {
 *     key "test-identifier"
 *     leaf test-identifier {
 *         type string;
 *     }
 *     leaf-list results {
 *         type string;
 *     }
 * }
 * </pre>
 * The schema path to identify an instance is
 * <i>SLI-API/test-results/test-result</i>
 *
 * <p>To create instances of this class use {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultBuilder}.
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultBuilder
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultKey
 *
 */
public interface TestResult
    extends
    ChildOf<TestResults>,
    Augmentable<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResult>,
    Identifiable<TestResultKey>
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.create("org:onap:ccsdk:sli:core:sliapi",
        "2016-11-10", "test-result").intern();

    /**
     * @return <code>java.lang.String</code> <code>testIdentifier</code>, or <code>null</code> if not present
     */
    java.lang.String getTestIdentifier();
    
    /**
     * @return <code>java.util.List</code> <code>results</code>, or <code>null</code> if not present
     */
    List<java.lang.String> getResults();
    
    /**
     * Returns Primary Key of Yang List Type
     *
     *
     *
     * @return <code>org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultKey</code> <code>key</code>, or <code>null</code> if not present
     */
    TestResultKey getKey();

}

