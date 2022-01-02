package com.uan.vsearch;

import java.util.List;

public interface IFastSearch {
    List<SearchResult> search(String voice, float dis);
}
