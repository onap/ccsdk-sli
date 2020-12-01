package org.opendaylight.yang.gen.v1.test;

public enum CosModelType {
    _4COS(0, "4COS"),
    
    _6COS(1, "6COS")
    ;

    private static final java.util.Map<java.lang.Integer, CosModelType> VALUE_MAP;

    static {
        final com.google.common.collect.ImmutableMap.Builder<java.lang.Integer, CosModelType> b = com.google.common.collect.ImmutableMap.builder();
        for (CosModelType enumItem : CosModelType.values()) {
            b.put(enumItem.value, enumItem);
        }

        VALUE_MAP = b.build();
    }

    private final java.lang.String name;
    private final int value;

    private CosModelType(int value, java.lang.String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Returns the name of the enumeration item as it is specified in the input yang.
     *
     * @return the name of the enumeration item as it is specified in the input yang
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * @return integer value
     */
    public int getIntValue() {
        return value;
    }

    /**
     * @param valueArg integer value
     * @return corresponding CosModelType item
     */
    public static CosModelType forValue(int valueArg) {
        return VALUE_MAP.get(valueArg);
    }
}