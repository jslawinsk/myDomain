package com.domain.model;

public enum DomainImages {
	NONE( "" ),
    BEER( "sports_bar_white_48dp.svg" ),
    WINE( "wine_bar_white_48dp.svg" ),
	AGRICULTURE( "agriculture_white_48dp.svg" ),
	COMPOST( "compost_white_48dp.svg" ),
    YARD( "yard_white_48dp.svg" ),
    EGG( "egg_white_48dp.svg" ),
    FLOWER( "filter_vintage_white_48dp.svg" ),
    FLOWER2( "local_florist_white_48dp.svg" ),
    HIVE( "hive_white_48dp.svg" ),
    NATURE( "emoji_nature_white_48dp.svg" ),
    FIRE( "fireplace_white_48dp.svg" ),
    GRILL( "outdoor_grill_white_48dp.svg" ),
    HOME( "home_white_48dp.svg" ),
    WORLD( "public_white_48dp.svg" );
	
	private String fileName;
	
	private DomainImages( String fileName ){
		this.fileName = fileName;
	}
	
	public String getFileName( ) {
		return fileName;
	}
}	
