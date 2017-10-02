package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;

/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter} instances.
 *
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter
 *
 */
public class SliParameterBuilder implements Builder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter> {

    private java.lang.Integer _intValue;
    private SliParameterKey _key;
    private java.lang.String _parameterName;
    private java.lang.String _stringValue;
    private java.lang.Boolean _booleanValue;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> augmentation = Collections.emptyMap();

    public SliParameterBuilder() {
    }
    public SliParameterBuilder(org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting arg) {
        this._parameterName = arg.getParameterName();
        this._intValue = arg.getIntValue();
        this._stringValue = arg.getStringValue();
        this._booleanValue = arg.isBooleanValue();
    }

    public SliParameterBuilder(SliParameter base) {
        if (base.getKey() == null) {
            this._key = new SliParameterKey(
                base.getParameterName()
            );
            this._parameterName = base.getParameterName();
        } else {
            this._key = base.getKey();
            this._parameterName = _key.getParameterName();
        }
        this._intValue = base.getIntValue();
        this._stringValue = base.getStringValue();
        this._booleanValue = base.isBooleanValue();
        if (base instanceof SliParameterImpl) {
            SliParameterImpl impl = (SliParameterImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting) {
            this._parameterName = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting)arg).getParameterName();
            this._intValue = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting)arg).getIntValue();
            this._stringValue = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting)arg).getStringValue();
            this._booleanValue = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting)arg).isBooleanValue();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ParameterSetting] \n" +
              "but was: " + arg
            );
        }
    }

    public java.lang.Integer getIntValue() {
        return _intValue;
    }
    
    public SliParameterKey getKey() {
        return _key;
    }
    
    public java.lang.String getParameterName() {
        return _parameterName;
    }
    
    public java.lang.String getStringValue() {
        return _stringValue;
    }
    
    public java.lang.Boolean isBooleanValue() {
        return _booleanValue;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public SliParameterBuilder setIntValue(final java.lang.Integer value) {
        this._intValue = value;
        return this;
    }
    
     
    public SliParameterBuilder setKey(final SliParameterKey value) {
        this._key = value;
        return this;
    }
    
     
    public SliParameterBuilder setParameterName(final java.lang.String value) {
        this._parameterName = value;
        return this;
    }
    
     
    public SliParameterBuilder setStringValue(final java.lang.String value) {
        this._stringValue = value;
        return this;
    }
    
     
    public SliParameterBuilder setBooleanValue(final java.lang.Boolean value) {
        this._booleanValue = value;
        return this;
    }
    
    public SliParameterBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public SliParameterBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public SliParameter build() {
        return new SliParameterImpl(this);
    }

    private static final class SliParameterImpl implements SliParameter {

        @Override
        public java.lang.Class<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter.class;
        }

        private final java.lang.Integer _intValue;
        private final SliParameterKey _key;
        private final java.lang.String _parameterName;
        private final java.lang.String _stringValue;
        private final java.lang.Boolean _booleanValue;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> augmentation = Collections.emptyMap();

        private SliParameterImpl(SliParameterBuilder base) {
            if (base.getKey() == null) {
                this._key = new SliParameterKey(
                    base.getParameterName()
                );
                this._parameterName = base.getParameterName();
            } else {
                this._key = base.getKey();
                this._parameterName = _key.getParameterName();
            }
            this._intValue = base.getIntValue();
            this._stringValue = base.getStringValue();
            this._booleanValue = base.isBooleanValue();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.Integer getIntValue() {
            return _intValue;
        }
        
        @Override
        public SliParameterKey getKey() {
            return _key;
        }
        
        @Override
        public java.lang.String getParameterName() {
            return _parameterName;
        }
        
        @Override
        public java.lang.String getStringValue() {
            return _stringValue;
        }
        
        @Override
        public java.lang.Boolean isBooleanValue() {
            return _booleanValue;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_intValue);
            result = prime * result + Objects.hashCode(_key);
            result = prime * result + Objects.hashCode(_parameterName);
            result = prime * result + Objects.hashCode(_stringValue);
            result = prime * result + Objects.hashCode(_booleanValue);
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
            if (!org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter other = (org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter)obj;
            if (!Objects.equals(_intValue, other.getIntValue())) {
                return false;
            }
            if (!Objects.equals(_key, other.getKey())) {
                return false;
            }
            if (!Objects.equals(_parameterName, other.getParameterName())) {
                return false;
            }
            if (!Objects.equals(_stringValue, other.getStringValue())) {
                return false;
            }
            if (!Objects.equals(_booleanValue, other.isBooleanValue())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                SliParameterImpl otherImpl = (SliParameterImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameter>> e : augmentation.entrySet()) {
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
            java.lang.String name = "SliParameter [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder (name);
            if (_intValue != null) {
                builder.append("_intValue=");
                builder.append(_intValue);
                builder.append(", ");
            }
            if (_key != null) {
                builder.append("_key=");
                builder.append(_key);
                builder.append(", ");
            }
            if (_parameterName != null) {
                builder.append("_parameterName=");
                builder.append(_parameterName);
                builder.append(", ");
            }
            if (_stringValue != null) {
                builder.append("_stringValue=");
                builder.append(_stringValue);
                builder.append(", ");
            }
            if (_booleanValue != null) {
                builder.append("_booleanValue=");
                builder.append(_booleanValue);
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
