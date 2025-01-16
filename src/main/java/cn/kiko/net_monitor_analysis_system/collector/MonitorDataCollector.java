package cn.kiko.net_monitor_analysis_system.collector;

import cn.kiko.net_monitor_analysis_system.algo.FlowKey;
import cn.kiko.net_monitor_analysis_system.model.ExportedMonitorData;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class MonitorDataCollector {
    // 监听端口
    private int port;

    private ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 0L,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());

    public MonitorDataCollector(int port) {
        this.port = port;
    }

    public void handleClient(Socket clientSocket) {
        pool.submit(() -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[64 * 1024];
            try (BufferedInputStream ips = new BufferedInputStream(clientSocket.getInputStream())) {
                int sz;
                while ((sz = ips.read(buffer)) != -1) {
                    os.write(buffer,0, sz);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ExportedMonitorData<FlowKey> monitorData = ExportedMonitorData.parseFromBytes(os.toByteArray(), FlowKey.class);
            // TODO(kiko): 将数据上送至 kafka
        });
    }

    public void startCollectorServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
