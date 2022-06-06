package com.domain.model;

import java.util.Date;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "batch", schema="domain")
public class Batch {

	@Id
	@SequenceGenerator(name="batchseq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="batchseq")
	private Long id;
	private boolean active;
	
	@Column(name="name", nullable=false, unique=true)
	private String name;
	
	private String description;

	@Schema( description = "Status of syncronizing data with another instance of the service" )
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
	private String dbSynchToken;
	
	@ManyToOne
    @JoinColumn
	private Category category;

	@Column( name = "startTime" )
    @Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date startTime;

//	    private LocalDateTime dateTime;	
	
	public Batch() {
		super();
		this.active = false;
		this.dbSynch = DbSync.ADD; 
	}
	public Batch( boolean active, String name, String description, Category category, Date startTime) {
		super();
		this.active = active;
		this.name = name;
		this.description = description;
		this.category = category;
		this.startTime = startTime;
		this.dbSynch = DbSync.ADD; 
	}
	public Batch( boolean active, String name, String description, Category category, Date startTime, DbSync dbSynch, String dbSynchToken ) {
		super();
		this.active = active;
		this.name = name;
		this.description = description;
		this.category = category;
		this.startTime = startTime;
    	this.dbSynch = dbSynch;
    	this.dbSynchToken = dbSynchToken;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}

    public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
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
		return "Batch [active=" + active + ", name=" + name + ", id=" + id + ", description=" + description + ", category=" + category + ", startTime=" + startTime
				+ ", dbSynch=" + dbSynch + ", dbSynchToken=" + dbSynchToken
				+ "]";
	}

}
