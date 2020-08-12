package com.github.nayasis.spring.extension.query.entity;

import com.github.nayasis.basica.base.Types;
import com.github.nayasis.basica.model.NMap;
import com.github.nayasis.basica.reflection.Reflector;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.github.nayasis.spring.extension.query.common.QueryConstant.FOR_EACH_INDEX;
import static com.github.nayasis.spring.extension.query.common.QueryConstant.PARAMETER_SINGLE;

/**
 * Query Parameter
 *
 * @author 1002159
 * @since 2016-01-07
 */
@NoArgsConstructor
public class QueryParameter extends NMap<String,Object> {

    public QueryParameter( Object value ) {
        init( value );
    }

    /**
     * Init Query Parameter from value
     *
     * @param value single value (primitive or array or list) or Map
     * @return self instance
     */
    public QueryParameter init( Object value ) {

        clear();

        putAll( Reflector.toMapFrom(ThreadlocalQueryParameter.getAll()) );

        if( value == null || isPrimitive( value ) ) {
            put( PARAMETER_SINGLE, value );
        } else {
            bind( value );
        }

        return this;

    }

    private boolean isPrimitive( Object value ) {
        if( Types.isImmutable(value) ) return true;
        if( Types.isArrayOrCollection(value) ) return true;
        return false;
    }

    public boolean hasSingleParameter() {
        return containsKey(PARAMETER_SINGLE);
    }

    public Object getSingleParameter() {
        return get(PARAMETER_SINGLE);
    }

    public List<Integer> getForeachIndex() {
        if( ! containsKey(FOR_EACH_INDEX) ) {
            put( FOR_EACH_INDEX, new ArrayList<Integer>() );
        }
        return (List<Integer>) get( FOR_EACH_INDEX );
    }

    /**
     * add ForEach index
     *
     * @return added element's index
     */
    public int addForeachIndex( int index ) {
        List<Integer> indices = getForeachIndex();
        indices.add( index );
        return indices.size() - 1;
    }

}