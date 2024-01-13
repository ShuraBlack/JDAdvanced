package de.shurablack.mapping;

import java.util.*;

/**
 * <p>
 * The MultiKeyMap class is a generic implementation of a map that allows multiple keys to be associated with a single value.
 * <br><br>
 * It extends the Map interface and provides methods for adding, removing, and accessing elements in the map.
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

    /**
     * The underlying map implementation used by the MultiKeyMap class.
     * <br><br>
     * It stores the mapping from keys to values.
     */
    private final Map<K, V> map;

    /**
     * A reverse mapping from values to lists of keys.
     * It is used to store the list of keys associated with each value in the map.
     */
    private final Map<V, List<K>> reverseMap;

    /**
     * This is the constructor for the MultiKeyMap class.
     * It initializes the map and reverseMap fields using a HashMap implementation.
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

    @Override
    public V put(final K key, final V value) {
        final List<K> keys = reverseMap.computeIfAbsent(value, k -> new ArrayList<>());
        keys.add(key);

        return map.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        final V value = map.get(key);
        final List<K> keys = reverseMap.get(value);
        if (keys != null) {
            keys.remove(key);
        }

        return map.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

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
     * Returns a list of all keys associated with the specified value
     * @param value the specified value
     * @return a list if K objects
     */
    public List<K> getKeys(final V value) {
        return reverseMap.get(value);
    }
}
