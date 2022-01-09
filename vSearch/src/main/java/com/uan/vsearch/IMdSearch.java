package com.uan.vsearch;

import java.util.List;

/**
 * 普通模糊搜索接口，通过{@link MdSearch.Builder#build()}获得接口实例
 */
public interface IMdSearch {

    /**
     * 通过传入被搜索的字符串集合和搜索关键字，返回搜索结果。
     * 搜索模糊距离为{@link MdSearch#NEAR_SEARCH_DEPTH_DEFAULT}
     *
     * @param list 被搜索字符串集合
     * @param key  搜索关键字符串
     * @return 搜索结果。按相似得分降序
     */
    List<SearchResult> search(List<String> list, String key);

    /**
     * 通过传入被搜索的字符串集合、搜索关键字符串和搜索模糊距离，返回搜索结果。
     *
     * @param list      被搜索字符串集合
     * @param key       搜索关键字符串
     * @param nearDepth 搜索模糊距离。范围0～1
     * @return 搜索结果。按相似得分降序
     */
    List<SearchResult> search(List<String> list, String key, float nearDepth);
}
