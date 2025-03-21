package com.domain.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "domainMeasureType", schema="domain")
public class DomainMeasureType {
	
	@Id
	@SequenceGenerator(name="domainmeasuretypeseq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="domainmeasuretypeseq")
	private Long id;
	
	@ManyToOne
    @JoinColumn
	private Domain domain;
	
	@ManyToOne
    @JoinColumn
	private MeasureType measureType;

	@Column( name = "startTime" )
    @Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date updateTime;
	
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
	private String dbSynchToken;
	
	public DomainMeasureType() {
		super();
		this.dbSynch = DbSync.ADD; 
	}
    public DomainMeasureType( Domain domain,  MeasureType measureType, Date updateTime, DbSync dbSynch, String dbSynchToken ) {
		this.measureType = measureType;
		this.domain = domain;
		this.updateTime = updateTime;
    	this.dbSynch = dbSynch;
    	this.dbSynchToken = dbSynchToken;
    }
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
    public Domain getDomain() {
		return domain;
	}
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public MeasureType getMeasureType() {
		return measureType;
	}
	public void setMeasureType(MeasureType measureType) {
		this.measureType = measureType;
	}
	
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public DbSync getDbSynch() {
		return dbSynch;
	}
	public void setDbSynch(DbSync dbSynch) {
		this.dbSynch = dbSynch;
	}

	public String getDbSynchToken() {
		return dbSynchToken;
	}
	public void setDbSynchToken(String dbSynchToken) {
		this.dbSynchToken = dbSynchToken;
	}
	
	@Override
	public String toString() {
		return "DomainCategory [id=" + id + ", domain=" + domain + ", measureType=" + measureType + ", updateTime="
				+ updateTime + ", dbSynch=" + dbSynch + ", dbSynchToken=" + dbSynchToken + "]";
	}
	
}
