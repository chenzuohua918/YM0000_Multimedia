package com.semisky.ym_multimedia.radio.event;

public class SearchAMResultEvent {
	private int frequency;
	private SearchAMState state;

	public enum SearchAMState {
		CLEARLIST, UNFINISH, FINISH, TIMEOUT, INTERRUPT
	}

	public SearchAMResultEvent(int frequency, SearchAMState state) {
		super();
		this.frequency = frequency;
		this.state = state;
	}

	public int getFrequency() {
		return frequency;
	}

	public SearchAMState getState() {
		return state;
	}

}
