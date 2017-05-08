package com.wba.horizon.persistence;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="HRZ_DAILY_POS_BATCH")
public class DailyPOSBatch {

	@Id
	@Column(name="DPB_ID")
	private Integer dpbId;
	
	@Column(name="DPB_STR_NO")
	private Integer dpStoreNumb;
	
	@Column(name="DPB_START_DTTM")
	@Temporal(TemporalType.TIMESTAMP)
    private Calendar dpbStartDttm;
	
	@Column(name="DPB_STATUS")
    private String dpbStatus;
	
	@Column(name="DPB_END_DTTM")
	@Temporal(TemporalType.TIMESTAMP)
    private Calendar dpbEndDttm;
	
	@Column(name="DPB_CREATED_DTTM")
	@Temporal(TemporalType.TIMESTAMP)
    private Calendar dpbCreatedDttm;
	
	@Column(name="DPB_CREATED_USER_ID")
    private Integer dpbCreateUserId;
	
	@Column(name="DPB_UPDATED_DTTM")
	@Temporal(TemporalType.TIMESTAMP)
    private Calendar dpbUpdatedDttm;
	
	@Column(name="DPB_UPDATED_USER_ID")
    private Integer dpbUpdatedUserId;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dpbId == null) ? 0 : dpbId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DailyPOSBatch other = (DailyPOSBatch) obj;
		if (dpbId == null) {
			if (other.dpbId != null)
				return false;
		} else if (!dpbId.equals(other.dpbId))
			return false;
		return true;
	}

	public Integer getDpbId() {
		return dpbId;
	}

	public void setDpbId(Integer dpbId) {
		this.dpbId = dpbId;
	}

	public Integer getDpStoreNumb() {
		return dpStoreNumb;
	}

	public void setDpStoreNumb(Integer dpStoreNumb) {
		this.dpStoreNumb = dpStoreNumb;
	}

	public Calendar getDpbStartDttm() {
		return dpbStartDttm;
	}

	public void setDpbStartDttm(Calendar dpbStartDttm) {
		this.dpbStartDttm = dpbStartDttm;
	}

	public String getDpbStatus() {
		return dpbStatus;
	}

	public void setDpbStatus(String dpbStatus) {
		this.dpbStatus = dpbStatus;
	}

	public Calendar getDpbEndDttm() {
		return dpbEndDttm;
	}

	public void setDpbEndDttm(Calendar dpbEndDttm) {
		this.dpbEndDttm = dpbEndDttm;
	}

	public Calendar getDpbCreatedDttm() {
		return dpbCreatedDttm;
	}

	public void setDpbCreatedDttm(Calendar dpbCreatedDttm) {
		this.dpbCreatedDttm = dpbCreatedDttm;
	}

	public Integer getDpbCreateUserId() {
		return dpbCreateUserId;
	}

	public void setDpbCreateUserId(Integer dpbCreateUserId) {
		this.dpbCreateUserId = dpbCreateUserId;
	}

	public Calendar getDpbUpdatedDttm() {
		return dpbUpdatedDttm;
	}

	public void setDpbUpdatedDttm(Calendar dpbUpdatedDttm) {
		this.dpbUpdatedDttm = dpbUpdatedDttm;
	}

	public Integer getDpbUpdatedUserId() {
		return dpbUpdatedUserId;
	}

	public void setDpbUpdatedUserId(Integer dpbUpdatedUserId) {
		this.dpbUpdatedUserId = dpbUpdatedUserId;
	}
    
	
	
}
