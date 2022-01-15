package com.uan.search.fuzzy;

import java.util.List;

/**
 * 通用搜索接口定义
 */
interface ICommonSearch {

    /**
     * 通用搜索接口，传入被搜索字符串集合、搜索key、以及模糊的距离，返回模糊搜索的结果。
     *
     * @param sourceList 被搜索的字符串集合
     * @param voiceList  搜索关键拼音列表
     * @param dis        模糊的范围 0 ～ 1
     * @return 模糊搜索结果
     */
    List<SearchResult> search(List<String> sourceList, VoiceList voiceList, float dis);
}
