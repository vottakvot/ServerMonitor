package ru.servermonitor.gui;

import java.text.ParseException;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.SystemColor;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import ru.servermonitor.actions.CustActions;

import javax.swing.JTextPane;
import javax.swing.BoxLayout;


public class CustAboutDlg extends JDialog {

	private static final long serialVersionUID = -7907571182562773578L;
	private JPanel contentPane;

	/**
	 * Create and config about dialog.
	 * @throws ParseException
	 */
	public CustAboutDlg(CustWindowWithTabs mainFrame) throws ParseException {

		setLocationRelativeTo(mainFrame);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setAutoRequestFocus(true);
		setModal(true);
		setAlwaysOnTop(true);
		setTitle("\u041E \u043F\u0440\u043E\u0433\u0440\u0430\u043C\u043C\u0435");
		setResizable(false);
		setBounds(200, 200, 400, 600);

		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.menu);
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));


        String aboutMeAndMyShit = "<html>\n" +
                "<p align='center'><b><u>��������� ����������� ���������� ����� � ����-����������:</u></b></p> \n" +
                "<ul>\n" +
                "<li><font color=#4D4D4D><i>���������� �������������� �����: <br>- GSM �����;<br>- ������ SMS.ru (����� � ����� ����� ������, �� ���� �������� ��������� ��� ���� ������);<br>- �������� ����� ����� (����� ������ ��� �������� ������ gmail!)</i></font>\n" +
                "<li><font color=#4D4D4D><i>��� ������ ����� ������ ������������ �����, ��������� ��� ����������������� (�������� ������� � ���� AT+&lt;�������&gt, &lt;CR&gt; ��� &lt;CTRL-Z&gt ����������� ����). ������ \"��������� �����\" �� ����������� (������ ����������� �������� ���������).</i></a>\n" +
                "<li><font color=#4D4D4D><i>��� HDD ����� ������ ��������� ���������, ��� ����� ������������ �������� � ����������� ��������, ���� ����� ���� ���������. ����� ����� ������ ������� ������ ������ � ��� � ����������������� ������, �.�. ��� ������� ��������� ����� ����� �����������.</i></font>\n" +
                "<li><font color=#4D4D4D><i>�������� ����� ����� ������������� ����� ������ SMS.ru � gmail �����. �������� ����� ����������� � �� ������ ��������, �� ����� �������� ���� ��������. �������� �� ����� ����� ����������� ������ � ����� gmail � � ���� �.�. �������� ������ �� ������������� ����������! " +
                							"<font color=#0000FF><a href='https://www.google.com/settings/security/lesssecureapps'><i>https://www.google.com/settings/security/lesssecureapps</i></a></font>\n" +
                "<li><font color=#4D4D4D><i>���� ����������� ���������� �������� �������, ���� ������� ��� ��� �� ��������� �������, �� �������� ����� ������ � �������������. ����������� ������ ������ ��-��������� \\cfg\\default.xml.</i></font>\n" +
                "</ul>\n";

		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText(aboutMeAndMyShit);
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.addHyperlinkListener(new CustActions.ActivatedHyperlinkListener());

		JScrollPane scrollNumbers = new JScrollPane(textPane);
		contentPane.add(scrollNumbers);

		setEnabled(false);
		setVisible(false);
	}
}
