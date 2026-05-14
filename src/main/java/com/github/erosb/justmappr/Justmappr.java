package com.github.erosb.justmappr;

public interface Justmappr {

    static Justmappr create(JustmapprConfig config) {
        return new DefaultJustmappr();
    }

    static JustmapprConfig.JustmapprConfigBuilder config() {
        return JustmapprConfig.builder();
    }

}

class DefaultJustmappr implements Justmappr {

}
