package com.semisky.ym_multimedia.radio.model;

public interface IRadioPlayModel {
	void addIRadioPlayCallback(IRadioPlayCallback callback);

	void removeIRadioPlayCallback(IRadioPlayCallback callback);

	void playRadio(int targetType, int frequency);
}
