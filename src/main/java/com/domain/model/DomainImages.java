package com.domain.model;

public enum DomainImages {
	NONE( "" ),
	AGRICULTURE( "agriculture_white_48dp.svg" ),
	COMPOST( "compost_white_48dp.svg" ),
    BEER( "sports_bar_white_48dp.svg" ),
    WINE( "wine_bar_white_48dp.svg" ),
    FLOWER( "filter_vintage_white_48dp.svg" ),
    NATURE( "emoji_nature_white_48dp.svg" ),
    YARD( "yard_white_48dp.svg" );
	
	private String fileName;
	
	private DomainImages( String fileName ){
		this.fileName = fileName;
	}
	
	public String getFileName( ) {
		return fileName;
	}
}	
