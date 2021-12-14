package ru.gooamoko;

import java.math.BigDecimal;
import java.util.Date;

public interface PaymentsArchiveProcessor {

    /**
     * Обработка архива.
     */
    void processArchive();

    /**
     * Возвращает сумму платежей за указанный день.
     *
     * @param date дата, определяющая день, за который нужна сумма
     * @return сумма платежей. Если платежей нет, вернется 0.
     */
    BigDecimal getAmountPerDate(Date date);
}
