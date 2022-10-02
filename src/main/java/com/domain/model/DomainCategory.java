package com.domain.model;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "domainCategory", schema="domain")
public class DomainCategory {
	
	@Id
	@SequenceGenerator(name="domaincategoryseq", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="domaincategoryseq")
	private Long id;
	
	@ManyToOne
    @JoinColumn
	private Domain domain;
	
	@ManyToOne
    @JoinColumn
	private Category category;

	@Column( name = "startTime" )
    @Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date updateTime;
	
	@Enumerated( EnumType.STRING )
	private DbSync dbSynch;
	private String dbSynchToken;
	
	public DomainCategory() {
		super();
		this.dbSynch = DbSync.ADD; 
	}
    public DomainCategory( Domain domain, Category category, Date updateTime, DbSync dbSynch, String dbSynchToken ) {
		this.category = category;
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
	
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
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
		return "DomainCategory [id=" + id + ", domain=" + domain + ", category=" + category + ", updateTime="
				+ updateTime + ", dbSynch=" + dbSynch + ", dbSynchToken=" + dbSynchToken + "]";
	}
	
}
