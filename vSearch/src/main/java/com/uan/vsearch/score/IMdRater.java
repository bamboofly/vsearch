package com.uan.vsearch.score;

/**
 * 标记距离相似评分接口定义
 */
public interface IMdRater {
    /**
     * 计算两个字符串的标记距离得分
     *
     * @param record 标记记录{@link MRecord}
     * @return 相似得分
     */
    float scoring(MRecord record);
}
