package org.wind.gui;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.wind.common.FileHandle;
import org.wind.common.HttpReq;

public class WindMainPanel {
    private JPanel uiMainPanel;
    private JTextField uiTargetTextField;
    private JButton uiStartButton;
    private JRadioButton uiAspRadioButton;
    private JRadioButton uiPhpRadioButton;
    private JRadioButton uiDirRadioButton;
    private JRadioButton uiJspRadioButton;
    private JRadioButton uiInfoRadioButton;
    private JRadioButton uiCustRadioButton;
    private JTable uiResultTable;
    private JLabel uiNowScanLabel;
    private JSlider uiThreadSlider;
    private JLabel uiThreadNumLabel;

    public WindMainPanel() {
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        int DIALOG_WHITE = 600;
        int DIALOG_HEIGHT = 400;

        JFrame uiMainFrame= new JFrame("Wind");
        uiMainFrame.setContentPane(uiMainPanel);
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/wind.png"));
        uiMainFrame.setIconImage(imageIcon.getImage());
        uiMainFrame.setSize(DIALOG_WHITE, DIALOG_HEIGHT);
        uiMainFrame.setBounds(point.x - DIALOG_WHITE / 2, point.y - DIALOG_HEIGHT / 2, DIALOG_WHITE, DIALOG_HEIGHT);
        uiMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        uiMainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        uiMainFrame.setVisible(true);

        // 线程设置界面 拖拉进度条 修改线程数
        uiThreadNumLabel.setText("Threads: " + uiThreadSlider.getValue());
        uiThreadSlider.addChangeListener(new ChangeListener() {
            /**
             * Invoked when the target of the listener has changed its state.
             *
             * @param e a ChangeEvent object
             */
            @Override
            public void stateChanged(ChangeEvent e) {
                uiThreadNumLabel.setText("Threads: " + uiThreadSlider.getValue());
            }
        });

        // 字典选择界面
        String wordListPath;
        String path = WindMainPanel.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.endsWith(".jar")) {
            wordListPath = path.substring(0, path.lastIndexOf("/") + 1) + "wordlists/";
        } else {
            wordListPath = path + "wordlists/";
        }
        String dicAspExt = wordListPath + "ASP.txt";
        String dicPhpExt = wordListPath + "PHP.txt";
        String dicJspExt = wordListPath + "JSP.txt";
        String dicDirExt = wordListPath + "DIR.txt";
        String dicCustExt = wordListPath + "CUST.txt";
        String dicInfoExt = wordListPath + "INFO.txt";
        uiAspRadioButton.setText("ASP " + FileHandle.getFileLineNum(dicAspExt));
        uiPhpRadioButton.setText("PHP " + FileHandle.getFileLineNum(dicPhpExt));
        uiJspRadioButton.setText("JSP " + FileHandle.getFileLineNum(dicJspExt));
        uiDirRadioButton.setText("DIRS " + FileHandle.getFileLineNum(dicDirExt));
        uiInfoRadioButton.setText("INFO " + FileHandle.getFileLineNum(dicInfoExt));
        uiCustRadioButton.setText("CUST " + FileHandle.getFileLineNum(dicCustExt));

        // 表格展示 扫描结果
        String[] columnTitle = {
                "ID",
                "Found",
                "Status",
                "Size",
        };
        Object[][] tableData = {};
        DefaultTableModel mainTableModel = new DefaultTableModel(tableData, columnTitle) {
            // 设置表格不可编辑 但可选
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        uiResultTable.setModel(mainTableModel);
        uiResultTable.setAutoCreateRowSorter(true);
        // 设置表格列宽
        uiResultTable.getColumnModel().getColumn(1).setPreferredWidth(350);
        // 表格右键菜单定义
        uiResultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseE) {
                // 双击触发 获取当前选择的Url 打开网址
                if (mouseE.getClickCount() == 2) {
                    // 获取当前选择的行数
                    int resSelectRow = uiResultTable.getSelectedRow();
                    // 通过行数去对应的值
                    String selectURL = uiResultTable.getValueAt(resSelectRow, 1).toString();
                    // 通过系统默认浏览器打开网站
                    try {
                        URI openClickSite = new URI(selectURL);
                        Desktop.getDesktop().browse(openClickSite);
                    } catch (Exception browseError) {
                        browseError.printStackTrace();
                    }
                } else if (SwingUtilities.isRightMouseButton(mouseE)) {
                    // 通过点击位置找到点击为表格中的行
                    int focusedRowIndex = uiResultTable.rowAtPoint(mouseE.getPoint());
                    // 将表格所选项设为当前右键点击的行
                    // 如果选中多行的情况下就不标记当前行
                    if (uiResultTable.getSelectedRows().length == 1) {
                        uiResultTable.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
                    }
                    // 获取已选中的行 跟上面顺序不能乱
                    int[] resSelectRows = uiResultTable.getSelectedRows();
                    // 生成右键菜单
                    JPopupMenu tableRightMenu = new JPopupMenu();
                    JMenuItem delOption = new JMenuItem("Delete");
                    JMenuItem cpOption = new JMenuItem("Copy");
                    JMenuItem reportOption = new JMenuItem("Report");
                    JMenuItem selectAllOption = new JMenuItem("Select All");
                    cpOption.addActionListener(evt -> {
                        // 获取选择的网站地址
                        StringBuilder copyData = new StringBuilder();
                        for (int r : resSelectRows) {
                            copyData.append(uiResultTable.getValueAt(r, 1).toString());
                            copyData.append("\n");
                        }
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        // 封装文本内容
                        Transferable trans = new StringSelection(copyData.toString().trim());
                        // 把文本内容设置到系统剪贴板
                        clipboard.setContents(trans, null);
                    });
                    delOption.addActionListener(evt -> {
                        int askDelOpt = JOptionPane.showConfirmDialog(null,
                                "Are you sure to delete this item?",
                                "Notice",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (askDelOpt == 0) {
                            for (int i = 0; i < resSelectRows.length; i++) {
                                //转换为Model的索引 这句很重要 否则索引不对应
                                resSelectRows[i] = uiResultTable.convertRowIndexToModel(resSelectRows[i]);
                            }
                            //排序 这句很重要 否则顺序是乱的
                            Arrays.sort(resSelectRows);
                            //降序删除
                            for (int i = resSelectRows.length - 1; i >= 0; i--) {
                                mainTableModel.removeRow(resSelectRows[i]);
                            }
                        }
                    });

                    selectAllOption.addActionListener(evt ->
                            uiResultTable.setRowSelectionInterval(0, (uiResultTable.getRowCount() - 1))
                    );

                    reportOption.addActionListener(evt ->
                            {
                                int input = JOptionPane.showConfirmDialog(null,
                                        "Are you sure to delete this item?",
                                        "Notice",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
                                System.out.println(input);
                            }
                    );
                    // 右键菜单样式
                    tableRightMenu.add(cpOption);
                    tableRightMenu.add(selectAllOption);
                    tableRightMenu.add(delOption);
                    // 一条分割线
                    tableRightMenu.addSeparator();
                    tableRightMenu.add(reportOption);
                    tableRightMenu.show(mouseE.getComponent(), mouseE.getX(), mouseE.getY());
                }
            }
        });

        // 监控网址输入框 回车按键
        uiTargetTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent enterKey)
            {
                //判断按下的键是否是回车键
                if(enterKey.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    // 触发开始按钮事件
                    uiStartButton.doClick();
                }
            }
        });

        // 开始按钮事件
        uiStartButton.addActionListener(startButtonEven -> {
            // 创建线程池
            ExecutorService executor = Executors.newCachedThreadPool();
            // 获取线程数量
            int threadNum = uiThreadSlider.getValue();

            //定义信号量 即线程数量
            final Semaphore semaphore = new Semaphore(threadNum);
            if(uiStartButton.getText().equals("Start")) {
                String target = uiTargetTextField.getText();
                if(target.length() == 0){
                    JOptionPane.showMessageDialog(null, "Please set target url","",JOptionPane.WARNING_MESSAGE);
                } else {
                    String site;
                    if (target.contains("http://") || target.contains("https://")) {
                        site = target;
                    } else {
                        site = "http://" + target;
                    }
                    // 清除原有表格内容
                    mainTableModel.setRowCount(0);
                    List<String> scanDirList =new ArrayList<>();
                    if (uiAspRadioButton.isSelected()) {
                        List<String> tmpAspList = FileHandle.getDicList(dicAspExt);
                        scanDirList.addAll(tmpAspList);
                    }
                    if (uiPhpRadioButton.isSelected()) {
                        List<String> tmpPhpList = FileHandle.getDicList(dicPhpExt);
                        scanDirList.addAll(tmpPhpList);
                    }
                    if (uiJspRadioButton.isSelected()) {
                        List<String> tmpJspList = FileHandle.getDicList(dicJspExt);
                        scanDirList.addAll(tmpJspList);
                    }
                    if (uiDirRadioButton.isSelected()) {
                        List<String> tmpDirList = FileHandle.getDicList(dicDirExt);
                        scanDirList.addAll(tmpDirList);
                    }
                    if (uiInfoRadioButton.isSelected()) {
                        List<String> tmpBakList = FileHandle.getDicList(dicInfoExt);
                        scanDirList.addAll(tmpBakList);
                    }
                    if (uiCustRadioButton.isSelected()) {
                        List<String> tmpInfoList = FileHandle.getDicList(dicCustExt);
                        scanDirList.addAll(tmpInfoList);
                    }
                    if (scanDirList.size() != 0) {
                        CompletableFuture.runAsync(() -> {

                            int allTargetCount = scanDirList.size();
                            AtomicInteger scanTargetCount = new AtomicInteger();
                            AtomicInteger uid = new AtomicInteger();

                            // 获取404页面信息
                            String randomUrl = site + HttpReq.getRandomString(8);
                            Integer[] randomRes = HttpReq.sendGet(randomUrl);
                            int errorPageFlag;
                            if (randomRes[1] != null) {
                                errorPageFlag = randomRes[1];
                            } else {
                                errorPageFlag = 0;
                            }

                            //创建线程池
                            for (String i : scanDirList) {
                                String reqSite = site + i.trim();
                                Runnable runnable = () -> {
                                    try {
                                        //获取许可
                                        semaphore.acquire();
                                        // 运行主体
                                        Integer[] res = HttpReq.sendGet(reqSite);
                                        String nowScan;
                                        if (reqSite.length() > 50) {
                                            nowScan = reqSite.substring(0, 50) + "...";
                                        } else {
                                            nowScan = reqSite;
                                        }
                                        scanTargetCount.addAndGet(1);
                                        uiNowScanLabel.setText(scanTargetCount + "/" + allTargetCount + " Scanning: " + nowScan);
                                        if (res[0] != null && res[1] != null && !((399 < res[0]) && (res[0] < 600))) {
                                            int respCode = res[0];
                                            int respSize = res[1];
                                            if (respSize != errorPageFlag) {
                                                uid.addAndGet(1);
                                                Vector<String> dataVector = new Vector<>();
                                                dataVector.add(uid.toString());
                                                dataVector.add(reqSite);
                                                dataVector.add(Integer.toString(respCode));
                                                dataVector.add(Integer.toString(respSize));
                                                mainTableModel.addRow(dataVector);
                                                uiResultTable.setModel(mainTableModel);
                                            }
                                        }
                                    } catch (Exception threadError) {
                                        threadError.printStackTrace();
                                    } finally {
                                        //访问完后 释放 一定要放在finally里面
                                        semaphore.release();
                                    }
                                };
                                executor.execute(runnable);
                            }
                            while (!(semaphore.availablePermits() == threadNum)) {
                                try {
                                    if (uiStartButton.getText().equals("Start")) {
                                        executor.shutdownNow();
                                        while (semaphore.hasQueuedThreads()) {
                                            Thread.sleep(7000);
                                        }
                                        uiNowScanLabel.setText(null);
                                        break;
                                    }
                                    Thread.sleep(5000);
                                } catch (InterruptedException timeSleepError) {
                                    timeSleepError.printStackTrace();
                                }
//                                    System.out.println(semaphore.availablePermits());
                                // 设置日期格式
                                // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                // System.out.println(df.format(new Date()));
                            }
                            System.out.println("Scan done");
                            // 退出线程池
                            executor.shutdown();
                            uiNowScanLabel.setText(null);
                            uiStartButton.setText("Start");
                        });
                        uiStartButton.setText("Stop");
                    } else {
                        JOptionPane.showMessageDialog(null, "Please select a dictionary","",JOptionPane.WARNING_MESSAGE);
                    }
                }
            } else if (uiStartButton.getText().equals("Stop")) {
                int stopOpt = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to perform this action?",
                        "Notice",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (stopOpt == 0) {
                    uiStartButton.setText("Start");
                    System.out.println("Task stop");
                }
            }
        });
    }
}
