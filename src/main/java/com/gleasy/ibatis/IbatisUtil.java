package com.gleasy.ibatis;

import javax.swing.*;

import com.gleasy.configuration.Configuration;
import com.gleasy.configuration.ConfigurationParser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Rudy 4072883@qq.com
 * @since 2013-7-1
 */
public class IbatisUtil {

    private JFrame frmIbatisutil;
    private JTextField url;
    private JTextField packagePrefix;
    private JTextArea log;
    private JPanel panel;
    private JScrollPane scrollPane;
    private JPanel panel_2;
    private Configuration configuration;
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    IbatisUtil window = new IbatisUtil();
                    window.frmIbatisutil.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public IbatisUtil() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    
    private void parseConfigation(){
    	configuration = ConfigurationParser.parse("./configFile.xml");
    	
    	if(configuration.getUrl() != null){
    		url.setText(configuration.getUrl());
    	}
    	
    	if(configuration.getPackagePrefix() != null){
    		packagePrefix.setText(configuration.getPackagePrefix());
    	}
    }
    
    private void initialize() {
    	
        frmIbatisutil = new JFrame();
        frmIbatisutil.setTitle("IbatisUtil");
        frmIbatisutil.setBounds(100, 100, 600, 450);
        frmIbatisutil.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmIbatisutil.getContentPane().setLayout(new BorderLayout(0, 0));

        panel = new JPanel();
        frmIbatisutil.getContentPane().add(panel, BorderLayout.NORTH);
        panel.setLayout(new GridLayout(3, 1, 0, 0));

        url = new JTextField();
        url.setAlignmentY(0.0f);
        url.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(url);
        //url.setText("jdbc:sqlserver://183.131.153.162:1433;DatabaseName=implatform;user=pdtr;password=pdtr123456;");
        //url.setText("jdbc:sqlserver://123.59.87.196:1433;DatabaseName=AZPlatForm;user=kchf526pa;password=help7575;");
        //url.setText("jdbc:mysql://192.168.0.11:3307/sample-local-0?user=mysql3307&password=mysql3307");
        url.setColumns(10);

        packagePrefix = new JTextField();
        packagePrefix.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        packagePrefix.setAlignmentX(0.0f);
        panel.add(packagePrefix);
        //packagePrefix.setText("com.gleasy.pdtr");
        //packagePrefix.setText("com.gleasy.biso");
        packagePrefix.setColumns(10);

        JButton go = new JButton("GO");
        panel.add(go);
        parseConfigation();
        go.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.setText("ING...!");
                new Thread() {
                    public void run() {
                        try {
                        	GenerateBeansAndXmls g = new GenerateBeansAndXmls(url.getText(), packagePrefix.getText());
                            if(configuration.getIncludeTables().isEmpty()){
                            	g.generate();
                            }else{
                            	g.generateByList(configuration.getIncludeTables());
                            }
                            log.setText("SUCCESS!");
                        } catch (Exception ex) {
                            log.setText(ex.getMessage());
                        }
                    }
                }.start();

            }
        });

        scrollPane = new JScrollPane();
        frmIbatisutil.getContentPane().add(scrollPane, BorderLayout.CENTER);

        panel_2 = new JPanel();
        scrollPane.setViewportView(panel_2);
        panel_2.setLayout(new BorderLayout(0, 0));

        log = new JTextArea();
        log.setEditable(false);
        panel_2.add(log, BorderLayout.CENTER);

        String text = "1、根据指定的Url，读取数据库表信息，自动生成Bean、Dao、Cache、Service和配置文件等。";
        text += "\r\n" + "2、生成的文件内容可用Eclipse、Intellij格式化。";
        text += "\r\n" + "3、映射规则如下：";
        text += "\r\n" + "\tvarchar\t-> String";
        text += "\r\n" + "\tbigint\t-> Long";
        text += "\r\n" + "\tsmallint\t-> Integer";
        text += "\r\n" + "\ttinyint\t-> Byte";
        text += "\r\n" + "\tdatetime\t-> Date";
        text += "\r\n" + "\tdate\t-> java.sql.Date";
        text += "\r\n" + "\ttime\t-> java.sql.Time";
        text += "\r\n" + "\ttext\t-> String";
        text += "\r\n" + "\tdouble\t-> Double";
        text += "\r\n" + "\tother\t-> NULL";
        text += "\r\n" + "4、工具源码：http://svn.dev.gleasy.com/svn/gleasy.com/sample/ibatis-util";
        text += "\r\n" + "5、配套框架：http://svn.dev.gleasy.com/svn/gleasy.com/sample/trunks";
        text += "\r\n" + "6、需求及BUG反馈：dingyong@gleasy.net";
        log.setText(text);
    }
}
