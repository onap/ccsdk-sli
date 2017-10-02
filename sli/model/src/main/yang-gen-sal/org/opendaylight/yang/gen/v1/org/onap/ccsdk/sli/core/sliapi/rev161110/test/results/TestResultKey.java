package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results;
import org.opendaylight.yangtools.yang.binding.Identifier;
import java.util.Objects;

public class TestResultKey
 implements Identifier<TestResult> {
    private static final long serialVersionUID = 3857649555637491806L;
    private final java.lang.String _testIdentifier;


    public TestResultKey(java.lang.String _testIdentifier) {
    
    
        this._testIdentifier = _testIdentifier;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public TestResultKey(TestResultKey source) {
        this._testIdentifier = source._testIdentifier;
    }


    public java.lang.String getTestIdentifier() {
        return _testIdentifier;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(_testIdentifier);
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestResultKey other = (TestResultKey) obj;
        if (!Objects.equals(_testIdentifier, other._testIdentifier)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResultKey.class.getSimpleName()).append(" [");
        boolean first = true;
    
        if (_testIdentifier != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_testIdentifier=");
            builder.append(_testIdentifier);
         }
        return builder.append(']').toString();
    }
}

