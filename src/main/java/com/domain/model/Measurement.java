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
@Table(name = "measurement", schema="domain")
public class Measurement {

	@Id
	// Below is for H2 DB
	// @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	// Below is for postgres DB
	@GeneratedValue(strategy = GenerationType.AUTO, generator="measurement_id_seq")
    @SequenceGenerator(name="measurement_id_seq", sequenceName="measurement_id_seq", allocationSize=1)
    @Column(name = "id")
	private Long id;
	
	private double valueNumber;
	private String valueText;
	@ManyToOne
    @JoinColumn
    private Batch batch;
	
	@ManyToOne
    @JoinColumn
	private Process process;

	@ManyToOne
    @JoinColumn
	private MeasureType type;

	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
	private String dbSynchToken;

	@Column( name = "startTime" )
    @Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date measurementTime;

	public Measurement() {
		super();
		this.dbSynch = DbSync.ADD; 
	}
	public Measurement( double valueNumber, String valueText, Batch batch, Process process, MeasureType type, Date measurementTime) 
	{
		super();
		this.valueNumber = valueNumber;
		this.valueText = valueText;
		this.batch = batch;
		this.process = process;
		this.type = type;
		this.measurementTime = measurementTime;
		this.dbSynch = DbSync.ADD; 
	}
	public Measurement( double valueNumber, String valueText, Batch batch, Process process, MeasureType type, Date measurementTime, DbSync dbSynch, String dbSynchToken) 
	{
		super();
		this.valueNumber = valueNumber;
		this.valueText = valueText;
		this.batch = batch;
		this.process = process;
		this.type = type;
		this.measurementTime = measurementTime;
    	this.dbSynch = dbSynch;
    	this.dbSynchToken = dbSynchToken;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public double getValueNumber() {
		return valueNumber;
	}
	public void setValueNumber(double valueNumber) {
		this.valueNumber = valueNumber;
	}
	
	public String getValueText() {
		return valueText;
	}
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}
	
	public Batch getBatch() {
		return batch;
	}
	public void setBatch(Batch batch) {
		this.batch = batch;
	}
	public Process getProcess() {
		return process;
	}
	public void setProcess(Process process) {
		this.process = process;
	}

    public MeasureType getType() {
		return type;
	}
	public void setType(MeasureType type) {
		this.type = type;
	}
	
	public Date getMeasurementTime() {
		return measurementTime;
	}
	public void setMeasurementTime(Date measurementTime) {
		this.measurementTime = measurementTime;
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
		return "Measurement [id=" + id + ", valueNumber=" + valueNumber + ", valueText=" + valueText + ", batch="
				+ batch + ", process=" + process + ", type=" + type + ", measurementTime=" + measurementTime 
				+ ", dbSynch=" + dbSynch + ", dbSynchToken=" + dbSynchToken
				+ "]";
	}
}
