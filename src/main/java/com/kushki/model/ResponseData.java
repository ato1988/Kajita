package com.kushki.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseData implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String cart_id;
	private String kushkiToken;
	private String kushkiPaymentMethod;
}