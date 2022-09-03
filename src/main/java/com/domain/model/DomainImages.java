package com.domain.model;

public enum DomainImages {
	NONE( "" ),
	AGRICULTURE( "agriculture_white_36dp.svg" ),
	COMPOST( "compost_white_36dp.svg" ),
    BEER( "sports_bar_white_36dp.svg" ),
    YARD( "yard_white_36dp.svg" );
	
	private String fileName;
	
	private DomainImages( String fileName ){
		this.fileName = fileName;
	}
	
	public String getFileName( ) {
		return fileName;
	}
}	
