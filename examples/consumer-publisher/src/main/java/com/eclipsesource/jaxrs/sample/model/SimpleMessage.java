package com.eclipsesource.jaxrs.sample.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SimpleMessage {

    private long timeStamp;
    private String message;

    public SimpleMessage() {

    }

    public SimpleMessage(long timeStamp, String message) {
        this.timeStamp = timeStamp;
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
