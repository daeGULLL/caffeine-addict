package com.caffeineaddict.caffeineaddictmode.blocks.CoffeeMachine;

import com.caffeineaddict.caffeineaddictmode.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CoffeeMachineMenu extends AbstractContainerMenu {
    private static final int COFFEE_MAX_STACK = 1;
    private static final int MILK_MAX_STACK = 8;
    private final ContainerLevelAccess access;
    private final Container shotInv;
    private final Container steamInv;
    private final ContainerData gaugeData;
    private final BlockPos pos;

    public CoffeeMachineMenu(int id, Inventory playerInv, FriendlyByteBuf data) {
        this(id, playerInv, BlockPos.of(data.readLong()), new SimpleContainer(6), new SimpleContainer(4), new SimpleContainerData(5));
    }

    public CoffeeMachineMenu(int id, Inventory playerInv, BlockPos pos, Container shotInv, Container steamInv, ContainerData gaugeData) {
        super(ModMenus.COFFEE_MACHINE_MENU.get(), id);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInv.player.level, pos);
        this.shotInv = shotInv;
        this.steamInv = steamInv;
        this.gaugeData = gaugeData;

        // Inputs
        this.addSlot(new Slot(shotInv, 0, 104, 35));
        this.addSlot(new Slot(shotInv, 1, 158, 35));
        this.addSlot(new Slot(shotInv, 2, 212, 35));

        // Outputs (read-only)
        this.addSlot(new Slot(shotInv, 3, 104, 80) {
            @Override public int getMaxStackSize() {return COFFEE_MAX_STACK;}
        });
        this.addSlot(new Slot(shotInv, 4, 158, 80) {
            @Override public int getMaxStackSize() {return COFFEE_MAX_STACK;}
        });
        this.addSlot(new Slot(shotInv, 5, 212, 80) {
            @Override public int getMaxStackSize() {return COFFEE_MAX_STACK;}
        });

        // Steam Inputs
        this.addSlot(new Slot(steamInv, 0, 50, 35));
        this.addSlot(new Slot(steamInv, 1, 266, 35));

        this.addSlot(new Slot(steamInv, 2, 50, 80) {
            @Override public int getMaxStackSize() {return MILK_MAX_STACK;}
        });
        this.addSlot(new Slot(steamInv, 3, 266, 80) {
            @Override public int getMaxStackSize() {return MILK_MAX_STACK;}
        });

        addDataSlots(gaugeData);

        // Add player inventory slots (start at y=140 based on texture)
        int startX = 8;
        int startY = 96;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
            }
        }

// Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, startX + col * 18, startY + 58));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Block inventory size = 3, player = 3*9 + 9
            if (index < 10) {
                // Move from block inventory to player
                if (!this.moveItemStackTo(itemstack1, 6, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player to block inventory
                if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
    public BlockPos getPos(){return pos;}
    public int getProgressForSlot(int slotIndex) {
        return gaugeData.get(slotIndex); // 0~24 for animation
    }
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
