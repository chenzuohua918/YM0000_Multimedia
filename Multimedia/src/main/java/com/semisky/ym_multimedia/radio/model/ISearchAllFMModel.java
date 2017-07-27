package com.semisky.ym_multimedia.radio.model;

public interface ISearchAllFMModel {
	void searchAllFM();
	
	void notifyObserversListClear();
	
	void notifyObserversSearchAllFMFinish(int frequency);
	
	void notifyObserversSearchAllFMUnFinish(int frequency);
	
	void notifyObserversSearchAllFMInterrupt(int frequency);
	
	void notifyObserversSearchAllFMTimeout(int frequency);
}
