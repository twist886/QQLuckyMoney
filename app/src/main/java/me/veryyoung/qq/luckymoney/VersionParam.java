package me.veryyoung.qq.luckymoney;

public class VersionParam {

    public static String RedPacketDetailsViewHolderClass = "ncp";

    public static void init(String version) {
        switch (version) {
			case "6.2.0":
                RedPacketDetailsViewHolderClass = "jla";
                break;
			case "6.2.1":
                RedPacketDetailsViewHolderClass = "jir";
                break;
			case "6.2.3":
                RedPacketDetailsViewHolderClass = "jjv";
                break;
			case "6.3.1":
                RedPacketDetailsViewHolderClass = "kwy";
                break;
			case "6.3.3":
                RedPacketDetailsViewHolderClass = "kzo";
                break;
			case "6.3.5":
                RedPacketDetailsViewHolderClass = "mca";
                break;
			case "6.3.6":
                RedPacketDetailsViewHolderClass = "mca";
                break;
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
            case "6.5.8":
                RedPacketDetailsViewHolderClass = "ncp";
                break;
            default:
                RedPacketDetailsViewHolderClass = "ncp";
        }
    }

}
