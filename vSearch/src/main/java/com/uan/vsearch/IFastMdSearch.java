package com.uan.vsearch;

import java.util.List;

public interface IFastMdSearch extends IMdSearch {

    List<SearchResult> search(String key);

    List<SearchResult> search(String key, float nearDepth);
}
