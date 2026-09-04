package eu.orchestrator.common.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public final class NullCheckUtil {

    /**
     * Checks whether an object is empty, in a null safe manner Checks cases of String, Collection, Map, Array objects
     *
     * @param value The object to check
     * @return <tt>true</tt> if the object is empty or <tt>null</tt>
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            return (((String) value).trim().length() == 0);
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        } else {
            return value.getClass().isArray() && (Array.getLength(value) == 0);
        }
    }

    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

}
