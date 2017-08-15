package com.ffdc.stats;

/**
 * Enum representing  of Various compression intervals
 * @author LENOVO
 *
 */
public enum DataCompressionTypes {

	Week(300000),
	Month(900000),
	Beyond(3600000) ;
	
	private int value;  
	private DataCompressionTypes(int value){  
	this.value=value;  
	}
	public int getValue()
	{
		return value;
	}
	
}