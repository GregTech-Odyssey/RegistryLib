package com.gto.registrylib.composite;

import java.util.List;

public interface IComponentItem<T extends IComponentItem<T>> {

    @SuppressWarnings("unchecked")
    default <I> I self() {
        return (I) this;
    }

    List<ItemAttachment<T>> getAttachments();

    void attachAttachment(ItemAttachment<T> attachment);
}
