package org.yesworkflow.annotations;

import org.yesworkflow.YWKeywords;

public class Out extends Flow {
    
    public Out(String comment) throws Exception {        
        super(comment, YWKeywords.STANDARD_OUT_KEYWORD);
    }  
}