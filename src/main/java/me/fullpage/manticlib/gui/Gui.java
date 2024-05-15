package me.fullpage.manticlib.gui;

import lombok.Getter;
import lombok.Setter;
import me.fullpage.manticlib.builders.ItemBuilder;
import me.fullpage.manticlib.utils.Utils;
import me.fullpage.manticlib.wrappers.Pair;
import me.fullpage.manticlib.wrappers.SoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Gui implements Listener, InventoryHolder {

    protected final static HashMap<UUID, Pair<Inventory, Gui>> inventories = new HashMap<>();
    public static HashSet<Gui> guis = new HashSet<>();
    public static Listener listener = null;

    protected String key = "";
    protected final Inventory inventory;
    protected final HashMap<Integer, GuiItem> guiItems = new HashMap<>();
    protected final String title;
    @Getter
    protected final HashSet<UUID> viewers = new HashSet<>();
    protected boolean destroyOnClose = false;
    protected boolean closeOnClick = false;
    protected @Nullable String[] guiElements = null;
    protected @Nullable Gui.CloseAction closeAction = null;
    protected boolean cancelByDefault = true, canPickupDroppedItems = true;
    protected Plugin providingPlugin = JavaPlugin.getProvidingPlugin(this.getClass());

    protected static @Nullable SoundEffect CLOSE_SOUND = new SoundEffect("BLOCK_CHEST_CLOSE", 0.75f, 1.0f), OPEN_SOUND = new SoundEffect("BLOCK_CHEST_OPEN", 0.75f, 1.0f);

    @Getter
    @Setter
    protected @Nullable SoundEffect closeSound = CLOSE_SOUND, openSound = OPEN_SOUND;


    @Getter
    protected final int rows;


    public Gui(String title, int size) {
        this.inventory = Bukkit.createInventory(null, size, title);
        this.title = title;
        this.rows = Math.min(6, size / 9);
        this.initialiseGui();
    }

    public Gui(String title, String[] guiElements) {
        this(guiElements.length * 9, title);
        this.guiElements = guiElements.length > 6 ? Arrays.copyOfRange(guiElements, 0, 6) : guiElements;
        this.initialiseGui();
    }

    public Gui(String title, Collection<String> guiElements) {
        this(title, guiElements.toArray(new String[0]));
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public Gui(int size, String title) {
        this(title, size);
    }

    private void initialiseGui() {
        guis.add(this);
        if (listener == null) {
            Bukkit.getServer().getPluginManager().registerEvents(listener = new GUIListener(), JavaPlugin.getProvidingPlugin(getClass()));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void addItem(GuiItem item, int index) {
        final ItemStack item1 = item.getItem();
        if (item1 == null) {
            return;
        }
        this.inventory.setItem(index, item1);
        guiItems.put(index, item);
    }

    public void addItem(KeyGuiItem... item) {
        for (KeyGuiItem guiItem : item) {
            addItem(guiItem);
        }
    }

    public void fillEmptyKey(KeyGuiItem item) {
        addItem(item, false);
    }

    public List<Integer> getSlotsWithKey(char key) {
        if (Utils.isNullOrEmpty(guiElements)) {
            return Collections.emptyList();
        }
        List<Integer> slots = new ArrayList<>();
        int y = 0;
        for (String element : guiElements) {
            if (element != null) {
                int x = 0;
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (key == c) {
                        int index = y * 9 + x;
                        slots.add(index);
                    }
                    x++;
                }
            }
            y++;
        }
        return slots;
    }

    public int getAmountOfSlotsWithKey(char key) {
        if (Utils.isNullOrEmpty(guiElements)) {
            return 0;
        }
        int count = 0;
        for (String element : guiElements) {
            if (element != null) {
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (key == c) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public int firstSlotWithKey(char key) {
        if (Utils.isNullOrEmpty(guiElements)) {
            return -1;
        }
        int y = 0;
        for (String element : guiElements) {
            if (element != null) {
                int x = 0;
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (key == c) {
                        return y * 9 + x;
                    }
                    x++;
                }
            }
            y++;
        }
        return -1;
    }

    public void fillKey(char key, List<GuiItem> guiItems) {
        final List<GuiItem> reversed;
        Collections.reverse(reversed = new ArrayList<>(guiItems)/*.subList(0, guiItems.size())*/);
        if (this.guiElements != null) {
            int y = 0;
            for (String element : guiElements) {
                if (element == null) {
                    y++;
                    continue;
                }
                int x = 0;
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (reversed.isEmpty()) return;
                    if (key == c) {
                        int index = y * 9 + x;
                        addItem(reversed.get(reversed.size() - 1), index);
                        reversed.remove(reversed.size() - 1);
                    }
                    x++;
                }
                y++;
            }
        }
    }

    public void clearKey(char key) {
        if (this.guiElements != null) {
            int y = 0;
            for (String element : guiElements) {
                if (element == null) {
                    y++;
                    continue;
                }
                int x = 0;
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (key == c) {
                        int index = y * 9 + x;
                        this.inventory.setItem(index, new ItemBuilder(Material.AIR));
                        guiItems.remove(index);
                    }
                    x++;
                }
                y++;
            }
        }
    }


    public void addItem(KeyGuiItem item) {
        addItem(item, true);
    }

    @Nullable
    public GuiItem getItemAt(int i) {
        return guiItems.get(i);
    }

    @Nullable
    public KeyGuiItem getKeyItemAt(char key) {
        final Optional<GuiItem> first = guiItems.values().stream().filter(guiItem -> guiItem instanceof KeyGuiItem && ((KeyGuiItem) guiItem).getKey() == key).findFirst();
        return (KeyGuiItem) first.orElse(null);
    }

    private void addItem(KeyGuiItem item, boolean replace) {
        final char key = item.getKey();
        if (this.guiElements != null) {
            int y = 0;
            for (String element : guiElements) {
                if (element == null) {
                    y++;
                    continue;
                }
                int x = 0;
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (key == c) {
                        int index = y * 9 + x;
                        if (replace) {
                            addItem(item, index);
                        } else {
                            if (!guiItems.containsKey(index)) {
                                addItem(item, index);
                            }
                        }
                    }
                    x++;
                }
                y++;
            }
        }
    }

    public void removeItem(int index) {
        this.inventory.setItem(index, null);
        guiItems.remove(index);
    }

    public void fillInventory(GuiItem item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item.getItem());
            guiItems.put(i, item);
        }
    }

    public void show(Player player) {
        Inventory inventory = getInventory();
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(JavaPlugin.getProvidingPlugin(getClass()), () -> show(player));
            return;
        }
        player.openInventory(inventory);
        if (openSound != null) {
            openSound.playSound(player);
        }
        UUID uniqueId = player.getUniqueId();
        viewers.add(uniqueId);
        inventories.put(uniqueId, new Pair<>(inventory, this));
    }

    public static Pair<Inventory, Gui> getInventoryGUIPair(Player player) {
        return inventories.get(player.getUniqueId());
    }

    public static Inventory getInventory(Player player) {
        final Pair<Inventory, Gui> inventoryGUIPair = inventories.get(player.getUniqueId());
        if (inventoryGUIPair == null) {
            return null;
        }
        return inventoryGUIPair.getLeft();
    }

    public List<ItemStack> getItemsAtKey(char key) {
        List<ItemStack> itemStacks = new ArrayList<>();
        final Inventory inventory = getInventory();
        if (this.guiElements != null) {
            int y = 0;
            for (String element : guiElements) {
                if (element == null) {
                    y++;
                    continue;
                }
                int x = 0;
                final char[] chars = element.toCharArray();
                for (char c : chars) {
                    if (key == c) {
                        int index = y * 9 + x;
                        final ItemStack item = getInventory().getItem(index);
                        if (item != null && !item.getType().equals(Material.AIR)) {
                            itemStacks.add(item);
                        }
                    }
                    x++;
                }
                y++;
            }
        }
        return itemStacks;
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public boolean isViewing(Player player) {
        return viewers.contains(player);
    }

    public void setDestroyOnClose(boolean destroyOnClose) {
        this.destroyOnClose = destroyOnClose;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setCloseAction(@Nullable CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public @Nullable CloseAction getCloseAction() {
        return closeAction;
    }

    public boolean isCloseOnClick() {
        return closeOnClick;
    }

    public void setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
    }

    public boolean isCancelByDefault() {
        return cancelByDefault;
    }

    public void setCancelByDefault(boolean cancelByDefault) {
        this.cancelByDefault = cancelByDefault;
    }

    public boolean isCanPickupDroppedItems() {
        return canPickupDroppedItems;
    }

    public void setCanPickupDroppedItems(boolean canPickupDroppedItems) {
        this.canPickupDroppedItems = canPickupDroppedItems;
    }

    public static class GUIListener implements Listener {

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            Pair<Inventory, Gui> inventoryGUIPair = getInventoryGUIPair(player);

            if (inventoryGUIPair == null) {
                return;
            }

            Inventory storedInventory = inventoryGUIPair.getLeft();
            InventoryView view = event.getView();
            Inventory inventory = view.getTopInventory();

            if (!inventory.equals(storedInventory)) {
                return;
            }

            Gui gui = inventoryGUIPair.getRight();

            if ((gui == null) || (!view.getTitle().equals(gui.title))
                    || (inventory.getType() != gui.getInventory().getType()) || (inventory.getSize() != gui.getInventory().getSize())) {
                return;
            }


            if (gui.isCancelByDefault()) {
                event.setCancelled(true);
            }

            int slot = -1;
            if (event.getRawSlot() < view.getTopInventory().getSize()) {
                slot = event.getRawSlot();
            } else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                slot = event.getInventory().firstEmpty();
            }

            if (gui instanceof PaginatedGui) {
                PaginatedGui paginatedGUI = (PaginatedGui) gui;
                if (!paginatedGUI.isCloseOnClick()) {
                    if (paginatedGUI.getNextPageItemSlot() == slot) {
                        event.setCancelled(true);
                        if (paginatedGUI.canGoNextPage(player)) {
                            paginatedGUI.next(player);
                        } else {
                            final ItemStack currentItem = event.getCurrentItem();
                            if (currentItem != null) {
                                inventory.setItem(event.getSlot(), ItemBuilder.from(currentItem).type(Material.BARRIER));
                            }
                        }
                    } else if (paginatedGUI.getPreviousPageItemSlot() == slot) {
                        event.setCancelled(true);
                        if (paginatedGUI.canGoPreviousPage(player)) {
                            paginatedGUI.previous(player);
                        } else {
                            final ItemStack currentItem = event.getCurrentItem();
                            if (currentItem != null) {
                                inventory.setItem(event.getSlot(), ItemBuilder.from(currentItem).type(Material.BARRIER));
                            }
                        }
                    }
                }
            }

            // todo ? if slot is equal to or above 0 it's inside action otherwise outside action

            GuiItem guiItem = gui.guiItems.get(slot);
            if (guiItem == null) {
                return;
            }

            if (guiItem.getSoundEffect() != null) {
                guiItem.getSoundEffect().playSound(player);
            }

            ClickAction action = guiItem.getAction();
            if (action == null) {
                return;
            }

            if (action.onClick(new ClickAction.Click(gui, slot, event))) {
                event.setCancelled(true);
            }


            if (gui.isCloseOnClick()) {
                gui.close(player);
            }

        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerQuit(PlayerQuitEvent event) {
            event.getPlayer().closeInventory();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerQuit(PlayerKickEvent event) {
            event.getPlayer().closeInventory();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryPickupItemEvent(PlayerPickupItemEvent event) {
            Player player = event.getPlayer();
            Pair<Inventory, Gui> inventoryGUIPair = getInventoryGUIPair(player);

            if (inventoryGUIPair == null) {
                return;
            }

            Inventory storedInventory = inventoryGUIPair.getLeft();
            InventoryView openInventory = event.getPlayer().getOpenInventory();
            Inventory inventory = openInventory.getTopInventory();

            if (!inventory.equals(storedInventory)) {
                return;
            }

            Gui gui = inventoryGUIPair.getRight();

            if ((gui == null) || (!openInventory.getTitle().equals(gui.title))
                    || (inventory.getType() != gui.getInventory().getType()) || (inventory.getSize() != gui.getInventory().getSize())) {
                return;
            }

            if (!gui.isCanPickupDroppedItems()) {
                event.setCancelled(true);
            }


        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onInventoryClose(InventoryCloseEvent event) {
            if (!(event.getPlayer() instanceof Player)) return;
            Player player = (Player) event.getPlayer();

            for (Gui gui : guis) {
                if (event.getInventory().equals(gui.getInventory())) {
                    if (gui.getCloseAction() != null) {
                        gui.getCloseAction().onClose(new CloseAction.Close(gui, event));
                    }

                    boolean handleClose;
                    if (gui instanceof PaginatedGui) {
                        PaginatedGui paginatedGUI = (PaginatedGui) gui;
                        PaginatedGui.Page page = paginatedGUI.getUuidPages().get(player.getUniqueId());
                        handleClose = page == null || !page.isTransitioningPage();
                        if (handleClose && page != null) {
                            paginatedGUI.removeFromUUIDPages(player);
                        }
                    } else {
                        handleClose = true;
                        if (gui.closeSound != null) {
                            gui.closeSound.playSound(player);
                        }
                    }

                    if (handleClose) {
                        removeFromViewing(gui, player);
                        removeFromInventories(player);
                        if (gui.destroyOnClose && gui.viewers.isEmpty()) {
                            removeGui(gui);
                            return;
                        }
                    }

                    return;
                }
            }
        }

        private void removeGui(Gui gui) {
            guis.remove(gui);
        }

        private void removeFromViewing(Gui gui, Player player) {
            gui.viewers.remove(player.getUniqueId());
        }

        private void removeFromInventories(HumanEntity player) {
            inventories.remove(player.getUniqueId());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin().equals(JavaPlugin.getProvidingPlugin(getClass())))
                for (Gui gui : new ArrayList<>(guis)) {
                    if (gui != null && !gui.viewers.isEmpty()) {
                        for (UUID viewer : new ArrayList<>(gui.viewers)) {
                            Player player = Bukkit.getPlayer(viewer);
                            if (player != null) {
                                if (player.getOpenInventory().getType() != InventoryType.CRAFTING) {
                                    player.closeInventory();
                                }
                            }
                        }
                    }
                }
        }

    }

    public interface CloseAction {

        boolean onClose(Close close);


        class Close {
            private final Gui gui;
            private final InventoryCloseEvent event;

            public Close(Gui gui, InventoryCloseEvent event) {
                this.gui = gui;
                this.event = event;
            }

            public Inventory getInventory() {
                return event.getInventory();
            }

            public HumanEntity getWhoClosed() {
                return event.getPlayer();
            }

            public Player getPlayer() {
                return this.getWhoClosed() instanceof Player ? (Player) this.getWhoClosed() : null;
            }

            public Gui getGui() {
                return gui;
            }

            public InventoryCloseEvent getEvent() {
                return event;
            }
        }

    }

    public interface ClickAction {

        boolean onClick(Click click);

        class Click {
            private final Gui gui;
            private final int slot;
            private final InventoryClickEvent event;

            public Click(Gui gui, int slot, InventoryClickEvent event) {
                this.gui = gui;
                this.slot = slot;
                this.event = event;
            }

            public int getSlot() {
                return slot;
            }

            public ClickType getType() {
                return event.getClick();
            }

            public HumanEntity getWhoClicked() {
                return event.getWhoClicked();
            }

            public Player getPlayer() {
                return this.getWhoClicked() instanceof Player ? (Player) this.getWhoClicked() : null;
            }

            public Gui getGui() {
                return gui;
            }

            public InventoryClickEvent getEvent() {
                return event;
            }
        }

    }

    public @Nullable
    String[] getGuiElements() {
        return guiElements;
    }

    public static int getHighestLength(String... strings) {
        int length = 0;
        for (String string : strings) {
            int i = 0;
            for (char c : string.toCharArray()) {
                if (!Character.isSpaceChar(c)) {
                    i++;
                }
                if (i > length) {
                    length = i;
                }
            }
        }
        return length;
    }

    public PaginatedGui paginated() {
        return ((PaginatedGui) this).initialiseGui();
    }

    public static Collection<Gui> getGuis() {
        return guis;
    }

    public Plugin getProvidingPlugin() {
        return providingPlugin;
    }
}