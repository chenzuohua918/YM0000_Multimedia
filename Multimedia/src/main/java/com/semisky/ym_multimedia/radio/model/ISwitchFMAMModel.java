package com.semisky.ym_multimedia.radio.model;

public interface ISwitchFMAMModel {
	void addISwitchFMAMCallback(ISwitchFMAMCallback callback);

	void removeISwitchFMAMCallback(ISwitchFMAMCallback callback);

	void switchRadioType(int radioType, boolean resetFragment);
	void jumpBackPlayMin(); //正在搜索的时候切换至音乐等，回来播放最小频点
}
