package com.nsoz.server;

import java.util.List;
import java.util.Scanner;

import com.nsoz.clan.Clan;
import com.nsoz.db.jdbc.DbManager;
import com.nsoz.model.Char;
import com.nsoz.stall.StallManager;
import com.nsoz.util.Log;
import com.nsoz.util.NinjaUtils;

/**
 *
 * @author ASD
 */
public class NinjaSchool {

    // private Frame frame;

    public static boolean isStop = false;

    // public NinjaSchool() {
    // // try {
    // // frame = new Frame("Manger");
    // // InputStream is =
    // getClass().getClassLoader().getResourceAsStream("icon.png");
    // // byte[] data = new byte[is.available()];
    // // is.read(data);
    // // ImageIcon img = new ImageIcon(data);
    // // frame.setIconImage(img.getImage());
    // // frame.setSize(200, 360);
    // // frame.setBackground(Color.DARK_GRAY);
    // // frame.setResizable(false);
    // // frame.addWindowListener(this);
    // // //Button b = new Button("Bảo trì");
    // // //b.setBounds(30, 60, 140, 30);
    // // //b.setActionCommand("stop");
    // // //b.addActionListener(this);
    // // //frame.add(b);
    // // Button b2 = new Button("Lưu Shinwa");
    // // b2.setBounds(30, 100, 140, 30);
    // // b2.setActionCommand("shinwa");
    // // b2.addActionListener(this);
    // // frame.add(b2);
    // // Button b3 = new Button("Lưu dữ liệu gia tộc");
    // // b3.setBounds(30, 140, 140, 30);
    // // b3.setActionCommand("clan");
    // // b3.addActionListener(this);
    // // frame.add(b3);
    // // Button b4 = new Button("Lưu dữ liệu người chơi");
    // // b4.setBounds(30, 180, 140, 30);
    // // b4.setActionCommand("player");
    // // b4.addActionListener(this);
    // // frame.add(b4);
    // // Button b5 = new Button("Làm mới TOP");
    // // b5.setBounds(30, 220, 140, 30);
    // // b5.setActionCommand("rank");
    // // b5.addActionListener(this);
    // // frame.add(b5);
    // // Button b6 = new Button("Restart DB");
    // // b6.setBounds(30, 260, 140, 30);
    // // b6.setActionCommand("restartDB");
    // // b6.addActionListener(this);
    // // frame.add(b6);
    // // Button b7 = new Button("Gửi Đồ");
    // // b7.setBounds(30, 300, 140, 30);
    // // b7.setActionCommand("sendItem");
    // // b7.addActionListener(this);
    // // frame.add(b7);
    // // frame.setLocationRelativeTo(null);
    // // frame.setLayout(null);
    // // frame.setVisible(true);
    // // } catch (IOException ex) {
    // // Logger.getLogger(NinjaSchool.class.getName()).log(Level.SEVERE, null, ex);
    // // }
    // }

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        if (Config.getInstance().load()) {
            if (!DbManager.getInstance().start()) {
                return;
            }
            if (NinjaUtils.availablePort(Config.getInstance().getPort())) {
                new NinjaSchool();
                if (!Server.init()) {
                    Log.error("Khoi tao that bai!");
                    return;
                }
                new Thread(() -> {
                    Server.start();
                }).start();
                while (true) {
                    System.out.println("Nhập lệnh (shinwa/clan/rank/player/restart/exit): ");
                    String command = scanner.nextLine();

                    switch (command) {
                        case "shinwa":
                            System.out.println("Đang Lưu Shinwa");
                            saveShinwa();
                            break;
                        case "clan":
                            System.out.println("Đang Lưu gia tộc");
                            saveClan();
                            break;
                        case "rank":
                            System.out.println("Đang Làm mới bảng xếp hạng");
                            refreshRank();
                            break;
                        case "player":
                            System.out.println("Đang Lưu dữ liệu người chơi");
                            savePlayerData();
                            break;
                        case "restart":
                            System.out.println("Đang Khởi động lại cơ sở dữ liệu");
                            restartDatabase();
                            break;
                        case "exit":
                            System.out.println("Đang dừng server...");
                            Server.stop();
                            scanner.close();
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Lệnh không hợp lệ. Vui lòng thử lại.");
                    }
                }
            }

            else {
                Log.error("Port " + Config.getInstance().getPort() + " da duoc su dung!");
            }
        } else {
            Log.error("Vui long kiem tra lai cau hinh!");
        }
    }

    private static void restartDatabase() {
        Log.info("Bắt đầu khởi động lại!");
        DbManager.getInstance().shutdown();
        DbManager.getInstance().start();
        Log.info("Khởi động xong!");
    }

    private static void savePlayerData() {
        Log.info("Lưu dữ liệu người chơi");
        List<Char> chars = ServerManager.getChars();
        for (Char _char : chars) {
            try {
                if (_char != null && !_char.isCleaned) {
                    _char.saveData();
                    if (_char.clone != null && !_char.clone.isCleaned) {
                        _char.clone.saveData();
                    }
                    if (_char.user != null && !_char.user.isCleaned) {
                        if (_char.user != null) {
                            _char.user.saveData();
                        }

                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Log.info("Lưu xong");
    }

    private static void refreshRank() {
        List<Char> chars = ServerManager.getChars();
        for (Char _char : chars) {
            _char.saveData();
        }
        Log.info("Làm mới bảng xếp hạng");
        Ranked.refresh();
    }

    private static void saveClan() {
        Log.info("Lưu dữ liệu gia tộc.");
        List<Clan> clans = Clan.getClanDAO().getAll();
        synchronized (clans) {
            for (Clan clan : clans) {
                Clan.getClanDAO().update(clan);
            }
        }
        Log.info("Lưu xong");
    }

    private static void saveShinwa() {
        if (Server.start) {
            Log.info("Lưu Shinwa");
            StallManager.getInstance().save();
            Log.info("Lưu xong");
        } else {
            Log.info("Mãy chủ chưa bật");
        }
    }
}
