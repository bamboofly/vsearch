package com.uan.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.uan.vsearch.IFastMdSearch;
import com.uan.vsearch.IMdSearch;
import com.uan.vsearch.MdSearch;
import com.uan.vsearch.pinyin.LineReader;
import com.uan.vsearch.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String s = "㐁";
        char charAt = s.charAt(0);
        int i = charAt;
        Log.e("lianghuan", "charAt " + Integer.toHexString(i));

        String s2 = "齐";
        char charAt2 = s2.charAt(0);
        int i2 = charAt2;
        Log.e("lianghuan", "charAt2 " + Integer.toHexString(i2));

        String s3 = "ì";
        char charAt3 = s3.charAt(0);
        int i3 = charAt3;
        Log.e("lianghuan", "charAt3 " + Integer.toHexString(i3));

        String s4 = "á";
        char charAt4 = s4.charAt(0);
        int i4 = charAt4;
        Log.e("lianghuan", "charAt4 " + Integer.toHexString(i4));

        String s5 = "ē";
        char charAt5 = s5.charAt(0);
        int i5 = charAt5;
        Log.e("lianghuan", "chartAt5 " + Integer.toHexString(i5));

        String s6 = "ú";
        char charAt6 = s6.charAt(0);
        int i6 = charAt6;
        Log.e("lianghuan", "charAt6 " + Integer.toHexString(i6));

        String s7 = "\uD874\uDC16";
        int i77 = s7.codePointAt(0);
        Log.e("lianghuan", "codePointAt i7 = " + Integer.toHexString(i77));
        Log.e("lianghuan", "charAt s7 = " + Integer.toHexString(s7.charAt(0)));

        String a1 = "zhao";
        String a2 = "zha1o";
        String a3 = "zha2o";
        String a4 = "zha3o";

        int i7 = a1.compareTo(a2);
        int i8 = a2.compareTo(a3);
        int i9 = a3.compareTo(a1);
        int i10 = a3.compareTo(a4);
        Log.e("lianghuan", "i7 = " + i7 + " i8 = " + i8 + " i9 = " + i9 + " i10 = " + i10);

//        PinyinStore pinyinStore = new PinyinStore(this);
//        pinyinStore.buildPinyin();
//        Log.e("lianghuan", "buildPinyin map end");
//
//        String pinyin = pinyinStore.getPinyin("齕");
//        Log.e("lianghuan", "pinyin = " + pinyin);

//        NearPinyinGraph nearPinyinGraph = new NearPinyinGraph();
//        nearPinyinGraph.buildPinyinGraph(this);
//
//        LinkedList<NearPinyin> zhao = nearPinyinGraph.getNearPinyin("hua1n", 0.2f);
//        Log.e("lianghuan", "zhao near pinyin size " + zhao.size());
//
//        for (NearPinyin n : zhao) {
//            Log.e("lianghuan", "pinyin " + n.pinyin + ", alike " + n.alike);
//        }

        ArrayList<String> contactsDataArrayList = new ArrayList<>();
        contactsDataArrayList.add("梁欢");
        contactsDataArrayList.add("百度-张三");
        contactsDataArrayList.add("华为深圳利四");
        contactsDataArrayList.add("东莞华为昭武");
        contactsDataArrayList.add("83号技师");
        contactsDataArrayList.add("雅阁77技师");
        contactsDataArrayList.add("贝贝");
        contactsDataArrayList.add("配钥匙");
        contactsDataArrayList.add("北京-欢欢");
        contactsDataArrayList.add("小混蛋");
        contactsDataArrayList.add("小日子");
        contactsDataArrayList.add("岗头村委");
        contactsDataArrayList.add("自己人");
        contactsDataArrayList.add("刘一");
        contactsDataArrayList.add("陈吉科");
        contactsDataArrayList.add("刘斯宁");
        contactsDataArrayList.add("喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗欢欢喜喜欢欢嘻嘻嘻");
        contactsDataArrayList.add("黄芸欢");
        contactsDataArrayList.add("王欢欢");
        contactsDataArrayList.add("欢欢嘻嘻");
        contactsDataArrayList.add("系不系");
        contactsDataArrayList.add("124453");
        contactsDataArrayList.add("12153");
        new LineReader(getAssets(), "contacts.txt").eachLine(l -> {
            contactsDataArrayList.add(l.trim());
        });

        fastSearch(contactsDataArrayList);
//        fastSearch(contactsDataArrayList);
//        fastSearch(contactsDataArrayList);
//        fastSearch(contactsDataArrayList);
//        normalSearch(contactsDataArrayList);
    }

    private void normalSearch(ArrayList<String> contactsDataArrayList) {
        IMdSearch search = new MdSearch.Builder()
                .context(this)
                .build();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 1000000; i++) {
//
//                    Log.i("lianghuan", "search start");
//                    long start = System.currentTimeMillis();
//
////                    List<SearchResult> list = search.search("欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗", 0.3f);
//                    List<SearchResult> list = search.search(contactsDataArrayList, "欢欢北京", 0.3f);
//                    long end = System.currentTimeMillis();
//                    Log.i("lianghuan", "search end, cost time " + (end - start));
//                    try {
//                        Thread.sleep(50);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();

        Log.i("lianghuan", "search start");
        long start = System.currentTimeMillis();

//        List<SearchResult> list = search.search(contactsDataArrayList, "18651199784", 0.3f);
//        List<SearchResult> list = search.search(contactsDataArrayList, "欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗", 0.3f);
        List<SearchResult> list = search.search(contactsDataArrayList,"剪鱼帮", 0.3f);
//        List<SearchResult> list = search.search(contactsDataArrayList, "12453", 0.3f);
        long end = System.currentTimeMillis();
        Log.i("lianghuan", "search end, cost time " + (end - start));
        for (SearchResult data : list) {
            if (data.getScore() < 0.05) {
                continue;
            }
            Log.e("lianghuan", "name " + data.getString() + ", --score " + data.getScore());
        }
    }

    private void fastSearch(ArrayList<String> contactsDataArrayList) {
        IFastMdSearch fastSearch = new MdSearch.Builder()
                .context(this)
                .build(contactsDataArrayList);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for (int i = 0; i < 100000000; i++) {
//
////                    Log.i("lianghuan", "search start");
//                    long start = System.currentTimeMillis();
//
////                    List<SearchResult> list = search.search("欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗", 0.3f);
//                    List<SearchResult> list = fastSearch.search("欢欢北京", 0.3f);
//                    long end = System.currentTimeMillis();
////                    Log.i("lianghuan", "search end, cost time " + (end - start));
////                    try {
////                        Thread.sleep(50);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
//                }
//            }
//        }).start();

        Log.i("lianghuan", "search start");
        long start = System.currentTimeMillis();

        List<SearchResult> list = fastSearch.search("是不是", 0.3f);
//        List<SearchResult> list = fastSearch.search("欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗欢欢喜喜欢欢嘻嘻嘻嘻欢欢欢欢洗洗", 0.3f);
//        List<SearchResult> list = search.search("王欢欢", 0.3f);
//        List<SearchResult> list = search.search("12453", 0.3f);
        long end = System.currentTimeMillis();
        Log.i("lianghuan", "search end, cost time " + (end - start));
        for (SearchResult data : list) {
            if (data.getScore() < 0.05) {
                continue;
            }
            Log.e("lianghuan", "name " + data.getString() + ", --score " + data.getScore());
        }
    }
}