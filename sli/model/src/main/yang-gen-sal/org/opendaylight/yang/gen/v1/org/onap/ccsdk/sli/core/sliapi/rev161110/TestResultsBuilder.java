package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.List;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.test.results.TestResult;
import java.util.Collections;
import java.util.Map;

/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults} instances.
 *
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults
 *
 */
public class TestResultsBuilder implements Builder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults> {

    private List<TestResult> _testResult;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> augmentation = Collections.emptyMap();

    public TestResultsBuilder() {
    }

    public TestResultsBuilder(TestResults base) {
        this._testResult = base.getTestResult();
        if (base instanceof TestResultsImpl) {
            TestResultsImpl impl = (TestResultsImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }


    public List<TestResult> getTestResult() {
        return _testResult;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public TestResultsBuilder setTestResult(final List<TestResult> value) {
        this._testResult = value;
        return this;
    }
    
    public TestResultsBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public TestResultsBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public TestResults build() {
        return new TestResultsImpl(this);
    }

    private static final class TestResultsImpl implements TestResults {

        @Override
        public java.lang.Class<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults.class;
        }

        private final List<TestResult> _testResult;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> augmentation = Collections.emptyMap();

        private TestResultsImpl(TestResultsBuilder base) {
            this._testResult = base.getTestResult();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public List<TestResult> getTestResult() {
            return _testResult;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> E getAugmentation(java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        private int hash = 0;
        private volatile boolean hashValid = false;
        
        @Override
        public int hashCode() {
            if (hashValid) {
                return hash;
            }
        
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(_testResult);
            result = prime * result + Objects.hashCode(augmentation);
        
            hash = result;
            hashValid = true;
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults other = (org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults)obj;
            if (!Objects.equals(_testResult, other.getTestResult())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                TestResultsImpl otherImpl = (TestResultsImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.TestResults>> e : augmentation.entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.String name = "TestResults [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder (name);
            if (_testResult != null) {
                builder.append("_testResult=");
                builder.append(_testResult);
            }
            final int builderLength = builder.length();
            final int builderAdditionalLength = builder.substring(name.length(), builderLength).length();
            if (builderAdditionalLength > 2 && !builder.substring(builderLength - 2, builderLength).equals(", ")) {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

}
