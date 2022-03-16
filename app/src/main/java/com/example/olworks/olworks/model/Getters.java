package com.example.olworks.olworks.model;

/**
 * Created by OLANG on 6/18/2017.
 */

public class Getters {

    String imageUrl, photoUrl, description, duration, uid, category, email
            , phone, otherName, firstName, jobVicinity, type, message, placeId, addressLocation, pushKey;

    long timestamp;
    boolean urgent;

    public Getters() {
    }

    public Getters(String imageUrl, String photoUrl, String description, String duration, String uid, String category, String email, String phone, String otherName, String firstName, String jobVicinity, String type, String message, String placeId, String addressLocation, String pushKey, long timestamp, boolean urgent) {
        this.imageUrl = imageUrl;
        this.photoUrl = photoUrl;
        this.description = description;
        this.duration = duration;
        this.uid = uid;
        this.category = category;
        this.email = email;
        this.phone = phone;
        this.otherName = otherName;
        this.firstName = firstName;
        this.jobVicinity = jobVicinity;
        this.type = type;
        this.message = message;
        this.placeId = placeId;
        this.addressLocation = addressLocation;
        this.pushKey = pushKey;
        this.timestamp = timestamp;
        this.urgent = urgent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public boolean getUrgent() {
        return urgent;
    }

    public String getCategory() {
        return category;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getOtherName() {
        return otherName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getJobVicinity() {
        return jobVicinity;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getAddressLocation() {
        return addressLocation;
    }

    public String getPushKey() {
        return pushKey;
    }
}
