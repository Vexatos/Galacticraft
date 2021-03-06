package codechicken.nei;

import java.text.*;
import java.util.*;

import org.lwjgl.input.Keyboard;

import codechicken.core.inventory.InventoryRange;
import codechicken.core.inventory.InventoryUtils;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.IInfiniteItemHandler;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.ItemInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

import static codechicken.nei.NEIClientConfig.*;

public class NEIClientUtils extends NEIServerUtils
{
    public static Minecraft mc()
    {
        return Minecraft.getMinecraft();
    }
    
    public static String translate(String s, Object... format)
    {
        return ClientHandler.lang.translate(s, format);
    }

    public static void addChatMessage(String s)
    {
        if(mc().ingameGUI != null)
            mc().ingameGUI.getChatGUI().printChatMessage(s);
    }

    public static void deleteHeldItem()
    {
        deleteSlotStack(-999);
    }
    
    public static void dropHeldItem()
    {
        mc().playerController.windowClick(((GuiContainer)mc().currentScreen).inventorySlots.windowId, -999, 0, 0, mc().thePlayer);
    }

    public static void deleteSlotStack(int slotNumber)
    {
        setSlotContents(slotNumber, null, true);
    }
    
    public static void decreaseSlotStack(int slotNumber)
    {
        ItemStack stack = slotNumber == -999 ? getHeldItem() : mc().thePlayer.openContainer.getSlot(slotNumber).getStack();
        if(stack == null)
            return;
        
        if(stack.stackSize == 1)
            deleteSlotStack(slotNumber);
        else
        {
            stack = stack.copy();
            stack.stackSize--;
            setSlotContents(slotNumber, stack, true);
        }
    }
    
    public static void deleteEverything()
    {
        NEICPH.sendDeleteAllItems();
    }
    
    public static void deleteItemsOfType(ItemStack itemstack)
    {
        Container c = getGuiContainer().inventorySlots;
        for(int i = 0; i < c.inventorySlots.size(); i++)
        {
            Slot slot = c.getSlot(i);
            if(slot == null)
            {
                continue;
            }
            ItemStack itemstack1 = slot.getStack();
            if(itemstack1 != null && itemstack1.itemID == itemstack.itemID && itemstack1.getItemDamage() == itemstack.getItemDamage())
            {
                setSlotContents(i, null, true);
                slot.putStack((ItemStack)null);
            }
        }
    }
    
    public static ItemStack getHeldItem()
    {
        return mc().thePlayer.inventory.getItemStack();
    }

    public static void setSlotContents(int slot, ItemStack item, boolean containerInv)
    {
        NEICPH.sendSetSlot(slot, item, containerInv);
        
        if(slot == -999)
        {
            mc().thePlayer.inventory.setItemStack(item);
        }
    }
    
    /**
     * 
     * @param typeStack
     * @param button
     * @param mode -1 = normal cheats, 0 = no infinites, 1 = replenish stack
     */
    public static void cheatItem(ItemStack typeStack, int button, int mode)
    {
        if(!canPerformAction("item"))
            return;
        
        if(mode == -1 && button == 0 && NEIClientUtils.shiftKey())
        {
            for(IInfiniteItemHandler handler : ItemInfo.infiniteHandlers)
            {
                if(!handler.canHandleItem(typeStack))
                    continue;
                
                ItemStack stack = handler.getInfiniteItem(typeStack);
                if(stack != null)
                {
                    giveStack(stack, stack.stackSize, true);
                    return;
                }
            }
            cheatItem(typeStack, button, 0);
        }
        else if(button == 1)
        {
            giveStack(typeStack, 1);
        }
        else
        {
            if(mode == 1 && typeStack.stackSize < typeStack.getMaxStackSize())
            {
                giveStack(typeStack, typeStack.getMaxStackSize() - typeStack.stackSize);
            }
            else
            {
                int amount = getItemQuantity();
                if(amount == 0)
                    amount = typeStack.getMaxStackSize();
                giveStack(typeStack, amount);
            }
        }
    }
    
    public static void giveStack(ItemStack itemstack)
    {
        giveStack(itemstack, itemstack.stackSize);
    }
    
    public static void giveStack(ItemStack itemstack, int i)
    {
        giveStack(itemstack, i, false);
    }

    public static void giveStack(ItemStack itemstack, int i, boolean infinite)
    {
        ItemStack itemstack1 = copyStack(itemstack, i);
        if(hasSMPCounterPart())
        {
            ItemStack typestack = copyStack(itemstack1, 1);
            if(!infinite && !canItemFitInInventory(mc().thePlayer, itemstack1) && (mc().currentScreen instanceof GuiContainer))
            {    
                GuiContainer gui = getGuiContainer();
                int increment = typestack.getMaxStackSize();
                int given;
                for(given = 0; given < itemstack1.stackSize; )
                {
                    int qty = Math.min(itemstack1.stackSize-given, increment);
                    int slotNo = -1;
                    for(INEIGuiHandler handler : GuiInfo.guiHandlers)
                    {
                        slotNo = handler.getItemSpawnSlot(gui, typestack);
                        if(slotNo >= 0)
                            break;
                    }
                    if(slotNo == -1)
                        break;
                    
                    Slot slot = gui.inventorySlots.getSlot(slotNo);  
                    int current = (slot.getHasStack() ? slot.getStack().stackSize : 0);
                    qty = Math.min(qty, slot.getSlotStackLimit() - current);
                    
                    ItemStack newStack = copyStack(typestack, qty+current);
                    slot.putStack(newStack);
                    setSlotContents(slotNo, newStack, true);
                    given+=qty;
                }
                NEICPH.sendSpawnItem(copyStack(typestack, given), infinite, false);
            }
            else
            {            
                NEICPH.sendSpawnItem(itemstack1, infinite, true);
            }
        }
        else
        {
            for(int given = 0; given < itemstack1.stackSize; )
            {
                int qty = Math.min(itemstack1.stackSize-given, itemstack1.getMaxStackSize());
                sendCommand(getStringSetting("command.item"), mc().thePlayer.username, itemstack1.itemID, qty, itemstack1.getItemDamage());
                given+=qty;
            }
        }
    }

    public static void updateUnlimitedItems()
    {
        ItemStack itemstack = getHeldItem();
        if(itemstack != null && itemstack.stackSize > 64)
            itemstack.stackSize = 1;
        
        ItemStack aitemstack[] = mc().thePlayer.inventory.mainInventory;
        for(int slot = 0; slot < aitemstack.length; slot++)
        {
            ItemStack itemstack1 = aitemstack[slot];
            if(itemstack1 == null)
                continue;
            
            if(itemstack1.stackSize < 0 || itemstack1.stackSize > 64)
                itemstack1.stackSize = 111;
            
            if(itemstack1.getItemDamage() > -32000 && itemstack1.getItemDamage() < -30000)
                itemstack1.setItemDamage(-32000);
        }
    }
        
    public static boolean isValidItem(ItemStack test)
    {
        for(ItemStack stack : getValidItems(test.itemID))
            if(areStacksIdentical(stack, test))
                return true;
        
        return false;
    }

    public static boolean canItemFitInInventory(EntityPlayer player, ItemStack itemstack)
    {
        return InventoryUtils.getInsertableQuantity(new InventoryRange(player.inventory, 0, 36), itemstack) > 0;
    }

    public static boolean shiftKey()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }
    
    public static boolean controlKey()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }
    
    public static int getGamemode()
    {
        if(invCreativeMode())
            return 2;
        else if(mc().playerController.isInCreativeMode())
            return 1;
        else if(mc().playerController.currentGameType.isAdventure())
            return 3;
        else
            return 0;
    }
    
    public static boolean isValidGamemode(String s)
    {
        return s.equals("survival") ||
                canPerformAction(s) &&
                Arrays.asList(getStringArrSetting("inventory.gamemodes")).contains(s);
    }
    
    public static int getNextGamemode()
    {
        int mode = getGamemode();
        int nmode = mode;
        while(true)
        {
            nmode = (nmode+1)%NEIActions.gameModes.length;
            if(nmode == mode || isValidGamemode(NEIActions.gameModes[nmode]))
                break;
        }
        return nmode;
    }
    
    public static void cycleGamemode()
    {
        int mode = getGamemode();
        int nmode = getNextGamemode();
        if(mode == nmode)
            return;
        
        if(hasSMPCounterPart())
            NEICPH.sendGamemode(nmode);
        else
            sendCommand(getStringSetting("command.creative"), getGameType(nmode), mc().thePlayer.username);
    }

    public static long getTime()
    {
        return mc().theWorld.getWorldInfo().getWorldTime();
    }

    public static void setTime(long l)
    {
        mc().theWorld.getWorldInfo().setWorldTime(l);
    }

    public static void setHourForward(int hour)
    {
        long day = (getTime() / 24000L) * 24000L;
        long newTime = day + 24000L + hour * 1000;
        
        if(hasSMPCounterPart())
            NEICPH.sendSetTime(hour);
        else
            sendCommand(getStringSetting("command.time"), newTime);
    }
        
    public static void sendCommand(String command, Object... args)
    {        
        if(command.length() == 0)
            return;
        
        NumberFormat numberformat = NumberFormat.getIntegerInstance();
        numberformat.setGroupingUsed(false);
        MessageFormat messageformat = new MessageFormat(command);
        for(int i = 0; i < args.length; i++)
            if(args[i] instanceof Integer || args[i] instanceof Long)
                messageformat.setFormatByArgumentIndex(i, numberformat);

        mc().thePlayer.sendChatMessage(messageformat.format(args));
    }

    public static boolean isRaining()
    {
        return mc().theWorld.getWorldInfo().isRaining();
    }

    public static void toggleRaining()
    {
        if(hasSMPCounterPart())
            NEICPH.sendToggleRain();
        else
            sendCommand(getStringSetting("command.rain"), isRaining() ? 0 : 1);
    }
    
    public static void healPlayer()
    {
        if(hasSMPCounterPart())
            NEICPH.sendHeal();
        else
            sendCommand(getStringSetting("command.heal"), mc().thePlayer.username);
    }

    public static void toggleMagnetMode()
    {
        if(hasSMPCounterPart())
            NEICPH.sendToggleMagnetMode();
    }
        
    public static ArrayList<int[]> concatIntegersToRanges(List<Integer> damages)
    {
        ArrayList<int[]> ranges = new ArrayList<int[]>();
        if(damages.size() == 0)return ranges;
        
        Collections.sort(damages);
        int start = -1;
        int next = 0;
        for(Integer i : damages)
        {
            if(start == -1)
            {
                start = next = i;
                continue;
            }
            if(next+1 != i)
            {
                ranges.add(new int[]{start, next});
                start = next = i;
                continue;
            }
            next = i;
        }
        ranges.add(new int[]{start, next});
        return ranges;
    }
    
    public static ArrayList<int[]> addIntegersToRanges(List<int[]> ranges, List<Integer> damages)
    {
        for(int[] range : ranges)
            for(int integer = range[0]; integer <= range[1]; integer++)
                damages.add(integer);

        return concatIntegersToRanges(damages);
    }
    
    public static boolean safeKeyDown(int keyCode)
    {
        try
        {        
            return Keyboard.isKeyDown(keyCode);
        }
        catch (IndexOutOfBoundsException e) 
        {
            return false;
        }
    }
    
    public static void setItemQuantity(int i)
    {
        world.nbt.setInteger("quantity", i);
        world.saveNBT();
        LayoutManager.quantity.setText(Integer.toString(i));
    }

    public static GuiContainer getGuiContainer()
    {
        if(mc().currentScreen instanceof GuiContainer)
            return (GuiContainer) mc().currentScreen;
        
        return null;
    }

    public static void overlayScreen(GuiScreen gui)
    {
        if(mc().currentScreen instanceof GuiContainer)
            FastTransferManager.clickSlot(getGuiContainer(), -999);
        
        mc().currentScreen = null;
        mc().displayGuiScreen(gui);
    }

    public static boolean altKey()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    public static List<ItemStack> getValidItems(int itemID)
    {
        List<ItemStack> values = ItemList.itemMap[itemID];
        if(values == null)
            return Collections.emptyList();
        return values;
    }
}
