package com.gto.registrylib.tooltip;

import java.util.List;

/** 已解析的独立框根节点——包含 {@link RootNode} 配置和其下属的 {@link SubNode} 列表。 */
public record ResolvedRoot(RootNode rootNode, List<SubNode> subNodes) {}
