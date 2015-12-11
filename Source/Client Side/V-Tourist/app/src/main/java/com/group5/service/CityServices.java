package com.group5.service;

import com.group5.model.City;
import com.group5.parser.DataParser;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 27/11/2015.
 */
public class CityServices {
    public static City getCity(String id) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("City");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        ParseObject object = new ParseObject("City");
        object = query.get(id);

        return DataParser.parseCity(object);
    }

    public static ArrayList<City> getCitiesList() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("City");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        ArrayList<City> cityList = new ArrayList<City>();
        List<ParseObject> listObject = query.find();
        for (ParseObject object: listObject) {
            cityList.add(DataParser.parseCity(object));
        }

        return cityList;
    }
}
