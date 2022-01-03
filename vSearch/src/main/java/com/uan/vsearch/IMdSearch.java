package com.uan.vsearch;

import java.util.List;

public interface IMdSearch {

    List<SearchResult> search(List<String> list, String key);

    List<SearchResult> search(List<String> list, String key, float nearDepth);
}
