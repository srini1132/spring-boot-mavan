package com.wba.horizon.persistence;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="HRZ_DAILY_POS_DEPT_SALES")
public class DailyPOSDeptSales {
	
	@Id
	@Column(name="DPDS_ID")
	private Integer dpDsId;
	
	@Column(name="DPDS_STR_NO")
	private Integer dpDsStoreNumb;
	
	@Column(name="DPDS_DEPT_No")
	private Integer dpDsDeptNo;
	
	@Column(name="DPDS_DEPT_CODE")
	private String dpDsDepCode;
	
	@Column(name="DPDS_SALE_DTM")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar dpDsDttm;
	
	@Column(name="DPDS_SALE_DATA")
	private Integer dpDsSaleData;
	
	@Column(name="DPDS_REFUND_DATA")
	private Integer dpDsRefundData;
	
	@Column(name="DPDS_CREATED_DTTM")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar dpDsCreatedDttm;
	
	@Column(name="DPDS_CREATED_USER_ID")
	private Integer dpDsCreatedUserId;
	
	@Column(name="DPDS_MODIFIED_DTTM")
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar dpDsModifiedDttm;
	
	@Column(name="DPDS_MODIFIED_USER_ID")
	private Integer dpDsModifiedUserId;

	
	public Integer getDpDsId() {
		return dpDsId;
	}

	public void setDpDsId(Integer dpDsId) {
		this.dpDsId = dpDsId;
	}

	public Integer getDpDsStoreNumb() {
		return dpDsStoreNumb;
	}

	public void setDpDsStoreNumb(Integer dpDsStoreNumb) {
		this.dpDsStoreNumb = dpDsStoreNumb;
	}

	public Integer getDpDsDeptNo() {
		return dpDsDeptNo;
	}

	public void setDpDsDeptNo(Integer dpDsDeptNo) {
		this.dpDsDeptNo = dpDsDeptNo;
	}

	public String getDpDsDepCode() {
		return dpDsDepCode;
	}

	public void setDpDsDepCode(String dpDsDepCode) {
		this.dpDsDepCode = dpDsDepCode;
	}

	public Calendar getDpDsDttm() {
		return dpDsDttm;
	}

	public void setDpDsDttm(Calendar dpDsDttm) {
		this.dpDsDttm = dpDsDttm;
	}

	public Integer getDpDsSaleData() {
		return dpDsSaleData;
	}

	public void setDpDsSaleData(Integer dpDsSaleData) {
		this.dpDsSaleData = dpDsSaleData;
	}

	public Integer getDpDsRefundData() {
		return dpDsRefundData;
	}

	public void setDpDsRefundData(Integer dpDsRefundData) {
		this.dpDsRefundData = dpDsRefundData;
	}

	public Calendar getDpDsCreatedDttm() {
		return dpDsCreatedDttm;
	}

	public void setDpDsCreatedDttm(Calendar dpDsCreatedDttm) {
		this.dpDsCreatedDttm = dpDsCreatedDttm;
	}

	public Integer getDpDsCreatedUserId() {
		return dpDsCreatedUserId;
	}

	public void setDpDsCreatedUserId(Integer dpDsCreatedUserId) {
		this.dpDsCreatedUserId = dpDsCreatedUserId;
	}

	public Calendar getDpDsModifiedDttm() {
		return dpDsModifiedDttm;
	}

	public void setDpDsModifiedDttm(Calendar dpDsModifiedDttm) {
		this.dpDsModifiedDttm = dpDsModifiedDttm;
	}

	public Integer getDpDsModifiedUserId() {
		return dpDsModifiedUserId;
	}

	public void setDpDsModifiedUserId(Integer dpDsModifiedUserId) {
		this.dpDsModifiedUserId = dpDsModifiedUserId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dpDsId == null) ? 0 : dpDsId.hashCode());
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
		DailyPOSDeptSales other = (DailyPOSDeptSales) obj;
		if (dpDsId == null) {
			if (other.dpDsId != null)
				return false;
		} else if (!dpDsId.equals(other.dpDsId))
			return false;
		return true;
	}

	
	
}
