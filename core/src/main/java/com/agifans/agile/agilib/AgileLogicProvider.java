package com.agifans.agile.agilib;

import java.io.IOException;
import java.io.InputStream;

import com.sierra.agi.io.IOUtils;
import com.sierra.agi.logic.Logic;
import com.sierra.agi.logic.LogicException;
import com.sierra.agi.logic.LogicProvider;

/**
 * An implementation of the JAGI LogicProvider interface that loads the Logic 
 * in a form more easily used by AGILE.
 */
public class AgileLogicProvider implements LogicProvider {

    @Override
    public Logic loadLogic(short logicNumber, InputStream inputStream, int size) throws IOException, LogicException {
        byte[] rawData = new byte[size];
        IOUtils.fill(inputStream, rawData, 0, size);
        return new AgileLogicWrapper(new com.agifans.agile.agilib.Logic(rawData, false));
    }

    public static class AgileLogicWrapper implements Logic {

        private com.agifans.agile.agilib.Logic agileLogic;
        
        public AgileLogicWrapper(com.agifans.agile.agilib.Logic agileLogic) {
            this.agileLogic = agileLogic;
        }
        
        public com.agifans.agile.agilib.Logic getAgileLogic() {
            return agileLogic;
        }
    }
}
