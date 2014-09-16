package edu.buffalo.cse.cse486586.simpledht;

import java.util.Hashtable;

import android.content.ContentValues;

import java.util.HashMap;

import java.io.Serializable;

public class Message implements Serializable {
    
    String port;
    String predecessor;
    String successor;
    String first_node;
    String last_node;
    String msg;
    String key;
    String value;
    HashMap<String, String> data;
    
	public Message(String msg,String predecessor, String successor,String fn , String ln) {

        this.predecessor = predecessor;
        this.successor = successor;
        this.first_node = fn;
        this.last_node = ln;
        this.msg = msg;
    }
	public Message(String msg, String port)
	{
	    this.msg = msg;
	    this.port = port;
	}
	
	public Message(String msg, HashMap<String, String> data, String value)
	{
	    this.msg = msg;
	    this.data = data;
	    this.value = value;
	}
	public Message(String msg, String key, String value)
	{
	    this.msg = msg;
	    this.key = key;
	    this.value = value;
	}
	
	public String getPort() {
	    return port;
	}

	public void setPort(String port) {
	    this.port = port;
	}

	public String getFirst_node() {
	    return first_node;
	}

	public void setFirst_node(String first_node) {
	    this.first_node = first_node;
	}

	public String getLast_node() {
	    return last_node;
	}

	public void setLast_node(String last_node) {
	    this.last_node = last_node;
	}

	public String getKey() {
	    return key;
	}

	public void setKey(String key) {
	    this.key = key;
	}

	public String getValue() {
	    return value;
	}

	public void setValue(String value) {
	    this.value = value;
	}

	public HashMap<String, String> getData() {
	    return data;
	}

	public void setData(HashMap<String, String> data) {
	    this.data = data;
	}

	
   
    public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String port() {
        return port;
    }

	public String getport() {
		return port;
	}

	public void setport(String port) {
		this.port = port;
	}

	public String getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(String predecessor) {
		this.predecessor = predecessor;
	}

	public String getSuccessor() {
		return successor;
	}

	public void setSuccessor(String successor) {
		this.successor = successor;
	}

}
