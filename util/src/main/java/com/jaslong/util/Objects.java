package com.jaslong.util;

public final class Objects {

    public static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 == null ? o2 == null : o1.equals(o2));
    }

    private Objects() {
        throw new UnsupportedOperationException();
    }

}
