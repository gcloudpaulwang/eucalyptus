package com.eucalyptus.webui.client.place;

import com.eucalyptus.webui.client.service.Categories;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ImagePlace extends SearchPlace {

  public ImagePlace( String search ) {
    super( search );
  }

  @Prefix( Categories.IMAGE )
  public static class Tokenizer implements PlaceTokenizer<ImagePlace> {

    @Override
    public ImagePlace getPlace( String search ) {
      return new ImagePlace( search );
    }

    @Override
    public String getToken( ImagePlace place ) {
      return place.getSearch( );
    }
    
  }
  
}
