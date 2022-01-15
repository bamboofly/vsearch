package com.uan.search.fuzzy;

import com.uan.search.pinyin.PinyinStore;

/**
 * 将字符串转成拼音的公共类
 */
public class VoiceConvert {

    /**
     * 将字符串的每一个字转成拼音，返回一个VoiceList
     *
     * @param str         字符串
     * @param pinyinStore 拼音仓库
     * @return {@link VoiceList}
     */
    public static VoiceList stringToVoices(String str, PinyinStore pinyinStore) {
        VoiceList voiceList = new VoiceList();
        int[] unicodeArray = str.codePoints().toArray();
        int length = unicodeArray.length;
        for (int i = 0; i < length; i++) {
            int u = unicodeArray[i];
            String pinyin = pinyinStore.getPinyin(u);
            if (pinyin == null) {
                voiceList.add(null);
            } else {
                VoiceList.Voice voice = new VoiceList.Voice(u, pinyin);
                voiceList.add(voice);
            }
        }
        return voiceList;
    }
}
