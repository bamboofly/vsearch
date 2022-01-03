package com.uan.vsearch;

import java.util.List;

interface ICommonSearch {

    List<SearchResult> search(List<String> sourceList, String key, float dis);
}
