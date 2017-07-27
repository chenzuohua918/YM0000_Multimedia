package com.semisky.ym_multimedia.ymbluetooth.func;



import com.semisky.ym_multimedia.ymbluetooth.data.PhoneBookInfos;

import java.util.Comparator;


public class PinyinComparator implements Comparator<PhoneBookInfos> {

	public int compare(PhoneBookInfos o1, PhoneBookInfos o2) {
		if (o1.getSortLetter().equals("@")
				|| o2.getSortLetter().equals("#")) {
			return 1;
		} else if (o1.getSortLetter().equals("#")
				|| o2.getSortLetter().equals("@")) {
			return -1;
		} else {
			return o1.getSortLetter().compareTo(o2.getSortLetter());
		}
	}

}
