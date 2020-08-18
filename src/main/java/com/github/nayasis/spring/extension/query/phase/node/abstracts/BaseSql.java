package com.github.nayasis.spring.extension.query.phase.node.abstracts;

import com.github.nayasis.basica.base.Classes;
import com.github.nayasis.basica.base.Strings;
import com.github.nayasis.spring.extension.query.entity.QueryParameter;
import com.github.nayasis.spring.extension.query.phase.node.ElseIfSql;
import com.github.nayasis.spring.extension.query.phase.node.ElseSql;
import com.github.nayasis.spring.extension.query.phase.node.IfSql;
import com.github.nayasis.spring.extension.query.phase.node.RootSql;
import com.github.nayasis.spring.extension.query.phase.node.WhenFirstSql;
import com.github.nayasis.spring.extension.query.phase.node.WhenSql;
import com.github.nayasis.spring.extension.query.resolve.parse.QueryResolver;
import com.github.nayasis.spring.extension.query.resolve.parse.implement.SqlResolver;
import org.springframework.expression.ParseException;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSql {

	protected BaseSql       parent   = null;
    protected List<BaseSql> children = new ArrayList<>();

    protected Class<? extends QueryResolver> classQueryResolver = SqlResolver.class;

	public String toString( QueryParameter param ) throws ParseException {
		StringBuilder sb = new StringBuilder();
		for( ElementText element : toStringList( param ) ) {
			sb.append( element.getText() );
		}
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for( BaseSql child : children ) {
			sb.append( child.toString() );
		}
		return sb.toString();
	}

	protected String getTab( int depth ) {
		return Strings.lpad( "", depth * 2, ' ' );
	}

	public void append( BaseSql sql ) {
		children.add( sql );
		sql.parent = this;
	}

	public RootSql getRoot() {
		BaseSql curr = this;
		while( curr.parent != null ) {
			curr = curr.parent;
		}
		try {
			return (RootSql) curr;
		} catch ( Exception e ) {
			e.printStackTrace();
			throw e;
		}
	}

	protected BaseSql setQueryResolver( Class<? extends QueryResolver> classQueryResolver ) {
		this.classQueryResolver = classQueryResolver;
		children.forEach( child -> child.setQueryResolver(classQueryResolver) );
		return this;
	}

	protected QueryResolver createQueryResolver() {
		return Classes.createInstance( classQueryResolver );
	}

	public List<BaseSql> children() {
		return children;
	}

	protected List<ElementText> toStringList( QueryParameter param ) {

		List<ElementText> list = new ArrayList<>();

		Boolean previousCondition = null;

		for( BaseSql element : children ) {

			if( isElseSeries(element) ) {
				if( previousCondition != null && previousCondition != true ) {
					list.add( new ElementText( element.getClass(), element.toString( param ) ) );
				}
			} else {
				list.add( new ElementText( element.getClass(), element.toString( param ) ) );
			}

			if( isIf(element) ) {
				previousCondition = getIfSeriesResult( element, param );

			} else if( isElseIf( element ) ) {
				if( previousCondition == null || previousCondition == false ) {
					previousCondition = getIfSeriesResult( element, param );
				}

			} else if( isElse( element ) ) {
				previousCondition = null;
			}

		}

		return list;

	}

	private boolean isIf( BaseSql element ) {
		Class klass = element.getClass();
		return klass == IfSql.class || klass == WhenFirstSql.class;
	}

	private boolean isElseIf( BaseSql element ) {
		Class klass = element.getClass();
		return klass == ElseIfSql.class || klass == WhenSql.class;
	}

	private boolean isElse( BaseSql element ) {
		Class klass = element.getClass();
		return klass == ElseSql.class && klass != WhenFirstSql.class;
	}

	private boolean getIfSeriesResult( BaseSql element, Object param ) {
		if( ! isIf(element) && ! isElseIf(element) ) return false;
		return ((IfSql) element).isTrue( param );
	}

	private boolean isElseSeries( BaseSql element ) {
		Class klass = element.getClass();
		if( klass == ElseIfSql.class ) return true;
		if( klass == WhenSql.class   ) return true;
		return klass == ElseSql.class;
	}

}