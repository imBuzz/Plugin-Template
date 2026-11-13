package it.buzz.template.storage.database;

import it.buzz.template.storage.migration.Migrations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves the two H2 promises: parallel writers inside one server, and several OS processes
 * writing to the very same database file at the same time (auto-server mode).
 */
class H2ConcurrencyTest {

    private static final String[] COLUMNS = {"uuid", "name", "data", "created_at", "updated_at"};
    private static final String[] UPDATED = {"name", "data", "updated_at"};

    @TempDir
    Path dir;

    @Test
    @Timeout(60)
    void manyThreadsWriteInParallel() throws Exception {
        try (Database db = open(dir.resolve("threads"), false)) {
            int threads = 16, rowsPerThread = 200;
            CountDownLatch start = new CountDownLatch(1);
            try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();
                for (int t = 0; t < threads; t++) {
                    futures.add(pool.submit(() -> {
                        start.await();
                        insertRows(db, rowsPerThread);
                        return null;
                    }));
                }
                start.countDown();
                for (Future<?> f : futures) {
                    f.get();
                }
            }
            assertEquals(threads * rowsPerThread, count(db));
        }
    }

    @Test
    @Timeout(120)
    void severalProcessesShareTheSameFile() throws Exception {
        Path file = dir.resolve("shared");
        try (Database db = open(file, true)) { // this JVM becomes the auto-server owner
            int processes = 3, rowsPerProcess = 300;
            List<Process> children = new ArrayList<>();
            for (int i = 0; i < processes; i++) {
                children.add(new ProcessBuilder(
                        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-cp", System.getProperty("java.class.path"),
                        Writer.class.getName(), file.toString(), String.valueOf(rowsPerProcess))
                        .inheritIO()
                        .start());
            }
            insertRows(db, rowsPerProcess); // write concurrently with the children
            for (Process child : children) {
                if (!child.waitFor(90, TimeUnit.SECONDS)) {
                    child.destroyForcibly();
                }
                assertEquals(0, child.exitValue(), "child process failed");
            }
            assertEquals((processes + 1) * rowsPerProcess, count(db));
        }
    }

    /** Entry point of the child processes spawned above. */
    public static final class Writer {
        public static void main(String[] args) throws Exception {
            try (Database db = open(Path.of(args[0]), true)) {
                insertRows(db, Integer.parseInt(args[1]));
            }
        }
    }

    // ------------------------------------------------------------------

    static Database open(Path file, boolean autoServer) throws Exception {
        Database db = new Database(DatabaseSettings.h2(file, "c_", 8, autoServer), "h2-test");
        db.migrate(Migrations.ALL, Logger.getAnonymousLogger());
        return db;
    }

    static void insertRows(Database db, int rows) throws Exception {
        String sql = DatabaseType.H2.dialect().upsert(db.table("players"), "uuid", COLUMNS, UPDATED);
        for (int i = 0; i < rows; i++) {
            try (Connection c = db.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
                long now = System.currentTimeMillis();
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, "p" + i);
                ps.setString(3, "{}");
                ps.setLong(4, now);
                ps.setLong(5, now);
                ps.executeUpdate();
            }
        }
    }

    static int count(Database db) throws Exception {
        try (Connection c = db.connection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + db.table("players"))) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
