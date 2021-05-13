/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.core.utils.pool;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to manage a pool of things.
 * <p>
 * The class is parameterized so that the type of objects maintained in the pool is definable by some provided type.
 * This type must implement the <code>Comparable</code> interface so that it can be managed in the pool.
 * </p>
 *
 * @param <T>
 *            The type of element being pooled
 */

public class Pool<T extends Closeable> {
    private Deque<T> free;
    private List<T> allocated;
    private int minPool;
    private int maxPool;
    private Allocator<T> allocator;
    private org.onap.ccsdk.sli.core.utils.pool.Destructor<T> destructor;
    private ReadWriteLock lock;
    private AtomicBoolean drained;
    private Properties properties;

    /**
     * Create the pool
     *
     * @param minPool
     *            The minimum size of the pool
     * @param maxPool
     *            The maximum size of the pool, set to zero (0) for unbounded
     * @throws PoolSpecificationException
     *             If the minimum size is less than 0, or if the max size is non-zero and less than the min size.
     */
    public Pool(int minPool, int maxPool) throws PoolSpecificationException {

        if (minPool < 0) {
            throw new PoolSpecificationException(String.format("The minimum pool size must be a "
                + "positive value or zero, %d is not valid.", minPool));
        }
        if (maxPool != 0 && maxPool < minPool) {
            throw new PoolSpecificationException(String.format("The maximum pool size must be a "
                + "positive value greater than the minimum size, or zero. %d is not valid.", maxPool));
        }

        this.minPool = minPool;
        this.maxPool = maxPool;

        properties = new Properties();
        free = new ArrayDeque<T>();
        allocated = new ArrayList<T>();
        lock = new ReentrantReadWriteLock();
        drained = new AtomicBoolean(false);
    }

    /**
     * Returns the amount of objects on the free collection
     *
     * @return The number of objects on the free collection
     */
    public int getFreeSize() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return free.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns the value for a specified property of this pool, if defined.
     *
     * @param key
     *            The key of the desired property
     * @return The value of the property, or null if not defined
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Sets the value of the specified property or replaces it if it already exists
     *
     * @param key
     *            The key of the property to be set
     * @param value
     *            The value to set the property to
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * @return The properties object for the pool
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Returns the number of objects that are currently allocated
     *
     * @return The allocate collection size
     */
    public int getAllocatedSize() {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return allocated.size();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return the value of allocator
     */
    public Allocator<T> getAllocator() {
        return allocator;
    }

    /**
     * @param allocator
     *            the value for allocator
     */
    public void setAllocator(Allocator<T> allocator) {
        this.allocator = allocator;
    }

    /**
     * @return the value of destructor
     */
    public org.onap.ccsdk.sli.core.utils.pool.Destructor<T> getDestructor() {
        return destructor;
    }

    /**
     * @return the value of minPool
     */
    public int getMinPool() {
        return minPool;
    }

    /**
     * @return the value of maxPool
     */
    public int getMaxPool() {
        return maxPool;
    }

    /**
     * @param destructor
     *            the value for destructor
     */
    public void setDestructor(org.onap.ccsdk.sli.core.utils.pool.Destructor<T> destructor) {
        this.destructor = destructor;
    }

    /**
     * Drains the pool, releasing and destroying all pooled objects, even if they are currently allocated.
     */
    public void drain() {
        if (drained.compareAndSet(false, true)) {
            Lock writeLock = lock.writeLock();
            writeLock.lock();
            try {
                int size = getAllocatedSize();
                /*
                 * We can't use the "release" method call here because we are modifying the list we are iterating
                 */
                ListIterator<T> it = allocated.listIterator();
                while (it.hasNext()) {
                    T obj = it.next();
                    it.remove();
                    free.addFirst(obj);
                }
                size = getFreeSize();
                trim(size);
            } finally {
                writeLock.unlock();
            }
        }
    }

    /**
     * Returns an indication if the pool has been drained
     *
     * @return True indicates that the pool has been drained. Once a pool has been drained, it can no longer be used.
     */
    public boolean isDrained() {
        return drained.get();
    }

    /**
     * Reserves an object of type T from the pool for the caller and returns it
     *
     * @return The object of type T to be used by the caller
     * @throws org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException
     *             If the pool cannot be extended
     * @throws org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException
     *             If the caller is trying to reserve an element from a drained pool
     */
    @SuppressWarnings("unchecked")
    public T reserve() throws org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException, org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException {
        if (isDrained()) {
            throw new org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException("The pool has been drained and cannot be used.");
        }

        T obj = null;
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            int freeSize = getFreeSize();
            int allocatedSize = getAllocatedSize();

            if (freeSize == 0) {
                if (allocatedSize == 0) {
                    extend(minPool == 0 ? 1 : minPool);
                } else if (allocatedSize >= maxPool && maxPool > 0) {
                    throw new org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException(String.format("Unable to add "
                                                                                      + "more elements, pool is at maximum size of %d", maxPool));
                } else {
                    extend(1);
                }
            }

            obj = free.removeFirst();
            allocated.add(obj);
        } finally {
            writeLock.unlock();
        }

        /*
         * Now that we have the real object, lets wrap it in a dynamic proxy so that we can intercept the close call and
         * just return the context to the free pool. obj.getClass().getInterfaces(). We need to find ALL interfaces that
         * the object (and all superclasses) implement and have the proxy implement them too
         */
        Class<?> cls = obj.getClass();
        Class<?>[] array;
        List<Class<?>> interfaces = new ArrayList<Class<?>>();
        while (!cls.equals(Object.class)) {
            array = cls.getInterfaces();
            for (Class<?> item : array) {
                if (!interfaces.contains(item)) {
                    interfaces.add(item);
                }
            }
            cls = cls.getSuperclass();
        }
        array = new Class<?>[interfaces.size()];
        array = interfaces.toArray(array);
        return org.onap.ccsdk.sli.core.utils.pool.CachedElement.newInstance(this, obj, array);
    }

    /**
     * releases the allocated object back to the free pool to be used by another request.
     *
     * @param obj
     *            The object to be returned to the pool
     * @throws org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException
     *             If the caller is trying to release an element to a drained pool
     */
    public void release(T obj) throws org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException {
        if (isDrained()) {
            throw new org.onap.ccsdk.sli.core.utils.pool.PoolDrainedException("The pool has been drained and cannot be used.");
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            if (allocated.remove(obj)) {
                free.addFirst(obj);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Extend the free pool by some number of elements
     *
     * @param count
     *            The number of elements to add to the pool
     * @throws org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException
     *             if the pool cannot be extended because no allocator has been specified.
     */
    private void extend(int count) throws org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException {
        if (allocator == null) {
            throw new org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException(String.format("Unable to extend pool "
                                                                              + "because no allocator has been specified"));
        }
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            for (int index = 0; index < count; index++) {
                T obj = allocator.allocate(this);
                if (obj == null) {
                    throw new org.onap.ccsdk.sli.core.utils.pool.PoolExtensionException(
                        "The allocator failed to allocate a new context to extend the pool.");
                }
                free.push(obj);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Used to trim the free collection by some specified number of elements, or the free element count, whichever is
     * less. The elements are removed from the end of the free element deque, thus trimming the oldest elements first.
     *
     * @param count
     *            The number of elements to trim
     */
    private void trim(int count) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            int trimCount = count;
            if (getFreeSize() < count) {
                trimCount = getFreeSize();
            }
            for (int i = 0; i < trimCount; i++) {
                T obj = free.removeLast();
                if (destructor != null) {
                    destructor.destroy(obj, this);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }
}
