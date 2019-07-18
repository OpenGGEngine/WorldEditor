package worldeditor;

import com.opengg.core.util.FileUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AssetBrowserListRenderer extends DefaultListCellRenderer {
    private static final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<String> keyOrder = new ConcurrentLinkedQueue<>();
    private static final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    private static int CACHE_SIZE = 180;

    public static ImageIcon requestImageThumbnail(File file){
        String ext = FileUtil.getFileExt(file.getName());
        switch (ext) {
            case "png","jpg" -> {
                if (!iconCache.containsKey(file.getAbsolutePath())) {
                    executor.submit(() -> {
                        try {
                            ImageIcon icon = new ImageIcon(ImageIO.read(file).getScaledInstance(32, 32, Image.SCALE_DEFAULT));
                            iconCache.put(file.getAbsolutePath(), icon);
                            keyOrder.add(file.getAbsolutePath());
                            if (keyOrder.size() > CACHE_SIZE) {
                                iconCache.remove(keyOrder.remove());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }else{
                    return iconCache.get(file.getAbsolutePath());
                }
            }
        }
        return null;
    }
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        FileTreeModel.FileToStringFix file = (FileTreeModel.FileToStringFix) value;
        String ext = FileUtil.getFileExt(file.getName());
        switch (ext) {
            case "ogg", "mp3" -> label.setIcon(Theme.sound);
            case "ssf" -> label.setIcon(Theme.scriptIcon);
            case "png","jpg" -> {
                if (iconCache.containsKey(file.getAbsolutePath())) {
                    label.setIcon(iconCache.get(file.getAbsolutePath()));
                } else {
                    label.setIcon(Theme.fileIcon);
                }
            }
            default -> label.setIcon(Theme.fileIcon);
        }
        if (!((FileTreeModel.FileToStringFix) value).getName().contains(".")) label.setIcon(Theme.folderIcon);

        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
}
