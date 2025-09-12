package com.ishciv.installer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class Main {

    private static final String FABRIC_API_URL = "https://dl.ishciv.com/modpacks/fabric-api-0.92.6+1.20.1.jar";
    private static final String VOICE_CHAT_URL = "https://dl.ishciv.com/modpacks/voicechat-fabric-1.20.1-2.5.35.jar";
    private static final String SODIUM_URL = "https://dl.ishciv.com/modpacks/sodium-fabric-0.5.13+mc1.20.1.jar";
    private static final String IRIS_URL = "https://dl.ishciv.com/modpacks/iris-1.7.6+mc1.20.1.jar";
    private static final String LOGICAL_ZOOM_URL = "https://dl.ishciv.com/modpacks/logical_zoom-0.0.20.jar";
    private static final String REPLAYMOD_URL = "https://dl.ishciv.com/modpacks/replaymod-1.20.1-2.6.23.jar";
    private static final String REPLAYVC_URL = "https://dl.ishciv.com/modpacks/replayvoicechat-fabric-1.20.1-1.3.10.jar";
    private static final String BOBBY_URL = "https://dl.ishciv.com/modpacks/bobby-5.0.1.jar";
    private static final String SERVERS_URL = "https://dl.ishciv.com/modpacks/servers.dat";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isMac = os.contains("mac");

            JFrame frame = new JFrame("State Mod Installer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 350);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);

            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBorder(new EmptyBorder(20, 20, 20, 20));

            ImageIcon icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/logo.png")));
            Image scaled = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaled));
            iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel title = new JLabel("Let’s get you setup!");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel desc = new JLabel(
                    "<html>It’s time to prepare you for ish's Purge Event! This will create a new profile in your Minecraft Launcher with all mods installed.</html>");
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setBorder(new EmptyBorder(10, 0, 20, 0));

            JButton begin = new JButton("Start Install");
            begin.setAlignmentX(Component.LEFT_ALIGNMENT);

            left.add(iconLabel);
            left.add(title);
            left.add(desc);
            left.add(begin);

            if (isMac) {
                left.add(Box.createRigidArea(new Dimension(0, 10)));

                JButton launch = new JButton("Launch Game (Fix for macOS)");
                launch.setAlignmentX(Component.LEFT_ALIGNMENT);
                launch.addActionListener(e -> {
                    try {
                        new ProcessBuilder("/Applications/Minecraft.app/Contents/MacOS/launcher")
                                .inheritIO()
                                .start();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame,
                                "Could not launch Minecraft:\n" + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                left.add(launch);
            }

            left.add(Box.createVerticalGlue());

            JCheckBox fabricApi = new JCheckBox("Fabric API");
            fabricApi.setSelected(true);
            fabricApi.setEnabled(false);

            JCheckBox voiceChat = new JCheckBox("Simple Voice Chat");
            voiceChat.setSelected(true);
            voiceChat.setEnabled(false);

            JCheckBox sodium = new JCheckBox("Sodium");
            JCheckBox iris = new JCheckBox("Iris Shaders");
            JCheckBox logicalZoom = new JCheckBox("Logical Zoom");
            JCheckBox replayMod = new JCheckBox("Replay Mod");
            JCheckBox replayVoiceChat = new JCheckBox("Replay Voice Chat");
            JCheckBox bobby = new JCheckBox("Bobby");
            
            // Initially disable replayVoiceChat since replayMod starts unchecked
            replayVoiceChat.setEnabled(false);
            
            // Add listener to replayMod to control replayVoiceChat
            replayMod.addActionListener(e -> {
                if (replayMod.isSelected()) {
                    replayVoiceChat.setEnabled(true);
                } else {
                    replayVoiceChat.setEnabled(false);
                    replayVoiceChat.setSelected(false);
                }
            });

            JPanel required = new JPanel(new GridLayout(0, 1, 5, 5));
            required.setBorder(new TitledBorder("Required"));
            required.add(fabricApi);
            required.add(voiceChat);

            JPanel optional = new JPanel(new GridLayout(0, 1, 5, 5));
            optional.setBorder(new TitledBorder("Enhancements"));
            optional.add(sodium);
            optional.add(iris);
            optional.add(logicalZoom);
            optional.add(replayMod);
            optional.add(replayVoiceChat);
            optional.add(bobby);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setBorder(new EmptyBorder(20, 20, 20, 20));
            right.add(required);
            right.add(Box.createVerticalStrut(20));
            right.add(optional);
            right.add(Box.createVerticalGlue());

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
            split.setResizeWeight(0.5);
            split.setDividerSize(0);
            split.setEnabled(false);
            frame.getContentPane().add(split);

            begin.addActionListener(e -> {
                begin.setEnabled(false);
                begin.setText("Installing...");
                frame.repaint();

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            Installer.downloadAndCopyMod(FABRIC_API_URL, "fabric-api.jar");
                            Installer.downloadAndCopyMod(VOICE_CHAT_URL, "voicechat.jar");
                            if (sodium.isSelected())
                                Installer.downloadAndCopyMod(SODIUM_URL, "sodium.jar");
                            if (iris.isSelected())
                                Installer.downloadAndCopyMod(IRIS_URL, "iris.jar");
                            if (logicalZoom.isSelected())
                                Installer.downloadAndCopyMod(LOGICAL_ZOOM_URL, "logical_zoom.jar");
                            if (replayMod.isSelected())
                                Installer.downloadAndCopyMod(REPLAYMOD_URL, "replaymod.jar");
                            if (replayVoiceChat.isSelected())
                                Installer.downloadAndCopyMod(REPLAYVC_URL, "replayvoicechat.jar");
                            if (bobby.isSelected())
                                Installer.downloadAndCopyMod(BOBBY_URL, "bobby.jar");
                            Installer.downloadToGameDir(SERVERS_URL, "servers.dat");
                            Installer.addLauncherProfile();
                        } catch (Exception ex) {
                            begin.setEnabled(true);
                            begin.setText("Start Install");
                            JOptionPane.showMessageDialog(frame,
                                    "Installation failed:\n" + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        begin.setEnabled(true);
                        begin.setText("Start Install");
                        JOptionPane.showMessageDialog(frame,
                                "All selected components installed successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);
                    }
                };
                worker.execute();
            });

            frame.setVisible(true);
        });
    }
}
