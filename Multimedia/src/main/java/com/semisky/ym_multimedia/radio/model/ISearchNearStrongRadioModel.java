package com.semisky.ym_multimedia.radio.model;

/**
 * 收音机搜索上下一个强信号台Model
 * 
 * @author Anter
 * 
 */
public interface ISearchNearStrongRadioModel {
	void addISearchNearStrongRadioCallback(
			ISearchNearStrongRadioCallback callback);

	void removeISearchNearStrongRadioCallback(
			ISearchNearStrongRadioCallback callback);

	void searchPreviousStrongRadio();

	void searchNextStrongRadio();
}
