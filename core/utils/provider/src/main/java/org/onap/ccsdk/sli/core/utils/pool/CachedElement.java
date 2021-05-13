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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used as a "wrapper" for any closeable elements that are cached in a pool. It is
 * implemented as a dynamic proxy, so that it appears to be the same class of object to the client
 * as the interface being cached. The generic type being cached MUST be an interface.
 *
 * @param <T> The generic type that we create a cached element for. This type is used to wrap
 *        instances of this type and expose access to the {@link Closeable} interface by
 *        using a dynamic proxy.
 */

public class CachedElement<T extends Closeable>
        implements Closeable, InvocationHandler, CacheManagement {
        private static final EELFLogger LOG = EELFManager.getInstance().getLogger(CachedElement.class);

    /**
     * The pool that is managing this cached element
     */
    private Pool<T> pool;

    /**
     * The element that we are caching in the pool
     */
    private T element;

    /**
     * A thread-safe atomic indicator that tells us that the wrapped element has been released to
     * the pool already, and not to do it again.
     */
    private AtomicBoolean released = new AtomicBoolean(false);

    /**
     * Create a new instance of a cached element dynamic proxy for use in the pool.
     * <p>
     * This returns an instance of the proxy to the caller that appears to be the same interface(s)
     * as the object being cached. The dynamic proxy then intercepts all open and close semantics
     * and directs that element to the pool.
     * </p>
     * <p>
     * If the object being proxied does not implement the {@link CacheManagement} interface, then
     * that interface is added to the dynamic proxy being created. This interface is actually
     * implemented by the invocation handler (this object) for the proxy and allows direct access to
     * the wrapped object inside the proxy.
     * </p>
     *
     * @param pool The pool that we are caching these elements within
     * @param element The element actually being cached
     * @param interfaces The interface list of interfaces the element must implement (usually one)
     * @return The dynamic proxy
     */
    @SuppressWarnings("unchecked")
    public static <T extends Closeable> T newInstance(Pool<T> pool, T element,
            Class<?>[] interfaces) {
        ClassLoader cl = element.getClass().getClassLoader();
        CachedElement<T> ce = new CachedElement<>(pool, element);
        boolean found = false;
        for (Class<?> intf : interfaces) {
            if (intf.isAssignableFrom(CacheManagement.class)) {
                found = true;
                break;
            }
        }

        int length = found ? interfaces.length : interfaces.length + 1;
        Class<?>[] proxyInterfaces = new Class[length];
        System.arraycopy(interfaces, 0, proxyInterfaces, 0, interfaces.length);

        if (!found) {
            proxyInterfaces[interfaces.length] = CacheManagement.class;
        }

        return (T) Proxy.newProxyInstance(cl, proxyInterfaces, ce);
    }

    /**
     * Construct a cached element and assign it to the pool as a free element
     *
     * @param pool The pool that the element will be managed within
     * @param element The element we are caching
     */
    @SuppressWarnings("unchecked")
    public CachedElement(Pool<T> pool, T element) {
        this.pool = pool;
        this.element = element;

        try {
            pool.release((T) this);
        } catch (PoolDrainedException e) {
            LOG.error("Pool is empty", e);
        }
    }

    /**
     * This method delegates the close call to the actual wrapped element.
     * <p>
     * NOTE: This is not the same method that is called by the dynamic proxy. This method is in
     * place to satisfy the signature of the {@link Closeable} interface. If it were to be
     * called directly, then we will delegate the close to the underlying context. However, when the
     * cached element is called as a synamic proxy, entry is in the
     * {@link #invoke(Object, Method, Object[])} method.
     * </p>
     *
     * @see Closeable#close()
     */
    @Override
    public void close() throws IOException {
        element.close();
    }

    /**
     * This method is the magic part of dynamic proxies. When the caller makes a method call based
     * on the interface being proxied, this method is given control. This informs us of the method
     * and arguments of the call. The object reference is that of the dynamic proxy itself, which is
     * us.
     * <p>
     * Here we will check to see if the user is trying to close the "element" (the dynamic proxy
     * acts like the wrapped element). If he is, then we don't really close it, but instead release
     * the element that we are wrapping back to the free pool. Once this has happened, we mark the
     * element as "closed" (from the perspective of this dynamic proxy) so that we wont try to
     * release it again.
     * </p>
     * <p>
     * If the method is the <code>equals</code> method then we assume that we are comparing the
     * cached element in one dynamic proxy to the cached element in another. We execute the
     * comparison between the cached elements, and not the dynamic proxies themselves. This
     * preserves the allusion to the caller that the dynamic proxy is the object being wrapped.
     * </p>
     * <p>
     * For convenience, we also implement the <code>getWrappedObject</code> method so that the
     * dynamic proxy can be called to obtain the actual wrapped object if desired. Note, to use this
     * method, the caller would have to invoke it through reflection.
     * </p>
     * <p>
     * If the method being invoked is not one that we intercept, then we simply delegate that method
     * onto the wrapped object.
     * </p>
     *
     * @see InvocationHandler#invoke(Object, Method,
     *      Object[])
     */
    @SuppressWarnings({"unchecked", "nls"})
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        Object result = null;

        switch (method.getName()) {
            case "close":
                if (released.compareAndSet(false, true) && !pool.isDrained()) {
                    pool.release((T) proxy);
                }
                break;
            case "equals":
                CacheManagement cm = (CacheManagement) proxy;
                T other = (T) cm.getWrappedObject();
                result = element.equals(other);
                break;
            case "getWrappedObject":
                return element;
            default:
                result = method.invoke(element, args);
                break;
        }

        return result;
    }

    /**
     * This method is used to be able to access the wrapped object underneath the dynamic proxy
     *
     * @see org.onap.ccsdk.sli.core.utils.pool.CacheManagement#getWrappedObject()
     */
    @Override
    public T getWrappedObject() {
        return element;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return element == null ? "null" : element.toString();
    }
}
