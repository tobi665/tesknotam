package com.example.tesknotam;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

public class Common {

    public final ArrayList<String> LIST_OF_INODE_ADDRESSES = new ArrayList<String>() {{
        //add("D0:F0:18:44:0C:4B"); // iNode-440D74, p0
        //add("D0:F0:18:44:0D:73"); // iNode-440C4B, p1
        //add("D0:F0:18:44:0D:74"); // iNode-440D73, p2
        add("D0:F0:18:44:0C:4C"); // iNode-440D74, p4
        add("D0:F0:18:44:0D:6E"); // iNode-440D74, p5
        add("D0:F0:18:44:0D:6F"); // iNode-440D74, p6
    }};

    public final int NUMBER_OF_INODE_DEVICES = LIST_OF_INODE_ADDRESSES.size();
}
