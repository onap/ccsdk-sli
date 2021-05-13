/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM
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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CachedElementTest implements Allocator<Testable>, Destructor<Testable> {
    private static final int MIN = 10;
    private static final int MAX = 100;
    private Pool<Testable> pool;
    private int index = 0;
    private int destroyCount = 0;

    /**
     * setup
     *
     * @throws PoolSpecificationException
     *             If the minimum size is less than 0, or if the max size is non-zero and less than the min size.
     */
    @Before
    public void setup() throws PoolSpecificationException {
        pool = new Pool<>(MIN, MAX);
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
        assertEquals(Integer.valueOf(MIN - 1), Integer.valueOf(value1.getId()));
        assertEquals(1, pool.getAllocatedSize());
        assertEquals(MIN - 1, pool.getFreeSize());
        assertEquals(1, pool.getAllocatedSize());

        Testable value2 = pool.reserve();
        assertNotNull(value2);
        assertEquals(Integer.valueOf(MIN - 2), Integer.valueOf(value2.getId()));
        assertEquals(2, pool.getAllocatedSize());
        assertEquals(MIN - 2, pool.getFreeSize());
        assertEquals(2, pool.getAllocatedSize());

        Testable value3 = pool.reserve();
        assertNotNull(value3);
        assertEquals(Integer.valueOf(MIN - 3), Integer.valueOf(value3.getId()));
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
        assertEquals(Integer.valueOf(MIN - 3), Integer.valueOf(value1.getId()));

        value2 = pool.reserve();
        assertNotNull(value2);
        assertEquals(Integer.valueOf(MIN - 2), Integer.valueOf(value2.getId()));

        value3 = pool.reserve();
        assertNotNull(value3);
        assertEquals(Integer.valueOf(MIN - 1), Integer.valueOf(value3.getId()));
    }

    /**
     * Test that we can trim the pool to a desired size
     *
     * @throws PoolDrainedException
     *             If the caller is trying to release or reserve an element from a drained pool
     * @throws PoolExtensionException
     *             If the pool cannot be extended
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
     * @throws SecurityException
     *             If a security manager, s, is present and any of the following conditions is met:
     *             <ul>
     *             <li>invocation of s.checkMemberAccess(this, Member.DECLARED) denies access to the declared method</li>
     *             <li>the caller's class loader is not the same as or an ancestor of the class loader for the current
     *             class and invocation of s.checkPackageAccess() denies access to the package of this class</li>
     *             </ul>
     * @throws NoSuchMethodException
     *             if a matching method is not found.
     */
    @SuppressWarnings("nls")
    @Test
    public void testTrim() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
        PoolDrainedException, PoolExtensionException, NoSuchMethodException, SecurityException {

        pool.setAllocator(this);
        int SIZE = 50;
        Testable[] array = new Testable[SIZE];

        assertEquals(0, pool.getAllocatedSize());
        for (int i = 0; i < SIZE; i++) {
            array[i] = pool.reserve();
        }
        assertEquals(SIZE, pool.getAllocatedSize());

        for (int i = 0; i < SIZE; i++) {
            pool.release(array[i]);
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
     * @throws PoolDrainedException
     *             If the caller is trying to release or reserve an element from a drained pool
     * @throws PoolExtensionException
     *             If the pool cannot be extended
     * @throws IOException
     *             if an I/O error occurs
     */
    @Test
    public void testDrain() throws PoolExtensionException, PoolDrainedException, IOException {
        int SIZE = 50;
        int FREE = 20;
        int ALLOC = SIZE - FREE;

        Testable[] array = new Testable[SIZE];
        pool.setAllocator(this);
        pool.setDestructor(this);

        assertFalse(pool.isDrained());

        assertEquals(0, pool.getAllocatedSize());
        for (int i = 0; i < SIZE; i++) {
            array[i] = pool.reserve();
        }
        assertEquals(SIZE, pool.getAllocatedSize());

        for (int i = 0; i < FREE; i++) {
            array[i].close();
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
     * @see org.onap.ccsdk.sli.core.utils.pool.Allocator#allocate(org.onap.ccsdk.sli.core.utils.pool.Pool)
     */
    @Override
    public Testable allocate(Pool<Testable> pool) {
        Testable element = new org.onap.ccsdk.sli.core.utils.pool.Element(index++);
        Testable ce = CachedElement.newInstance(pool, element, new Class[] {
            Testable.class
        });
        return ce;
    }

    /**
     * @see org.onap.ccsdk.sli.core.utils.pool.Destructor#destroy(java.io.Closeable, org.onap.ccsdk.sli.core.utils.pool.Pool)
     */
    @Override
    public void destroy(Testable obj, Pool<Testable> pool) {
        destroyCount++;
    }

    @Test
    public void testGetWrappedObject()
    {
        Testable element = new org.onap.ccsdk.sli.core.utils.pool.Element(index++);
        CachedElement cachedElement = new CachedElement(pool, element);
        assertNotNull(cachedElement.getWrappedObject());
        assertSame(element, cachedElement.getWrappedObject());
    }

    @Test
    public void testToString()
    {
        Testable element = new org.onap.ccsdk.sli.core.utils.pool.Element(index++);
        CachedElement cachedElement = new CachedElement(pool, element);
        assertTrue(cachedElement.toString() instanceof String);
    }
}
