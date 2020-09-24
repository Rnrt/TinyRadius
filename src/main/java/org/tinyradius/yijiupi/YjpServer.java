package org.tinyradius.yijiupi;

import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;
import org.tinyradius.yijiupi.eap.EapData;
import org.tinyradius.yijiupi.eap.EapMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class YjpServer extends RadiusServer {
    @Override
    public String getSharedSecret(InetSocketAddress client) {
        return "123456";
    }

    @Override
    public String getUserPassword(String userName) {
        return null;
    }

    @Override
    protected RadiusPacket handlePacket(InetSocketAddress localAddress, InetSocketAddress remoteAddress, RadiusPacket request, String sharedSecret) throws RadiusException, IOException {
        return super.handlePacket(localAddress, remoteAddress, request, sharedSecret);
    }

    @Override
    public RadiusPacket accessRequestReceived(AccessRequest accessRequest, InetSocketAddress client) throws RadiusException {
        String rawUsername = accessRequest.getUserName();
        String rawPassword = accessRequest.getUserPassword();
        String requestMac = getRequestMac(accessRequest);

        System.out.println("AccessRequest Mac: " + requestMac + " Protocol: " + accessRequest.getAuthProtocol());
        //    print("AccessRequest Body:", accessRequest);

        RadiusPacket answer = new RadiusPacket();
        int type = RadiusPacket.ACCESS_REJECT;
        if (AccessRequest.AUTH_EAP.equals(accessRequest.getAuthProtocol())) {
            byte[] attributeData = accessRequest.getAttribute(79).getAttributeData();
            EapMessage eapMessage = EapMessage.decodeMessage(attributeData);
            print("Client Request EapMessage:", eapMessage);
            EapMessage response = processEap(eapMessage, rawUsername.getBytes());
            if (response != null) {
                type = RadiusPacket.ACCESS_CHALLENGE;
                print("Server Response EapMessage:", response);
                answer.addAttribute(new RadiusAttribute(79, EapMessage.encodeMessage(response)));
            }

        } else {
            AuthResult authResult = verifyAccount(rawUsername, rawPassword);
            if (authResult.isSuccess()) {
                type = RadiusPacket.ACCESS_ACCEPT;
            }
            answer.addAttribute("Reply-Message", authResult.getReason());
        }

        answer.setPacketType(type);
        answer.setPacketIdentifier(accessRequest.getPacketIdentifier());
        copyProxyState(accessRequest, answer);
        System.out.println("---------------");
        System.out.println("Answer : " + answer);
        System.out.println("---------------");
        return answer;
    }

    private MessageDigest md5Digest;

    protected MessageDigest getMd5Digest() {
        if (md5Digest == null)
            try {
                md5Digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException("md5 digest not available", nsae);
            }
        return md5Digest;
    }


    private EapMessage processEap(EapMessage request, byte[] data) {
        if (EapData.TYPE_IDENTITY == request.getData().getType()) {
            return EapMessage.create(request.getIdentifier(), EapData.TYPE_NAK, new byte[] { EapData.TYPE_MD5_CHALLENGE });
        } else {

            byte identifier = request.getIdentifier();
            byte[] eapData = request.getData().getData();

            byte md5len = eapData[0];
            byte[] md5data = new byte[md5len];

            System.arraycopy(eapData, 1, md5data, 0, md5len);

            byte[] response = new byte[17];
            response[0] = 16;
            System.arraycopy(md5(identifier, "testpwd".getBytes(), md5data), 0, response, 1, 16);
            return EapMessage.create(identifier, EapData.TYPE_MD5_CHALLENGE, response);

        }
    }

    private byte[] md5ChallengeData(byte id, byte[] data) {
//        byte len = data[0];
//        byte[] md5Data = new byte[len];
//        System.arraycopy(data, 1, md5Data, 0, len);
        System.out.println("Md5 Challenge Data: " + Arrays.toString(data));
        byte[] response = new byte[17];
        response[0] = 16;
        System.arraycopy(md5(id, "testpwd".getBytes(), data), 0, response, 1, 16);
        return response;
    }

    private byte[] md5(byte id, byte[] password, byte[] data) {
        MessageDigest md5Digest = getMd5Digest();
        md5Digest.reset();
        md5Digest.update(id);
        md5Digest.update(password);
        md5Digest.update(data);
        return md5Digest.digest();
    }

    @Override
    public RadiusPacket accountingRequestReceived(AccountingRequest accountingRequest, InetSocketAddress client) throws RadiusException {
        String requestMac = getRequestMac(accountingRequest);
        System.out.println("AccountingRequest Mac: " + requestMac);
        return super.accountingRequestReceived(accountingRequest, client);
    }

    private String getRequestMac(RadiusPacket request) {
        RadiusAttribute attribute = request.getAttribute("Calling-Station-Id");
        return attribute == null ? null : attribute.getAttributeValue();
    }

    private void setExpire(RadiusPacket packet, long time) {
        packet.addAttribute("Session-Timeout", String.valueOf(time));
    }

    private AuthResult verifyAccount(String rawUsername, String rawPassword) {
        System.out.println("Username: " + rawUsername + "  Password: " + rawPassword);
        if ("test".equals(rawUsername) && "testpwd".equals(rawPassword)) {
            return AuthResult.create(true, "Test Account / Password Success.");
        } else {
            return AuthResult.fail("Test Account/Password Incorrect");
        }
//        String[] data = rawUsername.split(":");
//        System.out.println(rawUsername);
//        if (data[0].startsWith(AccountPrefix.GUEST)) {
//            String username = data[1];
//        } else {
        //  return AuthService.oaAuth(rawUsername, rawPassword);
        //  }
        // return AuthResult.fail("不支持的登录方式");
    }

    private void print(String prompt, Object obj) {
        System.out.println("==========");
        System.out.println(prompt);
        System.out.println();
        System.out.println(obj);
        System.out.println("\n");
        System.out.println("==========");
    }

    public static class AccountPrefix {
        /**
         * OA 登录
         */
        public static final String OA = "OA";
        /**
         * 访客
         */
        public static final String GUEST = "GUEST";
        /**
         * 二维码验证
         */
        public static final String QR = "QR";

    }


    public static void main(String[] args) {
        YjpServer yjpServer = new YjpServer();
        yjpServer.start(true, true);
        System.out.println("Server started.");
    }
}
