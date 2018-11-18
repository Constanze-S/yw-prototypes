package org.yesworkflow.annotations;

import org.yesworkflow.YWKeywords;

public class Data extends Annotation {

    public Data(Long id, Long sourceId, Long lineNumber, String comment) throws Exception {        
        super(id, sourceId, lineNumber, comment, YWKeywords.Tag.DATA);
    }
}
