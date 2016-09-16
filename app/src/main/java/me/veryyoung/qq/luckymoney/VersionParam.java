package me.veryyoung.qq.luckymoney;

/**
 * Created by veryyoung on 16/9/16.
 */
public class VersionParam {

    public static String RedPacketDetailsViewHolderClass = "nnv";

    public static void init(String version) {
        switch (version) {
            case "6.3.7":
                RedPacketDetailsViewHolderClass = "mbw";
                break;
            case "6.5.0":
                RedPacketDetailsViewHolderClass = "mjv";
                break;
            case "6.5.3":
                RedPacketDetailsViewHolderClass = "mkh";
                break;
            case "6.5.5":
                RedPacketDetailsViewHolderClass = "nnv";
                break;
            default:
                RedPacketDetailsViewHolderClass = "nnv";
        }
    }

}
