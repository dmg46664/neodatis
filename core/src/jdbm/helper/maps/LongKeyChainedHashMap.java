/*
 *  Primitive Collections for Java.
 *  Copyright (C) 2003  S�ren Bak
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jdbm.helper.maps;

import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *  This class represents chained hash table based maps from
 *  long values to objects.
 *
 *  @see        LongKeyOpenHashMap
 *  @see        java.util.Map
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.2     21-08-2003 19:42
 *  @since      1.0
 *  
 *  ********* JDBM Project Note *************
 *  This class was extracted from the pcj project (with permission)
 *  for use in jdbm only.  Modifications to original were performed
 *  by Kevin Day to make it work outside of the pcj class structure. 
 *  
 */
final public class LongKeyChainedHashMap<E> extends AbstractLongKeyMap<E> implements LongKeyMap<E>, Cloneable, Serializable {

	private static final long serialVersionUID = 5732061193461471028L;

	/** Constant indicating relative growth policy. */
    private static final int    GROWTH_POLICY_RELATIVE      = 0;

    /** Constant indicating absolute growth policy. */
    private static final int    GROWTH_POLICY_ABSOLUTE      = 1;

    /**
     *  The default growth policy of this map.
     *  @see    #GROWTH_POLICY_RELATIVE
     *  @see    #GROWTH_POLICY_ABSOLUTE
     */
    private static final int    DEFAULT_GROWTH_POLICY       = GROWTH_POLICY_RELATIVE;

    /** The default factor with which to increase the capacity of this map. */
    public static final double DEFAULT_GROWTH_FACTOR        = 1.0;

    /** The default chunk size with which to increase the capacity of this map. */
    public static final int    DEFAULT_GROWTH_CHUNK         = 10;

    /** The default capacity of this map. */
    public static final int    DEFAULT_CAPACITY             = 11;

    /** The default load factor of this map. */
    public static final double DEFAULT_LOAD_FACTOR          = 0.75;

    /**
     *  The hash function used to hash keys in this map.
     *  @serial
     */
    private LongHashFunction keyhash;

    /**
     *  The size of this map.
     *  @serial
     */
    private int size;

    /** The hash table backing up this map. Contains linked Entry<E> values. */
    private transient Entry<E>[] data;

    /**
     *  The growth policy of this map (0 is relative growth, 1 is absolute growth).
     *  @serial
     */
    private int growthPolicy;

    /**
     *  The growth factor of this map, if the growth policy is
     *  relative.
     *  @serial
     */
    private double growthFactor;

    /**
     *  The growth chunk size of this map, if the growth policy is
     *  absolute.
     *  @serial
     */
    private int growthChunk;

    /**
     *  The load factor of this map.
     *  @serial
     */
    private double loadFactor;

    /**
     *  The next size at which to expand the data[].
     *  @serial
     */
    private int expandAt;

    /** A set view of the keys of this map. */
    private transient LongSet keys;

    /** A collection view of the values of this map. */
    private transient Collection<E> values;

    @SuppressWarnings("unchecked")
	private LongKeyChainedHashMap(LongHashFunction keyhash, int capacity, int growthPolicy, double growthFactor, int growthChunk, double loadFactor) {
        if (keyhash == null)
            Exceptions.nullArgument("hash function");
        if (capacity < 0)
            Exceptions.negativeArgument("capacity", String.valueOf(capacity));
        if (growthFactor < 0.0)
            Exceptions.negativeArgument("growthFactor", String.valueOf(growthFactor));
        if (growthChunk < 0)
            Exceptions.negativeArgument("growthChunk", String.valueOf(growthChunk));
        if (loadFactor <= 0.0)
            Exceptions.negativeOrZeroArgument("loadFactor", String.valueOf(loadFactor));
        this.keyhash = keyhash;
        data = makeEntryArray(capacity);
        size = 0;
        expandAt = (int)Math.round(loadFactor*capacity);
        this.growthPolicy = growthPolicy;
        this.growthFactor = growthFactor;
        this.growthChunk = growthChunk;
        this.loadFactor = loadFactor;
    }

    private LongKeyChainedHashMap(int capacity, int growthPolicy, double growthFactor, int growthChunk, double loadFactor) {
        this(DefaultLongHashFunction.INSTANCE, capacity, growthPolicy, growthFactor, growthChunk, loadFactor);
    }

    /**
     *  Creates a new hash map with capacity 11, a relative
     *  growth factor of 1.0, and a load factor of 75%.
     */
    public LongKeyChainedHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     *  Creates a new hash map with the same mappings as a specified map.
     *
     *  @param      map
     *              the map whose mappings to put into the new map.
     *
     *  @throws     NullPointerException
     *              if <tt>map</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongKeyMap<E> map) {
        this();
        putAll(map);
    }

    /**
     *  Creates a new hash map with a specified capacity, a relative
     *  growth factor of 1.0, and a load factor of 75%.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative.
     */
    public LongKeyChainedHashMap(int capacity) {
        this(capacity, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, DEFAULT_LOAD_FACTOR);
    }

    /**
     *  Creates a new hash map with a capacity of 11, a relative
     *  growth factor of 1.0, and a specified load factor.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative.
     */
    public LongKeyChainedHashMap(double loadFactor) {
        this(DEFAULT_CAPACITY, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity and
     *  load factor, and a relative growth factor of 1.0.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive.
     */
    public LongKeyChainedHashMap(int capacity, double loadFactor) {
        this(capacity, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity,
     *  load factor, and relative growth factor.
     *
     *  <p>The map capacity increases to <tt>capacity()*(1+growthFactor)</tt>.
     *  This strategy is good for avoiding many capacity increases, but
     *  the amount of wasted memory is approximately the size of the map.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @param      growthFactor
     *              the relative amount with which to increase the
     *              the capacity when a capacity increase is needed.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive;
     *              if <tt>growthFactor</tt> is not positive.
     */
    public LongKeyChainedHashMap(int capacity, double loadFactor, double growthFactor) {
        this(capacity, GROWTH_POLICY_RELATIVE, growthFactor, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity,
     *  load factor, and absolute growth factor.
     *
     *  <p>The map capacity increases to <tt>capacity()+growthChunk</tt>.
     *  This strategy is good for avoiding wasting memory. However, an
     *  overhead is potentially introduced by frequent capacity increases.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @param      growthChunk
     *              the absolute amount with which to increase the
     *              the capacity when a capacity increase is needed.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive;
     *              if <tt>growthChunk</tt> is not positive;
     */
    public LongKeyChainedHashMap(int capacity, double loadFactor, int growthChunk) {
        this(capacity, GROWTH_POLICY_ABSOLUTE, DEFAULT_GROWTH_FACTOR, growthChunk, loadFactor);
    }

    // ---------------------------------------------------------------
    //      Constructors with hash function argument
    // ---------------------------------------------------------------

    /**
     *  Creates a new hash map with capacity 11, a relative
     *  growth factor of 1.0, and a load factor of 75%.
     *
     *  @param      keyhash
     *              the hash function to use when hashing keys.
     *
     *  @throws     NullPointerException
     *              if <tt>keyhash</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongHashFunction keyhash) {
        this(keyhash, DEFAULT_CAPACITY, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, DEFAULT_LOAD_FACTOR);
    }

    /**
     *  Creates a new hash map with a specified capacity, a relative
     *  growth factor of 1.0, and a load factor of 75%.
     *
     *  @param      keyhash
     *              the hash function to use when hashing keys.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative.
     *
     *  @throws     NullPointerException
     *              if <tt>keyhash</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongHashFunction keyhash, int capacity) {
        this(keyhash, capacity, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, DEFAULT_LOAD_FACTOR);
    }

    /**
     *  Creates a new hash map with a capacity of 11, a relative
     *  growth factor of 1.0, and a specified load factor.
     *
     *  @param      keyhash
     *              the hash function to use when hashing keys.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative.
     *
     *  @throws     NullPointerException
     *              if <tt>keyhash</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongHashFunction keyhash, double loadFactor) {
        this(keyhash, DEFAULT_CAPACITY, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity and
     *  load factor, and a relative growth factor of 1.0.
     *
     *  @param      keyhash
     *              the hash function to use when hashing keys.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive.
     *
     *  @throws     NullPointerException
     *              if <tt>keyhash</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongHashFunction keyhash, int capacity, double loadFactor) {
        this(keyhash, capacity, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity,
     *  load factor, and relative growth factor.
     *
     *  <p>The map capacity increases to <tt>capacity()*(1+growthFactor)</tt>.
     *  This strategy is good for avoiding many capacity increases, but
     *  the amount of wasted memory is approximately the size of the map.
     *
     *  @param      keyhash
     *              the hash function to use when hashing keys.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @param      growthFactor
     *              the relative amount with which to increase the
     *              the capacity when a capacity increase is needed.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive;
     *              if <tt>growthFactor</tt> is not positive.
     *
     *  @throws     NullPointerException
     *              if <tt>keyhash</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongHashFunction keyhash, int capacity, double loadFactor, double growthFactor) {
        this(keyhash, capacity, GROWTH_POLICY_RELATIVE, growthFactor, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity,
     *  load factor, and absolute growth factor.
     *
     *  <p>The map capacity increases to <tt>capacity()+growthChunk</tt>.
     *  This strategy is good for avoiding wasting memory. However, an
     *  overhead is potentially introduced by frequent capacity increases.
     *
     *  @param      keyhash
     *              the hash function to use when hashing keys.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @param      growthChunk
     *              the absolute amount with which to increase the
     *              the capacity when a capacity increase is needed.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive;
     *              if <tt>growthChunk</tt> is not positive;
     *
     *  @throws     NullPointerException
     *              if <tt>keyhash</tt> is <tt>null</tt>.
     */
    public LongKeyChainedHashMap(LongHashFunction keyhash, int capacity, double loadFactor, int growthChunk) {
        this(keyhash, capacity, GROWTH_POLICY_ABSOLUTE, DEFAULT_GROWTH_FACTOR, growthChunk, loadFactor);
    }

    // ---------------------------------------------------------------
    //      Hash table management
    // ---------------------------------------------------------------

    private void ensureCapacity(int elements) {
        if (elements >= expandAt) {
            int newcapacity;
            if (growthPolicy == GROWTH_POLICY_RELATIVE)
                newcapacity = (int)(data.length * (1.0 + growthFactor));
            else
                newcapacity = data.length + growthChunk;
            if (newcapacity*loadFactor < elements)
                newcapacity = (int)Math.round(((double)elements/loadFactor));
            newcapacity = Primes.nextPrime(newcapacity);
            expandAt = (int)Math.round(loadFactor*newcapacity);

            Entry<E>[] newdata = makeEntryArray(newcapacity);

            //  re-hash
            for (int i = 0; i < data.length; i++) {
                Entry<E> e = data[i];
                while (e != null) {
                    int index = Math.abs(keyhash.hash(e.key)) % newdata.length;
                    Entry<E> next = e.next;
                    e.next = newdata[index];
                    newdata[index] = e;
                    e = next;
                }
            }

            data = newdata;
        }
    }

	@SuppressWarnings("unchecked")
	private Entry<E>[] makeEntryArray(int newcapacity) {
		return (Entry<E>[]) new Entry[newcapacity];
	}

    private Entry<E> removeList(Entry<E> list, Entry<E> e) {
        if (list == e) {
            list = e.next;
            e.next = null;
            return list;
        }
        Entry<E> listStart = list;
        while (list.next != e)
            list = list.next;
        list.next = e.next;
        e.next = null;
        return listStart;
    }

    private Entry<E> searchList(Entry<E> list, long key) {
        while (list != null) {
            if (list.key == key)
                return list;
            list = list.next;
        }
        return null;
    }

    private Entry<E> getEntry(long key) {
        int index = Math.abs(keyhash.hash(key)) % data.length;
        return searchList(data[index], key);
    }

    // ---------------------------------------------------------------
    //      Operations not supported by abstract implementation
    // ---------------------------------------------------------------

    public LongSet keySet() {
        if (keys == null)
            keys = new KeySet();
        return keys;
    }

    public E put(long key, E value) {
        E result;
        int index = Math.abs(keyhash.hash(key)) % data.length;
        Entry<E> e = searchList(data[index], key);
        if (e == null) {
            result = null;
            e = new Entry<E>(key, value);
            e.next = data[index];
            data[index] = e;
            //  Capacity is increased after insertion in order to
            //  avoid recalculation of index
            ensureCapacity(size+1);
            size++;
        } else {
            result = e.value;
            e.value = value;
        }
        return result;
    }

    public Collection<E> values() {
        if (values == null)
            values = new ValueCollection();
        return values;
    }

    /**
     *  Returns a clone of this hash map.
     *
     *  @return     a clone of this hash map.
     *
     *  @since      1.1
     */
    @SuppressWarnings("unchecked")
	public Object clone() {
        try {
            LongKeyChainedHashMap<E> c = (LongKeyChainedHashMap<E>) super.clone();
            c.data = makeEntryArray(data.length);
            for (int i = 0; i < data.length; i++)
                c.data[i] = cloneList(data[i]);
            //  The views should not refer to this map's views
            c.values = null;
            c.keys = null;
            return c;
        } catch (CloneNotSupportedException e) {
            Exceptions.cloning(); return null;
        }
    }

    private Entry<E> cloneList(Entry<E> e) {
        if (e == null)
            return null;
        Entry<E> ne = new Entry<E>(e.getKey(), e.getValue());
        ne.next = cloneList(e.next);
        return ne;
    }

    private static class Entry<E> {
        long key;
        E value;
        Entry<E> next;

        Entry(long key, E value) {
            this.key = key;
            this.value = value;
        }

        public long getKey()
        { return key; }

        public E getValue()
        { return value; }

        @SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
            if (!(obj instanceof Entry))
                return false;
            Entry<E> e = (Entry<E>) obj;
            Object eval = e.getValue();
            if (eval == null)
                return e.getKey() == key && value == null;
            return e.getKey() == key && e.getValue().equals(value);
        }
    }


    public LongKeyMapIterator<E> entries() {
        return new LongKeyMapIterator<E>() {
            Entry<E> currEntry = null;
            int nextList = nextList(0);
            Entry<E> nextEntry = nextList == -1 ? null : data[nextList];

            int nextList(int index) {
                while (index < data.length && data[index] == null)
                    index++;
                return index < data.length ? index : -1;
            }

            public boolean hasNext() {
                return nextEntry != null;
            }

            public void next() {
                if (nextEntry == null)
                    Exceptions.endOfIterator();
                currEntry = nextEntry;

                //  Find next
                nextEntry = nextEntry.next;
                if (nextEntry == null) {
                    nextList = nextList(nextList+1);
                    if (nextList != -1)
                        nextEntry = data[nextList];
                }
            }

            public long getKey() {
                if (currEntry == null)
                    Exceptions.noElementToGet();
                return currEntry.getKey();
            }

            public E getValue() {
                if (currEntry == null)
                    Exceptions.noElementToGet();
                return currEntry.getValue();
            }

            public void remove() {
                if (currEntry == null)
                    Exceptions.noElementToRemove();
                 LongKeyChainedHashMap.this.remove(currEntry.getKey());
                 currEntry = null;
            }

        };
    }

    private class KeySet extends AbstractLongSet {

        public void clear()
        { LongKeyChainedHashMap.this.clear(); }

        public boolean contains(long v) {
            return getEntry(v) != null;
        }

        public LongIterator iterator() {
            return new LongIterator() {
                Entry<E> currEntry = null;
                int nextList = nextList(0);
                Entry<E> nextEntry = nextList == -1 ? null : data[nextList];

                int nextList(int index) {
                    while (index < data.length && data[index] == null)
                        index++;
                    return index < data.length ? index : -1;
                }

                public boolean hasNext() {
                    return nextEntry != null;
                }

                public long next() {
                    if (nextEntry == null)
                        Exceptions.endOfIterator();
                    currEntry = nextEntry;

                    //  Find next
                    nextEntry = nextEntry.next;
                    if (nextEntry == null) {
                        nextList = nextList(nextList+1);
                        if (nextList != -1)
                            nextEntry = data[nextList];
                    }
                    return currEntry.key;
                }

                public void remove() {
                    if (currEntry == null)
                        Exceptions.noElementToRemove();
                     LongKeyChainedHashMap.this.remove(currEntry.getKey());
                     currEntry = null;
                }
            };
        }

        public boolean remove(long v) {
            boolean result = containsKey(v);
            if (result)
                LongKeyChainedHashMap.this.remove(v);
            return result;
        }

        public int size()
        { return size; }

    }


    private class ValueCollection extends AbstractCollection<E> {

        public void clear()
        { LongKeyChainedHashMap.this.clear(); }

        public boolean contains(Object v) {
            return containsValue(v);
        }

        public Iterator<E> iterator() {
            return new Iterator<E>() {
                Entry<E> currEntry = null;
                int nextList = nextList(0);
                Entry<E> nextEntry = nextList == -1 ? null : data[nextList];

                int nextList(int index) {
                    while (index < data.length && data[index] == null)
                        index++;
                    return index < data.length ? index : -1;
                }

                public boolean hasNext() {
                    return nextEntry != null;
                }

                public E next() {
                    if (nextEntry == null)
                        Exceptions.endOfIterator();
                    currEntry = nextEntry;

                    //  Find next
                    nextEntry = nextEntry.next;
                    if (nextEntry == null) {
                        nextList = nextList(nextList+1);
                        if (nextList != -1)
                            nextEntry = data[nextList];
                    }
                    return currEntry.value;
                }

                public void remove() {
                    if (currEntry == null)
                        Exceptions.noElementToRemove();
                     LongKeyChainedHashMap.this.remove(currEntry.getKey());
                     currEntry = null;
                }
            };
        }

        public int size()
        { return size; }

    }

    // ---------------------------------------------------------------
    //      Operations overwritten for efficiency
    // ---------------------------------------------------------------

    public void clear() {
        java.util.Arrays.fill(data, null);
        size = 0;
    }

    public boolean containsKey(long key) {
        Entry<E> e = getEntry(key);
        return e != null;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            for (int i = 0; i < data.length; i++) {
                Entry<E> e = data[i];
                while (e != null) {
                    if (e.value == null)
                        return true;
                    e = e.next;
                }
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                Entry<E> e = data[i];
                while (e != null) {
                    if (value.equals(e.value))
                        return true;
                    e = e.next;
                }
            }
        }
        return false;
    }

    public E get(long key) {
        int index = Math.abs(keyhash.hash(key)) % data.length;
        Entry<E> e = searchList(data[index], key);
        return e != null ? e.value : null;
    }

    public boolean isEmpty()
    { return size == 0; }

    public E remove(long key) {
        int index = Math.abs(keyhash.hash(key)) % data.length;
        Entry<E> e = searchList(data[index], key);
        E value;
        if (e != null) {
            //  This can be improved to one iteration
            data[index] = removeList(data[index], e);
            value = e.value;
            size--;
        } else
            value = null;
        return value;
    }

    public int size()
    { return size; }

    // ---------------------------------------------------------------
    //      Serialization
    // ---------------------------------------------------------------

    /**
     *  @serialData     Default fields; the capacity of the
     *                  map (<tt>int</tt>); the maps's entries
     *                  (<tt>long</tt>, <tt>Object</tt>).
     *
     *  @since          1.1
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(data.length);
        LongKeyMapIterator<E> i = entries();
        while (i.hasNext()) {
            i.next();
            s.writeLong(i.getKey());
            s.writeObject(i.getValue());
        }
    }

    /**
     *  @since          1.1
     */
    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        data = makeEntryArray(s.readInt());
        for (int i = 0; i < size; i++) {
            long key = s.readLong();
            E value = (E) s.readObject();
            int index = Math.abs(keyhash.hash(key)) % data.length;
            Entry<E> e = new Entry<E>(key, value);
            e.next = data[index];
            data[index] = e;
        }
    }

}