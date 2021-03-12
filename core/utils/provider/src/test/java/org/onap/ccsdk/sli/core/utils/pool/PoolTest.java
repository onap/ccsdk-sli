/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.utils.pool.*;
import org.onap.ccsdk.sli.core.utils.pool.Allocator;
import org.onap.ccsdk.sli.core.utils.pool.Destructor;
import org.onap.ccsdk.sli.core.utils.pool.Pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PoolTest implements Allocator<Testable>, Destructor<Testable> {

    private Pool<Testable> pool;
    private static final int MIN = 10;
    private static final int MAX = 100;
    private int index = 0;
    private int destroyCount = 0;

    /**
     * Set up the test by allocating a pool with MIN-MAX size (bounded pool)
     *
     * @throws PoolSpecificationException
     *             If the minimum size is less than 0, or if the max size is non-zero and less than the min size.
     */
    @Before
    public void setup() throws PoolSpecificationException {
        pool = new Pool<>(MIN, MAX);
        index = 0;
        destroyCount = 0;
    }

    /**
     * Test that trying to construct a pool with a bad minimum throws an exception
     *
     * @throws PoolSpecificationException
     *             If the minimum size is less than 0, or if the max size is non-zero and less than the min size.
     */
    @Test(expected = PoolSpecificationException.class)
    public void testInvalidMinSize() throws PoolSpecificationException {
        pool = new Pool<>(-1, MAX);
    }

    /**
     * Test that trying to construct a pool with a bad maximum throws an exception
     *
     * @throws PoolSpecificationException
     *             If the minimum size is less than 0, or if the max size is non-zero and less than the min size.
     */
    @Test(expected = PoolSpecificationException.class)
    public void testInvalidMaxSize() throws PoolSpecificationException {
        pool = new Pool<>(MIN, -1);
    }

    /**
     * Test creation of a pool where max is less than min fails
     *
     * @throws PoolSpecificationException
     *             If the minimum size is less than 0, or if the max size is non-zero and less than the min size.
     */
    @Test(expected = PoolSpecificationException.class)
    public void testInvalidSizeRange() throws PoolSpecificationException {
        pool = new Pool<>(MAX, MIN);
    }

    /**
     * Test state
     */
    @Test
    public void testMinPool() {
        assertEquals(MIN, pool.getMinPool());
    }

    /**
     * Test state
     */
    @Test
    public void testMaxPool() {
        assertEquals(MAX, pool.getMaxPool());
    }

    /**
     * Test state
     */
    @Test
    public void testAllocator() {
        assertNull(pool.getAllocator());
        pool.setAllocator(this);
        assertNotNull(pool.getAllocator());
    }

    /**
     * Test state
     */
    @Test
    public void testDestructor() {
        assertNull(pool.getDestructor());
        pool.setDestructor(this);
        assertNotNull(pool.getDestructor());
    }

    /**
     * Test that we can allocate and release elements and that the pool maintains them in MRU order
     *
     * @throws PoolExtensionException
     *             If the pool cannot be extended
     * @throws PoolDrainedException
     *             If the caller is trying to reserve an element from a drained pool
     */
    @Test
    public void testAllocateAndRelease() throws PoolExtensionException, PoolDrainedException {
        pool.setAllocator(this);

        assertFalse(pool.isDrained());

        /*
         * Allocate three elements
         */
        Testable value1 = pool.reserve();
        assertNotNull(value1);
        assertEquals(Integer.valueOf(MIN - 1), value1.getId());
        assertEquals(1, pool.getAllocatedSize());
        assertEquals(MIN - 1, pool.getFreeSize());
        assertEquals(1, pool.getAllocatedSize());

        Testable value2 = pool.reserve();
        assertNotNull(value2);
        assertEquals(Integer.valueOf(MIN - 2), value2.getId());
        assertEquals(2, pool.getAllocatedSize());
        assertEquals(MIN - 2, pool.getFreeSize());
        assertEquals(2, pool.getAllocatedSize());

        Testable value3 = pool.reserve();
        assertNotNull(value3);
        assertEquals(Integer.valueOf(MIN - 3), value3.getId());
        assertEquals(3, pool.getAllocatedSize());
        assertEquals(MIN - 3, pool.getFreeSize());
        assertEquals(3, pool.getAllocatedSize());

        /*
         * Now, release them in the order obtained
         */
        pool.release(value1);
        pool.release(value2);
        pool.release(value3);

        assertEquals(0, pool.getAllocatedSize());
        assertEquals(MIN, pool.getFreeSize());

        /*
         * Now, allocate them again, but their values should be reversed (3, 2, 1) representing the most recently used
         * to the least recently used.
         */
        value1 = pool.reserve();
        assertNotNull(value1);
        assertEquals(Integer.valueOf(MIN - 3), value1.getId());

        value2 = pool.reserve();
        assertNotNull(value2);
        assertEquals(Integer.valueOf(MIN - 2), value2.getId());

        value3 = pool.reserve();
        assertNotNull(value3);
        assertEquals(Integer.valueOf(MIN - 1), value3.getId());
    }

    /**
     * Test that we can trim the pool to a desired size
     *
     * @throws PoolExtensionException
     *             If the pool cannot be extended
     * @throws NoSuchMethodException
     *             if a matching method is not found.
     * @throws SecurityException
     *             if the request is denied.
     * @throws IllegalAccessException
     *             if this Method object is enforcing Java language access control and the underlying method is
     *             inaccessible.
     * @throws IllegalArgumentException
     *             if the method is an instance method and the specified object argument is not an instance of the class
     *             or interface declaring the underlying method (or of a subclass or implementor thereof); if the number
     *             of actual and formal parameters differ; if an unwrapping conversion for primitive arguments fails; or
     *             if, after possible unwrapping, a parameter value cannot be converted to the corresponding formal
     *             parameter type by a method invocation conversion.
     * @throws InvocationTargetException
     *             if the underlying method throws an exception.
     * @throws PoolDrainedException
     *             If the caller is trying to reserve an element from a drained pool
     */
    @SuppressWarnings("nls")
    @Test
    public void testTrim() throws PoolExtensionException, NoSuchMethodException, SecurityException,
        IllegalAccessException, IllegalArgumentException, InvocationTargetException, PoolDrainedException {
        pool.setAllocator(this);
        int SIZE = 50;
        Proxy[] array = new Proxy[SIZE];

        assertEquals(0, pool.getAllocatedSize());
        for (int i = 0; i < SIZE; i++) {
            array[i] = (Proxy) pool.reserve();
        }
        assertEquals(SIZE, pool.getAllocatedSize());

        for (int i = 0; i < SIZE; i++) {
            pool.release((Testable) array[i]);
        }
        assertEquals(0, pool.getAllocatedSize());

        assertEquals(SIZE, pool.getFreeSize());

        Method trimMethod = Pool.class.getDeclaredMethod("trim", new Class[] {
            Integer.TYPE
        });
        trimMethod.setAccessible(true);
        trimMethod.invoke(pool, new Object[] {
            SIZE - MIN
        });

        assertEquals(MIN, pool.getFreeSize());
    }

    /**
     * Test that we can drain a pool containing a mix of free and allocated elements
     *
     * @throws PoolExtensionException
     *             If the pool cannot be extended
     * @throws PoolDrainedException
     *             If the caller is trying to reserve an element from a drained pool
     */
    @Test
    public void testDrain() throws PoolExtensionException, PoolDrainedException {
        int SIZE = 50;
        int FREE = 20;
        int ALLOC = SIZE - FREE;

        Proxy[] array = new Proxy[SIZE];
        pool.setAllocator(this);
        pool.setDestructor(this);

        assertFalse(pool.isDrained());

        assertEquals(0, pool.getAllocatedSize());
        for (int i = 0; i < SIZE; i++) {
            array[i] = (Proxy) pool.reserve();
        }
        assertEquals(SIZE, pool.getAllocatedSize());

        for (int i = 0; i < FREE; i++) {
            pool.release((Testable) array[i]);
        }
        assertEquals(ALLOC, pool.getAllocatedSize());
        assertEquals(FREE, pool.getFreeSize());

        pool.drain();
        assertEquals(0, pool.getFreeSize());
        assertEquals(0, pool.getAllocatedSize());
        assertTrue(pool.isDrained());

        assertEquals(SIZE, destroyCount);
    }

    /**
     * @see org.onap.ccsdk.sli.core.utils.pool.Destructor#destroy(java.io.Closeable, org.onap.ccsdk.sli.core.utils.pool.Pool)
     */
    @Override
    public void destroy(Testable obj, Pool<Testable> pool) {
        destroyCount++;
        try {
            obj.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.onap.ccsdk.sli.core.utils.pool.Allocator#allocate(org.onap.ccsdk.sli.core.utils.pool.Pool)
     */
    @Override
    public Testable allocate(Pool<Testable> pool) {
        Testable e = new org.onap.ccsdk.sli.core.utils.pool.Element(index++);

        return e;
    }

    @Test
    public void testGetAndSetProperties() throws PoolSpecificationException
    {
        pool= new Pool<Testable>(3, 5);
        pool.setProperty("key1", "value1");
        assertEquals("value1", pool.getProperty("key1"));
    }

    @Test
    public void testGetProperties() throws PoolSpecificationException
    {
        pool= new Pool<Testable>(3, 5);
        assertTrue(pool.getProperties() instanceof Properties);
    }
}
