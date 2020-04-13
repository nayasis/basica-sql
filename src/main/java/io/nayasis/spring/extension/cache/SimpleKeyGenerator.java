package io.nayasis.spring.extension.cache;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class SimpleKeyGenerator implements KeyGenerator {

    @Override
    public Object generate( Object target, Method method, Object... params ) {

        if ( params.length == 0 ) {
            return SimpleKey.EMPTY;
        }
        if (params.length == 1) {
            Object param = params[0];
            if (param != null && !param.getClass().isArray()) {
                return param;
            }
        }
        return new SimpleKey(params);

    }

}
