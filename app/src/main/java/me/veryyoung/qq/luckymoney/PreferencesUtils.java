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

    public static boolean password() {
        return getInstance().getBoolean("password", true);
    }

    public static boolean sendPassword() {
        return getInstance().getBoolean("send_password", true);
    }

    public static boolean reply() {
	return getInstance().getBoolean("reply", false);
    }
	
    public static boolean reply2() {
	return getInstance().getBoolean("reply2", false);
    }
	
    public static String reply1() {
     	return getInstance().getString("reply1", "");
    }
	
    public static String reply3() {
	return getInstance().getString("reply3", "");
    }	
	
    public static boolean delay() {
        return getInstance().getBoolean("delay", false);
    }
	
    public static int delayTime() {
        return getInstance().getInt("delay_time", 0);
    }
}


