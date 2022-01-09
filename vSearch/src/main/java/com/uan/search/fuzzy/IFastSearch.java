package com.uan.search.fuzzy;

import java.util.List;

/**
 * 快速搜索接口定义
 */
interface IFastSearch {
    /**
     * 快速搜索接口，传入搜索key和搜索模糊距离，返回搜索结果。
     * 被搜索字符串需要在初始化时传入{@link MdSearch.Builder#build(List)}
     *
     * @param voice 搜索关键字符串
     * @param dis   搜索模糊距离
     * @return 搜索结果集合
     */
    List<SearchResult> search(String voice, float dis);
}
