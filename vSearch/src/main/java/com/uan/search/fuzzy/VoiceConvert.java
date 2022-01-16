package com.uan.search.fuzzy;

import com.uan.search.pinyin.PinyinIndex;
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
            unicodeAddVoiceList(pinyinStore, voiceList, u);
        }
        return voiceList;
    }

    /**
     * 将字符串的每一个字转成拼音，返回一个VoiceList
     *
     * @param str         拼音串，空格隔开
     * @param pinyinStore 拼音仓库
     * @return {@link VoiceList}
     */
    public static VoiceList anyToVoices(String str, PinyinStore pinyinStore) {
        VoiceList voiceList = new VoiceList();
        int[] codeArray = str.codePoints().toArray();
        int length = codeArray.length;

        for (int i = 0; i < length; ) {
            int code = codeArray[i];
            if (code > 0x2E00) {
                unicodeAddVoiceList(pinyinStore, voiceList, code);
                i++;
            } else {
                // 空格开头，跳过
                if (code == 0x20) {
                    i++;
                    continue;
                }

                int k = i;
                for (; ; ) {
                    // 不是汉字并且不是空格
                    if (codeArray[k] > 0x2E00
                            || codeArray[k] == 0x20) {
                        break;
                    }

                    k++;
                    if (k >= length) {
                        break;
                    }

                }
                // 截取字符串判断是否是拼音
                String mayPinyin = str.substring(i, k);
                int pinyinIndex = pinyinStore.getPinyinIndex(mayPinyin);
                if (pinyinIndex != PinyinIndex.INDEX_NOT_FOUND) {
                    voiceList.add(new VoiceList.Voice(0, mayPinyin));
                    i = k;
                } else {
                    // 不是拼音按单个字符处理
                    for (int j = i; j < k; j++) {
                        unicodeAddVoiceList(pinyinStore, voiceList, codeArray[j]);
                    }
                    if (k < length && codeArray[k] == 0x20) {
                        i = k + 1;
                    } else {
                        i = k;
                    }
                }
            }
        }

        return voiceList;
    }

    private static void unicodeAddVoiceList(PinyinStore pinyinStore, VoiceList voiceList, int code) {
        String pinyin = pinyinStore.getPinyin(code);
        if (pinyin == null) {
            voiceList.add(null);
        } else {
            VoiceList.Voice voice = new VoiceList.Voice(code, pinyin);
            voiceList.add(voice);
        }
    }
}
