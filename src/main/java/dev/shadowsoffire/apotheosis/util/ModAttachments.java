package dev.shadowsoffire.apotheosis.util;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class ModAttachments {

    @SuppressWarnings("unchecked")
    public static <T> T getData(Object target, AttachmentType<T> type) {
        if (target instanceof AttachmentTarget at) {
            return at.getAttachedOrCreate(type);
        }
        return null;
    }

    public static <T> void setData(Object target, AttachmentType<T> type, T value) {
        if (target instanceof AttachmentTarget at) {
            at.setAttached(type, value);
        }
    }
}
