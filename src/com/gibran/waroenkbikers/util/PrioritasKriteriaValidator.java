package com.gibran.waroenkbikers.util;

import java.util.HashSet;
import java.util.Set;

public final class PrioritasKriteriaValidator {
    public static final String PESAN_PRIORITAS_DUPLIKAT = "Prioritas harus berbeda";

    private PrioritasKriteriaValidator() {
    }

    public static void validasiUnik(int[] prioritas) {
        Set<Integer> prioritasTerpakai = new HashSet<>();
        for (int nilai : prioritas) {
            if (!prioritasTerpakai.add(nilai)) {
                throw new IllegalArgumentException(PESAN_PRIORITAS_DUPLIKAT);
            }
        }
    }
}
