package com.protocol.core;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ljq on 2022/2/10
 */
public final class ProtocolFactory {

    private static final Map<Class<?>, Object> proxyCache = new HashMap<>();

    public static ProtocolFactory getInstance() {
        return Instance.instance;
    }

    /**
     * 动态代理 + 反射
     *
     * @param protocolClass Protocol接口
     */
    public synchronized <T> T invoke(Class<T> protocolClass) {
        Object proxy = proxyCache.get(protocolClass);
        if (proxy == null) {
            proxy = createProxy(protocolClass);
            proxyCache.put(protocolClass, proxy);
        }
        return (T) proxy;
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> protocolClass) {
        Class<?> protocolImplClass = ProtocolUtil.getProtocolImplClass(ProtocolUtil.getProtocol(protocolClass));
        Object protocol = ProtocolUtil.getProtocolImpl(protocolImplClass);
        if (protocolImplClass == null || protocol == null) {
            Log.e("ProtocolFactory", "未找到实现类: " + protocolClass.getCanonicalName());
        }
        return (T) Proxy.newProxyInstance(protocolClass.getClassLoader(), new Class<?>[]{protocolClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (protocolImplClass == null || protocol == null) {
                    return null;
                }
                try {
                    return protocolImplClass.getMethod(method.getName(), method.getParameterTypes()).invoke(protocol, args);
                } catch (Exception exception) {
                    Log.e("ProtocolFactory", "未找到实现类定义的方法: " + method.getName() + "异常" + exception.getLocalizedMessage());
                }
                return null;
            }
        });
    }

    private static class Instance {
        private final static ProtocolFactory instance = new ProtocolFactory();
    }

    public void clearProtocolMap() {
        proxyCache.clear();
    }
}
