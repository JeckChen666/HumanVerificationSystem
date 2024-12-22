package com.jeckchen.humanverificationsystem.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jeckchen.humanverificationsystem.config.VirtualThreadExecutor;
import com.jeckchen.humanverificationsystem.pojo.IpApiResp;
import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import com.jeckchen.humanverificationsystem.pojo.VerificationRequest;
import com.jeckchen.humanverificationsystem.utils.IpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class LogService {

    public static final String TODO = "TODO";

    ExecutorService executor = VirtualThreadExecutor.getInstance();

    private final ConcurrentHashMap<String, WeakReference<Lock>> locks = new ConcurrentHashMap<>();

    private final Map<String, IpApiResp> lruCache = new LinkedHashMap<String, IpApiResp>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, IpApiResp> eldest) {
            boolean remove = size() > 150;
            if (remove) {
                locks.remove(eldest.getKey());
            }
            return remove;
        }
    };

    TimedCache<String, String> timedCache = CacheUtil.newTimedCache(DateUnit.HOUR.getMillis() * 6, DateUnit.MINUTE.getMillis() * 5);

    private final Semaphore semaphore = new Semaphore(20); // 最大20个并发

    @Resource
    private IpApiRestTemplate ipApiRestTemplate;

    @Resource
    private LogRecordService logRecordService;

    private static final String JSON_DIR = "json/";

    public void logAccess(HttpServletRequest request) {
        String operation = "访问主页";
        LogRecord record = constructLogRecord(request, operation);
        log.info("构造Record完成：{}", record);
        saveLogRecord(record);
    }

    public void logAccess(HttpServletRequest request, String pageName) {
        String operation = "访问页面：" + pageName;
        LogRecord record = constructLogRecord(request, operation);
        saveLogRecord(record);
    }

    public void logAccess(HttpServletRequest request, String pageName, String originalUrl) {
        String operation = "访问页面：" + pageName;
        LogRecord record = constructLogRecord(request, operation, originalUrl);
        saveLogRecord(record);
    }


    public void logVerification(HttpServletRequest request, VerificationRequest verificationRequest, boolean isCorrect) {
        String operation = "验证题目：" + verificationRequest.getQuestion() + " 给出的答案：" + verificationRequest.getAnswer() + " 是否正确：" + isCorrect;
        LogRecord record = constructLogRecord(request, operation);
        saveLogRecord(record);
    }

    private LogRecord constructLogRecord(HttpServletRequest request, String operation) {
        return constructLogRecord(request, operation, Boolean.FALSE, null);
    }


    private LogRecord constructLogRecord(HttpServletRequest request, String operation, String originalUrl) {
        return constructLogRecord(request, operation, Boolean.FALSE, originalUrl);
    }

    private LogRecord constructLogRecord(HttpServletRequest request, String operation, Boolean useApi, String originalUrl) {
        String time = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
        HttpSession session = request.getSession();
        String sessionId = "null";
        if (null != session) {
            sessionId = session.getId();
        }
        String ip;
        if (StringUtils.isBlank(originalUrl)) {
            ip = IpUtil.getIpAddr(request);
        } else {
            ip = originalUrl;
        }
        String requestURI = request.getRequestURI();
        String region = "TODO";
        String city = "TODO";
        if (useApi) {
            IpApiResp ipApiResp = useApi(ip);
            region = ipApiResp.getCountry_name();
            city = ipApiResp.getCity();
        }
        String deviceInfo = request.getHeader("User-Agent");
        String firstVisit = getFirstVisit(sessionId);
        return LogRecord.builder()
                        .time(time)
                        .sessionId(sessionId)
                        .ip(ip)
                        .url(requestURI)
                        .country(region)
                        .city(city)
                        .deviceInfo(deviceInfo)
                        .operation(operation)
                        .firstVisit(firstVisit)
                        .build();
    }

    private void saveLogRecord(LogRecord entity) {
        log.info("准备写入日志：{}", entity);
        CompletableFuture.runAsync(() -> {
            try {
                saveToDB(entity);
            } catch (Exception e) {
                log.error("写入日志失败", e);
            }
        }, executor);
    }

    private void saveToJson(LogRecord entity) {
        String fileName = JSON_DIR + "sysLog" + DateUtil.format(LocalDateTime.now(), "yyyyMMdd") + ".txt";
        ensureDirectoryExists(JSON_DIR);
        String jsonStr = JSONUtil.toJsonStr(entity);
        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(jsonStr + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToDB(LogRecord entity) {
        logRecordService.save(entity);
    }

    private void ensureDirectoryExists(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) {
            synchronized (this) { // 使用同步块确保线程安全
                if (!directory.exists()) {
                    try {
                        boolean created = directory.mkdirs();
                        if (!created) {
                            throw new IOException("Failed to create directory: " + dir);
                        }
                    } catch (SecurityException e) {
                        throw new RuntimeException("Permission denied: " + dir, e);
                    } catch (IOException e) {
                        throw new RuntimeException("Error creating directory: " + dir, e);
                    }
                }
            }
        }
    }

    private String getFirstVisit(String sessionId) {
        String timeResp;
        if (StringUtils.isBlank(sessionId)) {
            return "-";
        }
        timeResp = timedCache.get(sessionId);
        if (StringUtils.isBlank(timeResp)) {
            String time = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
            timedCache.put(sessionId, time);
            return time;
        }
        return timeResp;
    }

    public IpApiResp useApi(String ip) {
        IpApiResp locationOfIp = lruCache.get(ip);
        if (null != locationOfIp) {
            return locationOfIp;
        }

        try {
            // 获取信号量许可
            semaphore.acquire();
            try {
                // 获取或创建锁对象
                WeakReference<Lock> lockRef = locks.get(ip);
                Lock lock = null;
                if (lockRef != null) {
                    lock = lockRef.get();
                }
                if (lock == null) {
                    lock = new ReentrantLock();
                    locks.put(ip, new WeakReference<>(lock));
                }

                lock.lock(); // 加锁
                try {
                    // 再次检查缓存，防止其他线程已经填充了缓存
                    locationOfIp = lruCache.get(ip);
                    if (null == locationOfIp) {
                        locationOfIp = ipApiRestTemplate.getLocationOfIp(ip);
                        log.info("use \"ipapi.co\" api Success, response:{}", JSONUtil.toJsonStr(locationOfIp));
                        if (null != locationOfIp) {
                            lruCache.put(ip, locationOfIp);
                        }
                    }
                } finally {
                    lock.unlock(); // 解锁
                }
            } finally {
                // 释放信号量许可
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread()
                  .interrupt();
            log.error("Thread interrupted while acquiring semaphore", e);
            return IpApiResp.getUnknown();
        } catch (Exception e) {
            log.error("use \"ipapi.co\" api Error", e);
            return IpApiResp.getUnknown();
        }

        if (null != locationOfIp) {
            return locationOfIp;
        }
        return IpApiResp.getUnknown();
    }

    @Async
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void scheduleUseApi() {
        List<LogRecord> oneHour = logRecordService.findAllWithinOneHour();
        log.info("oneHour size:{}", oneHour.size());
        if (CollUtil.isEmpty(oneHour)) {
            return;
        }
        for (LogRecord logRecord : oneHour) {
            if (!TODO.equals(logRecord.getCountry())) {
                continue;
            }
            CompletableFuture.runAsync(() -> {
                Long id = logRecord.getId();
                String ip = logRecord.getIp();
                log.info("schedule excuse useApi -- id:{}, ip:{}", id, ip);
                IpApiResp ipApiResp = useApi(ip);
                String countryName = ipApiResp.getCountry_name();
                logRecord.setCountry(countryName);
                String city = ipApiResp.getCity();
                logRecord.setCity(city);
                log.info("schedule excuse update DB -- id:{}, ip:{}, country:{}, city:{} ", id, ip, countryName, city);
                logRecordService.update(logRecord);
            }, executor);
        }
    }

    @Async
    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void scheduleUseApiEveryWeek() {
        List<LogRecord> twoDays = logRecordService.findAllWithinTwoDays();
        log.info("twoDay size:{}", twoDays.size());
        if (CollUtil.isEmpty(twoDays)) {
            return;
        }
        for (LogRecord logRecord : twoDays) {
            if (!TODO.equals(logRecord.getCountry())) {
                continue;
            }
            CompletableFuture.runAsync(() -> {
                Long id = logRecord.getId();
                String ip = logRecord.getIp();
                log.info("schedule excuse useApi -- id:{}, ip:{}", id, ip);
                IpApiResp ipApiResp = useApi(ip);
                String countryName = ipApiResp.getCountry_name();
                logRecord.setCountry(countryName);
                String city = ipApiResp.getCity();
                logRecord.setCity(city);
                log.info("schedule excuse update DB -- id:{}, ip:{}, country:{}, city:{} ", id, ip, countryName, city);
                logRecordService.update(logRecord);
            }, executor);
        }
    }

    @Async
    @Scheduled(cron = "0 0/3 * * * ?")
    public void printLockAndSemaphoreInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Lock Information:");
        if (locks.values().isEmpty()){
            sb.append("No locks found.");
        }else {
            for (Map.Entry<String, WeakReference<Lock>> entry : locks.entrySet()) {
                String ip = entry.getKey();
                WeakReference<Lock> lockRef = entry.getValue();
                Lock lock = lockRef.get();
                if (lock != null && lock instanceof ReentrantLock) {
                    ReentrantLock reentrantLock = (ReentrantLock) lock;
                    int waitingThreads = reentrantLock.getQueueLength();
                    log.info("IP: " + ip + ", Waiting Threads: " + waitingThreads);
                    sb.append("IP: " + ip + ", Waiting Threads: " + waitingThreads + "; ");
                }
            }
        }
        sb.append("\t");
        sb.append("Semaphore Information:");
        int availablePermits = semaphore.availablePermits();
        int queueLength = semaphore.getQueueLength();
        sb.append("Available Permits: " + availablePermits + ", Queued Threads: " + queueLength + "; ");
        sb.append("END");
        log.info(sb.toString());
    }
}
