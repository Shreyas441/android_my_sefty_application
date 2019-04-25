package com.thesavior.google.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;


public class Place {
    private String id;
    private String icon;
    private String name;
    private String vicinity;
    private Double latitude;
    private Double longitude;
    String refrence;
    String formatted_phone_number;
    String international_phone_number;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getReference() {
        return refrence;
    }

    public void setReference(String Reference) {
        this.refrence = Reference;
    }
  // for getting numbers
    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public void setFormatted_phone_number(String formatted_phone_number) {
        this.formatted_phone_number = formatted_phone_number;
    }
    
    public String getInternational_phone_number() {
        return international_phone_number;
    }

    public void setInternational_phone_number(String international_phone_number) {
        this.international_phone_number = international_phone_number;
    }
    
    
    static Place jsonToPontoReferencia(JSONObject pontoReferencia) {
        try {
            Place result = new Place();
            JSONObject geometry = (JSONObject) pontoReferencia.get("geometry");
            JSONObject location = (JSONObject) geometry.get("location");
            result.setLatitude((Double) location.get("lat"));
            result.setLongitude((Double) location.get("lng"));
            result.setIcon(pontoReferencia.getString("icon"));
            result.setName(pontoReferencia.getString("name"));
            result.setVicinity(pontoReferencia.getString("vicinity"));
            result.setId(pontoReferencia.getString("id"));
            result.setReference(pontoReferencia.getString("reference"));
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    static Place jsonToPontoReferenciaForDetails(JSONObject pontoReferencia) {
        try {
            Place result = new Place();
            if(pontoReferencia.has("international_phone_number"))
            {
    	        result.setInternational_phone_number(pontoReferencia.getString("international_phone_number"));
            }
            return result;
        } catch (JSONException ex) {
            Logger.getLogger(Place.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
	@Override
    public String toString() {
        return "Place{" + "id=" + id 
        				+ ", icon=" + icon 
        				+ ", name=" + name 
        				+ ", latitude=" + latitude 
        				+ ", longitude=" + longitude 
        				+ ", reference=" + refrence +'}';
    }

}
