package me.veryyoung.qq.luckymoney;


import de.robv.android.xposed.XSharedPreferences;

public class PreferencesUtils {

    private static XSharedPreferences instance = null;

    private static XSharedPreferences getInstance() {
        if (instance == null) {
            instance = new XSharedPreferences(PreferencesUtils.class.getPackage().getName());
            instance.makeWorldReadable();
        } else {
            instance.reload();
        }
        return instance;
    }

    public static boolean open() {
        return getInstance().getBoolean("open", true);
    }
    
    public static boolean amount() {
    	return getInstance().getBoolean("amount", true);
    }
    
    public static boolean we() {
        return getInstance().getBoolean("we", false);
    }
    public static int password() {
        return Integer.parseInt(getInstance().getString("password", "0"));
    }
    
    public static int reply() {
        return Integer.parseInt(getInstance().getString("reply", "0"));
    }
    
    public static String reply1() {
        return getInstance().getString("reply1", "");
    }
    
    public static String reply2() {
        return getInstance().getString("reply2", "");
    }
    
    public static String keywords() {
        return getInstance().getString("keywords", "").replace("，", ",");
    }
    
    public static String group() {
        return getInstance().getString("group", "").replace("，", ",");
    }
    
    public static boolean delay() {
        return getInstance().getBoolean("delay", false);
    }


    public static int delayTime() {
        return getInstance().getInt("delay_time", 0);
    }
}


