package uk.co.somestuff.newhouse.dairycontroller;

import com.fazecast.jSerialComm.SerialPort;
import com.mongodb.*;
import org.tinylog.Logger;

import javax.swing.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Com extends Thread {

    private Shedding shedding = null;

    public Com(Shedding shedding) {
        this.shedding = shedding;
    }

    @Override
    public void run() {

        // TODO: (needs testing) make function where you can give it an array of what to send and what to expect to get back, throw an exception is wrong?
        // TODO: (in testing) also try the while loop for reading tags, if tags are longer than 4 in the future it would break the program
        // TODO: but don't forget to make a db engine thing so that the program doesnt't have to rely on mongo but can easiy be changed in the class to use it

        SerialPort serialPort = Main.serialPort;
        Serial serial = new Serial(serialPort);
        System.out.println("Starting thread...");

        System.out.println("Checking if the device is awake or asleep");

        while (true) {

            SerialResponse res = null;
            while (res == null) {
                serial.write((byte) 0x20);
                res = serial.read();
            }

            if (res.getByte() == (byte) 0x50) { // The device is asleep and needs waking
                System.out.println("Waking the device");
                Logger.info("Waking the device");

                serial.write((byte) 0x20);
                res = serial.read();
                if (res.getByte() != (byte) 0x50) {
                    Logger.info("Device no longer needs waking?");
                    System.out.println("Device no longer needs waking?");
                    continue;
                }
                serial.write((byte) 0x50);
                res = serial.read();

                byte[] send = {0x06, 0x30, 0x30,0x30,0x30,0x30,0x30,0x30,0x31,0x30,0x30,0x30,0x30,0x30,0x30,0x0d};
                for (int i = 0; i < send.length; i++) {
                    byte r = 0x00;
                    while (r != send[i]) {
                        serial.write((byte) send[i]);
                        if (i == send.length-1) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            serial.write((byte) 0x20);
                        }
                        r = serial.read().getByte();
                        if (r == (byte) 0x3F) {
                            break;
                        }
                    }
                }
            }

            if (res.getByte() == (byte) 0x3F) { // The device is half awake
                Logger.info("Continuing to wake the device");
                System.out.println("Continuing to wake the device");
                byte[] send = {0x06, 0x31, 0x30, 0x30, 0x37, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x33, 0x31, 0x30, 0x0d};
                for (int i = 0; i < send.length; i++) {
                    byte r = 0x00;
                    while (r != send[i]) {
                        serial.write((byte) send[i]);
                        if (i == send.length-1) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            serial.write((byte) 0x20);
                        }
                        r = serial.read().getByte();
                        if (r == (byte) 0x51) {
                            break;
                        }
                    }
                }
            }

            if (res.getByte() == (byte) 0x43) { // TODO: Check what this is actually for, i'm not sure
                System.out.println("Listening for id");
                byte r = 0x43;
                while (r != (byte) 0x51) {
                    serial.write((byte) r);
                    r = serial.read().getByte();
                }
            }

            if (res.getByte() == (byte) 0x53) {
                System.out.println("Tag is waiting to be read");

                /** A Tag is ready to be read **/
                serial.write((byte) 0x20);
                serial.read();
                serial.write((byte) 0x07);
                res = serial.read();

                if (res.getByte() == (byte) 0x07) {
                    System.out.println("Acknowledgment acknowledged");

                    serial.write((byte) 0x0d);

                    StringBuilder tag = new StringBuilder();

                    for (int i = 0; i < 4; i++) {
                        res = serial.read();
                        tag.append(res.getString());
                        serial.write(res.getByte());
                    }

                    // TODO: currently doesnt work/not tested

                    /*while (res.getByte() != (byte) 0x0d) {
                        res = serial.read();
                        tag.append(res.getString());
                        serial.write(res.getByte());
                    }*/

                    serial.read();

                    Logger.info("Tag read '{}'", tag);
                    System.out.println("Tag read '" + tag + "'");


                    /** Now we shed or don't shed it **/

                    Shedding.SheddingResponse sheddingResponse = shedding.isShed(tag.toString());

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!sheddingResponse.isCowKnown()) {
                        Logger.info("Unknown cow '{}'", tag.toString());
                        System.out.println("Unknown cow '" + tag + "'");
                        //JOptionPane.showMessageDialog(null, "Unknown cow '" + tag.toString() + "'", "Moooooo", JOptionPane.ERROR_MESSAGE);
                    }

                    boolean isShed = sheddingResponse.isShed();

                    if (isShed) {
                        Logger.info("Shedding '{}'", sheddingResponse.getCow().toString());
                        System.out.println("Shedding " + sheddingResponse.getCow().toString());

                        List<Exchange> exchanges = new ArrayList<Exchange>();
                        /*exchanges.add(new Exchange((byte) 0x20, ?, true));
                        exchanges.add(new Exchange((byte) 0x20, ?, true));
                        exchanges.add(new Exchange((byte) 0x42, ?, false));
                        exchanges.add(new Exchange((byte) 0x20, ?, true));
                        exchange(exchanges, serial);*/

                        byte[] send = {0x20, 0x20, 0x42};
                        for (int i = 0; i < send.length; i++) {
                            byte r = 0x00;
                            while (r != send[i]) {
                                serial.write((byte) send[i]);
                                if (i == send.length-1) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    serial.write((byte) 0x20);
                                }
                                r = serial.read().getByte();
                                if (r == (byte) 0x51) {
                                    break;
                                }
                            }
                        }

                        exchanges = new ArrayList<Exchange>();
                        /*exchanges.add(new Exchange((byte) 0x06, ?, true));
                        exchanges.add(new Exchange((byte) 0x30, ?, true));
                        exchanges.add(new Exchange((byte) 0x30, ?, true));
                        exchanges.add(new Exchange((byte) 0x30, ?, true));
                        exchanges.add(new Exchange((byte) 0x30, ?, true));
                        exchanges.add(new Exchange((byte) 0x30, ?, true));
                        exchanges.add(new Exchange((byte) 0x30, ?, true));
                        exchanges.add(new Exchange((byte) 0x0d, ?, false));
                        exchanges.add(new Exchange((byte) 0x20, ?, false));
                        exchange(exchanges, serial);*/

                        send = new byte[]{0x06, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x0d};
                        for (int i = 0; i < send.length; i++) {
                            byte r = 0x00;
                            while (r != send[i]) {
                                serial.write((byte) send[i]);
                                if (i == send.length-1) {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    serial.write((byte) 0x20);
                                }
                                r = serial.read().getByte();
                                if (r == (byte) 0x51) {
                                    break;
                                }
                            }
                        }

                    } else {
                        Logger.info("Not shedding '{}'", sheddingResponse.getCow().toString());
                        System.out.println("Not shedding " + sheddingResponse.getCow().toString());

                        /**
                         * what do we do when we don't want to shed a cow, nothing
                         *
                         * **/
                    }

                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Exchange {
        private byte send;
        private Object expected;
        private boolean waitResponse;

        public Exchange(byte send, Object expected, boolean waitResponse) {
            this.send = send;
            this.expected = expected;
            this.waitResponse = waitResponse;
            if (expected == null) {
                this.waitResponse = false;
            }
        }
    }

    private void exchange(List<Exchange> exchanges, Serial serial) {

        /**
         * This should work by sending the send and then checking if the response is what the expected is, if it's not
         * it'll throw an exception (it currently doesn't do this it'll just keep sending the same data until the response it what is expected)
         *
         * TODO: currently not tested
         * TODO: (done) change the byte array to an object with what to send whats expected to be received if anything is expected
         * TODO: Add time out to throw the exception, possibly variable? although not really needed 2 attempts should be enough
         * **/

        for (int i = 0; i < exchanges.size(); i++) {
            if (exchanges.get(i).waitResponse) {
                byte r = 0x00;
                while (r != (byte) exchanges.get(i).expected) {
                    serial.write(exchanges.get(i).send);
                    r = serial.read().getByte();
                }
            }
        }

    }
}
