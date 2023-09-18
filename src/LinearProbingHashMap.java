import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.ArrayList;

/**
 * Implementation of a LinearProbingHashMap.
 *
 * @author Thang Huynh
 * @version 1.0
 *
 */
public class LinearProbingHashMap<K, V> {

    /**
     * The initial capacity of the LinearProbingHashMap when created with the
     * default constructor.
     */
    public static final int INITIAL_CAPACITY = 13;

    /**
     * The max load factor of the LinearProbingHashMap
     */
    public static final double MAX_LOAD_FACTOR = 0.67;

    private LinearProbingMapEntry<K, V>[] table;
    private int size;

    /**
     * Constructs a new LinearProbingHashMap.
     *
     * The backing array should have an initial capacity of INITIAL_CAPACITY.
     *
     * Using constructor chaining.
     */
    public LinearProbingHashMap() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Constructs a new LinearProbingHashMap.
     *
     * The backing array should have an initial capacity of initialCapacity.
     *
     * @param initialCapacity the initial capacity of the backing array
     */
    public LinearProbingHashMap(int initialCapacity) {
        table = new LinearProbingMapEntry[initialCapacity];
        size = 0;
    }

    /**
     * Adds the given key-value pair to the map. If an entry in the map
     * already has this key, replace the entry's value with the new one
     * passed in.
     *
     * In the case of a collision, we will use linear probing as resolution
     * strategy.
     *
     * Before actually adding any data to the HashMap, check to
     * see if the array would violate the max load factor if the data was
     * added. For example, let's say the array is of length 5 and the current
     * size is 3 (LF = 0.6). For this example, assume that no elements are
     * removed in between steps. If another entry is attempted to be added,
     * before doing anything else, check whether (3 + 1) / 5 = 0.8
     * is larger than the max LF. It is, so you would trigger a resize before
     * you even attempt to add the data or figure out if it's a duplicate.
     *
     * When regrowing, resize the length of the backing table to
     * 2 * old length + 1. Uses the resizeBackingTable method to do so.
     *
     * Return null if the key was not already in the map. If it was in the map,
     * return the old value associated with it.
     *
     * @param key   the key to add
     * @param value the value to add
     * @return null if the key was not already in the map. If it was in the
     * map, return the old value associated with it
     * @throws java.lang.IllegalArgumentException if key or value is null
     */
    public V put(K key, V value) {
        //The Exception
        if (key == null || value == null) {
            throw new IllegalArgumentException("The entered key or value was null");
        }

        //Checks loadFactor and Resizes if needed
        double loadFactor = (double) (size + 1) / table.length;
        if (loadFactor >= MAX_LOAD_FACTOR) {
            resizeBackingTable(2 * table.length + 1);
        }

        LinearProbingMapEntry<K, V> theEntry = new LinearProbingMapEntry<>(key, value);
        int theHashcode = theEntry.getKey().hashCode(); //Converts Key to hashcode
        int index = Math.abs(theHashcode % table.length); //Corresponding Index we will insert at

        //Case with no collision
        if (table[index] == null) {
            table[index] = theEntry;
            size++;
        } else if (table[index] != null) {

            int i = 0;
            boolean removedMarker = false;
            int removedIndex = 0;
            int indexToProbe = Math.abs((index + i) % table.length);

            //Looks at the index and then probes next spot until you find an open one
            //Or once you reach size of non-removed entries
            while (table[indexToProbe] != null) {
                if (i == size) {
                    break;
                }
                //The Case of the duplicate key
                if (key == table[indexToProbe].getKey()) {
                    V originalValue = table[indexToProbe].getValue(); //Saves old entry's value
                    table[indexToProbe] = theEntry; //Replace old entry with new entry
                    return originalValue;
                }

                //Case of the Removed Marker
                if (table[indexToProbe].isRemoved() && !removedMarker) {
                    removedMarker = true;
                    removedIndex = indexToProbe; //Saves the index of the removed marker
                }
                i++;
                indexToProbe = Math.abs((index + i) % table.length); //Updates indexToProbe
            }

            //Case of finding a null spot or removed marker
            if (removedMarker) {
                indexToProbe = removedIndex;
            }
            table[indexToProbe] = theEntry;
            size++;
        }

        return null;
    }

    /**
     * Removes the entry with a matching key from map by marking the entry as
     * removed.
     *
     * @param key the key to remove
     * @return the value previously associated with the key
     * @throws java.lang.IllegalArgumentException if key is null
     * @throws java.util.NoSuchElementException   if the key is not in the map
     */
    public V remove(K key) {
        //The Exception
        if (key == null) {
            throw new IllegalArgumentException("The key entered was null");
        }

        int theHashcode = key.hashCode(); //Converts Key to hashcode
        int index = Math.abs(theHashcode % table.length); //Corresponding Index we will remove at
        V removedValue = null;

        //The Exception
        if (table[index] == null) {
            throw new NoSuchElementException("The key was not present in the map");
        }

        //Case: Found Key at Index and remove flag is false
        if (key == table[index].getKey() && !table[index].isRemoved()) {
            removedValue = table[index].getValue(); //Sets the val to return
            table[index].setRemoved(true); //Sets removal status is true
            size--;
        }

        //Case: Collision Looking for Key (Probing)
        if (key != table[index].getKey()) {
            int i = 0;
            int indexToProbe = Math.abs((index + i) % table.length);
            while (key != table[indexToProbe].getKey() && !table[indexToProbe].isRemoved()) {
                i++;
                indexToProbe = Math.abs((index + i) % table.length); //Updates indexToProbe
                //The Exception - Key not Found in the map
                if (table[indexToProbe] == null) {
                    throw new NoSuchElementException("The key was not present in the map");
                }
            }

            //Case: Found Key at indexToProbe and remove flag is false
            removedValue = table[indexToProbe].getValue(); //Sets the val to return
            table[indexToProbe].setRemoved(true); //Sets removal status is true
            size--;
        }
        return removedValue;
    }

    /**
     * Gets the value associated with the given key.
     *
     * @param key the key to search for in the map
     * @return the value associated with the given key
     * @throws java.lang.IllegalArgumentException if key is null
     * @throws java.util.NoSuchElementException   if the key is not in the map
     */
    public V get(K key) {
        //The Exception
        if (key == null) {
            throw new IllegalArgumentException("The key entered was null");
        }
        int theHashcode = key.hashCode(); //Converts Key to hashcode
        int index = Math.abs(theHashcode % table.length); //Corresponding Index we will look search at

        //The Exception - Key not Found in the map
        if (table[index] == null) {
            throw new NoSuchElementException("The key was not present in the map");
        }
        boolean pos = true;
        if (table[index].getKey() == key && !table[index].isRemoved()) {
            return table[index].getValue();
        } else {
            int i = 0;
            int indexToProbe = Math.abs((index + i) % table.length);
            while (key != table[indexToProbe].getKey()) {
                i++;
                indexToProbe = Math.abs((index + i) % table.length); //Updates indexToProbe
                //The Exception - Key not Found in the map
                if (table[indexToProbe] == null) {
                    throw new NoSuchElementException("The key was not present in the map");
                }
            }
            //Case: Found Key but its status is removed
            if (table[indexToProbe].isRemoved()) {
                throw new NoSuchElementException("The key was not present in the map");
            }
            //Case: Found Key at indexToProbe and remove flag is false
            return table[indexToProbe].getValue();
        }

    }

    /**
     * Returns whether or not the key is in the map.
     *
     * @param key the key to search for in the map
     * @return true if the key is contained within the map, false
     * otherwise
     * @throws java.lang.IllegalArgumentException if key is null
     */
    public boolean containsKey(K key) {
        //The Exception
        if (key == null) {
            throw new IllegalArgumentException("The key entered was null");
        }
        int theHashcode = key.hashCode(); //Converts Key to hashcode
        int index = Math.abs(theHashcode % table.length); //Corresponding Index we will look search at

        if (table[index] == null) {
            return false;
        } else if (table[index].getKey() == key && !table[index].isRemoved()) {
            return true;
        } else {
            int i = 0;
            int indexToProbe = Math.abs((index + i) % table.length);
            while (key != table[indexToProbe].getKey()) {
                i++;
                indexToProbe = Math.abs((index + i) % table.length); //Updates indexToProbe
                //Case: Key not Found in the map
                if (table[indexToProbe] == null) {
                    return false;
                }
            }

            //Case: Found Key but its status is removed
            if (table[indexToProbe].isRemoved()) {
                return false;
            }
            //Case: Found Key at indexToProbe and remove flag is false
            return true;
        }
    }

    /**
     * Returns a Set view of the keys contained in this map.
     *
     * Use java.util.HashSet.
     *
     * @return the set of keys in this map
     */
    public Set<K> keySet() {
        HashSet<K> theSetView = new HashSet<>(table.length);
        for (LinearProbingMapEntry<K, V> i: table) {
            if (i != null && !i.isRemoved()) {
                theSetView.add(i.getKey());
            }
        }
        return theSetView;
    }

    /**
     * Returns a List view of the values contained in this map.
     *
     *
     * Iterates over the table in order of increasing index and add
     * entries to the List in the order in which they are traversed.
     *
     * @return list of values in this map
     */
    public List<V> values() {
        ArrayList<V> theValueList = new ArrayList<>(table.length);
        for (LinearProbingMapEntry<K, V> i: table) {
            if (i != null && !i.isRemoved()) {
                theValueList.add(i.getValue());
            }
        }
        return theValueList;
    }

    /**
     * Resize the backing table to length.
     *
     * We Disregard the load factor for this method. So, if the passed in length is
     * smaller than the current capacity, and this new length causes the table's
     * load factor to exceed MAX_LOAD_FACTOR, should still resize the table
     * to the specified length and leave it at that capacity.
     *
     * Iterates over the old table in order of increasing index and
     * add entries to the new table in the order in which they are traversed. 
     *
     * Since resizing the backing table is working with the non-duplicate
     * data already in the table, we shouldn't have tp explicitly check for
     * duplicates.
     *
     *
     * @param length new length of the backing table
     * @throws java.lang.IllegalArgumentException if length is less than the
     *                                            number of items in the hash
     *                                            map
     */
    public void resizeBackingTable(int length) {
        if (length < size) {
            throw new IllegalArgumentException("The length entered is smaller than the size of the hashmap");
        }

        LinearProbingMapEntry<K, V>[] newTable = new LinearProbingMapEntry[length];
        for (LinearProbingMapEntry<K, V> anEntry: table) {
            if (anEntry != null && !anEntry.isRemoved()) {
                int anIndex = Math.abs(anEntry.getKey().hashCode() % newTable.length);

                //Case of Empty Spot
                if (newTable[anIndex] == null) {
                    newTable[anIndex] = anEntry;
                } else { //Case of Collision
                    int i = 0;
                    int indexToProbe = Math.abs((anIndex + i) % table.length);
                    //Looks at the index and then probes next spot until you find an open one
                    while (table[indexToProbe] != null) {
                        i++;
                        indexToProbe = Math.abs((anIndex + i) % table.length);
                    }
                    newTable[indexToProbe] = anEntry;
                }
            }

        }
        table = newTable;
    }

    /**
     * Clears the map.
     *
     * Resets the table to a new array of the INITIAL_CAPACITY and resets the
     * size.
     *
     * Must be O(1).
     */
    public void clear() {
        table = new LinearProbingMapEntry[INITIAL_CAPACITY];
        size = 0;
    }

}
