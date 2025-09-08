package dev.mzcy.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class WatcherServiceEx implements AutoCloseable {
    private final WatchService ws;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Future<?> task;

    public WatcherServiceEx(Path dir, Consumer<Path> onCreate) throws IOException {
        this.ws = FileSystems.getDefault().newWatchService();
        dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
        this.task = exec.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = null;
                try {
                    key = ws.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (WatchEvent<?> evt : key.pollEvents()) {
                    if (evt.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                    Path created = dir.resolve((Path) evt.context());
                    // Kleine Verzögerung, bis Datei fertig geschrieben ist
                    try { Thread.sleep(250); } catch (InterruptedException ignored) {}
                    onCreate.accept(created);
                }
                key.reset();
            }
        });
    }

    @Override public void close() throws IOException {
        task.cancel(true);
        exec.shutdownNow();
        ws.close();
    }
}