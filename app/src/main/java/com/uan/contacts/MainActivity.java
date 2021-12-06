package com.uan.contacts;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.uan.vsearch.ContactsData;
import com.uan.vsearch.NearPinyin;
import com.uan.vsearch.NearPinyinGraph;
import com.uan.vsearch.PinyinStore;
import com.uan.vsearch.Search;

import java.util.ArrayList;
import java.util.LinkedList;
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

        Search search = new Search();
        search.init(this);

        ArrayList<ContactsData> contactsDataArrayList = new ArrayList<>();
        contactsDataArrayList.add(new ContactsData("梁欢", "10086"));
        contactsDataArrayList.add(new ContactsData("百度-张三", "10011"));
        contactsDataArrayList.add(new ContactsData("华为深圳利四", "323232"));
        contactsDataArrayList.add(new ContactsData("东莞华为昭武", "123532"));
        contactsDataArrayList.add(new ContactsData("83号技师", "10086"));
        contactsDataArrayList.add(new ContactsData("雅阁77技师", "10011"));
        search.addContacts(contactsDataArrayList);

        List<ContactsData> list = search.search("77号技师", 0.2f);
        for (ContactsData data : list) {
            Log.e("lianghuan", "name " + data.name);
        }
    }
}