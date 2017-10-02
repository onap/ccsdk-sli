package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.AugmentationHolder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import java.util.HashMap;
import org.opendaylight.yangtools.concepts.Builder;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;

/**
 * Class that builds {@link org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput} instances.
 *
 * @see org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput
 *
 */
public class ExecuteGraphOutputBuilder implements Builder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput> {

    private java.lang.String _ackFinalIndicator;
    private java.lang.String _contextMemoryJson;
    private java.lang.String _responseCode;
    private java.lang.String _responseMessage;

    Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> augmentation = Collections.emptyMap();

    public ExecuteGraphOutputBuilder() {
    }
    public ExecuteGraphOutputBuilder(org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields arg) {
        this._responseCode = arg.getResponseCode();
        this._ackFinalIndicator = arg.getAckFinalIndicator();
        this._responseMessage = arg.getResponseMessage();
        this._contextMemoryJson = arg.getContextMemoryJson();
    }

    public ExecuteGraphOutputBuilder(ExecuteGraphOutput base) {
        this._ackFinalIndicator = base.getAckFinalIndicator();
        this._contextMemoryJson = base.getContextMemoryJson();
        this._responseCode = base.getResponseCode();
        this._responseMessage = base.getResponseMessage();
        if (base instanceof ExecuteGraphOutputImpl) {
            ExecuteGraphOutputImpl impl = (ExecuteGraphOutputImpl) base;
            if (!impl.augmentation.isEmpty()) {
                this.augmentation = new HashMap<>(impl.augmentation);
            }
        } else if (base instanceof AugmentationHolder) {
            @SuppressWarnings("unchecked")
            AugmentationHolder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput> casted =(AugmentationHolder<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>) base;
            if (!casted.augmentations().isEmpty()) {
                this.augmentation = new HashMap<>(casted.augmentations());
            }
        }
    }

    /**
     *Set fields from given grouping argument. Valid argument is instance of one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields</li>
     * </ul>
     *
     * @param arg grouping object
     * @throws IllegalArgumentException if given argument is none of valid types
    */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields) {
            this._responseCode = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields)arg).getResponseCode();
            this._ackFinalIndicator = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields)arg).getAckFinalIndicator();
            this._responseMessage = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields)arg).getResponseMessage();
            this._contextMemoryJson = ((org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields)arg).getContextMemoryJson();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
              "expected one of: [org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ResponseFields] \n" +
              "but was: " + arg
            );
        }
    }

    public java.lang.String getAckFinalIndicator() {
        return _ackFinalIndicator;
    }
    
    public java.lang.String getContextMemoryJson() {
        return _contextMemoryJson;
    }
    
    public java.lang.String getResponseCode() {
        return _responseCode;
    }
    
    public java.lang.String getResponseMessage() {
        return _responseMessage;
    }
    
    @SuppressWarnings("unchecked")
    public <E extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

     
    public ExecuteGraphOutputBuilder setAckFinalIndicator(final java.lang.String value) {
        this._ackFinalIndicator = value;
        return this;
    }
    
     
    public ExecuteGraphOutputBuilder setContextMemoryJson(final java.lang.String value) {
        this._contextMemoryJson = value;
        return this;
    }
    
     
    public ExecuteGraphOutputBuilder setResponseCode(final java.lang.String value) {
        this._responseCode = value;
        return this;
    }
    
     
    public ExecuteGraphOutputBuilder setResponseMessage(final java.lang.String value) {
        this._responseMessage = value;
        return this;
    }
    
    public ExecuteGraphOutputBuilder addAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> augmentationType, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
    
        if (!(this.augmentation instanceof HashMap)) {
            this.augmentation = new HashMap<>();
        }
    
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }
    
    public ExecuteGraphOutputBuilder removeAugmentation(java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> augmentationType) {
        if (this.augmentation instanceof HashMap) {
            this.augmentation.remove(augmentationType);
        }
        return this;
    }

    @Override
    public ExecuteGraphOutput build() {
        return new ExecuteGraphOutputImpl(this);
    }

    private static final class ExecuteGraphOutputImpl implements ExecuteGraphOutput {

        @Override
        public java.lang.Class<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput> getImplementedInterface() {
            return org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput.class;
        }

        private final java.lang.String _ackFinalIndicator;
        private final java.lang.String _contextMemoryJson;
        private final java.lang.String _responseCode;
        private final java.lang.String _responseMessage;

        private Map<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> augmentation = Collections.emptyMap();

        private ExecuteGraphOutputImpl(ExecuteGraphOutputBuilder base) {
            this._ackFinalIndicator = base.getAckFinalIndicator();
            this._contextMemoryJson = base.getContextMemoryJson();
            this._responseCode = base.getResponseCode();
            this._responseMessage = base.getResponseMessage();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> e = base.augmentation.entrySet().iterator().next();
                this.augmentation = Collections.<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>>singletonMap(e.getKey(), e.getValue());
                break;
            default :
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.String getAckFinalIndicator() {
            return _ackFinalIndicator;
        }
        
        @Override
        public java.lang.String getContextMemoryJson() {
            return _contextMemoryJson;
        }
        
        @Override
        public java.lang.String getResponseCode() {
            return _responseCode;
        }
        
        @Override
        public java.lang.String getResponseMessage() {
            return _responseMessage;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> E getAugmentation(java.lang.Class<E> augmentationType) {
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
            result = prime * result + Objects.hashCode(_ackFinalIndicator);
            result = prime * result + Objects.hashCode(_contextMemoryJson);
            result = prime * result + Objects.hashCode(_responseCode);
            result = prime * result + Objects.hashCode(_responseMessage);
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
            if (!org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput.class.equals(((DataObject)obj).getImplementedInterface())) {
                return false;
            }
            org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput other = (org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput)obj;
            if (!Objects.equals(_ackFinalIndicator, other.getAckFinalIndicator())) {
                return false;
            }
            if (!Objects.equals(_contextMemoryJson, other.getContextMemoryJson())) {
                return false;
            }
            if (!Objects.equals(_responseCode, other.getResponseCode())) {
                return false;
            }
            if (!Objects.equals(_responseMessage, other.getResponseMessage())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                ExecuteGraphOutputImpl otherImpl = (ExecuteGraphOutputImpl) obj;
                if (!Objects.equals(augmentation, otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>>, Augmentation<org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.ExecuteGraphOutput>> e : augmentation.entrySet()) {
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
            java.lang.String name = "ExecuteGraphOutput [";
            java.lang.StringBuilder builder = new java.lang.StringBuilder (name);
            if (_ackFinalIndicator != null) {
                builder.append("_ackFinalIndicator=");
                builder.append(_ackFinalIndicator);
                builder.append(", ");
            }
            if (_contextMemoryJson != null) {
                builder.append("_contextMemoryJson=");
                builder.append(_contextMemoryJson);
                builder.append(", ");
            }
            if (_responseCode != null) {
                builder.append("_responseCode=");
                builder.append(_responseCode);
                builder.append(", ");
            }
            if (_responseMessage != null) {
                builder.append("_responseMessage=");
                builder.append(_responseMessage);
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
