package me.fullpage.manticlib.listeners;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.events.armourequipevent.ArmourEquipEvent;
import me.fullpage.manticlib.events.armourequipevent.ArmourType;
import me.fullpage.manticlib.string.Txt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArmourListener implements Listener {


    private static boolean REGISTERED = false;
    public static final String[] blocked = {"NOTE_BLOCK", "FURNACE", "CHEST", "TRAPPED_CHEST", "BEACON", "DISPENSER", "DROPPER", "HOPPER", "WORKBENCH", "ENCHANTMENT_TABLE", "ENDER_CHEST", "ANVIL", "BED_BLOCK", "FENCE_GATE", "SPRUCE_FENCE_GATE", "BIRCH_FENCE_GATE", "ACACIA_FENCE_GATE", "JUNGLE_FENCE_GATE", "DARK_OAK_FENCE_GATE", "IRON_DOOR_BLOCK", "WOODEN_DOOR", "SPRUCE_DOOR", "BIRCH_DOOR", "JUNGLE_DOOR", "ACACIA_DOOR", "DARK_OAK_DOOR", "WOOD_BUTTON", "STONE_BUTTON", "TRAP_DOOR", "IRON_TRAPDOOR", "DIODE_BLOCK_OFF", "DIODE_BLOCK_ON", "REDSTONE_COMPARATOR_OFF", "REDSTONE_COMPARATOR_ON", "FENCE", "SPRUCE_FENCE", "BIRCH_FENCE", "JUNGLE_FENCE", "DARK_OAK_FENCE", "ACACIA_FENCE", "NETHER_FENCE", "BREWING_STAND", "CAULDRON", "LEGACY_SIGN_POST", "LEGACY_WALL_SIGN", "LEGACY_SIGN", "ACACIA_SIGN", "ACACIA_WALL_SIGN", "BIRCH_SIGN", "BIRCH_WALL_SIGN", "DARK_OAK_SIGN", "DARK_OAK_WALL_SIGN", "JUNGLE_SIGN", "JUNGLE_WALL_SIGN", "OAK_SIGN", "OAK_WALL_SIGN", "SPRUCE_SIGN", "SPRUCE_WALL_SIGN", "LEVER", "BLACK_SHULKER_BOX", "BLUE_SHULKER_BOX", "BROWN_SHULKER_BOX", "CYAN_SHULKER_BOX", "GRAY_SHULKER_BOX", "GREEN_SHULKER_BOX", "LIGHT_BLUE_SHULKER_BOX", "LIME_SHULKER_BOX", "MAGENTA_SHULKER_BOX", "ORANGE_SHULKER_BOX", "PINK_SHULKER_BOX", "PURPLE_SHULKER_BOX", "RED_SHULKER_BOX", "SILVER_SHULKER_BOX", "WHITE_SHULKER_BOX", "YELLOW_SHULKER_BOX", "DAYLIGHT_DETECTOR_INVERTED", "DAYLIGHT_DETECTOR", "BARREL", "BLAST_FURNACE", "SMOKER", "CARTOGRAPHY_TABLE", "COMPOSTER", "GRINDSTONE", "LECTERN", "LOOM", "STONECUTTER", "BELL"};


    private final List<String> blockedMaterials;

    public ArmourListener() {
        this.blockedMaterials = Txt.list(blocked);
        if (!REGISTERED) {
            ManticLib.get().getServer().getPluginManager().registerEvents(this, ManticLib.get());
            REGISTERED = true;
        }
    }
    //Event Priority is highest because other plugins might cancel the events before we check.

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void inventoryClick( InventoryClickEvent e) {
        boolean shift = false, numberkey = false;
        if (e.getAction() == InventoryAction.NOTHING) return;// Why does this get called if nothing happens??
        if (e.getClick().equals(ClickType.SHIFT_LEFT) || e.getClick().equals(ClickType.SHIFT_RIGHT)) {
            shift = true;
        }
        if (e.getClick().equals(ClickType.NUMBER_KEY)) {
            numberkey = true;
        }
        if (e.getSlotType() != InventoryType.SlotType.ARMOR && e.getSlotType() != InventoryType.SlotType.QUICKBAR && e.getSlotType() != InventoryType.SlotType.CONTAINER)
            return;
        if (e.getClickedInventory() != null && !e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        if (!e.getInventory().getType().equals(InventoryType.CRAFTING) && !e.getInventory().getType().equals(InventoryType.PLAYER))
            return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        ArmourType newArmorType = ArmourType.matchType(shift ? e.getCurrentItem() : e.getCursor());
        if (!shift && newArmorType != null && e.getRawSlot() != newArmorType.getSlot()) {
            // Used for drag and drop checking to make sure you aren't trying to place a helmet in the boots slot.
            return;
        }
        if (shift) {
            newArmorType = ArmourType.matchType(e.getCurrentItem());
            if (newArmorType != null) {
                boolean equipping = e.getRawSlot() != newArmorType.getSlot();
                if (newArmorType.equals(ArmourType.HELMET) && (equipping == isAirOrNull(e.getWhoClicked().getInventory().getHelmet())) || newArmorType.equals(ArmourType.CHESTPLATE) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getChestplate()) : !isAirOrNull(e.getWhoClicked().getInventory().getChestplate())) || newArmorType.equals(ArmourType.LEGGINGS) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getLeggings()) : !isAirOrNull(e.getWhoClicked().getInventory().getLeggings())) || newArmorType.equals(ArmourType.BOOTS) && (equipping ? isAirOrNull(e.getWhoClicked().getInventory().getBoots()) : !isAirOrNull(e.getWhoClicked().getInventory().getBoots()))) {
                    ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent((Player) e.getWhoClicked(), ArmourEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, equipping ? null : e.getCurrentItem(), equipping ? e.getCurrentItem() : null);
                    ManticLib.get().getServer().getPluginManager().callEvent(armorEquipEvent);
                    if (armorEquipEvent.isCancelled()) {
                        e.setCancelled(true);
                    }
                }
            }
        } else {
            ItemStack newArmorPiece = e.getCursor();
            ItemStack oldArmorPiece = e.getCurrentItem();
            if (numberkey) {
                if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {// Prevents shit in the 2by2 crafting

                    // e.getClickedInventory() == The players inventory
                    // e.getHotBarButton() == key people are pressing to equip or unequip the item to or from.
                    // e.getRawSlot() == The slot the item is going to.
                    // e.getSlot() == Armor slot, can't use e.getRawSlot() as that gives a hotbar slot ;-;
                    ItemStack hotbarItem = e.getClickedInventory().getItem(e.getHotbarButton());
                    if (!isAirOrNull(hotbarItem)) {// Equipping
                        newArmorType = ArmourType.matchType(hotbarItem);
                        newArmorPiece = hotbarItem;
                        oldArmorPiece = e.getClickedInventory().getItem(e.getSlot());
                        // Fullpage start
                        if (newArmorType == null) {
                            ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent((Player) e.getWhoClicked(), ArmourEquipEvent.EquipMethod.SHIFT_CLICK, newArmorType, oldArmorPiece, newArmorPiece);
                            ManticLib.get().getServer().getPluginManager().callEvent(armorEquipEvent);
                            if (armorEquipEvent.isCancelled()) {
                                e.setCancelled(true);
                            }
                            return;
                        }
                        // Fullpage end
                    } else {// Unequipping
                        newArmorType = ArmourType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());

                    }
                }
            } else {
                if (isAirOrNull(e.getCursor()) && !isAirOrNull(e.getCurrentItem())) {// unequip with no new item going into the slot.
                    newArmorType = ArmourType.matchType(e.getCurrentItem());
                }
                // e.getCurrentItem() == Unequip
                // e.getCursor() == Equip
                // newArmorType = ArmourType.matchType(!isAirOrNull(e.getCurrentItem()) ? e.getCurrentItem() : e.getCursor());
            }

            // fullpage start
            ItemStack item; // this should fix the event not firing when a armour piece is being unequipped in the hotbar using the number key and going to a slot with another armour piece, but not an armour swap
            if (numberkey && e.getClickedInventory().getType().equals(InventoryType.PLAYER) && newArmorType != null && (item = e.getClickedInventory().getItem(newArmorType.getSlot())) == null&& e.getRawSlot() != newArmorType.getSlot()) {
                ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent((Player) e.getWhoClicked(), ArmourEquipEvent.EquipMethod.HOTBAR_SWAP, newArmorType, oldArmorPiece, item);
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                if (armorEquipEvent.isCancelled()) {
                    e.setCancelled(true);
                }
            } // fullpage end

            if (newArmorType != null && e.getRawSlot() == newArmorType.getSlot()) {
                ArmourEquipEvent.EquipMethod method = ArmourEquipEvent.EquipMethod.PICK_DROP;
                if (e.getAction().equals(InventoryAction.HOTBAR_SWAP) || numberkey)
                    method = ArmourEquipEvent.EquipMethod.HOTBAR_SWAP;
                ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent((Player) e.getWhoClicked(), method, newArmorType, oldArmorPiece, newArmorPiece);
                Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                if (armorEquipEvent.isCancelled()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent e) {
        if (e.useItemInHand().equals(Result.DENY)) return;
        //
        if (e.getAction() == Action.PHYSICAL) return;
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            if (!e.useInteractedBlock().equals(Result.DENY)) {
                if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {// Having both of these checks is useless, might as well do it though.
                    // Some blocks have actions when you right click them which stops the client from equipping the armor in hand.
                    Material mat = e.getClickedBlock().getType();
                    for (String s : blockedMaterials) {
                        if (mat.name().equalsIgnoreCase(s)) return;
                    }
                }
            }
            ArmourType newArmorType = ArmourType.matchType(e.getItem());
            if (newArmorType != null) {
                if (newArmorType.equals(ArmourType.HELMET) && isAirOrNull(e.getPlayer().getInventory().getHelmet()) || newArmorType.equals(ArmourType.CHESTPLATE) && isAirOrNull(e.getPlayer().getInventory().getChestplate()) || newArmorType.equals(ArmourType.LEGGINGS) && isAirOrNull(e.getPlayer().getInventory().getLeggings()) || newArmorType.equals(ArmourType.BOOTS) && isAirOrNull(e.getPlayer().getInventory().getBoots())) {
                    ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent(e.getPlayer(), ArmourEquipEvent.EquipMethod.HOTBAR, ArmourType.matchType(e.getItem()), null, e.getItem());
                    Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
                    if (armorEquipEvent.isCancelled()) {
                        e.setCancelled(true);
                        player.updateInventory();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void inventoryDrag(InventoryDragEvent event) {
        // getType() seems to always be even.
        // Old Cursor gives the item you are equipping
        // Raw slot is the ArmourType slot
        // Can't replace armor using this method making getCursor() useless.
        ArmourType type = ArmourType.matchType(event.getOldCursor());
        if (event.getRawSlots().isEmpty()) return;// Idk if this will ever happen
        if (type != null && type.getSlot() == event.getRawSlots().stream().findFirst().orElse(0)) {
            ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent((Player) event.getWhoClicked(), ArmourEquipEvent.EquipMethod.DRAG, type, null, event.getOldCursor());
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                event.setResult(Result.DENY);
                event.setCancelled(true);
            }
        }
        // Debug shit
		/*System.out.println("Slots: " + event.getInventorySlots().toString());
		System.out.println("Raw Slots: " + event.getRawSlots().toString());
		if(event.getCursor() != null){
			System.out.println("Cursor: " + event.getCursor().getType().name());
		}
		if(event.getOldCursor() != null){
			System.out.println("OldCursor: " + event.getOldCursor().getType().name());
		}
		System.out.println("Type: " + event.getType().name());*/
    }

    @EventHandler
    public void itemBreakEvent(PlayerItemBreakEvent e) {
        ArmourType type = ArmourType.matchType(e.getBrokenItem());
        if (type != null) {
            Player p = e.getPlayer();
            ArmourEquipEvent armorEquipEvent = new ArmourEquipEvent(p, ArmourEquipEvent.EquipMethod.BROKE, type, e.getBrokenItem(), null);
            Bukkit.getServer().getPluginManager().callEvent(armorEquipEvent);
            if (armorEquipEvent.isCancelled()) {
                ItemStack i = e.getBrokenItem().clone();
                i.setAmount(1);
                i.setDurability((short) (i.getDurability() - 1));
                if (type.equals(ArmourType.HELMET)) {
                    p.getInventory().setHelmet(i);
                } else if (type.equals(ArmourType.CHESTPLATE)) {
                    p.getInventory().setChestplate(i);
                } else if (type.equals(ArmourType.LEGGINGS)) {
                    p.getInventory().setLeggings(i);
                } else if (type.equals(ArmourType.BOOTS)) {
                    p.getInventory().setBoots(i);
                }
            }
        }
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (e.getKeepInventory()) return;
        for (ItemStack i : p.getInventory().getArmorContents()) {
            if (!isAirOrNull(i)) {
                Bukkit.getServer().getPluginManager().callEvent(new ArmourEquipEvent(p, ArmourEquipEvent.EquipMethod.DEATH, ArmourType.matchType(i), i, null));
                // No way to cancel a death event.
            }
        }
    }

    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}