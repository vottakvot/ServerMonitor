package ru.servermonitor.actions;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import ru.servermonitor.gui.CustPanelModem;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ModemCOMInterface {

	// ���������� ��� ���������� ������
	private ArrayList<Thread> currentThread = new ArrayList<Thread>();

	// ����, ���� ������������� ������ �������
	private boolean modemInit = false;

	// ��������� ��� �������� ���������� ��������
	private final int [] BAUDRATE_CONSTs = {115200, 57600, 38400, 19200, 9600};
	private final int [] DATABITS_CONSTs = {5, 6, 7, 8};
	private final int [] STOPBITS_CONSTs = {1, 2, 3};
	private final int [] PARITY_CONSTs = {0, 1, 2, 3, 4};

	// ���������� ��� ��������
	private int BAUDRATE = 115200;
	private int DATABITS = 8;
	private int STOPBITS = 1;
	private int PARITY = 0;

	// ��������� ��� ��������
	private Object mutex = new Object();

	// ������ ���-�����
	private static SerialPort serialPort = null;
	private static String dataFromCOM = null;
	// ���� � ������� ����� ��������� ������.
	// ���� ����� �� ����, �� ������ ��-��������� ��������� ����� � ������ ��������� ���������
	private static volatile String trueCOM = null;

	// ������ ���� ��������� ���-������ ��� ������ � �����
	private static String nameOfCom[] = null;

	// ����� ��� ������
	private String logCommonInfo = null;

	// �������� ��� ������� ������ �� ������, ���� 0 �� �� ��������
	private int sleepTime;

	// ������ ��� ������ ����
	private SimpleDateFormat currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static CustPanelModem panelModem = null;

	// Constructor
	public ModemCOMInterface(int sleepTime, CustPanelModem panelModem) {
		try {
				this.panelModem = panelModem;

				if(sleepTime > 49)
					this.sleepTime = sleepTime;
				else
					throw new Exception("����� ModemCOMInterface: �������� ������������ sleepTime ������ ���� ������ 50");

		} catch(Exception e){
				System.err.println(e.getMessage());
				e.printStackTrace();
				panelModem.insertTextToSMSPanel(currentTime.format(new Date()) + " : " + e.getMessage());
			}
	}


	// ����� ��� �������� �������� ���������
	public class RunModemThread extends Thread {
		private String nameThread = null;

		public RunModemThread(){
			super();
		}

		public RunModemThread(String nameThread){
			super();
			this.nameThread = nameThread;
		}

		@Override
	    public void run() {
			try {
					if(nameThread != null)
						Thread.currentThread().setName(nameThread);

					currentThread.add(Thread.currentThread());

					do {
							synchronized (mutex) {
								// ��������� ����� �� �������� ���������
								// ���� ��������� ���� � ���� ��� ��� ���������� ������� ���������

								mutex.notifyAll();
								mutex.wait();
							}

					} while(!Thread.currentThread().isInterrupted());

			} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.err.println("����� RunModemThread: ����� �������� ����������.");
					e.printStackTrace();
				} finally {
						CloseCOMPorts();
					}
		}
	}

	public void stopAllThreads(){
		if(!currentThread.isEmpty()){
			for(Thread item : currentThread){
				if(item.isAlive()){
					item.interrupt();
				}
			}
			currentThread.clear();
		}
	}

	public Thread getThreadByName(String nameThread){
		if(!currentThread.isEmpty())
			for(Thread item : currentThread){
				if(item.isAlive())
					if(item.getName().equals(nameThread)){
						return item;
					}
			}

		return null;
	}

	public boolean stopThreadByName(String nameThread){
		if(!currentThread.isEmpty())
			for(Thread item : currentThread){
				if(item.isAlive())
					if(item.getName().equals(nameThread)){
						item.interrupt();
						currentThread.remove(item);
						return true;
					}
			}

		return false;
	}

	public void AutoInitModem(){
		try {
				ScanCOMPorts();
				InitCOMPort();
		} catch(InterruptedException eInterrupt) {
			System.err.println("����� ModemCOMInterface - ����� ScanCOMPorts, ���-�� �� ���");
			eInterrupt.printStackTrace();
		}
	}

	public void sendAllMessage(String messageToSend){
		Thread gatewayRun = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	synchronized (mutex) {
		    		if(!panelModem.getModemPhoneListModel().isEmpty())
			    		for(Object item : panelModem.getModemPhoneListModel().toArray())
			    			sendMessageTextMode(item.toString(), messageToSend);

		    		panelModem.insertTextToSMSPanel(logCommonInfo);
					mutex.notifyAll();
		    	}
		    }
		});

		gatewayRun.setDaemon(true);
		gatewayRun.start();
	}

	// ���������� SMS
	public synchronized boolean sendMessageTextMode(String phoneNumber, String messageToSend) {
        try {
    			if(serialPort != null && serialPort.isOpened()){
    				if(messageToSend != null && messageToSend.matches("([0-9|A-Z|a-z|\\-|:|\\\\|/|\\s|!|,|;]+)") && phoneNumber != null && phoneNumber.matches("(\\+[0-9|-]{11,13})"))
	        			{
		        			phoneNumber = phoneNumber.trim().replace("-", "");
		        			messageToSend = messageToSend.trim().replaceAll(":", "-").replaceAll("\\\\", "").replaceAll(";", " ");

			        		serialPort.writeString("AT+CMGF=1\r\n");
			        		serialPort.writeString("AT+CMGS=\"" + phoneNumber + "\"\r");
			        		Thread.sleep(sleepTime);

			            	serialPort.writeString(messageToSend + "\032");
			            	Thread.sleep(sleepTime);

			            	logCommonInfo = currentTime.format(new Date()) + " - �������� ���." + "\n" + "\t" + dataFromCOM + "\n\n";

		        		} else
		        			throw new NullPointerException("����� ModemCOMInterface: ����� �������� � ��������� ��� �������� �� ������ ���� null ��� ������ �������.");
    			} else
    				throw new SerialPortException("serialPort", "SendMessageTextMode", "closed");

        }  catch (SerialPortException ex) {
		        	System.err.println(ex.getMessage());
		        	ex.printStackTrace();
		        	logCommonInfo = currentTime.format(new Date()) + " : " + ex.getMessage() + "\n\n";
		        	return false;
	       		} catch (NullPointerException eNull) {
	       				System.err.println(eNull.getMessage());
       				    eNull.printStackTrace();
       				    logCommonInfo = currentTime.format(new Date()) + " : " + eNull.getMessage() + "\n\n";
       				    return false;
		    		}catch (InterruptedException ex) {
		    				System.err.println(ex.getMessage());
		    				ex.printStackTrace();
		    				logCommonInfo = currentTime.format(new Date()) + " : " + ex.getMessage() + "\n\n";
		    				return false;
		            	}

        return true;
	}

	public boolean setParameters(	final int BAUDRATE,
									final int DATABITS,
									final int STOPBITS,
									final int PARITY) {
		try {
				//�������� ���������� �� ������������
				if(Arrays.binarySearch(BAUDRATE_CONSTs, BAUDRATE) != -1)
					this.BAUDRATE = BAUDRATE;
				 else
					throw new Exception("����� ModemCOMInterface: BAUDRATE ������������� ��������. ���������� �������� {115200, 57600, 38400, 19200, 9600}.");

				if(Arrays.binarySearch(DATABITS_CONSTs, DATABITS) != -1)
					this.DATABITS = DATABITS;
				 else
					 throw new Exception("����� ModemCOMInterface: DATABITS ������������� ��������. ���������� �������� {5, 6, 7, 8}.");

				if(Arrays.binarySearch(STOPBITS_CONSTs, STOPBITS) != -1)
					this.STOPBITS = STOPBITS;
				 else
					throw new Exception("����� ModemCOMInterface: STOPBITS ������������� ��������. ���������� �������� {1, 2, 3}.");

				if(Arrays.binarySearch(PARITY_CONSTs, PARITY) != -1)
					this.PARITY = PARITY;
				 else
					throw new Exception("����� ModemCOMInterface: PARITY ������������� ��������. ���������� �������� {0, 1, 2, 3, 4}.");

		} catch(NullPointerException eNull){
				eNull.printStackTrace();
				throw new NullPointerException("����� ModemCOMInterface: ������ phoneNumbers ������ ��������� ���� �� ���� ����� ��������.");
			} catch (Exception e) {
					System.err.println(e);
					System.err.println("����� ����������� ��������� ��-���������.");
					return false;
				}

		return true;
	}

	// ��������� �����
    private static class EventListener implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                	dataFromCOM = serialPort.readString(event.getEventValue());
                	//System.out.println(dataFromCOM);
                } catch (SerialPortException ex) {
                    	ex.printStackTrace();;
                	}
            }
        }
    }

	// ��������� ��� ��������� ���-�����, ���� GSM-������, ������� ���������� �� ��-�������
	public void ScanCOMPorts() throws InterruptedException {
        String[] portNames = SerialPortList.getPortNames();
     	nameOfCom = null;
     	trueCOM = null;
     	nameOfCom = new String[portNames.length];
     	logCommonInfo = currentTime.format(new Date()) + " - ������������ ������:" + "\n";

        for(int i = 0; i < portNames.length; i++) {
            try {
	            	dataFromCOM = null;
	            	serialPort = null;
	            	nameOfCom[i] = portNames[i].trim();
	            	serialPort = new SerialPort(portNames[i]);
	                serialPort.openPort();
	                serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY);
	                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
	                serialPort.addEventListener(new EventListener(), SerialPort.MASK_RXCHAR);

	                // ������ ������� �������������� �������������
	                serialPort.writeString("AT+CGMI\r\n");
	                Thread.sleep(sleepTime);

	                // ���� �� ������ ������ �����, �� ���������� ����
	                if(dataFromCOM != null && !dataFromCOM.equals("null") && !dataFromCOM.equals("")){
		                trueCOM = portNames[i];
		                nameOfCom[i] += " " + dataFromCOM;

		                // ������ �� ������������� ������
		                serialPort.writeString("AT+CGMM\r\n");
		                Thread.sleep(sleepTime);
		                nameOfCom[i] += dataFromCOM + "!";

		                logCommonInfo += trueCOM + "\n" + dataFromCOM + "\n";
	                }

	                CloseCOMPorts();
            } catch (SerialPortException ex) {
            		ex.printStackTrace();
            	}
        }
	}

	public boolean CloseCOMPorts() {
        try {
        	if(serialPort != null && serialPort.isOpened()){
        		serialPort.closePort();
        		modemInit = false;
        	} else
        		throw new SerialPortException("serialPort", "CloseCOMPorts", "closed");
        } catch (SerialPortException ex) {
            	ex.printStackTrace();
            	logCommonInfo = currentTime.format(new Date()) + " - ���� ��� ������.\n\n";
            	return false;
        	}

        return true;
	}

	// ���������� ������� ������
	@SuppressWarnings("finally")
	public String sendCommandToModem(String strCommand){
		try {
				dataFromCOM = null;

				if(serialPort != null && serialPort.isOpened()){
					strCommand = strCommand.trim().toUpperCase();
					if(strCommand.matches("^[A-Z|0-9|+|\"|=]+<CR>") || strCommand.matches("^[A-Z|0-9|+|\"|=]+<CTRL-Z>")){
						// �������� ����������� �������
						strCommand = strCommand.replaceAll("<CR>", "\r\n");
						strCommand = strCommand.replaceAll("<CTRL-Z>", "\032");

						// ���������� � ���� ���������� �������
						serialPort.writeString(strCommand);
						Thread.sleep(sleepTime);

						logCommonInfo = currentTime.format(new Date()) + " - ���������� ������� " + strCommand + ":\n" + dataFromCOM + "\n\n";
					} else
						throw new Exception("����� ModemCOMInterface: ������� ������������ �������. ���������� ����������� ������� <CR> - Enter, <ctrl-Z> - Esc.");

				} else
					throw new NullPointerException(	"����� ModemCOMInterface: serialPort - COM-���� �� ���������������!\n" +
													"��������������� ���������� ������� InitCOMPort().");
		}  catch (SerialPortException ex) {
					logCommonInfo = currentTime.format(new Date()) + " - ���� �� ���������������, �� ����� �� ����� ����!";
					System.err.println(ex.getMessage());
					ex.printStackTrace();
        		} catch(NullPointerException eNull) {
        				logCommonInfo = currentTime.format(new Date()) + " - ���� �� ���������������, �� ����� �� ����� ����!";
        				System.err.println(eNull.getMessage());
    					eNull.printStackTrace();
	    			}catch(Exception e) {
	    					logCommonInfo = currentTime.format(new Date()) + " - ������� �������� ������������ �������!";
	    					System.err.println(e.getMessage());
	        				e.printStackTrace();
						} finally {
								return dataFromCOM;
							}
	}

	// ������������� ������
	public boolean InitCOMPort() {
        try {
        	 	if((trueCOM != null) && (trueCOM.matches("(COM\\d+)"))){
    	        	serialPort = new SerialPort(trueCOM);
    	            serialPort.openPort();
    	            serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY);
    	            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
    	            serialPort.addEventListener(new EventListener(), SerialPort.MASK_RXCHAR);
    	            logCommonInfo = currentTime.format(new Date()) + " - ������������� ������ �������!" + "\n\n";
    	            modemInit = true;
        	 	} else
        	 		throw new NullPointerException("����� ModemCOMInterface: trueCOM - ����� COM-����� �� ����� ��� ����� �������!");
        }  catch (SerialPortException ex) {
            		System.err.println(ex.getMessage());
            		ex.printStackTrace();
            		logCommonInfo = currentTime.format(new Date()) + " ������ �������������." + "\n\n";
            		return false;
	    		} catch(NullPointerException eNull) {
	    				System.err.println(eNull.getMessage());
	    				eNull.printStackTrace();
	    				logCommonInfo = currentTime.format(new Date()) + " ������ �������������." + "\n\n";
	    				return false;
	    			}

        return true;
	}

	@SuppressWarnings("finally")
	public String CommonInfoAboutModem() {

		logCommonInfo = currentTime.format(new Date()) + " - ����� ���������� � ������:\n";

		try {
				if(serialPort != null && serialPort.isOpened()){
					// �������� ������� SIM-�����
					serialPort.writeString("AT+CPIN?\r\n");
					Thread.sleep(sleepTime);
					logCommonInfo += "������� SIM-�����:\n" + dataFromCOM + "\n";

					// ��������
					serialPort.writeString("AT+CGMR\r\n");
					Thread.sleep(sleepTime);
					logCommonInfo += "������ ��������:\n" + dataFromCOM + "\n";

					// ��������� ������ ������
					serialPort.writeString("AT+GCAP\r\n");
					Thread.sleep(sleepTime);
					logCommonInfo += "������ ������:\n" + dataFromCOM + "\n";

					// ���������� ������
					serialPort.writeString("AT+CPAS\r\n");
					Thread.sleep(sleepTime);
					logCommonInfo += "���������� ������:\n" +  dataFromCOM + "\n\n";

				} else
					throw new NullPointerException("����� ModemCOMInterface: serialPort - COM-���� �� ���������������!");
		}  catch (SerialPortException ex) {
					System.err.println(ex.getMessage());
					ex.printStackTrace();
				} catch(NullPointerException eNull) {
						System.err.println(eNull.getMessage());
						eNull.printStackTrace();
					}catch(Exception e) {
							System.err.println(e.getMessage());
		    				e.printStackTrace();
						} finally {
								return logCommonInfo;
							}
	}

	//----- GETTERS AND SETTERS
	public static String[] getNameOfCom() {
		for(int i = 0; i < nameOfCom.length; i++)
			nameOfCom[i] = nameOfCom[i].replace("\r\n", "").replace("OK", " ").replace("null", "");

		return nameOfCom;
	}

	public String getLogCommonInfo() {
		return logCommonInfo;
	}


    public static String getTrueCOM() {
		return trueCOM;
	}

	public static void setTrueCOM(String trueCOM) {
		ModemCOMInterface.trueCOM = trueCOM;

		panelModem.insertTextToSMSPanel("ModemCOMInterface.trueCOM - " + ModemCOMInterface.trueCOM + "\n\n");
	}

	public boolean isModemInit() {
		return modemInit;
	}

	public Object getMutex() {
		return mutex;
	}
}
