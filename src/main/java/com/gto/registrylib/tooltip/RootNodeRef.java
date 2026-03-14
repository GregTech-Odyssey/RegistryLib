package com.gto.registrylib.tooltip;

import java.util.Objects;

/**
 * {@link RootNode} 的惰性引用句柄。
 *
 * <p>
 * 在注册阶段可安全传递和引用。 跨模块使用时直接引用声明方的静态字段即可。
 */
public final class RootNodeRef {

    private final String id;

    public RootNodeRef(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RootNodeRef other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RootNodeRef(" + id + ")";
    }
}
