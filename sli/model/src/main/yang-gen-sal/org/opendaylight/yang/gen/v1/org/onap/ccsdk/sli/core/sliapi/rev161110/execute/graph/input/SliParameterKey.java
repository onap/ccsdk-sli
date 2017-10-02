package org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input;
import org.opendaylight.yangtools.yang.binding.Identifier;
import java.util.Objects;

public class SliParameterKey
 implements Identifier<SliParameter> {
    private static final long serialVersionUID = 8929025111457627032L;
    private final java.lang.String _parameterName;


    public SliParameterKey(java.lang.String _parameterName) {
    
    
        this._parameterName = _parameterName;
    }
    
    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public SliParameterKey(SliParameterKey source) {
        this._parameterName = source._parameterName;
    }


    public java.lang.String getParameterName() {
        return _parameterName;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(_parameterName);
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
        SliParameterKey other = (SliParameterKey) obj;
        if (!Objects.equals(_parameterName, other._parameterName)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.org.onap.ccsdk.sli.core.sliapi.rev161110.execute.graph.input.SliParameterKey.class.getSimpleName()).append(" [");
        boolean first = true;
    
        if (_parameterName != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("_parameterName=");
            builder.append(_parameterName);
         }
        return builder.append(']').toString();
    }
}

