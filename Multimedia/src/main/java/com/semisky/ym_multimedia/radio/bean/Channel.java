package com.semisky.ym_multimedia.radio.bean;

/**
 * 频道实体类
 * 
 * @author Anter
 * 
 */
public class Channel {
	// 频道频率
	private int channelFrequency;
	// 频道类型（0:FM/1:AM）
	private int channelType;
	// 频道信号强度
	private int channelSignal;
	// 记录位置，用以SortCursor排序
	private int order;

	public int getChannelFrequency() {
		return channelFrequency;
	}

	public void setChannelFrequency(int channelFrequency) {
		this.channelFrequency = channelFrequency;
	}

	public int getChannelType() {
		return channelType;
	}

	public void setChannelType(int channelType) {
		this.channelType = channelType;
	}

	public int getChannelSignal() {
		return channelSignal;
	}

	public void setChannelSignal(int channelSignal) {
		this.channelSignal = channelSignal;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
