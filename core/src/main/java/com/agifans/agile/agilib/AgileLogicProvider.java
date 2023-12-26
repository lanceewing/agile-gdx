package com.agifans.agile.agilib;

import java.io.IOException;
import java.io.InputStream;

import com.agifans.agile.agilib.jagi.io.CryptedInputStream;
import com.agifans.agile.agilib.jagi.io.IOUtils;
import com.agifans.agile.agilib.jagi.io.LZWInputStream;
import com.agifans.agile.agilib.jagi.logic.Logic;
import com.agifans.agile.agilib.jagi.logic.LogicException;
import com.agifans.agile.agilib.jagi.logic.LogicProvider;

/**
 * An implementation of the JAGI LogicProvider interface that loads the Logic 
 * in a form more easily used by AGILE.
 */
public class AgileLogicProvider implements LogicProvider {

    @Override
    public Logic loadLogic(short logicNumber, InputStream inputStream, int size) throws IOException, LogicException {
        byte[] rawData = new byte[size];
        IOUtils.fill(inputStream, rawData, 0, size);
        // If the game is an AGIV2 one, then the JAGI InputStream used is the 
        // CryptedInputStream, which already takes care of decrypting the messages
        // for us. In that scenario, we pass false in for messagesCrypted, since
        // they will not be crypted, as far as the AGILE Logic class is concerned. 
        // For AGIV3 games, the InputStream can be either the LZWInputStream or the
        // SegmentedInputStream. In the first case, the messages are not crypted, but
        // in the second case they are.
        boolean messagesCrypted = !((inputStream instanceof CryptedInputStream) || (inputStream instanceof LZWInputStream));
        return new AgileLogicWrapper(new com.agifans.agile.agilib.Logic(rawData, messagesCrypted));
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
