package ru.servermonitor.actions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.servermonitor.gui.CustPanelGateway;;

public class SendBySMSGateway extends Thread  {

	private volatile String urlForSendSMS = "http://sms.ru/sms/send?api_id=&to=79*********,79*********&text=";
	private volatile String smsMessageForSend = null;

	private StringBuffer responseGet = null;
	private String fullResponseGet = null;

	private int responseCode = 0;

	private final String USER_AGENT = "Mozilla/5.0";

	// ������ ��� ������ ����
	private SimpleDateFormat currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String logInfo = "";

	private CustPanelGateway panelSMS = null;

	public SendBySMSGateway(CustPanelGateway panelSMS) {
		this.panelSMS = panelSMS;
		urlForSendSMS = panelSMS.getSMSURL().getText();
	}

	// ��������� ��������� � ������, ����� �� ����� ���������
	public void sendSMS(String messageToSend){

		Thread gatewayRun = new Thread(new Runnable() {
		    @Override
		    public void run() {
		    	synchronized (panelSMS) {
		    		urlForSendSMS = panelSMS.getSMSURL().getText().trim();
		    		sendSMSByThread(messageToSend);
		    	}
		    }
		});

		gatewayRun.setDaemon(true);
		gatewayRun.start();
	}

	// ���������� ���-�� ����� ������, ��������� � ������, ����� �� ����� ���������
	private synchronized boolean sendSMSByThread(String strForSend) {
		try {
				if(strForSend == null || strForSend.equals(""))
					throw new NullPointerException("class SendBySMSGateway - void sendSMS: String strForSend is null or \"\"!");

				smsMessageForSend = strForSend = strForSend.replace(" ", "+").replace("\\", "").replace(":", "-");

				panelSMS.getSMSURL().setEditable(false);
				final String urlForSendSMS = panelSMS.getSMSURL().getText() + strForSend;
				panelSMS.getSMSURL().setEditable(true);

				if(!urlForSendSMS.matches("http://(.*)"))
					throw new NullPointerException("class SendBySMSGateway - void sendSMS: urlForSendSMS - wrong URL!");

				panelSMS.insertTextToSMSPanel(currentTime.format(new Date()) + " - ���������� ���������.\n\n");
				sendGET(urlForSendSMS);
				parseGETResponse();
				panelSMS.insertTextToSMSPanel(currentTime.format(new Date()) + ": " + fullResponseGet + "\n\n");

		} catch(NullPointerException e){
				panelSMS.insertTextToSMSPanel(currentTime.format(new Date()) + " :" + e.getMessage() + "\n\n");
				e.getMessage();
				e.printStackTrace();
				return false;
			} catch(Exception e){
					panelSMS.insertTextToSMSPanel(currentTime.format(new Date()) + " :" + e.getMessage() + "\n\n");
					e.getMessage();
					e.printStackTrace();
					return false;
				}

		return true;
	}

	private synchronized void sendGET(final String urlSend) throws Exception {
		URL objURL = new URL(urlSend);
		HttpURLConnection conHttp = (HttpURLConnection) objURL.openConnection();
		// Optional default is GET
		conHttp.setRequestMethod("GET");
		// Add request header
		conHttp.setRequestProperty("User-Agent", USER_AGENT);

		responseCode = conHttp.getResponseCode();

		BufferedReader in = new BufferedReader(new InputStreamReader(conHttp.getInputStream()));
		String inputLine;
		responseGet = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			responseGet.append(inputLine + "\n");
		}

		in.close();
	}

	private void parseGETResponse() {
		switch(Integer.parseInt(responseGet.substring(0, responseGet.indexOf("\n")))) {
			case 100: fullResponseGet = "������ ��������.\n" + responseGet; break;
			case 200: fullResponseGet = "������������ api_id\n" + responseGet; break;
			case 210: fullResponseGet = "������������ GET, ��� ���������� ������������ POST\n" + responseGet; break;
			case 211: fullResponseGet = "����� �� ������\n" + responseGet; break;
			case 220: fullResponseGet = "������ �������� ����������, ���������� ���� �����.\n" + responseGet; break;
			case 300: fullResponseGet = "������������ token (�������� ����� ���� ��������, ���� ��� IP ���������)\n" + responseGet; break;
			case 301: fullResponseGet = "������������ ������, ���� ������������ �� ������\n" + responseGet; break;
			case 302: fullResponseGet = "������������ �����������, �� ������� �� ����������� (������������ �� ���� ���, ���������� � ��������������� ���)\n" + responseGet; break;
			case 201: fullResponseGet = "�� ������� ������� �� ������� �����\n" + responseGet; break;
			case 202: fullResponseGet = "����������� ������ ����������\n" + responseGet; break;
			case 203: fullResponseGet = "��� ������ ���������\n" + responseGet; break;
			case 204: fullResponseGet = "��� ����������� �� ����������� � ��������������\n" + responseGet; break;
			case 205: fullResponseGet = "��������� ������� ������� (��������� 8 ���)\n" + responseGet; break;
			case 206: fullResponseGet = "����� �������� ��� ��� �������� ������� ����� �� �������� ���������\n" + responseGet; break;
			case 207: fullResponseGet = "�� ���� ����� (��� ���� �� �������) ������ ���������� ���������, ���� ������� ����� 100 ������� � ������ �����������\n" + responseGet; break;
			case 208: fullResponseGet = "�������� time ������ �����������\n" + responseGet; break;
			case 209: fullResponseGet = "�� �������� ���� ����� (��� ���� �� �������) � ����-����\n" + responseGet; break;
			case 212: fullResponseGet = "����� ��������� ���������� �������� � ��������� UTF-8 (�� �������� � ������ ���������)\n" + responseGet; break;
			case 230: fullResponseGet = "��������� �� ������� � ��������, ��� ��� �� ���� ����� � ���� ������ ���������� ����� 60 ���������.\n" + responseGet; break;
		}
	}

	//----- GETERS AND SETTERS
	public String getUrlForSend() {
		return urlForSendSMS;
	}

	public void setUrlForSend(String urlForSend) {
		this.urlForSendSMS = urlForSend;
	}

	public String getSmsMessageForSend() {
		return smsMessageForSend;
	}

	public void setSmsMessageForSend(String smsMessageForSend) {
		this.smsMessageForSend = smsMessageForSend;
	}

	public String getFullResponseGet() {
		return fullResponseGet;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getLogInfo() {
		return logInfo;
	}
}
