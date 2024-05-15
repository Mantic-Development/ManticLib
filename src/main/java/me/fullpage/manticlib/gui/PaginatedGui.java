package me.fullpage.manticlib.gui;

import lombok.Getter;
import lombok.Setter;
import me.fullpage.manticlib.builders.ItemBuilder;
import me.fullpage.manticlib.utils.Utils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class PaginatedGui extends Gui {

    public static final KeyGuiItem PREVIOUS_ITEM = new KeyGuiItem(ItemBuilder.fromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZhMjJjYzZkZGQ1NjlhNmNlODk0YWFiOTA2YjczZGI4YmE4OWY2YTJiYjA3MWJhYjIyZTU3YTRmMDg4NWFiZiJ9fX0=")
            .name("&6&lPrevious"), click -> true, '<');

    public static final KeyGuiItem NEXT_ITEM = new KeyGuiItem(ItemBuilder.fromBase64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjYzMTRkMzFiMDk1ZTRkNDIxNzYwNDk3YmU2YTE1NmY0NTlkOGM5OTU3YjdlNmIxYzEyZGViNGU0Nzg2MGQ3MSJ9fX0=")
            .name("&6&lNext"), click -> true, '>');

    private final char pageItemKey;
    private int nextPageItemSlot, previousPageItemSlot;
    private List<GuiItem> pageItems;
    private HashMap<UUID, Page> uuidPages; // uuid of player, page number
    private int fillableSlotsPerPage;
    private int maxPage;

    public PaginatedGui(@NotNull String title, @NotNull String[] guiElements, char pageItemKey) {
        super(title, guiElements);
        this.pageItemKey = pageItemKey;
        this.initialiseGui();
    }

    public PaginatedGui(@NotNull String title, @NotNull Collection<String> guiElements, char pageItemKey) {
        this(title, guiElements.toArray(new String[0]), pageItemKey);
    }



    protected PaginatedGui initialiseGui() {
        this.setDestroyOnClose(true);
        this.fillableSlotsPerPage = this.getAmountOfSlotsWithKey(this.pageItemKey);
        this.maxPage = 1; // needs to be updated every time pageItems is changed
        this.uuidPages = new HashMap<>();
        this.pageItems = new ArrayList<>();
        this.nextPageItemSlot = Integer.MIN_VALUE;
        this.previousPageItemSlot = Integer.MIN_VALUE;
        return this;
    }

    private void updateMaxPage() {
        if (Utils.isNullOrEmpty(pageItems)) {
            this.maxPage = 1;
            return;
        }
        this.maxPage = this.countPages(pageItems.size(), this.fillableSlotsPerPage);
    }

    private int countPages(int amount, int maxPerPage) {
        if (amount <= maxPerPage) {
            return 1;
        }
        int pages = amount / maxPerPage;
        if (amount % maxPerPage != 0) {
            pages++;
        }
        return pages;
    }

    public PaginatedGui setNextPageItem(@NotNull GuiItem guiItem) {
        if (guiItem instanceof KeyGuiItem) {
            this.addItem((KeyGuiItem) guiItem);
        }
        return this.setNextPageItem(this.findSlot(guiItem));
    }

    public PaginatedGui setNextPageItem(int slot) {
        this.nextPageItemSlot = slot;
        return this;
    }

    public PaginatedGui setPreviousPageItem(@NotNull GuiItem guiItem) {
        if (guiItem instanceof KeyGuiItem) {
            this.addItem((KeyGuiItem) guiItem);
        }
        return this.setPreviousPageItem(this.findSlot(guiItem));
    }

    public PaginatedGui setPreviousPageItem(int slot) {
        this.previousPageItemSlot = slot;
        return this;
    }

    private int findSlot(@NotNull GuiItem guiItem) {
        if (guiItem instanceof KeyGuiItem) {
            return this.firstSlotWithKey(((KeyGuiItem) guiItem).getKey());
        }
        final Map.Entry<Integer, GuiItem> entry = guiItems.entrySet().stream().filter(e -> e.getValue().equals(guiItem)).findFirst().orElse(null);
        return entry != null ? entry.getKey() : -1;
    }

    public PaginatedGui addPageItem(@NotNull GuiItem guiItem) {
        this.pageItems.add(guiItem);
        this.updateMaxPage();
        return this;
    }

    public PaginatedGui addPageItems(@NotNull GuiItem... guiItems) {
        for (GuiItem guiItem : guiItems) {
            this.addPageItem(guiItem);
        }
        return this;
    }

    public PaginatedGui addPageItems(@NotNull Collection<GuiItem> guiItems) {
        for (GuiItem guiItem : guiItems) {
            this.addPageItem(guiItem);
        }
        return this;
    }

    public PaginatedGui setPageItems(List<GuiItem> pageItems) {
        this.pageItems = pageItems;
        this.updateMaxPage();
        return this;
    }

    public boolean canGoNextPage(Player player) {
        final Page p = uuidPages.get(player.getUniqueId());
        if (p != null) {
            return (p.getCurrentPage() + 1) <= maxPage;
        }
        return false;
    }

    public boolean canGoPreviousPage(Player player) {
        final Page p = uuidPages.get(player.getUniqueId());
        if (p != null) {
            return (p.getCurrentPage() - 1) >= 1;
        }
        return false;
    }

    protected void next(Player player) {
        final Page p = uuidPages.get(player.getUniqueId());
        this.show(player, p == null ? 1 : p.getCurrentPage() + 1);
    }

    public void previous(Player player) {
        final Page p = uuidPages.get(player.getUniqueId());
        this.show(player, p == null ? 1 : p.getCurrentPage() - 1);
    }

    public void close(Player player) {
        if (closeSound != null) {
            closeSound.playSound(player);
        }
        player.closeInventory();
    }

    @Override
    public void show(Player player) {
        this.show(player, 1);
    }

    public void show(Player player, int page) {
        page = Math.max(1, page);
        final Page p = uuidPages.get(player.getUniqueId());
        if (p != null) {
            if (p.isTransitioningPage()) {
                return;
            }
            p.setPreviousPage(p.getCurrentPage());
            p.setCurrentPage(page);
            p.setTransitioningPage(true);
        } else {
            uuidPages.put(player.getUniqueId(), new Page(page));
        }

        final int[] range = getRange(this.pageItems.size(), this.fillableSlotsPerPage, page);
        this.clearKey(this.pageItemKey);
        this.fillKey(this.pageItemKey, pageItems.subList(range[0], range[1]));
        if (p != null && p.isTransitioningPage() && inventory != null) {
            if (previousPageItemSlot != Integer.MIN_VALUE) {
                final GuiItem itemAt = getItemAt(previousPageItemSlot);
                if (itemAt != null) {
                    inventory.setItem(previousPageItemSlot, itemAt.getItem());
                }
            }
            if (nextPageItemSlot != Integer.MIN_VALUE) {
                final GuiItem itemAt = getItemAt(nextPageItemSlot);
                if (itemAt != null) {
                    inventory.setItem(nextPageItemSlot, itemAt.getItem());
                }
            }
        }

        super.show(player);

        if (p != null) {
            p.setTransitioningPage(false);
        }

    }

    public void removeFromUUIDPages(Player player) {
        uuidPages.remove(player.getUniqueId());
    }

    private int[] getRange(int amount, int maxPerPage, int targetPage) {
        if (amount <= maxPerPage) {
            return new int[]{0, amount};
        }

        final int min = (targetPage - 1) * maxPerPage;
        final int max = Math.min(min + fillableSlotsPerPage + 1, amount);
        return new int[]{min, max};
    }

    public int getPage(Player player) {
        return uuidPages.getOrDefault(player.getUniqueId(), new Page(-1)).getCurrentPage();
    }

    @Getter
    @Setter
    public static class Page {

        private int previousPage;
        private int currentPage;
        private boolean isTransitioningPage;

        public Page(int page) {
            this.previousPage = -1;
            this.currentPage = page;
            this.isTransitioningPage = false;
        }

    }

}