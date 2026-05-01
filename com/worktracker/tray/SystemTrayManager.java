package com.worktracker.tray;

import com.worktracker.core.WorkSession;
import com.worktracker.utils.IconUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class SystemTrayManager {
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private JMenuItem statusItem;
    
    public void setupSystemTray(ActionListener openAction, Runnable exitAction) {
        if (!SystemTray.isSupported()) return;
        
        systemTray = SystemTray.getSystemTray();
        Image image = IconUtils.createTrayIcon();
        
        // Use Swing JPopupMenu instead of AWT PopupMenu for reliable cross-platform/Linux support
        JPopupMenu popup = new JPopupMenu();
        statusItem = new JMenuItem("Status: Ready");
        statusItem.setEnabled(false);
        JMenuItem openItem = new JMenuItem("Open Work Tracker");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        openItem.addActionListener(openAction);
        exitItem.addActionListener(e -> exitAction.run());
        
        popup.add(statusItem);
        popup.addSeparator();
        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        
        // Hidden dialog to manage JPopupMenu focus behavior
        JDialog hiddenDialog = new JDialog();
        hiddenDialog.setUndecorated(true);
        hiddenDialog.setSize(0, 0);
        
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { hiddenDialog.setVisible(false); }
            @Override public void popupMenuCanceled(PopupMenuEvent e) { hiddenDialog.setVisible(false); }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        });
        
        trayIcon = new TrayIcon(image, "Work Hour Tracker - Ready");
        trayIcon.setImageAutoSize(true);
        
        // Add cross-platform robust mouse click handling
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { handleMouse(e); }
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouse(e);
            }
            
            private void handleMouse(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    hiddenDialog.setLocation(e.getXOnScreen(), e.getYOnScreen());
                    hiddenDialog.setVisible(true);
                    popup.show(hiddenDialog, 0, 0);
                } else if (e.getButton() == MouseEvent.BUTTON1 && e.getID() == MouseEvent.MOUSE_PRESSED) {
                    // Fallback left-click support
                    openAction.actionPerformed(null);
                }
            }
        });
        
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
            statusItem.setText("Status: " + status);
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