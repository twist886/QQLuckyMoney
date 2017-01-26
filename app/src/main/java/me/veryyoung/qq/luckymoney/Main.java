package me.veryyoung.qq.luckymoney;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.os.SystemClock.sleep;
import static android.widget.Toast.LENGTH_LONG;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static java.lang.String.valueOf;
import static me.veryyoung.qq.luckymoney.HideModule.hideModule;
import static me.veryyoung.qq.luckymoney.XposedUtils.findFieldByClassAndTypeAndName;
import static me.veryyoung.qq.luckymoney.XposedUtils.findResultByMethodNameAndReturnTypeAndParams;


public class Main implements IXposedHookLoadPackage {

    public static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    private static long msgUid;
    private static String senderuin;
    private static String frienduin;
    private static int istroop;
    private static String selfuin;
    private static Context globalContext;
    private static Object HotChatManager;
    private static Object TicketManager;
    private static Object TroopManager;
    private static Object globalQQInterface = null;
    private static int n = 1;
    private static String qqVersion = "";


    private void dohook(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        initVersionCode(loadPackageParam);

        findAndHookMethod("com.tencent.mobileqq.data.MessageForQQWalletMsg", loadPackageParam.classLoader, "doParse", new
                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open() || msgUid == 0) {
                            return;
                        }
                        msgUid = 0;

                        int messageType = (int) getObjectField(param.thisObject, "messageType");
                        if (messageType == 6 && PreferencesUtils.password()==0) {
                            return;
                        }

                        Object mQQWalletRedPacketMsg = getObjectField(param.thisObject, "mQQWalletRedPacketMsg");
                        String redPacketId = getObjectField(mQQWalletRedPacketMsg, "redPacketId").toString();
                        String authkey = (String) getObjectField(mQQWalletRedPacketMsg, "authkey");
                        Object SessionInfo = newInstance(findClass("com.tencent.mobileqq.activity.aio.SessionInfo", loadPackageParam.classLoader));
                        findFieldByClassAndTypeAndName(findClass("com.tencent.mobileqq.activity.aio.SessionInfo", loadPackageParam.classLoader), String.class, "a").set(SessionInfo, frienduin);
                        findFieldByClassAndTypeAndName(findClass("com.tencent.mobileqq.activity.aio.SessionInfo", loadPackageParam.classLoader), Integer.TYPE, "a").setInt(SessionInfo, istroop);
                        Object QQWalletTransferMsgElem = XposedHelpers.getObjectField(mQQWalletRedPacketMsg, "elem");
                        String password = XposedHelpers.getObjectField(QQWalletTransferMsgElem, "title").toString();
                        Object messageParam = newInstance(findClass("com.tencent.mobileqq.activity.ChatActivityFacade$SendMsgParams", loadPackageParam.classLoader));
                        if(selfuin.equals(senderuin) && PreferencesUtils.we()){
                          return;
                        }
                        
                        String group = PreferencesUtils.group();
                        if (!TextUtils.isEmpty(group)) {
                            for (String group1 : group.split(",")) {
                                if (frienduin.equals(group1)) {
                                    toast("指定群不抢");
                                    return;
                                }
                            }
                        }
                        
                        String keywords = PreferencesUtils.keywords();
                        if (!TextUtils.isEmpty(keywords)) {
                            for (String keywords1 : keywords.split(",")) {
                                if (password.contains(keywords1)) {
                                    toast("关键词不抢");
                                    return;
                                }
                            }
                        }

                        ClassLoader walletClassLoader = (ClassLoader) callStaticMethod(findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", loadPackageParam.classLoader), "getOrCreateClassLoader", globalContext, "qwallet_plugin.apk");
                        StringBuffer requestUrl = new StringBuffer();
                        requestUrl.append("&uin=" + selfuin);
                        requestUrl.append("&listid=" + redPacketId);
                        requestUrl.append("&name=" + Uri.encode(""));
                        requestUrl.append("&answer=");
                        requestUrl.append("&groupid=" + (istroop == 0 ? selfuin : frienduin));
                        requestUrl.append("&grouptype=" + getGroupType());
                        requestUrl.append("&groupuin=" + getGroupuin(messageType));
                        requestUrl.append("&channel=" + getObjectField(mQQWalletRedPacketMsg, "redChannel"));
                        requestUrl.append("&authkey=" + authkey);
                        requestUrl.append("&agreement=0");

                        Class qqplugin = findClass(VersionParam.QQPluginClass, walletClassLoader);

                        int random = Math.abs(new Random().nextInt()) % 16;
                        String reqText = (String) callStaticMethod(qqplugin, "a", globalContext, random, false, requestUrl.toString());
                        StringBuffer hongbaoRequestUrl = new StringBuffer();
                        hongbaoRequestUrl.append("https://mqq.tenpay.com/cgi-bin/hongbao/qpay_hb_na_grap.cgi?ver=2.0&chv=3");
                        hongbaoRequestUrl.append("&req_text=" + reqText);
                        hongbaoRequestUrl.append("&random=" + random);
                        hongbaoRequestUrl.append("&skey_type=2");
                        hongbaoRequestUrl.append("&skey=" + callMethod(TicketManager, "getSkey", selfuin));
                        hongbaoRequestUrl.append("&msgno=" + generateNo(selfuin));

                        Class<?> walletClass = findClass("com.tenpay.android.qqplugin.b.d", walletClassLoader);
                        Object pickObject = newInstance(walletClass, callStaticMethod(qqplugin, "a", globalContext));
                        if (PreferencesUtils.delay()) {
                            sleep(PreferencesUtils.delayTime());
                        }
                        try {
                            Bundle bundle = (Bundle) callMethod(pickObject, "a", hongbaoRequestUrl.toString());
                            double d = ((double) new JSONObject(callStaticMethod(qqplugin, "a", globalContext, random, callStaticMethod(qqplugin, "a", globalContext, bundle, new JSONObject())).toString()).getJSONObject("recv_object").getInt("amount")) / 100.0d;
                            toast("QQ红包帮你抢到了" + d + "元");
                            if(PreferencesUtils.reply()==1 || PreferencesUtils.reply()==3  && !TextUtils.isEmpty(PreferencesUtils.reply1())) {
                              callStaticMethod(findClass("com.tencent.mobileqq.activity.ChatActivityFacade", loadPackageParam.classLoader), "a", globalQQInterface, globalContext, SessionInfo, PreferencesUtils.reply1(), new ArrayList(), messageParam);                             
                            }
                        } catch (Exception e2) {
                            toast("没抢到");
                            if(PreferencesUtils.reply()==2 || PreferencesUtils.reply()==3  && !TextUtils.isEmpty(PreferencesUtils.reply2())) {
                              callStaticMethod(findClass("com.tencent.mobileqq.activity.ChatActivityFacade", loadPackageParam.classLoader), "a", globalQQInterface, globalContext, SessionInfo, PreferencesUtils.reply2(), new ArrayList(), messageParam);                             
                            }
                        }
                        if (6 == messageType && PreferencesUtils.password()==1) {
                            callStaticMethod(findClass("com.tencent.mobileqq.activity.ChatActivityFacade", loadPackageParam.classLoader), "a", globalQQInterface, globalContext, SessionInfo, password, new ArrayList(), messageParam);
                        }
                    }
                }
        );


        findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", loadPackageParam.classLoader, "a",
                "com.tencent.mobileqq.app.QQAppInterface",
                "com.tencent.mobileqq.data.MessageRecord", Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) {
                            return;
                        }
                        int msgtype = (int) getObjectField(param.args[1], "msgtype");
                        if (msgtype == -2025) {
                            msgUid = (long) getObjectField(param.args[1], "msgUid");
                            senderuin = (String) getObjectField(param.args[1], "senderuin");
                            frienduin = getObjectField(param.args[1], "frienduin").toString();
                            istroop = (int) getObjectField(param.args[1], "istroop");
                            selfuin = getObjectField(param.args[1], "selfuin").toString();
                        }
                    }
                }

        );


        findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", loadPackageParam.classLoader, "doOnCreate", Bundle.class, new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        globalContext = (Context) param.thisObject;
                        globalQQInterface = findFirstFieldByExactType(findClass("com.tencent.mobileqq.activity.SplashActivity", loadPackageParam.classLoader), findClass("com.tencent.mobileqq.app.QQAppInterface", loadPackageParam.classLoader)).get(param.thisObject);

                    }
                }

        );


        findAndHookConstructor("mqq.app.TicketManagerImpl", loadPackageParam.classLoader, "mqq.app.AppRuntime", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TicketManager = param.thisObject;
            }
        });


        findAndHookConstructor("com.tencent.mobileqq.app.HotChatManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HotChatManager = param.thisObject;
                    }
                }
        );

        XposedHelpers.findAndHookConstructor("com.tencent.mobileqq.app.TroopManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) {
                TroopManager = methodHookParam.thisObject;
            }
        });

    }


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals(QQ_PACKAGE_NAME)) {
            hideModule(loadPackageParam);

            int ver = Build.VERSION.SDK_INT;
            if (ver < 21) {
                findAndHookMethod("com.tencent.common.app.BaseApplicationImpl", loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        dohook(loadPackageParam);
                    }
                });
            } else {
                dohook(loadPackageParam);
            }
        }


        if (loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) {
            findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    if (activity != null) {
                        Intent intent = activity.getIntent();
                        if (intent != null) {
                            String className = intent.getComponent().getClassName();
                            if (!TextUtils.isEmpty(className) && className.equals("com.tencent.mm.ui.LauncherUI") && intent.hasExtra("donate")) {
                                Intent donateIntent = new Intent();
                                donateIntent.setClassName(activity, "com.tencent.mm.plugin.remittance.ui.RemittanceUI");
                                donateIntent.putExtra("scene", 1);
                                donateIntent.putExtra("pay_scene", 32);
                                donateIntent.putExtra("fee", 10.0d);
                                donateIntent.putExtra("pay_channel", 13);
                                donateIntent.putExtra("receiver_name", "yang_xiongwei");
                                donateIntent.removeExtra("donate");
                                activity.startActivity(donateIntent);
                                activity.finish();
                            }
                        }
                    }
                }
            });
        }

    }

    private void initVersionCode(XC_LoadPackage.LoadPackageParam loadPackageParam) throws PackageManager.NameNotFoundException {
        if (TextUtils.isEmpty(qqVersion)) {
            Context context = (Context) callMethod(callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread", new Object[0]), "getSystemContext", new Object[0]);
            String versionName = context.getPackageManager().getPackageInfo(loadPackageParam.packageName, 0).versionName;
            log("Found QQ version:" + versionName);
            qqVersion = versionName;
            VersionParam.init(versionName);
        }
    }


    private int getGroupType() throws IllegalAccessException {
        int grouptype = 0;
        if (istroop == 3000) {
            grouptype = 2;

        } else if (istroop == 1) {
            Map map = (Map) findFirstFieldByExactType(HotChatManager.getClass(), Map.class).get(HotChatManager);
            if (map != null & map.containsKey(frienduin)) {
                grouptype = 5;
            } else {
                grouptype = 1;
            }
        } else if (istroop == 0) {
            grouptype = 0;
        } else if (istroop == 1004) {
            grouptype = 4;

        } else if (istroop == 1000) {
            grouptype = 3;

        } else if (istroop == 1001) {
            grouptype = 6;
        }
        return grouptype;
    }

    private String getGroupuin(int messageType) throws InvocationTargetException, IllegalAccessException {
        if (messageType != 6) {
            return senderuin;
        }
        if (istroop == 1) {
            return (String) getObjectField(findResultByMethodNameAndReturnTypeAndParams(TroopManager, "a", "com.tencent.mobileqq.data.TroopInfo", frienduin), "troopcode");
        } else if (istroop == 5) {
            return (String) getObjectField(findResultByMethodNameAndReturnTypeAndParams(HotChatManager, "a", "com.tencent.mobileqq.data.HotChatInfo", frienduin), "troopCode");
        }
        return senderuin;
    }

    private String generateNo(String selfuin) {
        StringBuilder stringBuilder = new StringBuilder(selfuin);
        stringBuilder.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        String count = valueOf(n++);
        int length = (28 - stringBuilder.length()) - count.length();
        for (int i = 0; i < length; i++) {
            stringBuilder.append("0");
        }
        stringBuilder.append(count);
        return stringBuilder.toString();
    }

    private void toast(final String content) {
        if (PreferencesUtils.amount()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(globalContext, content, LENGTH_LONG).show();
                }
            });

        }
    }


}
