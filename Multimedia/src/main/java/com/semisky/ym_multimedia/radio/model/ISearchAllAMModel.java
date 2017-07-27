package com.semisky.ym_multimedia.radio.model;

public interface ISearchAllAMModel {
	void searchAllAM();
	
	void notifyObserversListClear();
	
	void notifyObserversSearchAllAMFinish(int frequency);
	
	void notifyObserversSearchAllAMUnFinish(int frequency);
	
	void notifyObserversSearchAllAMInterrupt(int frequency);
	
	void notifyObserversSearchAllAMTimeout(int frequency);
}
