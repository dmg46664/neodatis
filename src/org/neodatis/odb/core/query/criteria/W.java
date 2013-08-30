
/*
NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

"This file is part of the NeoDatis ODB open source object database".

NeoDatis ODB is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

NeoDatis ODB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package org.neodatis.odb.core.query.criteria;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;

/**A simple factory to build all Criterion and Expression
 * 
 * @author olivier s
 *
 */
public class W {
	W(){}
	
	/********************************************************
	 * EQUALS
	 ********************************************************/
	
	/**
	 * @param attributeName The attribute name
	 * @param value The boolean value
	 * @return The criteria
	 * 
	 */
	public static Criterion equal(String attributeName,boolean value){
		return new EqualCriterion(attributeName,value?Boolean.TRUE:Boolean.FALSE);
	}
    public static Criterion equal(String attributeName,int value){
        return new EqualCriterion(attributeName,new Integer(value));
    }
    public static Criterion equal(String attributeName,short value){
        return new EqualCriterion(attributeName,new Short(value));
    }
    public static Criterion equal(String attributeName,byte value){
        return new EqualCriterion(attributeName,new Byte(value));
    }
    public static Criterion equal(String attributeName,float value){
        return new EqualCriterion(attributeName, new Float(value));
    }
    public static Criterion equal(String attributeName,double value){
        return new EqualCriterion(attributeName,new Double(value));
    }
    public static Criterion equal(String attributeName,long value){
        return new EqualCriterion(attributeName,new Long(value));
    }
    public static Criterion equal(String attributeName,char value){
        return new EqualCriterion(attributeName,new Character(value));
    }
    
    public static Criterion equal(String attributeName,Object value){
		return new EqualCriterion(attributeName,value);
	}

    public static Criterion iequal(String attributeName,char value){
        return new EqualCriterion(attributeName,new Character(value),false);
    }
    
    public static Criterion iequal(String attributeName,Object value){
		return new EqualCriterion(attributeName,value,false);
	}

    /***********************************************************
     * LIKE
     * @param attributeName The attribute name 
     * @param value The string value
     * @return The criterio
     ***********************************************************/
    public static Criterion like(String attributeName,String value){
        return new LikeCriterion(attributeName,value,true,true);
    }
    public static Criterion ilike(String attributeName,String value){
        return new LikeCriterion(attributeName,value,false,true);
    }
    /**
     * 
     * @param attributeName The name of the attribute
     * @param value The value 
     * @param escapeRegExpCharacters to tell NeoDatis to escape regExp (*+$?) characters. This can be used if you want to use regexp pattern
     * @return
     */
    public static Criterion like(String attributeName,String value, boolean escapeRegExpCharacters){
        return new LikeCriterion(attributeName,value,true,escapeRegExpCharacters);
    }
    public static Criterion ilike(String attributeName,String value, boolean escapeRegExpCharacters){
        return new LikeCriterion(attributeName,value,false,escapeRegExpCharacters);
    }

    /***********************************************************
     * GREATER THAN
     * @param attributeName 
     * @param value 
     * @return The criterion
     ***********************************************************/
    public static Criterion gt(String attributeName,Comparable value){
        return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_GT);
    }
    public static Criterion gt(String attributeName,int value){
    	return new ComparisonCriterion(attributeName,new Integer(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }
    public static Criterion gt(String attributeName,short value){
    	return new ComparisonCriterion(attributeName,new Short(value),ComparisonCriterion.COMPARISON_TYPE_GT);    	
    }
    public static Criterion gt(String attributeName,byte value){
    	return new ComparisonCriterion(attributeName,new Byte(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }
    public static Criterion gt(String attributeName,float value){
    	return new ComparisonCriterion(attributeName,new Float(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }
    public static Criterion gt(String attributeName,double value){
    	return new ComparisonCriterion(attributeName,new Double(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }
    public static Criterion gt(String attributeName,long value){
    	return new ComparisonCriterion(attributeName,new Long(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }

    public static Criterion gt(String attributeName,char value){
    	return new ComparisonCriterion(attributeName,new Character(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }

    /***********************************************************
     * GREATER OR EQUAL
     * @param attributeName 
     * @param value 
     * @return The criterion
     ***********************************************************/

    public static Criterion ge(String attributeName,Comparable value){
        return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_GE);
    }
    public static Criterion ge(String attributeName,int value){
    	return new ComparisonCriterion(attributeName,new Integer(value),ComparisonCriterion.COMPARISON_TYPE_GE);
    }
    public static Criterion ge(String attributeName,short value){
    	return new ComparisonCriterion(attributeName,new Short(value),ComparisonCriterion.COMPARISON_TYPE_GE);    	
    }
    public static Criterion ge(String attributeName,byte value){
    	return new ComparisonCriterion(attributeName,new Byte(value),ComparisonCriterion.COMPARISON_TYPE_GE);
    }
    public static Criterion ge(String attributeName,float value){
    	return new ComparisonCriterion(attributeName,new Float(value),ComparisonCriterion.COMPARISON_TYPE_GE);
    }
    public static Criterion ge(String attributeName,double value){
    	return new ComparisonCriterion(attributeName,new Double(value),ComparisonCriterion.COMPARISON_TYPE_GE);
    }
    public static Criterion ge(String attributeName,long value){
    	return new ComparisonCriterion(attributeName,new Long(value),ComparisonCriterion.COMPARISON_TYPE_GE);
    }
    
    public static Criterion ge(String attributeName,char value){
    	return new ComparisonCriterion(attributeName,new Character(value),ComparisonCriterion.COMPARISON_TYPE_GT);
    }

    /***********************************************************
     * LESS THAN
     * @param attributeName 
     * @param value 
     * @return The criterion
     ***********************************************************/
    public static Criterion lt(String attributeName,Comparable value){
        return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    public static Criterion lt(String attributeName,int value){
    	return new ComparisonCriterion(attributeName,new Integer(value),ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    public static Criterion lt(String attributeName,short value){
    	return new ComparisonCriterion(attributeName,new Short(value),ComparisonCriterion.COMPARISON_TYPE_LT);    	
    }
    public static Criterion lt(String attributeName,byte value){
    	return new ComparisonCriterion(attributeName,new Byte(value),ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    public static Criterion lt(String attributeName,float value){
    	return new ComparisonCriterion(attributeName,new Float(value),ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    public static Criterion lt(String attributeName,double value){
    	return new ComparisonCriterion(attributeName,new Double(value),ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    public static Criterion lt(String attributeName,long value){
    	return new ComparisonCriterion(attributeName,new Long(value),ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    public static Criterion lt(String attributeName,char value){
    	return new ComparisonCriterion(attributeName,new Character(value),ComparisonCriterion.COMPARISON_TYPE_LT);
    }
    /***********************************************************
     * LESS OR EQUAL
     * @param attributeName The attribute name
     * @param value The value
     * @return The criterion
     ***********************************************************/

    public static Criterion le(String attributeName,Comparable value){
        return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_LE);
    }
    public static Criterion le(String attributeName,int value){
    	return new ComparisonCriterion(attributeName,new Integer(value),ComparisonCriterion.COMPARISON_TYPE_LE);
    }
    public static Criterion le(String attributeName,short value){
    	return new ComparisonCriterion(attributeName,new Short(value),ComparisonCriterion.COMPARISON_TYPE_LE);    	
    }
    public static Criterion le(String attributeName,byte value){
    	return new ComparisonCriterion(attributeName,new Byte(value),ComparisonCriterion.COMPARISON_TYPE_LE);
    }
    public static Criterion le(String attributeName,float value){
    	return new ComparisonCriterion(attributeName,new Float(value),ComparisonCriterion.COMPARISON_TYPE_LE);
    }
    public static Criterion le(String attributeName,double value){
    	return new ComparisonCriterion(attributeName,new Double(value),ComparisonCriterion.COMPARISON_TYPE_LE);
    }
    public static Criterion le(String attributeName,long value){
    	return new ComparisonCriterion(attributeName,new Long(value),ComparisonCriterion.COMPARISON_TYPE_LE);
    }
    public static Criterion le(String attributeName,char value){
    	return new ComparisonCriterion(attributeName,new Character(value),ComparisonCriterion.COMPARISON_TYPE_LE);
    }

    
    
    /***********************************************************
     * CONTAIN
     ***********************************************************/
    
    
    /**
     * The 
     * @param attributeName The attribute name
     * @param value The value
     * @return The criterion
     */
    public static Criterion contain(String attributeName,boolean value){
    	return new ContainsCriterion(attributeName,value?Boolean.TRUE:Boolean.FALSE);
	}
	
    public static Criterion contain(String attributeName,int value){
    	return new ContainsCriterion(attributeName,new Integer(value));
    }
    public static Criterion contain(String attributeName,short value){
    	return new ContainsCriterion(attributeName,new Short(value));
    }
    public static Criterion contain(String attributeName,byte value){
    	return new ContainsCriterion(attributeName,new Byte(value));
    }
    public static Criterion contain(String attributeName,float value){
    	return new ContainsCriterion(attributeName, new Float(value));
    }
    public static Criterion contain(String attributeName,double value){
    	return new ContainsCriterion(attributeName,new Double(value));
    }
    public static Criterion contain(String attributeName,long value){
    	return new ContainsCriterion(attributeName,new Long(value));
    }
    public static Criterion contain(String attributeName,char value){
    	return new ContainsCriterion(attributeName,new Character(value));
    }
    public static Criterion contain(String attributeName,Object value){
		return new ContainsCriterion(attributeName,value);
    }

    public static Criterion isNull(String attributeName){
    	return new IsNullCriterion(attributeName);
    }
    public static Criterion isNotNull(String attributeName){
    	return new IsNotNullCriterion(attributeName);
    }
    
    public static Criterion sizeEq(String attributeName,int size){
    	return new CollectionSizeCriterion(attributeName,size,CollectionSizeCriterion.SIZE_EQ);
    }
    public static Criterion sizeNe(String attributeName,int size){
    	return new CollectionSizeCriterion(attributeName,size,CollectionSizeCriterion.SIZE_NE);
    }
    public static Criterion sizeGt(String attributeName,int size){
    	return new CollectionSizeCriterion(attributeName,size,CollectionSizeCriterion.SIZE_GT);
    }
    public static Criterion sizeGe(String attributeName,int size){
    	return new CollectionSizeCriterion(attributeName,size,CollectionSizeCriterion.SIZE_GE);
    }
    public static Criterion sizeLt(String attributeName,int size){
    	return new CollectionSizeCriterion(attributeName,size,CollectionSizeCriterion.SIZE_LT);
    }
    public static Criterion sizeLe(String attributeName,int size){
    	return new CollectionSizeCriterion(attributeName,size,CollectionSizeCriterion.SIZE_LE);
    }
    
    public static Or or(){
    	return new Or();
    }
    public static And and(){
    	return new And();
    }
    public static Not not(Criterion criterion){
    	return new Not(criterion);
    }

    public static Criterion get(String attributeName,Operator operator,Object value){
        if(operator == Operator.EQUAL){
            return new EqualCriterion(attributeName,value);
        }
        if(operator == Operator.LIKE){
            return new LikeCriterion(attributeName,value.toString(),true, true);
        }
        if(operator == Operator.GREATER_OR_EQUAL){
            return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_GE);
        }
        if(operator == Operator.GREATER_THAN){
            return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_GT);
        }
        if(operator == Operator.LESS_THAN){
        	return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_LT);
        }
        if(operator == Operator.LESS_OR_EQUAL){
        	return new ComparisonCriterion(attributeName,value,ComparisonCriterion.COMPARISON_TYPE_LE);
        }
        if(operator == Operator.CONTAIN){
            return new ContainsCriterion(attributeName,value);
        }

        throw new NeoDatisRuntimeException(NeoDatisError.QUERY_UNKNOWN_OPERATOR.addParameter(operator.getName()));
    }
    

}
