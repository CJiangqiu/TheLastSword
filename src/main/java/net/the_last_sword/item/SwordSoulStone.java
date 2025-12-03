package net.the_last_sword.item;

//剑之魂石 - 预设绑定终焉之剑剑灵的魂石
public class SwordSoulStone extends DragonCrystalSoulStone {

    public SwordSoulStone() {
        super();
    }

    @Override
    public String getDefaultBoundEntityId() {
        return "the_last_sword:the_last_end_sword_wraith";
    }

    @Override
    public String getDefaultBoundEntityDisplayName() {
        return "entity.the_last_sword.the_last_end_sword_wraith";
    }
}
