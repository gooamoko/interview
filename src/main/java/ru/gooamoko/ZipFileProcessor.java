package ru.gooamoko;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Класс для обработки зип-архива с информацией о платежах.
 */
public class ZipFileProcessor implements PaymentsArchiveProcessor {
    private static final Logger log = LoggerFactory.getLogger(ZipFileProcessor.class);
    private static final int THREADS_COUNT = 4;
    private static final int BUFFER_SIZE = 512;

    private final InputStream sourceInputStream;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private final ConcurrentHashMap<String, BigDecimal> payments = new ConcurrentHashMap<>();

    public ZipFileProcessor(InputStream sourceInputStream) {
        if (sourceInputStream == null) {
            throw new IllegalArgumentException("Source InputStream is null!");
        }
        this.sourceInputStream = sourceInputStream;
    }

    /**
     * Обрабатываем архив
     */
    public void processArchive() {
        final ExecutorService pool = Executors.newFixedThreadPool(THREADS_COUNT);
        final ExecutorCompletionService<Boolean> processingService = new ExecutorCompletionService<>(pool);

        long startTime = System.currentTimeMillis();
        log.info("Запущена обработка архива с документами.");
        try (ZipInputStream zipInputStream = new ZipInputStream(sourceInputStream)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int entryCount = 0;
            int failCount = 0;
            int readCount;

            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                entryCount += 1;
                String fileName = entry.getName();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                while ((readCount = zipInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readCount);
                }

                ZipEntryProcessor processor = new ZipEntryProcessor(outputStream.toByteArray(), fileName);
                processingService.submit(processor);

                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }

            for (int i = 0; i < entryCount; i++) {
                final Future<Boolean> future = processingService.take();
                final Boolean result = future.get();
                if (result == null || !result) {
                    failCount += 1;
                }
            }

            log.info("Обработка архива завершена. Обработано файлов: {}. Из них с ошибками: {}. Обработка заняла {} мс.",
                    entryCount, failCount, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Ошибка при обработке zip архива.", e);
        }
    }

    /**
     * Возвращаем сумму платежей за указанный день.
     *
     * @param date дата, определяющая день, за который нужна сумма
     * @return сумма платежей, либо 0.
     */
    public BigDecimal getAmountPerDate(Date date) {
        String dateKey = dateFormatter.format(date);
        BigDecimal amount = payments.get(dateKey);
        return amount == null ? BigDecimal.ZERO : amount;
    }


    private class ZipEntryProcessor implements Callable<Boolean> {
        private final byte[] entryContent;
        private final String fileName;

        public ZipEntryProcessor(byte[] entryContent, String fileName) {
            this.entryContent = entryContent;
            this.fileName = fileName;
        }

        @Override
        public Boolean call() {
            log.debug("processing entry: {}", fileName);
            if (entryContent == null || entryContent.length == 0) {
                return Boolean.FALSE;
            }

            try {
                Gson gson = new Gson();
                String jsonText = new String(entryContent, StandardCharsets.UTF_8);
                PaymentInfo payment = gson.fromJson(jsonText, PaymentInfo.class);

                String dayKey = dateFormatter.format(payment.getPaymentDate());
                BigDecimal amount = payments.get(dayKey);
                amount = (amount == null) ? payment.getPaymentAmount() : amount.add(payment.getPaymentAmount());
                payments.put(dayKey, amount);
            } catch (Exception e) {
                log.error("Ошибка при обработке файла {}.", fileName, e);
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }
    }
}
