package com.github.nayasis.spring.extension.query.repository;

import com.github.nayasis.basica.model.NMap;
import com.github.nayasis.basica.reflection.Reflector;
import com.github.nayasis.spring.extension.query.entity.QueryParameter;
import com.github.nayasis.spring.extension.query.exception.DuplicatedQueryExistException;
import com.github.nayasis.spring.extension.query.phase.node.ForEachSql;
import com.github.nayasis.spring.extension.query.phase.node.IfSql;
import com.github.nayasis.spring.extension.query.phase.node.RootSql;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QueryRepositoryTest {

    @BeforeAll
    public static void init() throws DuplicatedQueryExistException {
        QueryRepository.read( "/sql/Grammar.xml" );
    }

    @Test
    public void basic() {
        log.debug( "{}", QueryRepository.log() );
    }

    @Test
    public void logForeach() {

        RootSql sql = QueryRepository.get( "Grammar.nestedForLoop" );

        ForEachSql foreach = (ForEachSql) sql.children().stream().filter( n -> n.getClass() == IfSql.class ).findFirst().orElse( null )
            .children().stream().filter( n -> n.getClass() == ForEachSql.class ).findFirst().orElse( null );

        QueryParameter param = new QueryParameter( sampleParam() );

        String query = foreach.toString( param );

        log.debug( ">> query\n{}", query );

        log.debug( "{}", Reflector.toJson(param,true) );

        Assertions.assertEquals( "[0, 1, 2, 0, 0, 1, 2, 1, 0, 1, 2, 0, 0, 1, 2, 0, 0, 1, 2, 1, 0, 1, 2, 1]", param.getForeachIndex().toString() );

        for( int i = 0; i < 2; i++ ) {

            String key1 = String.format("#{user[%d].name}", i );
            Assertions.assertTrue( query.contains(key1) );

            for( int j=0; j < 2; j++ ) {

                String key2 = String.format("#{user[%d].dept[%d].name}", i,j );
                Assertions.assertTrue( query.contains(key2) );

                for( int k = 0; k < 3; k++ ) {

                    String key3 = String.format("#{user[%d].dept[%d].subDept[%d].name}", i,j,k );
                    Assertions.assertTrue( query.contains(key3) );

                }
            }
        }

    }

    private Map sampleParam() {

        Map param = new HashMap();

        List<User> users = new ArrayList<>();

        for( int i=0; i < 2; i++ ) {
            User user = new User().name( "user-" + i ).age(i*20);
            users.add( user );
            for( int j=0; j < 2; j++ ) {
                Dept dept = new Dept().name("dept-"+j);
                user.dept().add( dept );
                for( int k = 0; k < 3; k++ ) {
                    Dept subdept = new Dept().name("subdept-"+k);
                    dept.subDept().add( subdept );
                }
            }
        }

        param.put( "user", users );

        param.put( "condition", new NMap<>("{'limit':{'age':10}}") );

        return param;

    }


    @Data @Accessors(fluent=true)
    class User {
        String     name;
        int        age;
        List<Dept> dept = new ArrayList<>();
    }

    @Data @Accessors(fluent=true)
    class Dept {
        String     name;
        List<Dept> subDept = new ArrayList<>();
    }
    

}