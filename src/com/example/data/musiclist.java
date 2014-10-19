package com.example.data;

import java.util.ArrayList;

/**
 * nusiclist类，采用单一实例只能用getmusiclist方法获取
 */
public class musiclist {
    private static ArrayList<music> musicarray=new ArrayList<music>();
    private musiclist(){}
    private static ArrayList<music> getmusiclist()
    {
        return musicarray;
    }
}
