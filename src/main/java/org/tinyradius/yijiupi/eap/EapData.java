package org.tinyradius.yijiupi.eap;

import java.nio.ByteBuffer;

public class EapData {
    public static final byte TYPE_IDENTITY = 1;
    public static final byte TYPE_NOTIFICATION = 2;
    public static final byte TYPE_NAK = 3;
    public static final byte TYPE_MD5_CHALLENGE = 4;
    public static final byte TYPE_OTP = 5;
    public static final byte TYPE_GTC = 6;
    public static final byte TYPE_EAP_TLS = 13;
    public static final byte TYPE_EAP_TtLS = 21;
    public static final byte TYPE_EAP_PEAP = 25;

    private byte type;
    private byte[] data;

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public int length() {
        return data.length + 1;
    }

    public int getDataLength() {
        return data == null ? 0 : data.length;
    }

    public static byte[] encode(EapData eapData) {
        byte[] data = new byte[eapData.length()];
        data[0] = eapData.type;
        System.arraycopy(eapData.data, 0, data, 1, eapData.data.length);
        return data;
    }

    public static EapData decode(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        EapData eapData = new EapData();
        eapData.type = byteBuffer.get();
        if (data.length == 1) {
            return eapData;
        }

        int len = data.length - 1;
        eapData.data = new byte[len];
        System.arraycopy(data, 1, eapData.data, 0, len);
        return eapData;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("<type=").append(type).append(",data=");
        for (byte datum : data) {
            str.append(datum).append(" ");
        }
        str.append(">");
        str.append("\n");
        str.append("data2str=").append(new String(data));
        return str.toString();
    }
}
