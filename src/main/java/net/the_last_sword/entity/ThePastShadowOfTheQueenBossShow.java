package net.the_last_sword.entity;

import net.eca.api.RegisterBossShow;
import net.eca.util.bossshow.BossShow;
import net.eca.util.bossshow.BossShowContext;
import net.eca.util.bossshow.BossShowManager;
import net.minecraft.resources.ResourceLocation;
import net.the_last_sword.init.ModEntities;

@RegisterBossShow
public final class ThePastShadowOfTheQueenBossShow extends BossShow {
    private static final String ACTIVATE_EVENT = "activate";

    static {
        BossShowManager.register(new ThePastShadowOfTheQueenBossShow());
    }

    private ThePastShadowOfTheQueenBossShow() {
        super(
                ResourceLocation.fromNamespaceAndPath("the_last_sword", "the_past_shadow_of_the_queen"),
                ModEntities.THE_PAST_SHADOW_OF_THE_QUEEN.get()
        );
    }

    @Override
    public void onStart(BossShowContext context) {
        if (context.target() instanceof ThePastShadowOfTheQueenEntity queen) {
            queen.startWakeAnimation();
        }
    }

    @Override
    public void onKeyframeEvent(String eventId, BossShowContext context) {
        if (ACTIVATE_EVENT.equals(eventId)) {
            activateTarget(context);
        }
    }

    @Override
    public void onEnd(BossShowContext context, boolean skipped) {
        activateTarget(context);
    }

    private void activateTarget(BossShowContext context) {
        if (context.target() instanceof ThePastShadowOfTheQueenEntity queen) {
            queen.activate();
        }
    }
}
