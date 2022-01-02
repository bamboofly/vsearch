package com.uan.vsearch;

import java.util.List;

public interface ISearch {

    List<SearchResult> search(List<String> sourceList, String key, float dis);
}
