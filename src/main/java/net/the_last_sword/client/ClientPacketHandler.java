package net.the_last_sword.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.the_last_sword.client.gui.PaperNoteScreen;
import net.the_last_sword.client.gui.TheLastEndScrollScreen;
import net.the_last_sword.client.gui.menu.DragonCrystalEnchantingTableMenu;
import net.the_last_sword.client.gui.menu.SummonWraithGuiMenu;
import net.the_last_sword.client.recipe.ClientDragonCrystalRecipeCache;
import net.the_last_sword.client.renderer.DragonShieldRenderer;
import net.the_last_sword.event.ClientEventHandler;
import net.the_last_sword.network.OpenLastEndScrollPacket.PaperNoteEntry;
import net.the_last_sword.network.PerceptionScanPacket.ScanType;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client-only effects of S2C packets. Network packet classes must stay safe to
 * load on a dedicated server and therefore delegate all Minecraft client access
 * to this class.
 */
public final class ClientPacketHandler {

    private ClientPacketHandler() {
    }

    public static void setMiningPreview(Set<BlockPos> blocks) {
        if (Minecraft.getInstance().level != null) {
            ClientEventHandler.setMiningPreviewBlocks(blocks);
        }
    }

    public static void clearPreviews() {
        if (Minecraft.getInstance().level != null) {
            ClientEventHandler.clearMiningPreview();
            ClientEventHandler.clearArenaPreview();
        }
    }

    public static void syncSummonGui(ItemStack soulStone) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        AbstractContainerMenu menu = minecraft.player.containerMenu;
        if (menu instanceof SummonWraithGuiMenu summonMenu
                && summonMenu.get().containsKey(0)
                && summonMenu.get().get(0).container instanceof ItemStackHandler handler) {
            handler.setStackInSlot(0, soulStone);
        }
    }

    public static void syncEnchantingTable(int containerId, int energy, int maxEnergy, int totalPowerTime) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof DragonCrystalEnchantingTableMenu menu
                && menu.containerId == containerId) {
            menu.setClientEnergy(energy);
            menu.setClientMaxEnergy(maxEnergy);
            menu.setClientTotalPowerTime(totalPowerTime);
        }
    }

    public static void updatePerceptionScan(Map<Integer, ScanType> scannedEntities, int glowDurationSeconds) {
        PerceptionScanData.update(scannedEntities, glowDurationSeconds);
    }

    public static void updateDragonArmorEnergyStatus(long currentEnergy, long maxEnergy, long consumptionPerTick) {
        DragonArmorEnergyStatus.update(currentEnergy, maxEnergy, consumptionPerTick);
    }

    public static void setArenaPreview(BlockPos minPos, BlockPos maxPos) {
        if (Minecraft.getInstance().level != null) {
            ClientEventHandler.setArenaPreview(minPos, maxPos);
        }
    }

    public static void triggerDragonShield(boolean hasDirection, float x, float y, float z) {
        if (hasDirection) {
            DragonShieldRenderer.trigger(x, y, z);
        }
    }

    public static void replaceDragonCrystalRecipes(List<DragonCrystalSmithingRecipe> recipes) {
        ClientDragonCrystalRecipeCache.replaceAll(recipes);
    }

    public static void openLastEndScroll(boolean hasLocation, int x, int z, List<PaperNoteEntry> collectedNotes) {
        Minecraft.getInstance().setScreen(new TheLastEndScrollScreen(hasLocation, x, z, collectedNotes));
    }

    public static void openPaperNote(String noteId, String nameKey, String guiContentKey, boolean collected) {
        Minecraft.getInstance().setScreen(new PaperNoteScreen(noteId, nameKey, guiContentKey, collected));
    }
}
