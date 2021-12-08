package uk.co.somestuff.newhouse.dairycontroller;

import java.io.UnsupportedEncodingException;

public class SerialResponse {

    private byte[] bytes;
    private String hex;
    private String string;

    public SerialResponse(byte[] b) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (byte bb :  b) {
            sb.append(String.format("%02X ", bb));
        }
        this.hex = sb.toString();
        this.string = new String(b, "UTF-8");
        this.bytes = b;
    }

    public byte getByte() {
        return this.bytes[0];
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public String getString() {
        return this.string;
    }

    public String getHex() {
        return this.hex;
    }
}
