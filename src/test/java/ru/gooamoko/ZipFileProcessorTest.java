package ru.gooamoko;

import com.google.gson.Gson;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;

public class ZipFileProcessorTest {


    @Test
    public void testSumIsCorrect() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipStream = new ZipOutputStream(byteArrayOutputStream);

        Date currentDate = new Date();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < 1000; i++) {
            String fileName = UUID.randomUUID() + ".json";

            BigDecimal paymentAmount = new BigDecimal("100.00");
            totalAmount = totalAmount.add(paymentAmount);

            PaymentInfo payment = new PaymentInfo();
            payment.setPaymentDate(currentDate);
            payment.setPaymentAmount(paymentAmount);
            Gson gson = new Gson();
            String json = gson.toJson(payment);
            byte[] jsonData = json.getBytes(StandardCharsets.UTF_8);

            zipStream.putNextEntry(new ZipEntry(fileName));
            zipStream.write(jsonData, 0, jsonData.length);
            zipStream.closeEntry();
        }
        zipStream.close();

        ByteArrayInputStream zipInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        PaymentsArchiveProcessor processor = new ZipFileProcessor(zipInputStream);
        processor.processArchive();

        assertEquals(totalAmount, processor.getAmountPerDate(currentDate));
    }
}