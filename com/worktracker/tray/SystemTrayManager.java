package com.worktracker.tray;

import com.worktracker.core.WorkSession;
import com.worktracker.utils.IconUtils;

import java.awt.*;
import java.awt.event.ActionListener;

public class SystemTrayManager {
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private MenuItem statusItem;
    
    public void setupSystemTray(ActionListener openAction, Runnable exitAction) {
        if (!SystemTray.isSupported()) return;
        
        systemTray = SystemTray.getSystemTray();
        Image image = IconUtils.createTrayIcon();
        
        PopupMenu popup = new PopupMenu();
        statusItem = new MenuItem("Status: Ready");
        statusItem.setEnabled(false);
        MenuItem openItem = new MenuItem("Open Work Tracker");
        MenuItem exitItem = new MenuItem("Exit");
        
        openItem.addActionListener(openAction);
        exitItem.addActionListener(e -> exitAction.run());
        
        popup.add(statusItem);
        popup.addSeparator();
        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        
        trayIcon = new TrayIcon(image, "Work Hour Tracker - Ready", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(openAction);
        
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayIcon could not be added.");
        }
    }
    

    
    public void updateTrayIcon(WorkSession.State state) {
        if (trayIcon == null) return;
        
        String status = "Ready";
        switch (state) {
            case WORKING: status = "Working"; break;
            case ON_BREAK: status = "On Break"; break;
            default:
                break;
        }
        
        trayIcon.setToolTip("Work Hour Tracker - " + status);
        if (statusItem != null) {
            statusItem.setLabel("Status: " + status);
        }
    }
    
    public void showMessage(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }
    
    public boolean isSupported() {
        return SystemTray.isSupported();
    }
}