package com.uan.vsearch;

import java.util.List;

/**
 * 快速模糊搜索接口，通过{@link MdSearch.Builder#build(List)}获得接口实例
 */
public interface IFastMdSearch extends IMdSearch {

    /**
     * 传入搜索key，返回搜索结果。搜索模糊距离是{@link MdSearch#NEAR_SEARCH_DEPTH_DEFAULT}
     * 被搜索字符串集合需要在构建实例的时候传入{@link MdSearch.Builder#build(List)}
     *
     * @param key 搜索关键字
     * @return 搜索结果集合。按相似得分降序
     */
    List<SearchResult> search(String key);

    /**
     * 传入搜索key和搜索模糊距离，返回搜索结果。
     * 被搜索字符串集合需要在构建实例的时候传入{@link MdSearch.Builder#build(List)}
     *
     * @param key       搜索关键字
     * @param nearDepth 搜索模糊距离。范围0～1
     * @return 搜索结果集合。按相似得分降序
     */
    List<SearchResult> search(String key, float nearDepth);
}
