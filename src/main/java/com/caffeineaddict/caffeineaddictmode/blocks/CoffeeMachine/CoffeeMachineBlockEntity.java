package com.caffeineaddict.caffeineaddictmode.blocks.CoffeeMachine;

import com.caffeineaddict.caffeineaddictmode.registry.ModBlockEntities;
import com.caffeineaddict.caffeineaddictmode.registry.ModItems;
import com.caffeineaddict.caffeineaddictmode.TagNSpecialConstant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoffeeMachineBlockEntity extends BlockEntity implements MenuProvider {
    private final Container inventory = new SimpleContainer(6);
    private final ContainerData gauges = new SimpleContainerData(3);
    private String lastUsedBy = "";
    private boolean extracting = false;

    public CoffeeMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COFFEE_MACHINE.get(), pos, state);
    }

    public void setLastUsedBy(String playerName) {
        this.lastUsedBy = playerName;
    }

    public String getLastUsedBy() {
        return lastUsedBy;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Coffee Machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, Player player) {
        System.out.println("Opened GUI");
        return new CoffeeMachineMenu(id, playerInventory, this.worldPosition, this.inventory, this.gauges);
    }

    // Save data
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TagNSpecialConstant.LAST_USEDBY.getText(), lastUsedBy);
        NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            stacks.set(i, inventory.getItem(i));
        }
        ContainerHelper.saveAllItems(tag, stacks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TagNSpecialConstant.LAST_USEDBY.getText())) {
            lastUsedBy = tag.getString(TagNSpecialConstant.LAST_USEDBY.getText());
        }
        NonNullList<ItemStack> stacks = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, stacks);
        for (int i = 0; i < stacks.size(); i++) {
            inventory.setItem(i, stacks.get(i));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void brew(int currentIndex){
        // 커피콩 소비
        inventory.removeItem(currentIndex, 1);

        int progress = gauges.get(currentIndex);
        int distance = Math.abs(progress - 12);
        // 에스프레소 추출
        int quality = 0;

        if (distance<=1) {
            quality = 5; // 최고 등급
        } else if (distance <= 3) {
            quality = 4;
        } else if (distance <= 6) {
            quality = 3;
        } else if (distance <= 9) {
            quality = 2;
        } else {
            quality = 1; // 완전 멀어지면 최저 등급
        }

        ItemStack espresso = new ItemStack(ModItems.ESPRESSO.get(), 1);
        CompoundTag tag = new CompoundTag();
        tag.putInt(TagNSpecialConstant.QUALITY.getText(), quality);
        tag.putString(TagNSpecialConstant.BARISTA.getText(), lastUsedBy);
        espresso.setTag(tag);
        inventory.setItem(currentIndex+2, espresso);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            for (int i = 0; i < 2; i++) {
                ItemStack input = inventory.getItem(i);
                ItemStack output = inventory.getItem(i+2);
                if (input.is(ModItems.GROUND_COFFEE.get()) && output.is(ModItems.SHOT_CUP.get())) {
                    extracting = true;
                    int progress = gauges.get(i);
                    // progress 값은 0~24 사이

                    if (progress >= 24) {
                        //brew(i);
                    }
                    if (progress % 20 == 0 && progress!=0) { // once per second
                        level.playSound(
                                null,         // null = all nearby players hear it
                                worldPosition,
                                SoundEvents.BREWING_STAND_BREW, // or your custom sound
                                SoundSource.BLOCKS,
                                1.0f,         // volume
                                1.0f          // pitch
                        );
                    }
                    progress = (progress + 1) % 25;
                    gauges.set(i, progress);
                } else {
                    gauges.set(i, 0); // reset if no item
                }
            }
            setChanged();
        }
    }

    public Container getInventory() {
        return inventory;
    }
}
