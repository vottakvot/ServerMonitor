package ru.servermonitor.gui;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import java.text.ParseException;

public class CustTabbedPane extends JTabbedPane {

	//----- ��� ������
	private JPanel firstModemPanel = null;
	private CustPanelModem custModemPane = null;
	private CustTwoButtons btnModemPane = null;

	//----- ��� ���������� ������
	private JPanel secondHDDPanel = null;
	private CustTwoButtons btnHDDPane = null;
	private CustPanelHDD custHDDPanel = null;

	//----- ��� ������� ������
	private JPanel thirdHDDPanel = null;
	private CustPanelGateway custSMSPanel = null;

	private CustAboutDlg aboutDlg = null;
	private CustWindowWithTabs mainFrame = null;

	public CustTabbedPane(CustWindowWithTabs mainFrame) throws ParseException {
		this.mainFrame = mainFrame;

		setTabPlacement(JTabbedPane.LEFT);
		setBorder(new CompoundBorder(	new SoftBevelBorder( BevelBorder.RAISED, null, null, null, null),
										new EmptyBorder(10, 10, 10, 10)));
		HddPanel();
		ModemPanel();
		SmsGatewayPanel();
		AboutDlg();

		setVisible(true);
	}

	private void ModemPanel() throws ParseException {
		firstModemPanel = new JPanel();
		firstModemPanel.setLayout(new BoxLayout(firstModemPanel, BoxLayout.Y_AXIS));
		custModemPane = new CustPanelModem(this);
		firstModemPanel.add(custModemPane);
		btnModemPane = new CustTwoButtons("��������� �����", "���������� �����");
		btnModemPane.getCustFirstBtn().setEnabled(false);
		btnModemPane.getCustSecondBtn().setEnabled(false);
		firstModemPanel.add(btnModemPane);
		addTab("������ GSM-������", firstModemPanel);
	}

	private void HddPanel() throws ParseException {
		secondHDDPanel = new JPanel();
		secondHDDPanel.setLayout(new BoxLayout(secondHDDPanel, BoxLayout.Y_AXIS));
		custHDDPanel = new CustPanelHDD(this);
		secondHDDPanel.add(custHDDPanel);
		btnHDDPane = new CustTwoButtons("��������� �����", "���������� �����");
		btnHDDPane.getCustSecondBtn().setEnabled(false);
		secondHDDPanel.add(btnHDDPane);
		addTab("������ HDD �����������", secondHDDPanel);
	}

	private void SmsGatewayPanel() throws ParseException {
		thirdHDDPanel = new JPanel();
		thirdHDDPanel.setLayout(new BoxLayout(thirdHDDPanel, BoxLayout.Y_AXIS));
		custSMSPanel = new CustPanelGateway(this);
		thirdHDDPanel.add(custSMSPanel);
		addTab("������ ����������� ����� �����", thirdHDDPanel);
	}

	private void AboutDlg() throws ParseException {
		aboutDlg = new CustAboutDlg(mainFrame);
	}

	//----- Getters and Setters
	public CustPanelHDD getCustHDDPane() {
		return custHDDPanel;
	}

	public CustTwoButtons getBtnHDDPane() {
		return btnHDDPane;
	}

	public CustPanelModem getCustModemPane() {
		return custModemPane;
	}

	public CustTwoButtons getBtnModemPane() {
		return btnModemPane;
	}

	public CustPanelGateway getCustGatewayPanel() {
		return custSMSPanel;
	}

	public JPanel getCustHDDSecondHDDPanel() {
		return secondHDDPanel;
	}

	public CustAboutDlg getAboutDlg() {
		return aboutDlg;
	}
}
