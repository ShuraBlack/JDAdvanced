package de.shurablack.mapping;

import java.util.*;

/**
 * <p>
 * A {@link HashMap}-backed {@link Map} that additionally keeps a reverse index from each value to the keys
 * mapped to it, so all keys sharing a value can be looked up with {@link #getKeys(Object)}.
 * <br><br>
 * Not thread-safe.
 * </p>
 *
 * @param <K> is the type of the keys
 * @param <V> is the type of the values
 *
 * @version mapping-1.0.0
 * @date 12.06.2023
 * @author ShuraBlack
 */
public class MultiKeyMap<K, V> implements Map<K, V> {

    /** The mapping from keys to values */
    private final Map<K, V> map;

    /** The reverse mapping from each value to the keys that were put with it */
    private final Map<V, List<K>> reverseMap;

    /**
     * Creates an empty map.
     */
    public MultiKeyMap() {
        map = new HashMap<>();
        reverseMap = new HashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(final Object key) {
        return map.get(key);
    }

    /**
     * Maps the key to the value and adds the key to the value's key list.
     * <br><br>
     * The key isn't removed from the key list of a previous value, and putting the same pair twice
     * adds the key twice.
     */
    @Override
    public V put(final K key, final V value) {
        final List<K> keys = reverseMap.computeIfAbsent(value, k -> new ArrayList<>());
        keys.add(key);

        return map.put(key, value);
    }

    /** Removes the key from the map and from the key list of its value. */
    @Override
    public V remove(final Object key) {
        final V value = map.get(key);
        final List<K> keys = reverseMap.get(value);
        if (keys != null) {
            keys.remove(key);
        }

        return map.remove(key);
    }

    /** Calls {@link #put(Object, Object)} for every entry. */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /** Removes all mappings, including the reverse index. */
    @Override
    public void clear() {
        map.clear();
        reverseMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * Returns all keys associated with the specified value.
     * @param value the specified value
     * @return the live list of keys, or {@code null} if the value was never put; may be empty after removals
     */
    public List<K> getKeys(final V value) {
        return reverseMap.get(value);
    }
}
