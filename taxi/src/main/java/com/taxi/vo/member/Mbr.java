package com.taxi.vo.member;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.taxi.util.CustomDateSerializer;
import com.taxi.vo.member.base.BaseMbr;


public class Mbr extends BaseMbr implements Serializable {
	private static final long serialVersionUID = 1L;

	
/*	//====================== AS-IS =======================//
 	
	protected String			mbrId;
	protected String 		mbrName;
	protected String 		mbrPhoneNo;
	protected String 		mbrPhotoUrl;
	protected String 		mbrGender;
	protected Date 			mbrRegDate;
	protected List<Frnd>	frndList;
	
	public List<Frnd> getFrndList() {
		return frndList;
	}
	public Mbr setFrndList(List<Frnd> frndList) {
		this.frndList = frndList;
		return this;
	}
	
	public String getMbrId() {
		return mbrId;
	}
	public Mbr setMbrId(String mbrId) {
		this.mbrId = mbrId;
		return this;
	}
	public String getMbrName() {
		return mbrName;
	}
	public Mbr setMbrName(String mbrName) {
		this.mbrName = mbrName;
		return this;
	}
	public String getMbrPhoneNo() {
		return mbrPhoneNo;
	}
	public Mbr setMbrPhoneNo(String mbrPhoneNo) {
		this.mbrPhoneNo = mbrPhoneNo;
		return this;
	}
	public String getMbrPhotoUrl() {
		return mbrPhotoUrl;
	}
	public Mbr setMbrPhotoUrl(String mbrPhotoUrl) {
		this.mbrPhotoUrl = mbrPhotoUrl;
		return this;
	}
	public String getMbrGender() {
		return mbrGender;
	}
	public Mbr setMbrGender(String mbrGender) {
		this.mbrGender = mbrGender;
		return this;
	}
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getMbrRegDate() {
		return mbrRegDate;
	}
	public Mbr setMbrRegDate(Date mbrRegDate) {
		this.mbrRegDate = mbrRegDate;
		return this;
	}
*/
	
}