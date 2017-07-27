package com.semisky.ym_multimedia.radio.model;

public interface ISwitchFMAMCallback {
	void onSwitchFMAMPrepare(int radioType, boolean resetFragment);

	void beginSwitchFMToFM();

	void beginSwitchFMToAM();

	void beginSwitchAMToFM();

	void beginSwitchAMToAM();
	
	void stopSearchWhenSwitch();

	void beginSwitchFMToFMWhenSearchNearStrongRadio();

	void beginSwitchFMToAMWhenSearchNearStrongRadio();

	void beginSwitchAMToFMWhenSearchNearStrongRadio();

	void beginSwitchAMToAMWhenSearchNearStrongRadio();
}
