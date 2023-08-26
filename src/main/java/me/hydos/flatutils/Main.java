package me.hydos.flatutils;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final List<Runnable> QUEUE = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        NativeFileDialog.NFD_Init();
        LafManager.installTheme(new DarculaTheme());
        var flatUtils = new FlatUtils();

        flatUtils.getRootPane().setDropTarget(new DropTarget() {
            @SuppressWarnings("unchecked") // Swing sucks
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    ((List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)).stream()
                            .map(file -> Paths.get(file.getAbsolutePath()))
                            .forEach(Main::handleFile);
                    evt.dropComplete(true);
                } catch (Exception e) {
                    evt.dropComplete(false);
                    throw new RuntimeException(e);
                }
            }
        });

        // Cry abt it IntelliJ
        //noinspection InfiniteLoopStatement
        while (true) {
            if (!QUEUE.isEmpty()) QUEUE.remove(0).run();
            Thread.sleep(10);
        }
    }

    private static void handleFile(Path path) {
        var fileName = path.getFileName().toString();
        var extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        QUEUE.add(() -> {
            if (extension.equals("json") || extension.equals("json5")) handleJson(path, fileName);
            else handleBinary(path, fileName, extension);
        });
    }

    private static void handleBinary(Path path, String fileName, String extension) {
        try {
            var outputFile = saveFile(fileName.replace(extension, "json"));
            var schemaTarget = extension + ".fbs";
            var schemaPath = Files.walk(Paths.get(""))
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".fbs"))
                    .filter(p -> p.getFileName().toString().equals(schemaTarget))
                    .findFirst().orElseThrow(() -> new RuntimeException("Failed to locate any schema with the name " + schemaTarget));
            var proc = new ProcessBuilder("flatc", "--raw-binary", "--defaults-json", "--strict-json", "-o", outputFile, "-t", schemaPath.toAbsolutePath().toString(), "--", path.toAbsolutePath().toString())
                    .directory(schemaPath.getParent().toFile())
                    .inheritIO()
                    .start();

            while (proc.isAlive()) Thread.sleep(50);
            System.out.println("Done");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleJson(Path path, String fileName) {
        System.out.println("handle " + fileName);
    }

    private static String saveFile(String defaultName) {
        try (var stack = MemoryStack.stackPush()) {
            var filters = NFDFilterItem.malloc(1);
            filters.get(0)
                    .name(stack.UTF8("Json"))
                    .spec(stack.UTF8("json,json5"));

            var pp = stack.mallocPointer(1);
            checkResult(NativeFileDialog.NFD_SaveDialog(pp, filters, null, defaultName), pp);
            return pp.getStringUTF8(0);
        }
    }

    private static void checkResult(int result, PointerBuffer path) {
        switch (result) {
            case NativeFileDialog.NFD_OKAY -> {
                System.out.println("Success!");
                System.out.println(path.getStringUTF8(0));
                NativeFileDialog.NFD_FreePath(path.get(0));
            }
            case NativeFileDialog.NFD_CANCEL -> System.out.println("User pressed cancel.");
            default -> System.err.format("Error: %s\n", NativeFileDialog.NFD_GetError());
        }
    }
}