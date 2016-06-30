package me.veryyoung.qq.luckymoney;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;


public class Main implements IXposedHookLoadPackage {

    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";

    static String msgUid = "";
    static String frienduin = "";
    static String istroop = "";
    static String selfuin = "";
    static Context globalcontext = null;
    static Object HotChatManager = null;
    static Object BaseChatPie = null;

    private void dohook(final XC_LoadPackage.LoadPackageParam loadPackageParam) {


        findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", loadPackageParam.classLoader, "a",
                "com.tencent.mobileqq.app.QQAppInterface",
                "com.tencent.mobileqq.data.MessageRecord", Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) {
                            return;
                        }
                        String msgtype = getObjectField(param.args[1], "msgtype").toString();
                        if (msgtype.equals("-2025")) {
                            msgUid = getObjectField(param.args[1], "msgUid").toString();
                            frienduin = getObjectField(param.args[1], "frienduin").toString();
                            istroop = getObjectField(param.args[1], "istroop").toString();
                            selfuin = getObjectField(param.args[1], "selfuin").toString();
                        }
                    }
                }
        );
        findAndHookMethod("com.tencent.mobileqq.data.MessageForQQWalletMsg", loadPackageParam.classLoader, "doParse", new
                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) {
                            return;
                        }
                        if (msgUid.equals("")) {
                            return;
                        }
                        msgUid = "";

                        Object mQQWalletRedPacketMsg = getObjectField(param.thisObject, "mQQWalletRedPacketMsg");
                        String redPacketId = getObjectField(mQQWalletRedPacketMsg, "redPacketId").toString();
                        int messageType = (int) getObjectField(param.thisObject, "messageType");

                        if (messageType == 6) {
                            if (!PreferencesUtils.password()) {
                                return;
                            }
                            Object SessionInfo = findFirstFieldByExactType(findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), findClass("com.tencent.mobileqq.activity.aio.SessionInfo", loadPackageParam.classLoader)).get(BaseChatPie);
                            Object PasswdRedBagManager = findFirstFieldByExactType(BaseChatPie.getClass(), findClass("com.tencent.mobileqq.activity.qwallet.PasswdRedBagManager", loadPackageParam.classLoader)).get(BaseChatPie);
                            if (PreferencesUtils.delay()) {
                                Thread.sleep(PreferencesUtils.delayTime());
                            }
                            callMethod(PasswdRedBagManager, "b", SessionInfo, redPacketId);
                        } else if (globalcontext != null) {
                            Intent intent = new Intent();
                            intent.setClassName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.PayBridgeActivity");
                            intent.putExtra("pay_requestcode", 5);

                            JSONObject extra_data = new JSONObject();
                            extra_data.put("listid", redPacketId);
                            extra_data.put("grouptype", getGroupType(istroop));

                            if (istroop.equals("0")) {
                                extra_data.put("groupid", selfuin);
                            } else {
                                extra_data.put("groupid", frienduin);
                            }
                            String authkey = getObjectField(mQQWalletRedPacketMsg, "authkey").toString();
                            extra_data.put("authkey", authkey);
                            extra_data.put("cftImageUrl", "");
                            intent.putExtra("cftImageUrl", "");
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("extra_data", extra_data);
                            jsonObject.put("userId", selfuin);
                            jsonObject.put("viewTag", "graphb");
                            jsonObject.put("app_info", "appid#1344242394|bargainor_id#1000030201|channel#msg");
                            jsonObject.put("come_from", 2);
                            intent.putExtra("json", jsonObject.toString());
                            if (PreferencesUtils.delay()) {
                                Thread.sleep(PreferencesUtils.delayTime());
                            }
                            globalcontext.startActivity(intent);
                        }
                    }
                }

        );


        findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", loadPackageParam.classLoader, "doOnCreate", Bundle.class, new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        globalcontext = (Context) param.thisObject;
                    }
                }

        );


        findAndHookConstructor("com.tencent.mobileqq.app.HotChatManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HotChatManager = param.thisObject;
                    }
                }

        );

        findAndHookMethod("com.tencent.mobileqq.pluginsdk.PluginProxyActivity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) callMethod(param.thisObject, "getIntent");
                ClassLoader classLoader = (ClassLoader) callStaticMethod(findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", loadPackageParam.classLoader), "a", param.thisObject, getObjectField(param.thisObject, "k").toString(), getObjectField(param.thisObject, "i").toString());
                if (intent.getStringExtra("pluginsdk_launchActivity").equals("com.tenpay.android.qqplugin.activity.GrapHbActivity")) {
                    findAndHookMethod("com.tenpay.android.qqplugin.activity.GrapHbActivity", classLoader, "a", JSONObject.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    Object obj = getObjectField(param.thisObject, "mCloseBtn");
                                    callMethod(param.thisObject, "finish");
                                    callMethod(obj, "performClick");
                                }
                            });
                }
            }
        });


        hookAllConstructors(findClass("com.tencent.mobileqq.activity.BaseChatPie", loadPackageParam.classLoader), new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        BaseChatPie = param.thisObject;
                    }
                }

        );


        findAndHookMethod("com.tencent.mobileqq.activity.aio.item.QQWalletMsgItemBuilder", loadPackageParam.classLoader, "a", "mbw", "com.tencent.mobileqq.data.MessageForQQWalletMsg", "com.tencent.mobileqq.activity.aio.OnLongClickAndTouchListener",
                new XC_MethodHook() {
                    int issend;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        issend = (int) getObjectField(param.args[1], "issend");
                        if (issend != 1) {
                            setObjectField(param.args[1], "issend", 1);
                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        setObjectField(param.args[1], "issend", issend);
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

    }

    private void hideModule(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> applicationList = (List) param.getResult();
                List<ApplicationInfo> resultapplicationList = new ArrayList<>();
                for (ApplicationInfo applicationInfo : applicationList) {
                    if (!applicationInfo.processName.contains("veryyoung")) {
                        resultapplicationList.add(applicationInfo);
                    }
                }
                param.setResult(resultapplicationList);
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<PackageInfo> packageInfoList = (List) param.getResult();
                List<PackageInfo> resultpackageInfoList = new ArrayList<>();
                for (PackageInfo packageInfo : packageInfoList) {
                    if (!packageInfo.packageName.contains("veryyoung")) {
                        resultpackageInfoList.add(packageInfo);
                    }
                }
                param.setResult(resultpackageInfoList);
            }
        });
    }


    private int getGroupType(String istroop) throws IllegalAccessException {
        int grouptype = 0;
        if (istroop.equals("3000")) {
            grouptype = 2;

        } else if (istroop.toString().equals("1")) {
            Map map = (Map) findFirstFieldByExactType(HotChatManager.getClass(), Map.class).get(HotChatManager);
            if (map != null & map.containsKey(frienduin)) {
                grouptype = 5;
            } else {
                grouptype = 1;
            }
        } else if (istroop.toString().equals("0")) {
            grouptype = 0;
        } else if (istroop.toString().equals("1004")) {
            grouptype = 4;

        } else if (istroop.toString().equals("1000")) {
            grouptype = 3;

        } else if (istroop.toString().equals("1001")) {
            grouptype = 6;
        }
        return grouptype;
    }


}