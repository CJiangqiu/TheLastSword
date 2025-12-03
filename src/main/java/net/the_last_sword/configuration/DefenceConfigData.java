package net.the_last_sword.configuration;

import java.util.HashMap;
import java.util.Map;

public class DefenceConfigData {
    public int configVersion = 1;
    public HudSettings hud = new HudSettings();

    public static class HudSettings {
        public Map<String, HudElement> elements = createDefaultElements();

        private static Map<String, HudElement> createDefaultElements() {
            Map<String, HudElement> map = new HashMap<>();
            map.put("justified_defence_overlay", new HudElement(0, 0));
            return map;
        }
    }

    public static class HudElement {
        public HudOffset offset;

        public HudElement(int x, int y) {
            this.offset = new HudOffset(x, y);
        }
    }

    public static class HudOffset {
        public int x;
        public int y;

        public HudOffset(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
