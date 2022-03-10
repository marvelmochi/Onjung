package com.cookandroid.onjung;

import android.provider.BaseColumns;

public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String MEMBERID = "memberid";
        public static final String DATE = "date";
        public static final String TITLE = "title";
        public static final String CONTENTS = "contents";
        public static final String _TABLENAME0 = "diary";
        public static final String _CREATE0 = "create table if not exists "+_TABLENAME0+"("
                +_ID+" integer primary key autoincrement, "
                +MEMBERID+" text not null , "
                +DATE+" text not null , "
                +TITLE+" text not null , "
                +CONTENTS+" text not null );";
    }
}
