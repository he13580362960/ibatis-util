package com.gleasy.ibatis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 * @author Rudy 4072883@qq.com
 * @since 2013-7-1
 */
public class Shell {

    private JFrame frame;
    private JPanel panel;
    private JTextField url;
    private JTextField userName;
    private JTextField password;
    private JTextField driver;
    private JTextField packagePrefix;
    private JTextArea includeTables;
    private JTextArea result;
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Shell window = new Shell();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Shell() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle("sql-parser");
        frame.setBounds(100, 100, 900, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container =  frame.getContentPane();
        container.setLayout(new BorderLayout(0, 0));

        panel = new JPanel();
        container.add(panel, BorderLayout.NORTH);
        panel.setLayout(new GridLayout(3, 1, 0, 0));
        
        
        url =new JTextField();
        url.setText("jdbc:oracle:thin:@120.132.13.31:1521:orcl");
        panel.add(url);
        
        JPanel p = new JPanel();
        panel.add(p);
        p.setLayout(new GridLayout(1, 4, 0, 0));
        
        userName =new JTextField();
        userName.setText("CNNGMOB_ADMIN");
        p.add(userName);
        
        password =new JTextField();
        password.setText("chang3m3");
        p.add(password);
        
        driver =new JTextField();
        driver.setText("oracle.jdbc.driver.OracleDriver");
        p.add(driver);
        
        packagePrefix =new JTextField();
        packagePrefix.setText("com.gleasy.report");
        p.add(packagePrefix);
        
        JButton go = new JButton("GO");
        panel.add(go);
        
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    public void run() {
                    	try {
                    		result.setText("GENERATING!!!!");
                    		String includeTable = includeTables.getText();
                    		String[] inCludeArray = includeTable.split("\n");
                    		List<String>includeList = new ArrayList<String>();
                    		for(String str:inCludeArray){
                    			str = str.replace("\r?\n?", "");
                    			includeList.add(str);
                    		}
                    		System.out.println(includeList.toString());
                    		System.out.println(includeList.size());
                    		Context context = new Context(url.getText(), driver.getText(), userName.getText(), password.getText(), packagePrefix.getText());
                    		Generator generator = new Generator(context);
                    		generator.generate(includeList);
                    		result.setText("SUCCESS");
						} catch (Exception e) {
							result.setText(e.getMessage());
						}
                    }
                }.start();
            }
        });
        
        p = new JPanel();
        container.add(p, BorderLayout.CENTER);
        p.setLayout(new GridLayout(1, 2, 0, 0));
        
        JScrollPane scPane = new JScrollPane(new JPanel());
        p.add(scPane);
        includeTables = new JTextArea();
        includeTables.setBorder(new LineBorder(Color.BLACK));
        scPane.getViewport().add(includeTables);
        
        scPane = new JScrollPane(new JPanel());
        p.add(scPane);
        result = new JTextArea();
        result.setBorder(new LineBorder(Color.BLACK));
        scPane.getViewport().add(result);
    }
}
