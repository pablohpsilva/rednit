package com.rednit.app.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by pablohenrique on 9/27/15.
 */
public class ResultPerson {

    private long id;
    private String identifier;
    private String socialMedia;
    private String firstName;
    private String secondName;
    private String pictureProfile;
    private ArrayList<String> interestList = new ArrayList<String>();

    public JSONObject toJSON(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("id",getId());
            jsonObject.accumulate("identifier",getIdentifier());
            jsonObject.accumulate("socialMedia",getSocialMedia());
            jsonObject.accumulate("firstName",getFirstName());
            jsonObject.accumulate("secondName",getSecondName());
            jsonObject.accumulate("pictureProfile",getPictureProfile());
            jsonObject.accumulate("interestList",listToArray());

            return jsonObject;
        } catch (JSONException ex){
            return null;
        }
    }

    public JSONArray listToArray(){
        JSONArray jsonArray = new JSONArray();
        for (String interest : getInterestList()){
            jsonArray.put(interest);
        }
        return jsonArray;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getPictureProfile() {
        return pictureProfile;
    }

    public void setPictureProfile(String pictureProfile) {
        this.pictureProfile = pictureProfile;
    }

    public void setInterestList(ArrayList<String> interestList) {
        this.interestList = interestList;
    }

    public ArrayList<String> getInterestList() {
        return this.interestList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSocialMedia() {
        return socialMedia;
    }

    public void setSocialMedia(String socialMedia) {
        this.socialMedia = socialMedia;
    }
}
