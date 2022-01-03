package com.uan.vsearch;

import java.util.List;

interface IFastSearch {
    List<SearchResult> search(String voice, float dis);
}
