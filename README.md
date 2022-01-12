# Voice Search
## 简介：
项目的功能是计算两个字符串之间发音相似度，可用于语音识别搜索应用中，如语音搜索联系人打电话。使用Java语音在Android平台上实现。

## 特点：
1、非常快。
2、强大的相似音搜索。

## 应用：

```java
    ArrayList<String> list = new ArrayList<>();
    list.add("李晓峰");
    list.add("路人B")
    list.add("10086");

    IMdSearch search = new MdSearch.Builder()
            .context(this) // Application context
            .build();

    List<SearchResult> searchResults = search.search(list, "黎笑疯");
    for (SearchResult result : searchResults) {
        Log.d("test", result.getString() + " = " + result.getScore());
    }
```
打印结果：
李一峰 = 2.53
爱疯B = -0.12878788
10086 = -0.21858585

你还可以使用加速接口：

```java
    ArrayList<String> list2 = new ArrayList<>();
    list2.add("客户-老柳");
    list2.add("电信客服");
    list2.add("老六");

    IFastMdSearch fastMdSearch = new MdSearch.Builder()
            .context(this)
            .build(list2);

    List<SearchResult> searchResults2 = fastMdSearch.search("客服老刘");
    for (SearchResult result : searchResults2) {
        Log.d("test", result.getString() + " = " + result.getScore());
    }
```
打印结果：
客户-老柳 = 1.4739287
老六 = 0.54499996
电信客服 = 0.4166667

## 导入Android项目中
在项目的build.gradle中添加

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
在app目录下的build.gradle添加依赖

```groovy
dependencies {
    implementation 'com.github.bamboofly:vsearch:1.0.2'
}
```
