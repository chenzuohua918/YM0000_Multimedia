package com.semisky.ym_multimedia.radio.event;

public class SearchFMResultEvent {
	private int frequency;
	private SearchFMState state;

	public enum SearchFMState {
		CLEARLIST, UNFINISH, FINISH, TIMEOUT, INTERRUPT
	}

	public SearchFMResultEvent(int frequency, SearchFMState state) {
		super();
		this.frequency = frequency;
		this.state = state;
	}

	public int getFrequency() {
		return frequency;
	}

	public SearchFMState getState() {
		return state;
	}

}
