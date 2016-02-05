package com.airasia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class airasiaGetData {
	private final String USER_AGENT = "Mozilla/5.0";
	final static Logger logger = Logger.getLogger(airasiaGetData.class);
	
	public static void main(String[] args) throws Exception {
		airasiaGetData http = new airasiaGetData();
		System.out.println("Send Http request");
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("origin", "CGK");
		data.put("destination", "KUL");
		data.put("culture", "id-ID");
		data.put("depart", "2016-02-10");//yyyy-mm-dd
		data.put("return", "2016-02-20");//optional, set "" if just one trip
		data.put("r", "true");//true if return, false if one trip
		data.put("adult", "1");
		data.put("child","0");//assumption :same fare price with adult 
		data.put("infant", "0");
		
		
		http.sendGet(data);
	}
	
	// HTTP GET request
	private void sendGet(HashMap<String,String> data) throws Exception {

		//String url ="https://booking.airasia.com/Flight/Select?o1=CGK&d1=SIN&culture=id-ID&dd1=2016-02-05&dd2=2016-02-10&r=true&ADT=1&CHD=0&inl=0&s=true&mon=true&cc=IDR&c=false";
		String url ="https://booking.airasia.com/Flight/Select?";
		if(data.get("r").toString().equalsIgnoreCase("true")){
			url = url+"o1="+data.get("origin")+"&d1="+data.get("destination")+"&culture="+data.get("culture")+"&dd1="+data.get("depart")+""
					+ "&dd2="+data.get("return")+"&r=true&ADT="+data.get("adult")+"&CHD="+data.get("child")+"&inl="+data.get("infant")+"&s=true&mon=true&cc=IDR&c=false";
		}else{
			url = url+"o1="+data.get("origin")+"&d1="+data.get("destination")+"&culture=id-ID&dd1="+data.get("depart")+"&&ADT="+data.get("adult")+""
					+ "&CHD="+data.get("child")+"&inl="+data.get("infant")+"&s=true&mon=true&cc=IDR&c=false";
		}
		logger.debug("URL>>> "+url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		//parsing HTML response to DOM doc
		Document doc = Jsoup.parse(response.toString());
		//get Depart Data
		Elements oneTrip = doc.getElementsByAttributeValue("name", "airAsiaAvailability.MarketFareKeys[0]");
		//get Return Data
		Elements roundTrip = doc.getElementsByAttributeValue("name", "airAsiaAvailability.MarketFareKeys[1]");
		//get Map of Price
		Elements elementPrice = doc.getElementsByClass("avail-fare-price-wrapper");
						 
		HashMap<String, String> priceMap = new HashMap<String,String>();
		for(int j=0;j<elementPrice.size();j++){
			Elements idMap = elementPrice.get(j).getElementsByAttribute("id");
			priceMap.put(idMap.attr("id"), idMap.attr("data-val"));
		}
		
		logger.debug("FLIGHT >>>>>>>>>>>>>>>>>>>");
		if(oneTrip!=null && oneTrip.size()>0){
			ArrayList<HashMap<String,Object>> departList = new ArrayList<HashMap<String,Object>>();
			departList = getList(oneTrip, priceMap);
			printList(departList);
		}else{
			logger.debug("Flight from "+data.get("origin")+" to "+data.get("destination")+ " not found");
		}
			if(roundTrip!=null && roundTrip.size()>0){
				logger.debug("RETURN >>>>>>>>>>>>>>>>>>>");
				ArrayList <HashMap<String,Object>> returnList = new ArrayList<HashMap<String,Object>>();
				returnList = getList(roundTrip, priceMap);
				printList(returnList);
			}
			
		}
		
		private ArrayList<HashMap<String, Object>> getList(Elements trip,HashMap<String,String> priceMap) {
			ArrayList <HashMap<String, Object>>list = new ArrayList<HashMap<String,Object>>();
			for(int i=0;i<trip.size();i++){
				String flight = trip.get(i).attr("value");
				String [] splitString = flight.split("~");
				String id = trip.get(i).attr("id");
				String price = (String) priceMap.get(id+"-ADT");
				String flightCode ="";
				if(!splitString[7].equalsIgnoreCase("")&&!splitString[8].equalsIgnoreCase("")){
					flightCode = splitString[7].substring(splitString[7].indexOf("|")+1)+splitString[8];
				}
				if(price!=null && !price.equalsIgnoreCase("")){
					HashMap<String,Object> tempMap = new HashMap<>();
					
					Double dblPrice = Double.parseDouble(price.substring(0, price.indexOf(",")));
					tempMap.put("id", id);
					tempMap.put("flightCode", flightCode);
					tempMap.put("origin", splitString[11]);
					tempMap.put("depart",splitString[12]);
					tempMap.put("destination", splitString[13]);
					tempMap.put("arrived", splitString[14]);
					tempMap.put("price", dblPrice);
					list.add(tempMap);
				}
				
			}
			return list;
			
		}
		
		private void printList(ArrayList<HashMap<String, Object>> listTrip){
			Collections.sort(listTrip, new MapComparator("price","ASC"));
			logger.debug("After Sort ASC");
			HashMap<String,Object> maxFare = new HashMap<String,Object>();
			for(int i=0;i<listTrip.size();i++){
				logger.debug(listTrip.get(i));
				maxFare = (HashMap<String,Object>) listTrip.get(i);
			}
			Collections.sort(listTrip, new MapComparator("price","DESC"));
			//logger.debug("After Sort DESC");
			HashMap<String,Object> minFare = new HashMap<String,Object>();
			for(int i=0;i<listTrip.size();i++){
				//logger.debug(listTrip.get(i));
				minFare = (HashMap<String,Object>) listTrip.get(i);
			}
			logger.debug("Minimum Fare >>>> "+minFare);
			logger.debug("Maximum Fare >>>> "+maxFare);
			
		}


	
}



