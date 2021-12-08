package uk.co.somestuff.newhouse.dairycontroller;

import com.fazecast.jSerialComm.SerialPort;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

public class Serial {

    private SerialPort serialPort;
    private int timeout = 2000;

    public Serial(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public Serial(SerialPort serialPort, int timeout) {
        this.serialPort = serialPort;
        this.timeout = timeout;
    }

    public SerialResponse read() {
        int waited = 0;
        while (this.serialPort.bytesAvailable() == 0) {
            try {
                Thread.sleep(20);
                waited += 20;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (waited > this.timeout) {
                return null;
            }
        }

        byte[] readBuffer = new byte[this.serialPort.bytesAvailable()];
        this.serialPort.readBytes(readBuffer, readBuffer.length);

        try {
            SerialResponse serialResponse = new SerialResponse(readBuffer);
            System.out.println("(" + Instant.now().getEpochSecond() + ") Rx: 0x" + serialResponse.getHex() + " (" + serialResponse.getString() + ")");
            return serialResponse;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void write(byte b) {
        byte[] sendData = new byte[]{(byte) b};
        this.serialPort.writeBytes(sendData, sendData.length);
        System.out.println("(" + Instant.now().getEpochSecond() + ") Tx: 0x" + byteArrayToString(sendData));
    }

    private String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }

        return sb.toString();
    }
}
