package enginuity.logger;

import enginuity.logger.comms.DefaultSerialPortDiscoverer;
import enginuity.logger.comms.DefaultTwoWaySerialComm;
import enginuity.logger.comms.SerialConnection;
import enginuity.logger.comms.SerialPortDiscoverer;
import enginuity.logger.comms.TwoWaySerialComm;
import enginuity.logger.query.DefaultQuery;
import enginuity.logger.query.LoggerCallback;
import gnu.io.CommPortIdentifier;
import static gnu.io.SerialPort.DATABITS_8;
import static gnu.io.SerialPort.PARITY_NONE;
import static gnu.io.SerialPort.STOPBITS_1;

import java.util.List;

public final class TestEcuLogger {
    private static final int CONNECT_TIMEOUT = 2000;

    private TestEcuLogger() {
    }

    public static void main(String... args) {
        try {
            //testTwoWaySerialComm();
            testLoggerController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testLoggerController() {
        LoggerController controller = new DefaultLoggerController();
        try {
            controller.start();
            addLogger(controller, "1");
            addLogger(controller, "2");
            addLogger(controller, "3");
            sleep(1000);
            controller.removeLogger("2");
            controller.removeLogger("1");
            controller.removeLogger("3");
            addLogger(controller, "4");
            sleep(1000);
        } finally {
            controller.stop();
        }

    }

    private static void addLogger(LoggerController controller, String address) {
        controller.addLogger(address, new LoggerCallback() {
            public void callback(byte[] value) {
                printResponse(value);
            }
        });
    }

    private static void testTwoWaySerialComm() {
        SerialPortDiscoverer serialPortDiscoverer = new DefaultSerialPortDiscoverer();
        List<CommPortIdentifier> portIdentifiers = serialPortDiscoverer.listPorts();
        for (CommPortIdentifier commPortIdentifier : portIdentifiers) {
            TwoWaySerialComm twoWaySerialComm = new DefaultTwoWaySerialComm();
            try {
                SerialConnection serialConnection = twoWaySerialComm.connect(commPortIdentifier.getName(), 10400, DATABITS_8,
                        STOPBITS_1, PARITY_NONE, CONNECT_TIMEOUT);
                executeQuery(serialConnection, "FooBar");
                executeQuery(serialConnection, "AbCdEfGhIjKlMnOpQrStUvWxYz0123456789");
                executeQuery(serialConnection, "HAZAA!!");
            } finally {
                twoWaySerialComm.disconnect();
            }
        }
    }

    private static void executeQuery(SerialConnection serialConnection, String queryString) {
        byte[] response = serialConnection.transmit(new DefaultQuery(queryString.getBytes()));
        printResponse(response);
    }

    private static void printResponse(byte[] value) {
        System.out.println("Bytes read: " + String.valueOf(value));
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
