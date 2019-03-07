package org.totemcraft.colif;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.sainttx.auctions.util.ReflectionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class BookAPI {

    public void openBook(Player player, List<String> pages) {
        openRawBook(player, pages.stream().map(plain -> ComponentSerializer.toString(new TextComponent(plain))).collect(Collectors.toList()));
    }

    public void openRawBook(Player player, List<String> pages) {
        if (player.getOpenInventory() != null) {
            player.getOpenInventory().close();
        }

        try {
            int handSlot = player.getInventory().getHeldItemSlot();
            int rawSlot = handSlot + 36;

            ItemStack item = createBook(pages);
            PacketContainer _setBookPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
            _setBookPacket.getIntegers().write(0, 0);
            _setBookPacket.getIntegers().write(1, rawSlot);
            _setBookPacket.getItemModifier().write(0, item);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, _setBookPacket);

            ByteBuf buf = Unpooled.buffer(256);
            buf.setByte(0, (byte) 0);
            buf.writerIndex(1);

            PacketContainer _openBookPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            _openBookPacket.getStrings().write(0, "MC|BOpen");
            //noinspection ConstantConditions
            _openBookPacket.getModifier().write(1, ReflectionUtil.getConstructor(ReflectionUtil.getNMSClass("PacketDataSerializer"), ByteBuf.class).newInstance(buf));

//            PacketPlayOutCustomPayload openBookPacket = new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
//            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(openBookPacket);

            ProtocolLibrary.getProtocolManager().sendServerPacket(player, _openBookPacket);

            ItemStack handItem = player.getInventory().getItem(handSlot);

            PacketContainer _restoreItemPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SET_SLOT);
            _restoreItemPacket.getIntegers().write(0, 0);
            _restoreItemPacket.getIntegers().write(1, rawSlot);
            _restoreItemPacket.getItemModifier().write(0, handItem);
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, _restoreItemPacket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ItemStack createBook(List<String> rawPages) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK, 1);
        item = BinaryTags.getCraftItemStack(item);
        BinaryTags.NBTCompound compound = BinaryTags.fromItemTag(item);
        compound.put("title", "");
        compound.put("author", "");
        compound.put("pages", BinaryTags.createList(rawPages));
        BinaryTags.setItemTag(item, compound);
        return item;
    }
}