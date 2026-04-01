package net.the_last_sword.client.gui.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.the_last_sword.block.entity.DragonCrystalSmithingTableBlockEntity;
import net.the_last_sword.init.ModBlocks;
import net.the_last_sword.init.ModItems;
import net.the_last_sword.init.ModMenus;
import net.the_last_sword.init.ModRecipes;
import net.the_last_sword.item.IDragonSmithingTemplate;
import net.the_last_sword.recipe.ConfigRecipeManager;
import net.the_last_sword.recipe.DragonCrystalSmithingRecipe;

import java.util.Optional;

public class DragonCrystalSmithingTableMenu extends AbstractContainerMenu {

    private final Level level;
    private final ContainerLevelAccess access;
    private final Container container;

    //从网络数据创建（客户端）
    public DragonCrystalSmithingTableMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, new SimpleContainer(4), ContainerLevelAccess.NULL);
    }

    //从方块实体创建（服务端）
    public DragonCrystalSmithingTableMenu(int id, Inventory playerInventory, DragonCrystalSmithingTableBlockEntity blockEntity) {
        this(id, playerInventory, blockEntity, ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private DragonCrystalSmithingTableMenu(int id, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.DRAGON_CRYSTAL_SMITHING_TABLE.get(), id);
        this.level = playerInventory.player.level();
        this.access = access;
        this.container = container;

        checkContainerSize(container, 4);
        container.startOpen(playerInventory.player);

        //槽位0：模板槽
        this.addSlot(new Slot(container, 0, 48, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof IDragonSmithingTemplate;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(container);
            }
        });

        //槽位1：基础槽
        this.addSlot(new Slot(container, 1, 66, 40) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(container);
            }
        });

        //槽位2：附加槽
        this.addSlot(new Slot(container, 2, 84, 40) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(container);
            }
        });

        //槽位3：输出槽
        this.addSlot(new Slot(container, 3, 138, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                //消耗输入物品
                container.removeItem(0, 1);
                container.removeItem(1, 1);
                container.removeItem(2, 1);

                //播放锻造台声音（事件ID 1044）
                access.execute((level, pos) -> level.levelEvent(1044, pos, 0));

                //重新检查配方
                updateResult();

                super.onTake(player, stack);
            }
        });

        //玩家背包
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 48 + j * 18, 71 + i * 18));
            }
        }

        //玩家快捷栏
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 48 + i * 18, 129));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.DRAGON_CRYSTAL_SMITHING_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 3) {
                //从输出槽快速移动
                if (!this.moveItemStackTo(slotStack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else if (index < 4) {
                //从输入槽快速移动到玩家背包
                if (!this.moveItemStackTo(slotStack, 4, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                //从玩家背包快速移动到输入槽
                if (!this.moveItemStackTo(slotStack, 0, 3, false)) {
                    if (index < 31) {
                        if (!this.moveItemStackTo(slotStack, 31, 40, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, 4, 31, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // 清空输出槽预览物品（未实际消耗材料，不应掉落）
        this.container.setItem(3, ItemStack.EMPTY);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.container);
        });
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        updateResult();
    }

    private void updateResult() {
        if (this.level.isClientSide) {
            return;
        }

        //获取输入物品
        ItemStack template = this.container.getItem(0);
        ItemStack base = this.container.getItem(1);
        ItemStack addition = this.container.getItem(2);

        //如果有任何输入为空，清空输出
        if (template.isEmpty() || base.isEmpty() || addition.isEmpty()) {
            this.container.setItem(3, ItemStack.EMPTY);
            return;
        }

        //创建临时容器用于配方匹配
        SimpleContainer recipeContainer = new SimpleContainer(3);
        recipeContainer.setItem(0, template);
        recipeContainer.setItem(1, base);
        recipeContainer.setItem(2, addition);

        //优先从config配方查找
        Optional<DragonCrystalSmithingRecipe> recipe = ConfigRecipeManager.findMatchingRecipe(recipeContainer, this.level);

        //如果config中没有找到，则从数据包配方查找
        if (recipe.isEmpty()) {
            recipe = this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.DRAGON_CRYSTAL_SMITHING_TYPE.get(), recipeContainer, this.level);
        }

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().assemble(recipeContainer, this.level.registryAccess());
            this.container.setItem(3, result);
        } else {
            this.container.setItem(3, ItemStack.EMPTY);
        }
    }
}
