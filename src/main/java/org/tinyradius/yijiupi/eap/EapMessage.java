package org.tinyradius.yijiupi.eap;


import java.nio.ByteBuffer;

public class EapMessage {
    public static final byte CODE_REQUEST = 1;
    public static final byte CODE_RESPONSE = 2;
    public static final byte CODE_SUCCESS = 3;
    public static final byte CODE_FAILURE = 4;

    public static final Integer EAP_MESSAGE_HEADER_LEN = 4;

    private byte code;
    private byte identifier;
    private int length;
    private EapData data;

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public byte getIdentifier() {
        return identifier;
    }

    public void setIdentifier(byte identifier) {
        this.identifier = identifier;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public EapData getData() {

        return data;
    }

    public void setData(EapData data) {
        this.data = data;
        this.length = EAP_MESSAGE_HEADER_LEN + data.length();
    }

    @Override
    public String toString() {
        return "code = " + code + "  " + (code == 1 ? "Request" : "Response") + "\n" +
                "identifier = " + identifier + "\n" +
                "length = " + length + "\n" +
                "data = " + data + "\n";
    }

    public static byte[] encodeMessage(EapMessage eapMessage) {

        byte[] ret = new byte[EAP_MESSAGE_HEADER_LEN + eapMessage.data.length()];
        ret[0] = eapMessage.code;
        ret[1] = eapMessage.identifier;
        ret[2] = (byte) ((ret.length & 0xFF00) >> 8);
        ret[3] = (byte) (ret.length & 0xFF);
        System.arraycopy(EapData.encode(eapMessage.data), 0, ret, 4, eapMessage.data.length());
        return ret;
    }

    public static EapMessage decodeMessage(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        EapMessage eapMessage = new EapMessage();
        eapMessage.code = byteBuffer.get();
        eapMessage.identifier = byteBuffer.get();
        eapMessage.length = byteBuffer.getShort();
        System.out.println("Length: " + eapMessage.length + "         Data Length:" + data.length);

        byte type = byteBuffer.get();

        int len = eapMessage.length - EAP_MESSAGE_HEADER_LEN - 1;
        byte[] bytes = new byte[len];
        byteBuffer.get(bytes);

        EapData eapData = new EapData();
        eapData.setType(type);
        eapData.setData(bytes);
        eapMessage.setData(eapData);
        return eapMessage;
    }

    public static EapMessage create(byte identifier, byte type, byte[] data) {
        EapMessage response = new EapMessage();
        response.setCode(EapMessage.CODE_RESPONSE);
        response.setIdentifier(identifier);
        EapData eapData = new EapData();
        eapData.setType(type);
        eapData.setData(data);
        response.setData(eapData);
        return response;
    }
}
