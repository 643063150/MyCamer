package com.android.zpp.videocamer.listener;

/**
 * @author LLhon
 * @Project diaoyur_android
 * @Package
 * @Date 2018/4/25 20:26
 * @description
 */
public interface RecordStateListener {

    void recordStart();
    void recordEnd(long time);
    void recordCancel();
}
