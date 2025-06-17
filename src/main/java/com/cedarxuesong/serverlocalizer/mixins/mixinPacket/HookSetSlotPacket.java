package com.cedarxuesong.serverlocalizer.mixins.mixinPacket;

import com.cedarxuesong.serverlocalizer.utils.ai.ModConfig;
import com.cedarxuesong.serverlocalizer.utils.translation.ItemTranslator;
import com.cedarxuesong.serverlocalizer.utils.translation.ModuleTranslationManager;
import com.cedarxuesong.serverlocalizer.utils.translation.TextProcessor;
import com.cedarxuesong.serverlocalizer.utils.mylog.mylog;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截单个物品槽位更新数据包
 */
@Mixin(S2FPacketSetSlot.class)
public class HookSetSlotPacket {
    @Unique
    private static final String ServerLocalizer$TAG = "ItemPacket";

    @Shadow
    private ItemStack item;

    @Shadow
    private int windowId;

    @Shadow
    private int slot;

    /**
     * 在数据包读取完成后注入，修改物品信息
     */
    @Inject(method = "readPacketData", at = @At("RETURN"))
    public void onReadPacketData(CallbackInfo ci) {
        if (this.item != null && ModConfig.getInstance().isItemTranslationEnabled()) {
            try {
                // 检查是否启用物品翻译
                ModConfig modConfig = ModConfig.getInstance();
                if (!modConfig.isItemTranslationEnabled()) {
                    return;
                }
                
                // 获取ModuleTranslationManager实例
                ModuleTranslationManager manager = ModuleTranslationManager.getInstance();
                
                // 确保翻译管理器已初始化
                if (!manager.isInitialized()) {
                    manager.initialize();
                }
                
                // 获取物品翻译器
                ItemTranslator itemTranslator = manager.getItemTranslator();
                
                // 获取物品的NBT数据
                NBTTagCompound nbt = this.item.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                    this.item.setTagCompound(nbt);
                }

                // 获取display标签
                NBTTagCompound display = nbt.getCompoundTag("display");
                if (!nbt.hasKey("display", 10)) {
                    display = new NBTTagCompound();
                    nbt.setTag("display", display);
                }

                // 翻译物品名称
                if (display.hasKey("Name", 8) && modConfig.isItemNameTranslationEnabled()) {
                    String originalName = display.getString("Name");
                    
                    // 使用TextProcessor处理颜色代码和翻译
                    TextProcessor.TemplateResult nameResult = TextProcessor.convertToTemplate(originalName);
                    String translatedTemplate = itemTranslator.translate(nameResult.getTemplate());
                    String translatedName = TextProcessor.fillTemplate(translatedTemplate, nameResult.getDynamicContent());
                    
                    if (!translatedName.equals(originalName)) {
                        display.setString("Name", translatedName);
                    }
                }

                // 翻译Lore（物品简介）
                if (display.hasKey("Lore", 9) && modConfig.isItemLoreTranslationEnabled()) {
                    NBTTagList lore = display.getTagList("Lore", 8);
                    NBTTagList newLore = new NBTTagList();
                    boolean hasChanges = false;

                    for (int i = 0; i < lore.tagCount(); i++) {
                        String originalLine = lore.getStringTagAt(i);
                        
                        // 使用TextProcessor处理颜色代码和翻译
                        TextProcessor.TemplateResult lineResult = TextProcessor.convertToTemplate(originalLine);
                        String translatedTemplate = itemTranslator.translate(lineResult.getTemplate());
                        String translatedLine = TextProcessor.fillTemplate(translatedTemplate, lineResult.getDynamicContent());
                        
                        newLore.appendTag(new NBTTagString(translatedLine));

                        if (!translatedLine.equals(originalLine)) {
                            hasChanges = true;
                        }
                    }

                    if (hasChanges) {
                        display.setTag("Lore", newLore);
                    }
                }

            } catch (Exception e) {
                mylog.error(ServerLocalizer$TAG, "处理物品数据包时发生错误", e);
            }
        }
    }
}