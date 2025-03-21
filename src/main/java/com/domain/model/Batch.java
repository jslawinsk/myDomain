package com.domain.model;

import java.util.Date;
import java.time.LocalDateTime;

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
import jakarta.persistence.Transient;

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

	@ManyToOne
    @JoinColumn
	private Domain domain;
	
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
	public Batch( boolean active, String name, String description, Category category, Domain domain, Date startTime) {
		super();
		this.active = active;
		this.name = name;
		this.description = description;
		this.category = category;
		this.domain = domain;
		this.startTime = startTime;
		this.dbSynch = DbSync.ADD; 
	}
	public Batch( boolean active, String name, String description, Category category, Domain domain, Date startTime, DbSync dbSynch, String dbSynchToken ) {
		super();
		this.active = active;
		this.name = name;
		this.description = description;
		this.category = category;
		this.domain = domain;
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

	public Domain getDomain() {
		return domain;
	}
	public void setDomain(Domain domain) {
		this.domain = domain;
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
		return "Batch [active=" + active + ", name=" + name + ", id=" + id + ", description=" + description 
				+ ", category=" + category + ", domain=" + domain + ", startTime=" + startTime
				+ ", dbSynch=" + dbSynch + ", dbSynchToken=" + dbSynchToken
				+ "]";
	}

}
