package com.agifans.agile.gwt;

import com.agifans.agile.WavePlayer;
import com.agifans.agile.worker.DedicatedWorkerGlobalScope;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/**
 * GWT/HTML5 implementation of the WavePlayer.
 */
public class GwtWavePlayer extends WavePlayer {
    
	@Override
	public void playWaveData(byte[] waveData, int endFlag) {
		// We need to transfer the data over to the UI thread, but in such a 
	    // way that we also keep it available in the web worker in case it is played
	    // again. We therefore need to create a copy. Hopefully the set() method
	    // is fast enough when the data is over 10 MB.
	    ArrayBuffer buffer = TypedArrays.createArrayBuffer(waveData.length);
	    Int8Array array = TypedArrays.createInt8Array(buffer);
	    array.set(waveData);
	    
	    // We also need to send the endFlag in the message, so UI thread can set the
	    // flag when the sound has finished playing.
	    DedicatedWorkerGlobalScope.get().postArrayBufferAndObject(
	            "PlaySound", buffer, createEndFlagObject(endFlag));
	}

	private native JavaScriptObject createEndFlagObject(int endFlag)/*-{
        return { endFlag: endFlag };
    }-*/;
	
	@Override
	public void stopPlaying(boolean wait) {
	    DedicatedWorkerGlobalScope.get().postObject("StopSound", JavaScriptObject.createObject());
	}

	@Override
	public void reset() {
	    DedicatedWorkerGlobalScope.get().postObject("StopSound", JavaScriptObject.createObject());
	}

	@Override
	public void dispose() {
	    // Nothing to do.
	}
}
