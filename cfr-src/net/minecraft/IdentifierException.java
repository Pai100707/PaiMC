/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringEscapeUtils
 */
package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class IdentifierException
extends RuntimeException {
    public IdentifierException(String $$0) {
        super(StringEscapeUtils.escapeJava((String)$$0));
    }

    public IdentifierException(String $$0, Throwable $$1) {
        super(StringEscapeUtils.escapeJava((String)$$0), $$1);
    }
}

