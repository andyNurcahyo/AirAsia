package com.airasia;

import java.util.Comparator;
import java.util.Map;

public class MapComparator implements Comparator<Map<String, Object>>
{
    private final String key;
    private final String order;

    public MapComparator(String key,String order)
    {
        this.key = key;
        this.order = order;
    }

    public int compare(Map<String, Object> first,
                       Map<String, Object> second)
    {
        // TODO: Null checking, both for maps and values
        Double firstValue = (Double) first.get(key);
        Double secondValue = (Double) second.get(key);
        if(order.equalsIgnoreCase("ASC")){
        	return firstValue.compareTo(secondValue);
        }else if (order.equalsIgnoreCase("DESC")) {
        	return secondValue.compareTo(firstValue);
		}
        
        return firstValue.compareTo(secondValue);
        
    }
    
}

