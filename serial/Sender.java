import java.io.IOException;

import java.util.*;
import jssc.*; 


public class Sender {
	private static SerialPort serialPort;


	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.printf("Call with argument: java -cp \"jssc-2.8.0.jar\" Sender <cmd>");
		} else { // don't need to try/catch here since all of the methods used do it
			String tg = "/dev/tty.usbserial-DA00866A";
			boolean isconnected = connect(tg);
			// System.out.printf("argv[0] : %s\n", args[0]);
			
			if(isconnected) {
				sendGCode(args[0]+"\n");
				disconnect();
			}
		}
	}


	private static boolean connect(String port){
		serialPort = new SerialPort(port);

		try{
			serialPort.openPort();

			serialPort.setParams(SerialPort.BAUDRATE_115200,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);

			serialPort.addEventListener(new PortListener(), SerialPort.MASK_RXCHAR);
			return true;
		}
		catch (SerialPortException ex) {
			System.out.println("Couldn't open port: " + ex);
			return false;
		}	
	}

	private static void disconnect() {
		try{
			sendGCode("$md\n"); //kill motors
			serialPort.closePort();
		} catch (SerialPortException ex) {
			System.out.println("Couldn't close port: " + ex);
		}
	}

	public static void sendGCode(String gCode){
		try{
			serialPort.writeString(gCode);
		} catch (SerialPortException ex) {
			System.out.println("Couldn't write to port: " + ex);
		}
	}


	private static class PortListener implements SerialPortEventListener {

		public void serialEvent(SerialPortEvent event) {
			if(event.isRXCHAR() && event.getEventValue() > 0) {
				try {
					String receivedData = serialPort.readString(event.getEventValue());
					// System.out.println(receivedData);
				}
				catch (SerialPortException ex) {
					System.out.println("Error in receiving string from COM-port: " + ex);
				}
			}
		}
	}
}