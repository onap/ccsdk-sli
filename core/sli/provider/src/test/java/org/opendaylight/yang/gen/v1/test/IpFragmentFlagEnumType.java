package org.opendaylight.yang.gen.v1.test;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
// import org.opendaylight.yangtools.yang.binding.Enumeration;

public enum IpFragmentFlagEnumType {
    DF(0, "DF"),
    
    ISF(1, "ISF"),
    
    FF(2, "FF"),
    
    LF(3, "LF")
    ;

    private static final Map<String, IpFragmentFlagEnumType> NAME_MAP;
    private static final Map<Integer, IpFragmentFlagEnumType> VALUE_MAP;

    static {
        final Builder<String, IpFragmentFlagEnumType> nb = ImmutableMap.builder();
        final Builder<Integer, IpFragmentFlagEnumType> vb = ImmutableMap.builder();
        for (IpFragmentFlagEnumType enumItem : IpFragmentFlagEnumType.values()) {
            vb.put(enumItem.value, enumItem);
            nb.put(enumItem.name, enumItem);
        }

        NAME_MAP = nb.build();
        VALUE_MAP = vb.build();
    }

    private final String name;
    private final int value;

    private IpFragmentFlagEnumType(int value, String name) {
        this.value = value;
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public int getIntValue() {
        return value;
    }

    /**
     * Return the enumeration member whose {@link #getName()} matches specified value.
     *
     * @param name YANG assigned name
     * @return corresponding IpFragmentFlagEnumType item, if present
     * @throws NullPointerException if name is null
     */
    public static Optional<IpFragmentFlagEnumType> forName(String name) {
        return Optional.ofNullable(NAME_MAP.get(Objects.requireNonNull(name)));
    }

    /**
     * Return the enumeration member whose {@link #getIntValue()} matches specified value.
     *
     * @param intValue integer value
     * @return corresponding IpFragmentFlagEnumType item, or null if no such item exists
     */
    public static IpFragmentFlagEnumType forValue(int intValue) {
        return VALUE_MAP.get(intValue);
    }
}
